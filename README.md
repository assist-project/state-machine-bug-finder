# StateMachineBugFinder

**StateMachineBugFinder** (or **SMBugFinder** for short) is the implementation of our automata-based bug detection technique.
**SMBugFinder** can be used both as a standalone testing tool, or as a library which can be incorporated in other state-fuzzing frameworks to automate analysis of learned models.
As a testing tool, it takes as input a DOT Mealy machine model and a *pattern folder*, containing patterns for the bugs we want to check.
It checks the model for these patterns, and optionally validates each found bug by executing tests via an external test harness.
Communication with the harness is performed over sockets and involves exchanging inputs and outputs.

**SMBugFinder** currently contains an extensive set of bug patterns developed for SSH servers, which are located in the 'src/main/resources/bugpatterns/ssh' directory, and a few bug patterns developed for DTLS clients, located in 'src/main/resources/bugpatterns/dtls'.
A much more comprehensive set of DTLS bug patterns has been incorporated in **DTLS-Fuzzer**.
SSH and DTLS Mealy machine models that can be used for bug detection are located in the 'src/main/resources/models' directory.
These models were learned using the **DTLS-Fuzzer**'s learning setup for DTLS, and the SSH learning setup available at the [link][sshharness].
These learning setups also provide test harnesses.

## Quick walkthrough

Suppose we want to test Dropbear V2020.81, for which a model is present in our models directory ('src/main/resources/models/ssh').
From within **SMBugFinder**'s directory we then run:

    > mvn install
    > java -jar target\sm-bug-finder.jar args/dropbear-v2020.81
    
First step installs **SMBugFinder**. 
Second executes **SMBugFinder** on the arguments included in the argument file 'args/dropbear-v2020.81'.
Executing the second command should reveal three bugs found in the model learned for this version of Dropbear.
To validate these bugs, we need to provide the additional argument '-validateBugs'.
We would also need the SSH test harness from the dataset at the [link][sshharness] to be online.

One of the bugs included in the **Listing Bugs** section is *Missing SR_AUTH*.
The bug entails the server engaging the authentication service without a prior service request for it (which is 'SR_AUTH' in our input alphabet).
The witness uncovered exposes this problem.

    > KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS

Authentication was successful ('UA_SUCCESS') without prior request for the authentication service (via 'SR_AUTH'). 
The bug pattern which lead to this bug's capture is defined in the DOT model 'src/main/resources/bugpatterns/ssh/missing_sr_auth.dot', which can be viewe with `xdot`.

[sshharness]:https://easy.dans.knaw.nl/ui/datasets/id/easy-dataset:77503