package net.ijus.nidi.bindings;

/**
 * Created by pfried on 6/16/14.
 */
public enum Scope implements Comparable {

	SINGLETON(0),
	CONTEXT_GLOBAL(1),
	ONE_PER_BINDING(2),
	ALWAYS_CREATE_NEW(3)

	int value

	Scope(int val) {
		this.value = val
	}

	static Scope forNumber(int num) {
		return values().find{Scope s-> s.value == num}
	}

}
