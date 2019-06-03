/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.storage.simpletext.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.metric.storage.ModuleMetricsRepository;
import oleg.sopilnyak.module.metric.storage.SelectCriteria;
import oleg.sopilnyak.module.metric.storage.impl.MetricEntity;
import org.springframework.util.StringUtils;

import java.io.*;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Realization for trivial modules-metric operations
 */
@Slf4j
public class SimpleTextMetricsRepositoryImpl implements ModuleMetricsRepository {
	static final String METRICS_INDEX = "metrics.index";
	private static final SecureRandom random = new SecureRandom();
	private static final MessageFormat METRIC_FORMATTER = new MessageFormat("{0}^{1}^{2}^{3}^{4}^{5}");
	private static final ReadWriteLock storeLock = new ReentrantReadWriteLock();
	/**
	 * To persis entity to repository scope
	 *
	 * @param entity entity to persist
	 */
	@Override
	public void persist(MetricEntity entity) {
		log.debug("Persisting metric {}", entity);
		storeLock.writeLock().lock();
		final Properties index = loadModuleIndex();
		final String dataFileName = getFileNameFor(entity.getName(), index);
		log.debug("Try to store metric to '{}'", dataFileName);
		try(final PrintWriter dataOutput = new PrintWriter(new FileWriter(dataFileName, true))) {
			final String dataLine = METRIC_FORMATTER.format(new Object[]{ entity.getName()
					, entity.getModule()
					, entity.getHost()
					, entity.getActionId()
					, entity.getMeasured().toString()
					, entity.getValueAsString()
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
	 * To find metrics by search criteria
	 *
	 * @param criteria criteria for select
	 * @param offset   offset of selected data
	 * @param limit    maximum quantity of entities to return
	 * @return collection of selected entities of empty collection
	 */
	@Override
	public Collection<MetricEntity> find(SelectCriteria criteria, int offset, int limit) {
		log.debug("Getting metrics by criteria {}", criteria);
		log.debug("Getting metrics offset is {} limit is {}", offset, limit);
		storeLock.readLock().lock();
		try {
			final Properties index = loadModuleIndex();
			final Properties suitable = indexByName(index, criteria.getName());
			return suitable.keySet().stream().map(key -> (String) key).sorted()
					.map(name->suitable.getProperty(name))
					.flatMap(fileName -> loadMetrics(fileName))
					.filter(metric-> matches(metric, criteria))
					.skip(offset)
					.limit(limit)
					.collect(Collectors.toSet())
					;
		} catch (IOException e) {
			log.error("Cannot find metrics", e);
			return Collections.EMPTY_SET;
		} finally {
			storeLock.readLock().unlock();
		}
	}

	// private methods
	static boolean matches(final MetricEntity metric, final SelectCriteria criteria) {
		if (!StringUtils.isEmpty(criteria.getName())){
			if (!criteria.getName().equalsIgnoreCase(metric.getName())){
				return false;
			}
		}
		if (!StringUtils.isEmpty(criteria.getHost())){
			if (!criteria.getHost().equalsIgnoreCase(metric.getHost())){
				return false;
			}
		}
		if (!StringUtils.isEmpty(criteria.getModule())){
			if (!criteria.getModule().equalsIgnoreCase(metric.getModule())){
				return false;
			}
		}
		if (criteria.getFrom() != null){
			if (metric.getMeasured().isBefore(criteria.getFrom())){
				return false;
			}
		}
		if (criteria.getTo() != null){
			if (metric.getMeasured().isAfter(criteria.getTo())){
				return false;
			}
		}
		if (!StringUtils.isEmpty(criteria.getValueReg())){
			return Pattern.compile(criteria.getValueReg()).matcher(metric.getValueAsString()).matches();
		}
		return true;
	}

	static Stream<MetricEntity> loadMetrics(String fileName) {
		try(final BufferedReader reader = new BufferedReader(new FileReader(fileName))){
			final List<MetricEntity> metrics = new LinkedList<>();
			String line;
			while (!StringUtils.isEmpty(line = reader.readLine())){
				final Object[] metric = METRIC_FORMATTER.parse(line);
				metrics.add(MetricEntity.builder()
						.name((String) metric[0])
						.module((String) metric[1])
						.host((String) metric[2])
						.actionId((String) metric[3])
						.measured(Instant.parse((String) metric[4]))
						.valueAsString((String) metric[5])
						.build()
				);
			}
			return metrics.stream();
		} catch (FileNotFoundException e) {
			log.error("Cannot load, no data-file", e);
		} catch (IOException e) {
			log.error("Cannot load, something went wrong", e);
		} catch (ParseException e) {
			log.error("Cannot parse line from file {}", fileName, e);
		}
		return Stream.empty();
	}

	static Properties indexByName(Properties index, String name) throws IOException {
		if (StringUtils.isEmpty(name)){
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			index.storeToXML(out,"transfer");
			final Properties selected = new Properties();
			selected.loadFromXML(new ByteArrayInputStream(out.toByteArray()));
			return selected;
		}
		final Properties selected = new Properties();
		for(Enumeration names = index.propertyNames();names.hasMoreElements();){
			String metricName = (String) names.nextElement();
			if (name.equalsIgnoreCase(metricName)){
				selected.setProperty(metricName, index.getProperty(metricName));
			}
		}
		return selected;
	}

	static Properties loadModuleIndex() {
		final Properties modules = new Properties();
		try(final FileReader reader = new FileReader(METRICS_INDEX)) {
			modules.load(reader);
		} catch (IOException e) {
			log.error("Cannot load modules index", e);
		}
		return modules;
	}

	static String getFileNameFor(String metricName, Properties index) {
		final String dataFileName = index.getProperty(metricName);
		if (StringUtils.isEmpty(dataFileName)) {
			index.setProperty(metricName, "" + Math.abs(random.nextLong()) + ".data");
			storeModulesIndex(index);
			return index.getProperty(metricName);
		}
		return dataFileName;
	}

	static void storeModulesIndex(Properties index) {
		try {
			final FileWriter writer = new FileWriter(METRICS_INDEX);
			index.store(writer, "Index of modules.");
			writer.close();
		} catch (IOException e) {
			log.error("Cannot store modules index.", e);
		}
	}
}
