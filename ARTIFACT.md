**StateMachineBugFinder** (or **SMBugFinder** for short) is the implementation of our automata-based bug detection technique.
**SMBugFinder** can be used both as a standalone testing tool, or as a library which can be incorporated in other learning setups (such as **DTLS-Fuzzer**'s) to automate analysis of learned models.
As a testing tool it requires connection to an online test harness.
This document presents our SSH server bug patterns and the learned models they were checked on, experimental results for SSH and how to *partially* reproduce them (partially is used since we omit validation).
It is assumed that **SMBugFinder** and are installed (see **SMBugFinder's**'s 'README.md'), and that all commands are run from within **SMBugFinder's**'s directory.

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
The folder (e.g. 'experiments/results/ssh/Dropbear-v2014.65') includes:

 - a summary of the results and statistics ('bug_report.txt');
 - models for the DFA-encoded bug patterns after the condensed notation (e.g. 'other') has been resolved (e.g. 'WrongCertificateTypeLanguage.dot');
 - DFA models for the SUT ('sutLanguage.dot') and its intersection with bug patterns, if this intersection is not empty (e.g. 'sutWrongCertificateTypeLanguage.dot);
 - witnesses for all the bugs found, contained in the 'bugs' directory.

# Reproducing experiments

We developed a script `run_bugchecker.sh`, which reproduces our experiments for SSH minus validation.



## Quick walkthrough

Suppose we want to test Dropbear V2020.81, for which a model is present in our models directory ('src/main/resources/models/ssh').
From within **SMBugFinder**'s directory we then run:

    > mvn install
    > java -jar target\sm-bug-finder.jar args/dropbear-v2020.81
    
First step installs **SMBugFinder**. 
Second executes **SMBugFinder** on the arguments included in the argument file 'args/dropbear-v2020.81'.
Executing the second command should reveal three bugs found in the model learned for this version of Dropbear.
To validate these bugs, we need to provide the additional argument '-vb', as well as a test harness for SSH.
One of the bug shown in the print-out is *Missing SR_AUTH*.
The bug entails the server engaging the authentication service without a prior service request for it (which is 'SR_AUTH' in our input alphabet).
The witness uncovered exposes this problem.

    > KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS

Authentication was successful (UA_SUCCESS) without prior request for the authentication service (via 'SR_AUTH'). 
The bug pattern which lead to this bug's capture is defined by the DOT model 'missing_sr_auth.dot'. 
This model is basic, consisting of two nodes and four edges.

## Reproducing experiments

For each SUT tested in our work, we provide a corresponding argument file in the 'args' directory.
Reproducing the experiments without validation can be done be running **SMBugFinder** on each of these argument files.
Validation of found bugs is out-of-scope for this artifact.

[sshharness]:https://easy.dans.knaw.nl/ui/datasets/id/easy-dataset:77503