**StateMachineBugFinder** (or **SMBugFinder** for short) is the implementation of our automata-based bug detection technique.
**SMBugFinder** can be used both as a standalone testing tool, or as a library which can be incorporated in other learning setups (such as **DTLS-Fuzzer**'s) to automate analysis of learned models.
As a testing tool it requires connection to an online test harness.
This document presents our SSH server bug patterns and the learned models they were checked on, experimental results for SSH and to reproduce them.
It is assumed that **SMBugFinder** and are installed (see **SMBugFinder's**'s 'README.md'), and that all commands are run from within **SMBugFinder's**'s directory.
The necessary software is already setup in the virtual machine provided as part of the artifact.
Also setup is Dropbear 2020.81, to exemplify validation.

# SSH bug patterns

Bug patterns for SSH servers are located in located in the 'src/main/resources/bugpatterns/ssh' directory.
Below is the *MISSING SR_AUTH* bug pattern located at 'src/main/resources/bugpatterns/ssh/missing_sr_auth.dot'. 

```
digraph G {
label=""
start [color="red"]
bug [shape="doublecircle"]

start -> start [label="other - {I_SR_AUTH}"]
start -> bug [label="{O_UA_SUCCESS}"]

__start0 [label="" shape="none" width="0" height="0"];
__start0 -> start;
}

```

Bug patterns are encoded as DOT graphs and can be viewed with `xdot`, e.g. by running:

    > xdot src/main/resources/bugpatterns/ssh/missing_sr_auth.dot

# Learned models for SSH

We used the [learning setup][sshharness]  developed in prior work to generate models for SSH servers that we later analyzed using **SMBugFinder**'s testing tool facility.
The SSH server models used in our submission are stored in 'src/main/resources/models/ssh'.
Each model is named after the SUT whose behavior it captures.

# Experimental results for SSH

Results for SSH servers along with a CSV summary are stored in 'experiments/results/ssh'.
There is a folder for each experiment, named after the SUT analyzed. 
A folder (e.g. 'experiments/results/ssh/Dropbear-v2020.81') includes:

 - a summary of the results and statistics ('bug_report.txt'), which includes bugs found in models, bugs validated and the number of inputs/resets used to validate them;
 - models for the DFA-encoded bug patterns after the condensed notation (e.g. 'other') has been resolved (e.g. 'MissingSR_AUTHLanguage.dot');
 - DFA models for the SUT ('sutLanguage.dot') and its intersection with bug patterns, if this intersection is not empty (e.g. 'sutMissingSR_AUTHLanguage.dot');

#  Performing a single bug detection experiment

Suppose we want to test Dropbear V2020.81, for which a model is present in our models directory ('src/main/resources/models/ssh').
From within **SMBugFinder**'s directory we first launch Dropbear, having it listen on port 7000.

    > suts/ssh/dropbear-2020.81/dropbear -p localhost:7001 -F -R -T 100

In a separate terminal, we then launch the SSH mapper (test infrastructure) taken from a prior [artifact][sshharness].

    > python2.7 ssh-mapper/paramiko/mapper/mapper.py -l localhost:7000 -s localhost:7001 -c Dropbear 
    
The mapper is listening on local port 7000 for inputs (e.g. 'KEXINIT') to execute on the SSH implementation listening at local port 7001.
Finally, in a third terminal, we execute **SMBugFinder** on the arguments included in the argument file 'args/dropbear-v2020.81', telling it, in addition, to validate the bugs using the `-vb` option.

    > java -jar target/sm-bug-finder.jar args/dropbear-v2020.81 -vb


This should produce the output:

```
--------------------------------------------------------------------------------
Listing Bugs
--------------------------------------------------------------------------------

Bug Id: 1
Bug Pattern: Invalid Closure Response
Severity: LOW
Description: Server does not respond to a CH_CLOSE with a CH_CLOSE (Property 12).
Trace: KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS CH_OPEN/CH_OPEN_SUCCESS CH_CLOSE/CH_EOF 
Inputs: KEXINIT KEX30 NEWKEYS UA_PK_OK CH_OPEN CH_CLOSE
Validation Status: VALIDATION_SUCCESSFUL

Bug Id: 2
Bug Pattern: Missing SR_AUTH
Severity: LOW
Description: Authentication is performed without SR_AUTH.
Trace: KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS 
Inputs: KEXINIT KEX30 NEWKEYS UA_PK_OK
Validation Status: VALIDATION_SUCCESSFUL

```

Executing should produce the above output revealing two bugs found in the model learned for this version of Dropbear.
One of the bug shown in the print-out is *Missing SR_AUTH*.
The bug entails the Dropbear server engaging the authentication service without a prior service request for it (which is 'SR_AUTH' in our input alphabet).
The witness uncovered exposes this problem.

    > KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS

Authentication was successful ('UA_SUCCESS') without prior request for the authentication service (via 'SR_AUTH'). 
The bug pattern which lead to this bug's capture is defined by the DOT model 'src/main/resources/bugpatterns/ssh/missing_sr_auth.dot' which we can view with `xdot`. 
Running this command also produces the output folder 'output/Dropbear2020.81' whose structure was described earlier. 

# Reproducing bug detection experiments

We developed a script, `run_bugchecker.sh`, designed to reproduce the SSH experiments conducted in our submission, the results of which are stored in 'experiments/results/ssh/' in our artifact.
The script executes the relevant bug checking experiments and collates the results into a CSV summary ('ssh_summary.csv').
Results are stores in 'output/ssh'.
The summary can be checked against the relevant table in our submission, or against the bugs we reported.
Validation is only supported for Dropbear.

## Without validation

Suppose we want to run bug detection on all three SSH servers without validation.
We then run:

    > ./run_bugchecker.sh -ao

Option `-ao` causes the summary file to be automatically opened once the experiments are done.
All bugs found are marked as 'not_validated'.
There should be two such bugs for Dropbear, five for OpenSSH and four for BitVise.

## With validation

Options `-v` and `-qv` enable BFS validation and quick, single witness validation, respectively. 
Suppose we want to run bug detection on Dropbear, this time using validation.
We first need to ensure Dropbear and the test harness are running.
For convenience, we provide the scripts to launch them: `run_dropbear.sh`, `run_dropbear_mapper.sh`.
Each script must be executed in a separate terminal.
We then run:

    > ./run_bugchecker.sh -v -ao

This should generate in 'output/ssh' experimental results and a summary that are consistent with those used for the submission ('experiments/results/ssh').
It should reveal two validated bugs in Dropbear.

[sshharness]:https://easy.dans.knaw.nl/ui/datasets/id/easy-dataset:77503