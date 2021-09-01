package se.uu.it.bugfinder.witness;

public enum SearchOrder {
	INSERTION, // don't prioritize the queue, use insertion order 
	MIN_VISIT, // prioritize paths which visit states the least number of times
	MIN_STATE_MIN_VISIT,// prioritize paths which visit states the least number of times 
	MIN_STATE, // prioritize paths which visit the fewest number of distinct states
	MAX_STATE, // prioritize paths which visit the largest number of distinct states
	MIN_VISIT_MIN_STATE
}
