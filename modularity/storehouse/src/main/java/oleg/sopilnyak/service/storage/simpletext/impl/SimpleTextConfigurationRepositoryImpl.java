/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.storage.simpletext.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;
import oleg.sopilnyak.service.dto.ModuleDto;
import oleg.sopilnyak.service.dto.VariableItemDto;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Service realization of configuration repository
 */
@Slf4j
public class SimpleTextConfigurationRepositoryImpl implements ConfigurationStorageRepository {
	private static final String MODULES_INDEX = "modules.index";
	private static final SecureRandom random = new SecureRandom();
	private static final ObjectMapper dataMapper = new ObjectMapper();

	/**
	 * To add items to exists configuration
	 *
	 * @param module module-owner of configuration
	 * @param extra  configuration's items to add
	 */
	@Override
	public void expandConfiguration(ModuleDto module, Map<String, VariableItem> extra) {
		log.debug("Expanding module {} by {}", module.primaryKey(), extra);
		final LinkedHashMap<String, VariableItem> current = new LinkedHashMap<>(getConfiguration(module));
		current.putAll(extra);
		replaceConfiguration(module, current);
	}

	/**
	 * To replace items of exists module configuration
	 *
	 * @param module        module-owner of configuration
	 * @param configuration configuration's items to replace
	 */
	@Override
	public void replaceConfiguration(ModuleDto module, Map<String, VariableItem> configuration) {
		log.debug("Updating configuration for module '{}'", module.primaryKey());
		final Properties index = loadModuleIndex();
		final String dataFileName = getFileNameFor(module, index);
		storeConfiguration(dataFileName, configuration);
	}

	/**
	 * To get configuration of module
	 *
	 * @param module the owner of configuration
	 * @return stored module's configuration
	 */
	@Override
	public Map<String, VariableItem> getConfiguration(ModuleDto module) {
		log.debug("Getting configuration for module '{}'", module.primaryKey());
		final Properties index = loadModuleIndex();
		final String dataFileName = getFileNameFor(module, index);
		return restoreConfiguration(dataFileName);
	}

	/**
	 * To remove configuration of module from repository
	 *
	 * @param module configuration of which is not necessary anymore
	 */
	void deleteModuleConfiguration(ModuleDto module){
		log.debug("Erasing configuration for module '{}'", module.primaryKey());
		final Properties index = loadModuleIndex();
		final String dataFileName = getFileNameFor(module, index);
		index.remove(module.primaryKey());
		storeModulesIndex(index);
		final File dataFile = new File(dataFileName);
		if (dataFile.exists() && dataFile.delete()){
			log.debug("Removed data file '{}'", dataFileName);
		}
	}

	// private methods
	private Properties loadModuleIndex() {
		final Properties modules = new Properties();
		try {
			final FileReader reader = new FileReader(MODULES_INDEX);
			modules.load(reader);
			reader.close();
		} catch (IOException e) {
			log.error("Cannot load modules index", e);
		}
		return modules;
	}

	private String getFileNameFor(ModuleDto module, Properties index) {
		final String key = module.primaryKey();
		final String dataFileName = index.getProperty(key);
		if (StringUtils.isEmpty(dataFileName)) {
			index.setProperty(key, "" + random.nextLong() + ".data");
			storeModulesIndex(index);
			return index.getProperty(key);
		}
		return dataFileName;
	}

	private void storeModulesIndex(Properties index) {
		try {
			final FileWriter writer = new FileWriter(MODULES_INDEX);
			index.store(writer, "Index of modules.");
			writer.close();
		} catch (IOException e) {
			log.error("Cannot store modules index.", e);
		}
	}

	private void storeConfiguration(String dataFileName, Map<String, VariableItem> configuration) {
		final File dataFile = new File(dataFileName);
		try {
			dataMapper.writeValue(dataFile, configuration);
		} catch (IOException e) {
			log.error("Cannot store map to file {}", dataFileName, e);
		}
	}

	private Map<String, VariableItem> restoreConfiguration(String dataFileName){
		final File dataFile = new File(dataFileName);
		try {
			return dataFile.exists() ?
					dataMapper.readValue(dataFile, new TypeReference<Map<String, VariableItemDto>>() {})
					: new LinkedHashMap<>()
					;
		}catch (IOException e){
			log.error("Cannot restore map from file {}", dataFileName, e);
			return new LinkedHashMap<>();
		}
	}

}
