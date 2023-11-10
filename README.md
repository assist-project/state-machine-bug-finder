# StateMachineBugFinder
**StateMachineBugFinder** (or **SMBugFinder** for short) is a cross-platform, automated bug detection framework implementing the technique presented at NDSS 2023 (see [publication][ndss2023]).
Provided a state machine model of a SUT (e.g., generated automatically by a protocol state fuzzer), **SMBugFinder** automatically analyzes the model for state machine bugs.
For analysis, **SMBugFinder** uses a catalogue of bug patterns.

## Dependencies

To use **SMBugFinder** you'll need:

* JDK 17
* Apache Maven
* (optional) [Graphviz][graphviz], Python 3.x and [xdot](https://pypi.org/project/xdot/) for model visualization

## Usage

**SMBugFinder** can be used as a standalone testing tool, or as a library which can be incorporated in other protocol state fuzzers (e.g., [DTLS-Fuzzer][dtlsfuzzer] or [EDHOC-Fuzzer][edhocfuzzer]) to automate analysis of the generated models they generate.

Suppose we want to test the SSH server implementation of [Dropbear][dropbear] V2020.81 using **SMBugFinder**.
From within **SMBugFinder**'s directory we then run:

    > mvn install
    > java -jar target/sm-bug-finder.jar -m /models/ssh/server/Dropbear-v2020.81.dot -p /patterns/ssh/server

First command installs **SMBugFinder**.
Second command executes **SMBugFinder** using two mandatory arguments:

  * the Mealy machine model of the SUT (in this case, the model for Dropbear-V2020.81 server, found [here](src/main/resources/models/ssh/server/Dropbear-v2020.81.dot));
  * the catalogue of bug patterns (in this case, patterns defined for SSH servers, found [here](src/main/resources/patterns/ssh/server));

Executing the second command should reveal three bugs identified in the Dropbear model.
One of the bugs is *Missing SR_AUTH*, for which  **SMBugFinder** gives the following information.

```
Bug Pattern: Missing SR_AUTH
Severity: LOW
Description: Authentication is performed without SR_AUTH.
Trace: KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS
Inputs: KEXINIT KEX30 NEWKEYS UA_PK_OK
Validation Status: NOT_VALIDATED
```

The witness (after `Trace: `) exposes the server performing authentication (indicated by `UA_SUCCESS` as output) without having received a request for the authentication service (done via a `SR_AUTH` input).
The witness was found on the model, however it has not been validated (validation status is `NOT_VALIDATED`) by running a corresponding test on the SUT to confirm the buggy behavior.
Enabling validation requires connection to a test harness.
More on that later.

## Output files

When executed, **SMBugFinder** generates an output directory  (named `output` by default) in which it stores:

*  bug patterns after the condensed notation has been resolved (e.g., `MissingSR_AUTHLanguage.dot`);
*  model of the DFA-conversion of the original SUT model ( `sutLanguage.dot`);
*  statistics file ( `statistics.txt`);
*  bug report listing all the bugs found, witnesses, etc. ( `bug_report.txt`);
*  if validation was enabled, for each validated bug a witness file  which **SMBugFinder** can execute to expose the bug on the SUT.

## Models

SUT models are specified as DOT graphs, and can be obtained automatically using existing protocol state fuzzers.
[src/main/resources/models](src/main/resources/models) contains sample models for DTLS and SSH, named after the SUT for which they were generated, which was done using [DTLS-Fuzzer][dtlsfuzzer] and an [SSH fuzzer](https://easy.dans.knaw.nl/ui/datasets/id/easy-dataset:77503).
The models can be visualized using [GraphViz][graphviz]'s `dot` utility, or better still, the `xdot` Python utility which builds on [GraphViz][graphviz].
The models are large, making visualization difficult.
See the respective fuzzer repos for scripts to trim the models.

## Bug patterns

**SMBugFinder** takes as argument the path to a folder containing bug paterns, which form our bug pattern catalogue.
Bug patterns are specified as DOT graphs, and currently have to be manually written.
A bug pattern defines a DFA which accepts only sequences exposing the presence of the bug.
[src/main/resources/models](src/main/resources/models) contains an extensive set of bug patterns for SSH (including all used in the NDSS publication), and a few bug patterns for DTLS (bug patterns used in the publication experiments can be found [here](https://gitlab.com/pfg666/dtls-fuzzer/-/tree/bugcheck-artifact/src/main/resources/bugpatterns)).
Below is the pattern for the *Missing SR_AUTH* bug we found in Dropbear, as displayed when running:

    > dot /patterns/ssh/server/missing_ua_success.dot

![missing_sr_auth](https://github.com/assist-project/state-machine-bug-finder/assets/2325013/e65a7a0a-b6f9-4f02-8bd7-02a5a95609fa)

The simplicity of the bug pattern is in large part due to the *condensed notation* we use in edges (see [publication][ndss2023]).
For example, the self-loop in start corresponds to transitions on all inputs other than 
The bug pattern is implemented by the following code.


```
digraph G {
label=""
start [color="red"]
bug [shape="doublecircle"]

start -> start [label="other - {I_SR_AUTH}"]
start -> bug [label="{O_UA_SUCCESS}"]

bug -> bug [label="other"]

__start0 [label="" shape="none" width="0" height="0"];
__start0 -> start;
}
```

In addition to bug patterns, the bug pattern folder must also include a mandatory `patterns.xml` file.
This file specifies the bug patterns to check and information on them (e.g., name, bug severity).
Below is the excerpt specifying the  *Missing SR_AUTH* bug pattern.

```xml
<bugPattern>
    <name>Missing SR_AUTH</name>
    <bugLanguage>missing_sr_auth.dot</bugLanguage>
    <description>Authentication is performed without SR_AUTH.</description>
</bugPattern>
```

## Validation

Validation requires arguments to establish TCP connection to a protocol-specific test harness, typically extracted from the state fuzzer used to generate models.
Using this connection, **SMBugFinder** instructs the test harness to execute sequences of inputs on the SUT and retrieve generated response.
For our *Missing SR_AUTH* bug, the input sequence would be `KEXINIT KEX30 NEWKEYS UA_PK_OK`.
**SMBugFinder** would check that the response, when combined with the input sequence, still exposes the bug.
Validation is disabled by default, and can be enabled via the `-vb` option.

Suppose that our test harness for SSH listens at address `localhost:7000`.
To run bug detection on Dropbear with validation enabled we would run:

    > java -jar target/sm-bug-finder.jar -m /models/ssh/Dropbear-v2020.81.dot -p /patterns/ssh/ -vb -ha localhost:7000

## Arguments

**SMBugFinder** supports many other useful options, e.g., for configuring the algorithm used to generate witnesses.
For a full list of options run:

    > java -jar target/sm-bug-finder.jar

For ease of use, **SMBugFinder** includes in the [args folder](args) folder, *argument files* containing arguments for executing common experiments.
**SMBugFinder** can be run on these argument files.
A good example is:


    > java -jar target/sm-bug-finder.jar args/dropbear-v2020.81

## Useful links

* the [NDSS publication][ndss2023], describing the bug detection technique and its evaluation on DTLS and SSH;
* the [publication artifact][artifact], which can be used to reproduce the experiments;
* the [DTLS component of the artifact][dtlsartifact], which is essentially DTLS-Fuzzer incorporating **SMBugFinder** as a library, to perform bug detection automatically;
* [DTLS-Fuzzer][dtlsfuzzer], [EDHOC-Fuzzer][edhocfuzzer] and [SSH-Fuzzer][sshfuzzer], fuzzers for DTLS, EDHOC and SSH (the latter is WIP) which can generate SUT models.


[artifact]:https://doi.org/10.5281/zenodo.7129240
[dtlsartifact]:https://gitlab.com/pfg666/dtls-fuzzer/-/blob/bugcheck-artifact
[graphviz]:https://graphviz.org/
[dropbear]: https://matt.ucc.asn.au/dropbear/dropbear.html
[edhocfuzzer]:https://github.com/protocol-fuzzing/edhoc-fuzzer
[dtlsfuzzer]:https://github.com/assist-project/dtls-fuzzer
[sshfuzzer]:https://github.com/assist-project/ssh-fuzzer
[ndss2023]:https://www.ndss-symposium.org/wp-content/uploads/2023/02/ndss2023_s68_paper.pdf
[sshharness]:https://easy.dans.knaw.nl/ui/datasets/id/easy-dataset:77503
