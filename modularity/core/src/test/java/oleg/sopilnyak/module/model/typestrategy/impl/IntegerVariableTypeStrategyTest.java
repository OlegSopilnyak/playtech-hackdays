/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.typestrategy.impl;

import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategiesFactory;
import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategy;
import org.junit.Test;

import java.util.Date;

import static oleg.sopilnyak.module.model.VariableItem.Type.INTEGER;
import static org.junit.Assert.*;

public class IntegerVariableTypeStrategyTest {
	private VariableTypeStrategy strategy = VariableTypeStrategiesFactory.get(INTEGER);

	@Test
	public void testAsString() {
		String value = strategy.asString(0.12);
		assertEquals("0", value);
		assertEquals("20", strategy.asString(20.15));
		assertEquals("10", strategy.asString(10));
	}
	@Test(expected = IllegalArgumentException.class)
	public void testAsStringBad() {
		String value = strategy.asString("0.12");
		fail("Here we are waiting for exception");
	}
	@Test(expected = IllegalArgumentException.class)
	public void testAsStringBad2() {
		String value = strategy.asString(new Date());
		fail("Here we are waiting for exception");
	}


	@Test
	public void testConvert() {
		Object value = strategy.convert("10.12");
		assertEquals(10, value);
		assertEquals(100, strategy.convert("100"));
	}
	@Test(expected = IllegalArgumentException.class)
	public void testConvertBad() {
		Object value = strategy.convert("water");
		fail("Here we are waiting for exception");
	}

	@Test
	public void testConvert1() {
		assertEquals(10.0, strategy.convert(Double.class, "10.12").doubleValue(), 0.0);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testConvert1Bad() {
		int value = strategy.convert(Integer.class, "water");
		fail("Here we are waiting for exception");
	}

	@Test
	public void testDefaultValue() {
		assertEquals("0", strategy.defaultValue());
	}

	@Test
	public void testIsValid() {
		assertTrue(strategy.isValid("100"));
		assertTrue(strategy.isValid("0.5"));
		assertFalse(strategy.isValid("01/01/2030"));
		assertFalse(strategy.isValid("water"));
	}

	@Test
	public void testToString() {
		assertEquals("IntegerVariableTypeStrategy{}", strategy.toString());
	}
}