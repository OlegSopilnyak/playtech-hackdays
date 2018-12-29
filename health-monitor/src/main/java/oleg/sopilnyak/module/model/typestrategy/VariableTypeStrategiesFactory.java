/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.typestrategy;

import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.module.model.typestrategy.impl.DoubleVariableTypeStrategy;
import oleg.sopilnyak.module.model.typestrategy.impl.IntegerVariableTypeStrategy;
import oleg.sopilnyak.module.model.typestrategy.impl.StringVariableTypeStrategy;
import oleg.sopilnyak.module.model.typestrategy.impl.TimeStampVariableTypeStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Type - factory for type converters strategy
 */
public final class VariableTypeStrategiesFactory {
	private static final Map<VariableItem.Type, VariableTypeStrategy> STRATEGIES_MAP = new HashMap<>();
	static {
		STRATEGIES_MAP.put(VariableItem.Type.STRING, new StringVariableTypeStrategy());
		STRATEGIES_MAP.put(VariableItem.Type.INTEGER, new IntegerVariableTypeStrategy());
		STRATEGIES_MAP.put(VariableItem.Type.DOUBLE, new DoubleVariableTypeStrategy());
		STRATEGIES_MAP.put(VariableItem.Type.TIME_STAMP, new TimeStampVariableTypeStrategy());
	}
	/**
	 * To get the instance of strategy by type
	 *
	 * @param type variable type
	 * @return strategy or IllegalArgumentException if wrong type
	 */
	public static VariableTypeStrategy get(VariableItem.Type type){
		final VariableTypeStrategy strategy = STRATEGIES_MAP.get(type);
		if (strategy == null){
			throw new IllegalArgumentException("No strtegy for type:"+type);
		}
		return strategy;
	}

	// private methods
	private VariableTypeStrategiesFactory() {
	}
}
