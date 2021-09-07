package se.uu.it.smbugfinder.witness;

public class SearchConfig {
	private int stateVisitBound = 1;
	private SearchOrder order = SearchOrder.MIN_VISIT_MIN_STATE;
	private boolean visitTargetStates = true;
	private int bound = -1;
	
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
}
