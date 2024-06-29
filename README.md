# State Machine Bug Finder

**StateMachineBugFinder** (or **SMBugFinder** for short) is a cross-platform, automated bug detection framework implementing the automata-based technique described in a [paper at NDSS 2023][ndss23paper].
**SMBugFinder** takes as input a state machine model of a System Under Test (SUT), e.g., a state machine model generated by a protocol state fuzzer for some protocol implementation, and a catalogue of bug patterns for the corresponding protocol.
It uses this catalogue to automatically detect bugs in the SUT's state machine.

## Dependencies

To use **SMBugFinder**, you will need:

* JDK 17
* Apache Maven
* (optional) [Graphviz][graphviz]
* (optional) Python 3.x and [xdot](https://pypi.org/project/xdot/) for model visualization

## Usage

**SMBugFinder** can be used as a stand-alone testing tool, or as a library which can be incorporated in other protocol state fuzzers (e.g., [DTLS-Fuzzer][dtlsfuzzer] or [EDHOC-Fuzzer][edhocfuzzer]) to automate analysis of the models they generate.

Suppose we want to test the SSH server implementation of [Dropbear][dropbear] V2020.81 using **SMBugFinder**.
From within **SMBugFinder**'s directory, we then run:

    > mvn install
    > java -jar target/sm-bug-finder.jar -m src/main/resources/models/ssh/server/Dropbear-v2020.81.dot -c src/main/resources/patterns/ssh/server/patterns.xml -eo NO_RESP

First command installs **SMBugFinder**.
Second command executes **SMBugFinder** using two mandatory arguments, and an optional one:

  * the Mealy machine model of the SUT (in this case, the model for Dropbear-V2020.81 server, found [here](src/main/resources/models/ssh/server/Dropbear-v2020.81.dot));
  * the catalogue of bug patterns (in this case, patterns defined for SSH servers, found [here](src/main/resources/patterns/ssh/server));
  * the 'empty output' symbol which is used in the SUT model to indicate when the SUT processes an input message without producing a response.

Executing the second command should reveal two bugs identified in the Dropbear model.
One of these bugs is *Missing SR_AUTH*, for which  **SMBugFinder** gives the following information.

```
Bug Pattern: Missing SR_AUTH
Severity: LOW
Description: Authentication is performed without SR_AUTH.
Trace: KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS
Inputs: KEXINIT KEX30 NEWKEYS UA_PK_OK
Validation Status: NOT_VALIDATED
```

The witness (after `Trace: `) exposes the server performing authentication (indicated by `UA_SUCCESS` as output) without having received a request for the authentication service (done via a `SR_AUTH` input).
The witness was found on the model, however it has not been validated (validation status is `NOT_VALIDATED`).
Validation is disabled by default and entails running a corresponding test on the SUT to confirm the buggy behavior.
Enabling validation requires connection to a test harness.
More on that later.

## Output files

When executed, **SMBugFinder** generates an output directory, named `output` by default, in which it stores:

*  bug patterns after the condensed notation has been resolved (e.g., `MissingSR_AUTHLanguage.dot`);
*  model of the DFA-conversion of the original SUT model ( `sutLanguage.dot`);
*  for each bug pattern, the result of its intersection with the SUT model converted to DFA (`sutMissingSR_AUTHLanguage.dot`);
*  bug report including a listing of all the bugs found, followed by statistics ( `bug_report.txt`).

## Models

SUT models are specified as DOT graphs, and can be obtained automatically using existing protocol state fuzzers.
The directory [src/main/resources/models](src/main/resources/models) contains sample models for DTLS and SSH, named after the SUT for which they were generated, which was done using [DTLS-Fuzzer][dtlsfuzzer] and an [SSH fuzzer](https://easy.dans.knaw.nl/ui/datasets/id/easy-dataset:77503).
The models can be visualized with the help of [GraphViz][graphviz]'s `dot` utility, or better still, the `xdot` Python [utility][xdot] which builds on [GraphViz][graphviz].
Oftentimes, the models are large, making visualization difficult.
See the respective fuzzer repositories for scripts to trim the models.

## Bug patterns

**SMBugFinder** takes as argument the path to a bug pattern catalogue which indexes bug patterns.
Bug patterns are specified as DOT graphs, and currently have to be manually written.
A bug pattern defines a DFA which accepts only sequences exposing the presence of the bug.
The directory [src/main/resources/models](src/main/resources/models) contains an extensive set of bug patterns for SSH (including all used in the NDSS'2023 paper), and a few bug patterns for DTLS.
(The complete set of bug patterns used in the publication experiments can be found [here](https://gitlab.com/pfg666/dtls-fuzzer/-/tree/bugcheck-artifact/src/main/resources/patterns)).
Below is the pattern for the *Missing SR_AUTH* bug we found in Dropbear, which we visualize by running:

    > xdot src/main/resources/patterns/ssh/server/missing_sr_auth.dot

![missing_sr_auth](https://github.com/assist-project/state-machine-bug-finder/assets/2325013/e03bf029-6bee-478d-9b19-ea2015dab499)

The simplicity of the bug pattern is in large part due to the *condensed notation* we use in edges (see [publication][ndss23paper]).
For example, the self-loop in state *start* describes transitions on all messages other than the input message `SR_AUTH` (which leads to an implicit sink state) and the output messages `UA_SUCCESS` and `UA_FAILURE`, which lead to state *bug*.
The bug pattern is implemented by the following code:


```
digraph G {
label=""
start [color="red"]
bug [shape="doublecircle"]

start -> start [label="other - {I_SR_AUTH}"]
start -> bug [label="{O_UA_SUCCESS, O_UA_FAILURE}"]

__start0 [label="" shape="none" width="0" height="0"];
__start0 -> start;
}
```

This bug pattern catalogue is given in XML, and specifies the bug patterns to check and information on them (e.g., name, bug severity).
Below is the excerpt specifying the  *Missing SR_AUTH* bug pattern:

```xml
<bugPattern>
    <name>Missing SR_AUTH</name>
    <bugLanguage>missing_sr_auth.dot</bugLanguage>
    <description>Authentication is performed without SR_AUTH.</description>
</bugPattern>
```

## Validation

The bug validation step requires arguments to establish TCP connection to a protocol-specific test harness, typically extracted from the state fuzzer used to generate models.
Using this connection, **SMBugFinder** instructs the test harness to execute sequences of inputs on the SUT and retrieve generated response.
For the *Missing SR_AUTH* bug, the input sequence would be `KEXINIT KEX30 NEWKEYS UA_PK_OK`.
**SMBugFinder** would check that the response, when combined with the input sequence, still exposes the bug.
Validation is disabled by default, and can be enabled via the `-vb` option.

Suppose that our test harness for SSH listens at address `localhost:7000`.
To run bug detection on Dropbear with validation enabled, one would run[^1]:

    > java -jar target/sm-bug-finder.jar -m /models/ssh/server/Dropbear-v2020.81.dot -c /patterns/ssh/server/patterns.xml -vb -ha localhost:7000

## Arguments

**SMBugFinder** supports many other options, e.g., for configuring the algorithm used to generate witnesses.
For a full list of options run:

    > java -jar target/sm-bug-finder.jar

For ease of use, **SMBugFinder** includes in the [args folder](args), *argument files* containing arguments for executing common experiments.
**SMBugFinder** can be run on these argument files.
Below is an an example which includes some of the arguments we have just covered:

    > java -jar target/sm-bug-finder.jar args/dropbear-v2020.81

## Useful links

* the [NDSS'2023 publication][ndss23paper], describing the bug detection technique and its evaluation on DTLS and SSH;
* the [NDSS'2023 artifact][ndss23artifact], which is a VM that can be used to reproduce the NDSS'2023 experiments[^2];
* the [DTLS component of the artifact][dtlsartifact], which is [DTLS-Fuzzer][dtlsfuzzer] incorporating **SMBugFinder** to perform bug detection automatically;
* the [SSH component of the artifact][sshartifact], containing additional scripts to reproduce experiments for SSH;
* [DTLS-Fuzzer][dtlsfuzzer], [EDHOC-Fuzzer][edhocfuzzer] and [SSH-Fuzzer][sshfuzzer], fuzzers for DTLS, EDHOC and SSH (the latter is WIP) which can generate SUT models.

[^1]:For convenience, **SMBugFinder** resolves `/models/ssh/Dropbear-v2020.81.dot` and `src/main/resources/models/ssh/Dropbear-v2020.81.dot` to the same file. When resolving a path starting with `/`, **SMBugFinder** first checks it in its source directories (e.g., `src/main/resources/`), and only if unsuccessful, in the root directory.
[^2]:Note that **SMBugFinder** has seen significant updates since the artifact. As a result, it is no longer compatible with the version that was used in the artifact.


[ndss23paper]:https://www.ndss-symposium.org/wp-content/uploads/2023/02/ndss2023_s68_paper.pdf
[ndss23artifact]:https://doi.org/10.5281/zenodo.7129240
[dtlsartifact]:https://gitlab.com/pfg666/dtls-fuzzer/-/blob/bugcheck-artifact
[sshartifact]:https://github.com/assist-project/state-machine-bug-finder/tree/bugcheck-artifact
[graphviz]:https://graphviz.org/
[xdot]:https://pypi.org/project/xdot/
[dropbear]: https://matt.ucc.asn.au/dropbear/dropbear.html
[dtlsfuzzer]:https://github.com/assist-project/dtls-fuzzer
[edhocfuzzer]:https://github.com/protocol-fuzzing/edhoc-fuzzer
[sshfuzzer]:https://github.com/assist-project/ssh-fuzzer
[sshharness]:https://easy.dans.knaw.nl/ui/datasets/id/easy-dataset:77503
