/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.model.typestrategy.impl;

import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategiesFactory;
import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategy;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Objects;

import static oleg.sopilnyak.module.model.VariableItem.Type.TIME_STAMP;
import static org.junit.Assert.*;

public class TimeStampVariableTypeStrategyTest {
	private VariableTypeStrategy strategy = VariableTypeStrategiesFactory.get(TIME_STAMP);

	@Test
	public void testAsString() {
		String value = strategy.asString(new Date());
		assertFalse(StringUtils.isEmpty(value));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAsStringBad() {
		String value = strategy.asString(0.12);
		fail("Here we are waiting for exception");
	}


	@Test
	public void testConvert() {
		Object value = strategy.convert("2012-05-29T10:30:45.100+0200");
		assertTrue(Objects.nonNull(value));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvert1() {
		int value = strategy.convert(Integer.class, "10.12");
		fail("Here we are waiting for exception");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvert1Bad() {
		int value = strategy.convert(Integer.class, "water");
		fail("Here we are waiting for exception");
	}

	@Test
	public void testDefaultValue() {
		String value = strategy.asString(new Date(0));
		assertEquals(value, strategy.defaultValue());
	}

	@Test
	public void testIsValid() {
		String value = strategy.asString(new Date(0));
		assertTrue(strategy.isValid(value));
	}

	@Test
	public void testToString() {
		assertEquals("TimeStampVariableTypeStrategy{}", strategy.toString());
	}
}