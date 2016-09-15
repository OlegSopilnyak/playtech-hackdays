package com.mobenga.health.monitor.strategy;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.monitor.strategy.impl.DoubleVariableTypeStrategy;
import com.mobenga.health.monitor.strategy.impl.IntegerVariableTypeStrategy;
import com.mobenga.health.monitor.strategy.impl.StringVariableTypeStrategy;
import com.mobenga.health.monitor.strategy.impl.TimeStampVariableTypeStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * The factory of strategies
 */
public class VariableTypeStrategies {
    private static final Map<ConfiguredVariableItem.Type, VariableTypeStrategy> strategyMap = new HashMap<>();
    static {
        strategyMap.put(ConfiguredVariableItem.Type.STRING, new StringVariableTypeStrategy());
        strategyMap.put(ConfiguredVariableItem.Type.INTEGER, new IntegerVariableTypeStrategy());
        strategyMap.put(ConfiguredVariableItem.Type.DOUBLE, new DoubleVariableTypeStrategy());
        strategyMap.put(ConfiguredVariableItem.Type.TIME_STAMP, new TimeStampVariableTypeStrategy());
    }
    /**
     * To get the instance of strategy by type
     *
     * @param type variable type
     * @return strategy or IllegalArgumentException if wrong type
     */
    public static VariableTypeStrategy get(ConfiguredVariableItem.Type type){
        final VariableTypeStrategy strategy = strategyMap.get(type);
        if (strategy == null){
            throw new IllegalArgumentException("No strtegy for type:"+type);
        }
        return strategy;
    }
}
