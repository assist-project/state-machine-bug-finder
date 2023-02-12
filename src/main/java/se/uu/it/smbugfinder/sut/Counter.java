package se.uu.it.smbugfinder.sut;

import java.util.concurrent.atomic.AtomicLong;

public class Counter {
	private AtomicLong atomicLong;
	private String name;

	public Counter(String name) {
		atomicLong = new AtomicLong();
		this.name = name;
	}

	public long increment() {
		return atomicLong.incrementAndGet();
	}

	public long add(long value) {
		return atomicLong.addAndGet(value);
	}

	public long get() {
		return atomicLong.get();
	}

	@Override
	public String toString() {
		return "Counter [name=" + name + ", " + "value=" + atomicLong.get() +  "]";
	}


}
