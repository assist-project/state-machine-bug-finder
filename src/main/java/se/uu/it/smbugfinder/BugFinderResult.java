package se.uu.it.smbugfinder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import net.automatalib.common.util.Pair;
import se.uu.it.smbugfinder.bug.Bug;
import se.uu.it.smbugfinder.bug.StateMachineBug;

public class BugFinderResult<I,O> extends ExportableResult {
    private static final int  BUG_ID_LENGTH = 3;

    private List<StateMachineBug<I,O>> bugs;
    private Statistics statistics;

    public BugFinderResult(List<StateMachineBug<I,O>> bugs, Statistics statistics) {
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

    public void generateExecutableWitnesses(File witnessFolder) throws IOException {
        for (StateMachineBug<I,O> bug : bugs) {
            File witnessFile = new File(witnessFolder, bugToCompactRepresentation(bug));
            writeWitness(bug, witnessFile);
        }
    }
    private String bugToCompactRepresentation(StateMachineBug<?,?> bug) {
        String repr = "bug_id_" + idToString(bug.getId()) + "_type_"+bug.getBugPattern().getShortenedName();
        return repr;
    }

    private String idToString(Integer id) {

        String idString = String.valueOf(id);
        StringBuilder builder = new StringBuilder();

        for (int i=0; i< BUG_ID_LENGTH - idString.length(); i++) {
            builder.append('0');
        }
        builder.append(idString);
        return builder.toString();
    }

    private void writeWitness(StateMachineBug<?,?> bug, File witnessFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(witnessFile, StandardCharsets.UTF_8))) {
            for (Pair<?,?> ioPair : bug.getTrace()) {
                pw.println(ioPair.getFirst().toString());
                pw.println("#" + ioPair.getSecond().toString());
            }
        }
    }

}
