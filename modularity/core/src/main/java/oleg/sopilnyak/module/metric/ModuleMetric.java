/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.metric;

import oleg.sopilnyak.module.model.ModuleAction;

import java.time.Instant;

/**
 * Type: metric of module
 */
public interface ModuleMetric extends Comparable<ModuleMetric>{
	/**
	 * To get action-owner of metrics
	 *
	 * @return reference to action
	 */
	ModuleAction action();

	/**
	 * To get time when metric was took
	 *
	 * @return the time
	 */
	Instant measured();

	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	String name();

	/**
	 * The values of metric, according to metric's type it maybe one or several values
	 *
	 * @return array of values
	 */
	default Object[] value(){
		return new Object[0];
	}

	/**
	 * To compose string from array of values according to metric's type
	 *
	 * @return representation of value as string
	 */
	default String valuesAsString(){
		return "";
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 *
	 * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)
	 *
	 * <p>The implementor must also ensure that the relation is transitive:
	 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.
	 *
	 * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
	 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
	 * all <tt>z</tt>.
	 *
	 * <p>It is strongly recommended, but <i>not</i> strictly required that
	 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
	 * class that implements the <tt>Comparable</tt> interface and violates
	 * this condition should clearly indicate this fact.  The recommended
	 * language is "Note: this class has a natural ordering that is
	 * inconsistent with equals."
	 *
	 * <p>In the foregoing description, the notation
	 * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
	 * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
	 * <tt>0</tt>, or <tt>1</tt> according to whether the value of
	 * <i>expression</i> is negative, zero or positive.
	 *
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                              from being compared to this object.
	 */
	@Override
	default int compareTo(ModuleMetric o){
		assert o != null;
		return measured().compareTo(o.measured());
	}
}
