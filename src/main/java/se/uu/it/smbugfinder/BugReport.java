package se.uu.it.smbugfinder;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import se.uu.it.smbugfinder.bug.Bug;
import se.uu.it.smbugfinder.bug.StateMachineBug;

public class BugReport<I,O> extends ExportableResult {
    private List<StateMachineBug<I,O>> bugs;
    private Statistics statistics;
    
    public BugReport(List<StateMachineBug<I,O>> bugs, Statistics statistics) {
        this.bugs = bugs;
        this.statistics = statistics;
    }

    public List<StateMachineBug<I, O>> getBugs() {
        return Collections.unmodifiableList(bugs);
    }

    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    protected void doExport(PrintWriter pw) {
        title("Bug Report", pw);
        if (!bugs.isEmpty()) {
            section("Listing Bugs", pw);
            for (Bug<?,?> bug : bugs) {
                pw.println("Bug Id: " + bug.getId());
                pw.println(bug.getDescription());
            }
        } else {
            section("No Bugs were Found", pw);
        }
        statistics.export(pw);
    }
}