/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.metric.storage.impl;

import oleg.sopilnyak.service.action.ModuleActionsRepository;
import oleg.sopilnyak.service.metric.storage.ModuleMetricsRepository;
import oleg.sopilnyak.service.metric.storage.SelectCriteria;
import oleg.sopilnyak.service.metric.storage.StoredMetric;
import oleg.sopilnyak.service.metric.storage.impl.MetricEntity;
import oleg.sopilnyak.service.metric.storage.impl.ModuleMetricStorageImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModuleMetricStorageImplTest {
	@Mock
	private ModuleMetricsRepository repository;
	@Mock
	private ModuleActionsRepository actions;

	@InjectMocks
	private ModuleMetricStorageImpl storage = new ModuleMetricStorageImpl();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		reset(actions, repository);
	}

	@Test
	public void storeMetric() {
		Instant time = Instant.now();
		MetricEntity entity = MetricEntity.builder()
				.name("test-name")
				.module("test-module")
				.measured(time)
				.host("test-host")
				.actionId("test-action-id")
				.valueAsString("test-metric-data")
				.build();

		storage.storeMetric(entity.getName(), entity.getModule(), entity.getMeasured(), entity.getHost(), entity.getActionId(), entity.getValueAsString());

		verify(repository, times(1)).persist(eq(entity));
	}

	@Test
	public void find() {
		Instant time = Instant.now();
		MetricEntity entity = MetricEntity.builder()
				.name("test-name")
				.module("test-module")
				.measured(time)
				.host("test-host")
				.actionId("test-action-id")
				.valueAsString("test-metric-data")
				.build();
		SelectCriteria criteria = mock(SelectCriteria.class);

		when(repository.find(criteria, 10, 100)).thenReturn(Collections.singletonList(entity));

		Collection<StoredMetric> found = storage.find(criteria, 10, 100);

		assertEquals(1, found.size());
		assertEquals(entity.getName(), found.iterator().next().getName());
		assertEquals(entity.getMeasured(), found.iterator().next().getMeasured());
		assertEquals(entity.getValueAsString(), found.iterator().next().valuesAsString());

		verify(repository, times(1)).find(eq(criteria), eq(10), eq(100));
		verify(actions, times(1)).getById(eq(entity.getActionId()));
	}
}