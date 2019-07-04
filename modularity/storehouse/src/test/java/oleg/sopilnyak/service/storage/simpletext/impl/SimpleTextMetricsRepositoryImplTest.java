/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.storage.simpletext.impl;

import oleg.sopilnyak.service.metric.storage.SelectCriteria;
import oleg.sopilnyak.service.metric.storage.impl.MetricEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.time.Instant;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleTextMetricsRepositoryImplTest {

	@InjectMocks
	private SimpleTextMetricsRepositoryImpl repository = new SimpleTextMetricsRepositoryImpl();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		Properties index = SimpleTextMetricsRepositoryImpl.loadModuleIndex();
		new File(SimpleTextMetricsRepositoryImpl.METRICS_INDEX).delete();
		index.values().forEach(file->new File((String)file).delete());
	}

	@Test
	public void persist() {
		MetricEntity entity = MetricEntity.builder()
				.name("test-name")
				.measured(Instant.now())
				.module("test-module")
				.host("test-host")
				.actionId("test-action-id")
				.valueAsString("test-metric-data")
				.build();
		repository.persist(entity);
	}

	@Test
	public void find() {
		MetricEntity entity1 = MetricEntity.builder()
				.name("test-name1")
				.measured(Instant.now())
				.module("test-module-1")
				.host("test-host")
				.actionId("test-action-id")
				.valueAsString("test-metric-data")
				.build();
		repository.persist(entity1);
		MetricEntity entity2 = MetricEntity.builder()
				.name("test-name2")
				.measured(Instant.now())
				.module("test-module-2")
				.host("test-host")
				.actionId("test-action-id")
				.valueAsString("test-metric-data")
				.build();
		repository.persist(entity2);

		SelectCriteria criteria = mock(SelectCriteria.class);
		Collection<MetricEntity> selected = repository.find(criteria, 0,10);

		assertEquals(2, selected.size());

		when(criteria.getModule()).thenReturn("test-module-2");
		selected = repository.find(criteria, 0,10);

		assertEquals(1, selected.size());
		assertEquals(entity2, selected.iterator().next());

	}
}