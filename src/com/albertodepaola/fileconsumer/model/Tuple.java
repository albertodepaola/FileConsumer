package com.albertodepaola.fileconsumer.model;

import java.util.Objects;

public class Tuple<X, Y> {
	
	public final X x;
	public final Y y;

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
        if (!(o instanceof Tuple)) {
            return false;
        }
        Tuple tuple = (Tuple) o;
        return Objects.equals(x, tuple.x) &&
                Objects.equals(y, tuple.y);
	}
}