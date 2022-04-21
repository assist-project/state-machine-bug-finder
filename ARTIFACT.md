# state-machine-bug-finder

**StateMachineBugFinder** (or **SMBugFinder** for short) is the implementation of our automata-based bug detection technique.
**SMBugFinder** can be used both as a standalone testing tool, or as a library which can be incorporated in other state-fuzzing frameworks to automate analysis of learned models.
As a testing tool, it takes as input a DOT Mealy machine model and a *pattern folder*, containing patterns for the bugs we want to check.
It checks the model for these patterns, and validates each found bug by executing tests via a test harness with which it communicates over sockets exchanging inputs and outputs.

Besides the source code, the artifact contains the bug patterns developed for SSH, which are located in the 'src/main/resources/patterns/ssh' directory.
During bug detection, these are checked on the models learned for SSH servers, located in the 'src/main/resources/models/ssh' directory.
The learning setup used to generate these models can be found at the given [link][sshharness].
This setup provides a test harness which was re-used as part of this work for validation.

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