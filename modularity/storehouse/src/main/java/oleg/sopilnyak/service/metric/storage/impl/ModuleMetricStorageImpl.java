/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric.storage.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.ModuleActionsRepository;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.metric.storage.ModuleMetricsRepository;
import oleg.sopilnyak.service.metric.storage.SelectCriteria;
import oleg.sopilnyak.service.metric.storage.StoredMetric;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Storage of module's metrics
 */
@Slf4j
public class ModuleMetricStorageImpl implements ModuleMetricStorage {

	@Autowired
	private ModuleMetricsRepository repository;
	@Autowired
	private ModuleActionsRepository actions;
	/**
	 * To store the metric
	 *
	 * @param name           name of metric
	 * @param module         PK of module-owner
	 * @param measured       time when metric was measured
	 * @param host           the host where module is running
	 * @param actionId       the id of metric's action
	 * @param metricAsString value of metric
	 */
	@Override
	public void storeMetric(String name, String module, Instant measured, String host, String actionId, String metricAsString) {
		final MetricEntity entity = MetricEntity.builder()
				.name(name)
				.module(module)
				.measured(measured)
				.host(host)
				.actionId(actionId)
				.valueAsString(metricAsString)
				.build();
		log.debug("Try to persist metric '{}'", entity);
		repository.persist(entity);
	}

	/**
	 * To find metrics by criteria
	 *
	 * @param criteria select metrics criteria
	 * @param offset   offset of result to return
	 * @param limit the size of returned set
	 * @return collection of stored metrics
	 */
	@Override
	public Collection<StoredMetric> find(SelectCriteria criteria, int offset, int limit) {
		final Collection<MetricEntity> metricEntities = repository.find(criteria, offset, limit);
		return metricEntities.stream().map(e->restore(e)).filter(m->m != null).collect(Collectors.toSet());
	}

	// private methods
	private StoredMetric restore(MetricEntity entity){
		final StoredMetricDto dto = new StoredMetricDto();
		dto.action = actions.getById(entity.getActionId());
		dto.measured = entity.getMeasured();
		dto.name = entity.getName();
		dto.valueAsString = entity.getValueAsString();
		return dto;
	}
	// inner class
	private static class StoredMetricDto implements StoredMetric{
		private ModuleAction action;
		private Instant measured;
		private String name;
		private String valueAsString;
		/**
		 * To get action-owner of metrics
		 *
		 * @return reference to action
		 */
		@Override
		public ModuleAction getAction() {
			return action;
		}

		/**
		 * To get time when metric was took
		 *
		 * @return the time
		 */
		@Override
		public Instant getMeasured() {
			return measured;
		}

		/**
		 * To get the name of metric
		 *
		 * @return the name
		 */
		@Override
		public String getName() {
			return name;
		}

		/**
		 * The values of metric, according to metric's type it maybe one or several values
		 *
		 * @return array of values
		 */
		@Override
		public Object[] getValue() {
			throw new UnsupportedOperationException("Method does not supported.");
		}

		/**
		 * To compose string from array of values according to metric's type
		 *
		 * @return representation of value as string
		 */
		@Override
		public String valuesAsString() {
			return valueAsString;
		}
	}
}
