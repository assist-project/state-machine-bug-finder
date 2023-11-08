package se.uu.it.smbugfinder.bugfinding;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import se.uu.it.smbugfinder.bug.BugValidationStatus;
import se.uu.it.smbugfinder.bug.StateMachineBug;

public class BugFindingTest {
    /**
     * Checks that, in the list of found bugs, the specific bugs capture the provided bug patterns.
     */
    protected void assertFoundSpecificBugPatterns(List<StateMachineBug<String,String>> bugs, String ... expectedBugPatterns) {
        String [] foundBugPatterns = bugs.stream()
                .filter(b -> !b.getBugPattern().isGeneral())
                .map(b -> b.getBugPattern().getName())
                .toArray(String []::new);
        Assert.assertArrayEquals( "Bug patterns detected different:\\n   expected: %s \\n   found: %s"
                .formatted(Arrays.toString(expectedBugPatterns), Arrays.toString(foundBugPatterns)),
                expectedBugPatterns, foundBugPatterns);
    }


    /**
     * Checks that validation was successful for all specific bugs, and not performed for general bugs.
     */
    protected void assertValidationSuccess(List<StateMachineBug<String,String>> bugs) {
        for (var bug : bugs) {
            // only specific bug patterns are validated
            if (!bug.getBugPattern().isGeneral()) {
                Assert.assertEquals(bug.getStatus(), BugValidationStatus.VALIDATION_SUCCESSFUL);
            } else {
                Assert.assertEquals(bug.getStatus(), BugValidationStatus.NOT_VALIDATED);
            }
        }
    }

    /**
     * Checks that validation was unsuccessful for all specific bugs, and not performed for general bugs.
     */
    protected void assertValidationFail(List<StateMachineBug<String,String>> bugs) {
        for (var bug : bugs) {
            // only specific bug patterns are validated
            if (!bug.getBugPattern().isGeneral()) {
                Assert.assertEquals(bug.getStatus(), BugValidationStatus.VALIDATION_FAILED);
            } else {
                Assert.assertEquals(bug.getStatus(), BugValidationStatus.NOT_VALIDATED);
            }
        }
    }
}
