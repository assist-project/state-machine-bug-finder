**StateMachineBugFinder** (or **SMBugFinder** for short) is the implementation of our automata-based bug detection technique.
**SMBugFinder** can be used both as a standalone testing tool, or as a library which can be incorporated in other learning setups (such as **DTLS-Fuzzer**'s) to automate analysis of learned models.
As a testing tool it requires connection to an online test harness.
This document presents our SSH server bug patterns and the learned models they were checked on, experimental results for SSH and how to reproduce them.
It is assumed that **SMBugFinder** is installed (see **SMBugFinder's**'s 'README.md'), and that all commands are run from within **SMBugFinder's**'s directory.
The necessary software is already setup in the virtual machine provided as part of the artifact.
Also setup are the SSH [learning setup][ssharness] provided by prior work ('ssh-mapper'), and  Dropbear 2020.81 ('suts/ssh/dropbear-2020.81'). 
We will use these to exemplify validation via an external test harness.

# SSH bug patterns

Bug patterns for SSH servers are located in located in the 'src/main/resources/bugpatterns/ssh' directory.
Below is the *MISSING SR_AUTH* bug pattern located at 'src/main/resources/bugpatterns/ssh/missing_sr_auth.dot'. 

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

Bug patterns are encoded as DOT graphs and can be viewed with `xdot`, e.g. by running:

    > xdot src/main/resources/bugpatterns/ssh/missing_sr_auth.dot

# Learned models for SSH

We used the [learning setup][sshharness]  developed in prior work to generate models for SSH servers that we later analyzed using **SMBugFinder**'s testing tool facility.
The SSH server models used in our submission are stored in 'src/main/resources/models/ssh'.
Each model is named after the SUT whose behavior it captures.

# Experimental results for SSH

Results for SSH servers along with a CSV summary ('ssh_summary.csv') are stored in 'experiments/results/ssh'.
There is a folder for each experiment, named after the SUT analyzed. 
A folder (e.g. 'experiments/results/ssh/Dropbear-v2020.81') includes:

 - a summary of the results and statistics ('bug_report.txt'), which includes bugs found in models, bugs validated and the number of inputs/resets used to validate them;
 - models for the DFA-encoded bug patterns after the condensed notation (e.g. 'other') has been resolved (e.g. 'MissingSR_AUTHLanguage.dot');
 - DFA models for the SUT ('sutLanguage.dot') and its intersection with bug patterns, if this intersection is not empty (e.g. 'sutMissingSR_AUTHLanguage.dot');

On the virtual machine, the CSV summary can be viewed with LibreOffice, e.g.

    > xdg-open experiments/results/ssh/ssh_summary.csv

A 'Text Import' LibreOffice window should pop up. 
Press 'OK' to view the summary.

#  Performing a single bug detection experiment

Suppose we want to test Dropbear V2020.81, for which a model is included in our models directory ('src/main/resources/models/ssh/Dropbear-v2020.81.dot').
From within **SMBugFinder**'s directory we first launch Dropbear, having it listen on port 7001.

    > suts/ssh/dropbear-2020.81/dropbear -p localhost:7001 -F -R -T 100

In a separate terminal, we then launch the SSH mapper (test infrastructure) taken from a prior [artifact][sshharness].

    > python2.7 ssh-mapper/paramiko/mapper/mapper.py -l localhost:7000 -s localhost:7001 -c Dropbear 
    
The mapper is listening on local port 7000 for inputs (e.g. 'KEXINIT') to execute on the SSH implementation listening at local port 7001.
Finally, in a third terminal, we execute **SMBugFinder** on the arguments included in the argument file 'args/dropbear-v2020.81', telling it to validate the bugs using the `-vb` option.

    > java -jar target/sm-bug-finder.jar args/dropbear-v2020.81 -vb


This should produce output which includes:

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

The output reveals two bugs found in the model learned for this version of Dropbear.
One of the bugs shown is *Missing SR_AUTH*.
The bug entails the Dropbear server engaging the authentication service without a prior service request for it (which is 'SR_AUTH' in our input alphabet).
The witness uncovered exposes this problem.

    > KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS

Authentication was engaged ('UA_SUCCESS') without prior request for the authentication service (via 'SR_AUTH'). 
The bug pattern which lead to this bug's capture is defined by the DOT model 'src/main/resources/bugpatterns/ssh/missing_sr_auth.dot' which we can view with `xdot`. 
Running this command also produces the output folder 'output/Dropbear-v2020.81' whose structure is similar to that of the experiment folders were described earlier. 

# Reproducing bug detection experiments

We developed a script, `run_bugchecker.sh`, designed to reproduce the SSH experiments conducted in our submission, the results of which are stored in 'experiments/results/ssh/' in our artifact.
The script executes the relevant bug checking experiments and collates the results into a CSV summary ('ssh_summary.csv').
Results are stored in 'output/ssh'.
The summary can be checked against Table 4 in our submission, or against the bugs we reported.
Validation is only supported for Dropbear.

## Without validation

Suppose we want to run bug detection on all three SSH servers without validation.
We then run:

    > ./run_bugchecker.sh -ao

Option `-ao` causes script to automatically open the summary file  once the experiments are done.
On the displayed summary, all bugs found in the implementations' models are marked as 'not_validated'.
There should be two such bugs for Dropbear, five for OpenSSH and four for BitVise.
User can also check the generated experiment folders in 'output/ssh'.

## With validation

We have found bugs, but without validation we cannot be sure they actually affect the implementation.
Options `-v` and `-qv` enable BFS validation and quick, single witness validation, respectively. 
Suppose we want to run bug detection on Dropbear, this time using validation.
We first need to ensure Dropbear and the test harness are running.
For convenience, we provide the scripts to launch them: `run_dropbear.sh`, `run_dropbear_mapper.sh`.
We then run:

    > ./run_bugchecker.sh -v -ao

This should generate in 'output/ssh' experimental results and a summary that are consistent with those used for the submission ('experiments/results/ssh').
It should reveal two validated bugs in Dropbear, Invalid Closure Response and Missing SR_AUTH.

[sshharness]:https://easy.dans.knaw.nl/ui/datasets/id/easy-dataset:77503