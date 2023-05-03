package se.uu.it.smbugfinder.witness;

import com.beust.jcommander.Parameter;

public class SearchConfig {
    @Parameter(names = {"-svb", "-stateVisitBound"}, required=false, description = "Bound on the number of times a state can be visited.")
    private int stateVisitBound = 1;
    @Parameter(names = {"-so", "-searchOrder"}, required=false, description = "The order in which states are visited during a search.")
    private SearchOrder order = SearchOrder.MIN_VISIT_MIN_STATE;
    private boolean visitTargetStates = false;
    @Parameter(names = {"-qb", "-queueBound"}, required=false, description = "The maximum size of the queue.")
    private int queueBound = 100000;

    public void setStateVisitBound(int stateVisitBound) {
        this.stateVisitBound = stateVisitBound;
    }

    public int getStateVisitBound() {
        return stateVisitBound;
    }

    public void setOrder(SearchOrder order) {
        this.order = order;
    }

    public SearchOrder getOrder() {
        return order;
    }

    public void setVisitTargetStates(boolean visitTargetStates) {
        this.visitTargetStates = visitTargetStates;
    }

    public boolean isVisitTargetStates() {
        return visitTargetStates;
    }

    public int getQueueBound() {
        return queueBound;
    }

    public void setQueueBound(int queueBound) {
        this.queueBound = queueBound;
    }
}
