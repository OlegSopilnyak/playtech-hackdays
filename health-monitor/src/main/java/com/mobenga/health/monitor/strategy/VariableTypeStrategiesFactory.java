package com.mobenga.health.monitor.strategy;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.monitor.strategy.impl.DoubleVariableTypeStrategy;
import com.mobenga.health.monitor.strategy.impl.IntegerVariableTypeStrategy;
import com.mobenga.health.monitor.strategy.impl.StringVariableTypeStrategy;
import com.mobenga.health.monitor.strategy.impl.TimeStampVariableTypeStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * The factory of strategies
 */
public class VariableTypeStrategiesFactory {
    private static final Map<ConfiguredVariableItem.Type, VariableTypeStrategy> STRATEGIES_MAP = new HashMap<>();
    static {
        STRATEGIES_MAP.put(ConfiguredVariableItem.Type.STRING, new StringVariableTypeStrategy());
        STRATEGIES_MAP.put(ConfiguredVariableItem.Type.INTEGER, new IntegerVariableTypeStrategy());
        STRATEGIES_MAP.put(ConfiguredVariableItem.Type.DOUBLE, new DoubleVariableTypeStrategy());
        STRATEGIES_MAP.put(ConfiguredVariableItem.Type.TIME_STAMP, new TimeStampVariableTypeStrategy());
    }
    /**
     * To get the instance of strategy by type
     *
     * @param type variable type
     * @return strategy or IllegalArgumentException if wrong type
     */
    public static VariableTypeStrategy get(ConfiguredVariableItem.Type type){
        final VariableTypeStrategy strategy = STRATEGIES_MAP.get(type);
        if (strategy == null){
            throw new IllegalArgumentException("No strtegy for type:"+type);
        }
        return strategy;
    }
}
