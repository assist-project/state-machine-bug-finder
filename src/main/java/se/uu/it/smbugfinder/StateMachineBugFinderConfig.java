package se.uu.it.smbugfinder;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

public class StateMachineBugFinderConfig {

    @Parameter(names = {"-m", "-model"}, required = true, description = "Mealy machine of the SUT in DOT format with which bug detection is performed.")
    private String model;

    @Parameter(names = {"-c", "-catalogue"}, required = true, description = "XML file which catalogues DFA-encoded bug patterns, expressed in DOT format.")
    private String patterns;

    @Parameter(names = {"-ha", "-harnessAddress"}, required = false, description = "Listening address of the test harness used to validate bugs. "
            + "The harness should be able to apply inputs on the SUT, retrieve its outputs and reset it.")
    private String harnessAddress;

    @Parameter(names = {"-vm", "-validationModel"}, required = false, description = "Mealy machine in DOT format used to simulate a SUT.")
    private String validationModel;

    @Parameter(names = {"-os", "-outputSeparator"}, required = false, description = "Separator used to split compound outputs into atomic outputs.")
    private String separator = "+";

    @Parameter(names = {"-eo", "-emptyOutput"}, required = false, description = "String corresponding to the empty output."
            + "An empty output is mapped to an empty set of DFA symbols. ")
    private String emptyOutput = "TIMEOUT";

    @Parameter(names = {"-od", "-outputDir"}, required = false, description = "Directory to export DFA models, statistics and the bug report to.")
    private String outputDir = "output";

    @Parameter(names = {"-rm", "-resetMessage"}, required = false, description = "Message to send to the test harness in order to reset the SUT.")
    private String resetMessage = "reset";

    @Parameter(names = {"-rcm", "-resetConfirmationMessage"}, required = false, description = "Confirmation message to read from the test harness after resetting the SUT. "
            + "If unset, no confirmation message is read.")
    private String resetConfirmationMessage = null;

    @ParametersDelegate
    private StateMachineBugFinderCoreConfig smBugFinderConfig;

    public StateMachineBugFinderConfig() {
        smBugFinderConfig = new StateMachineBugFinderCoreConfig();
    }

    public String getModel() {
        return model;
    }

    public String getHarnessAddress() {
        return harnessAddress;
    }

    public String getValidationModel() {
        return validationModel;
    }

    public String getSeparator() {
        return separator;
    }

    public String getEmptyOutput() {
        return emptyOutput;
    }

    public StateMachineBugFinderCoreConfig getSmBugFinderConfig() {
        return smBugFinderConfig;
    }

    public String getPatterns() {
        return patterns;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getResetMessage() {
        return resetMessage;
    }

    public String getResetConfirmationMessage() {
        return resetConfirmationMessage;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setValidationModel(String validationModel) {
        this.validationModel = validationModel;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setEmptyOutput(String emptyOutput) {
        this.emptyOutput = emptyOutput;
    }

    public void setPatterns(String patterns) {
        this.patterns = patterns;
    }
}
