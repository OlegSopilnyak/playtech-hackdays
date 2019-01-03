/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.metric.storage;

import java.time.Instant;
import java.util.Collection;

/**
 * Service: storage of module's metrics
 */
public interface ModuleMetricStorage {
	/**
	 * To store the metric
	 *
	 * @param name name of metric
	 * @param module PK of module-owner
	 * @param measured time when metric was measured
	 * @param host the host where module is running
	 * @param metricAsString value of metric
	 */
	void storeMetric(String name, String module, Instant measured, String host, String metricAsString);

	/**
	 * To find metrics by criteria
	 *
	 * @param criteria select metrics criteria
	 * @param offset offset of result to return
	 * @param pageSize the size of returned set
	 * @return collection of stored metrics
	 */
	Collection<StoredMetric> find(SelectCriteria criteria, int offset, int pageSize);
}
