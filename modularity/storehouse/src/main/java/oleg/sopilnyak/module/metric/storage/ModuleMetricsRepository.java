/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.metric.storage;

import oleg.sopilnyak.module.metric.storage.impl.MetricEntity;

import java.util.Collection;

/**
 * Repository of trivial operation with metrics
 */
public interface ModuleMetricsRepository {
	/**
	 * To persis entity to repository scope
	 *
	 * @param entity entity to persist
	 */
	void persist(MetricEntity entity);

	/**
	 * To find metrics by search criteria
	 *
	 * @param criteria criteria for select
	 * @param offset offset of selected data
	 * @param limit maximum quantity of entities to return
	 * @return collection of selected entities of empty collection
	 */
	Collection<MetricEntity> find(SelectCriteria criteria, int offset, int limit);
}
