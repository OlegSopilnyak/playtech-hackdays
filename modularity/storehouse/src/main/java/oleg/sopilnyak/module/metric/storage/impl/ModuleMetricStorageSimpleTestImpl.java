/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.metric.storage.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.module.metric.storage.SelectCriteria;
import oleg.sopilnyak.module.metric.storage.StoredMetric;
import org.springframework.util.StringUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service: storage of module's metrics
 */
@Slf4j
public class ModuleMetricStorageSimpleTestImpl implements ModuleMetricStorage {
	private static final String METRICS_INDEX = "metrics.index";
	private static final SecureRandom random = new SecureRandom();
	private static final MessageFormat METRIC_FORMATTER = new MessageFormat("{0}^{1}^{2}^{3}");
	private static final ReadWriteLock storeLock = new ReentrantReadWriteLock();
	/**
	 * To store the metric
	 *
	 * @param name           name of metric
	 * @param module         PK of module-owner
	 * @param measured       time when metric was measured
	 * @param host           the host where module is running
	 * @param metricAsString value of metric
	 */
	@Override
	public void storeMetric(String name, String module, Instant measured, String host, String metricAsString) {
		storeLock.writeLock().lock();
		final Properties index = loadModuleIndex();
		final String dataFileName = getFileNameFor(name, index);
		log.debug("Try to store metric to '{}'", dataFileName);
		try(final PrintWriter dataOutput = new PrintWriter(new FileWriter(dataFileName, true))) {
			final String dataLine = METRIC_FORMATTER.format(new Object[]{
					module,
					measured.toString(),
					host,
					metricAsString
			});
			log.debug("metric '{}'", dataLine);
			dataOutput.println(dataLine);
		} catch (IOException e) {
			log.error("Cannot store data to file '{}'", dataFileName, e);
			e.printStackTrace();
		} finally {
			storeLock.writeLock().unlock();
		}
	}

	/**
	 * To find metrics by criteria
	 *
	 * @param criteria select metrics criteria
	 * @param offset   offset of result to return
	 * @param pageSize the size of returned set
	 * @return collection of stored metrics
	 */
	@Override
	public Collection<StoredMetric> find(SelectCriteria criteria, int offset, int pageSize) {
		storeLock.readLock().lock();
		final Properties index = loadModuleIndex();
		final String metricName = criteria.getName();
		final String dataFileName = getFileNameFor(metricName, index);
		try {
			return null;
		}finally {
			storeLock.readLock().unlock();
		}
	}
	// private methods
	private Properties loadModuleIndex() {
		final Properties modules = new Properties();
		try(final FileReader reader = new FileReader(METRICS_INDEX)) {
			modules.load(reader);
		} catch (IOException e) {
			log.error("Cannot load modules index", e);
		}
		return modules;
	}

	private String getFileNameFor(String metricName, Properties index) {
		final String dataFileName = index.getProperty(metricName);
		if (StringUtils.isEmpty(dataFileName)) {
			index.setProperty(metricName, "" + random.nextLong() + ".data");
			storeModulesIndex(index);
			return index.getProperty(metricName);
		}
		return dataFileName;
	}

	private void storeModulesIndex(Properties index) {
		try {
			final FileWriter writer = new FileWriter(METRICS_INDEX);
			index.store(writer, "Index of modules.");
			writer.close();
		} catch (IOException e) {
			log.error("Cannot store modules index.", e);
		}
	}

}
