package oleg.sopilnyak.service.storage.simpletext.impl;

import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.dto.ModuleDto;
import oleg.sopilnyak.service.dto.VariableItemDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SimpleTextConfigurationRepositoryImplTest {

	private final SimpleTextConfigurationRepositoryImpl repo = new SimpleTextConfigurationRepositoryImpl();
	private ModuleDto module = new ModuleDto();

	@Before
	public void setUp(){
		module.setSystemId("test-system");
		module.setModuleId("test-module");
		module.setVersionId("test-version");
		module.setDescription("Hello world.");
	}

	@After
	public void tearDown() {
		repo.deleteModuleConfiguration(module);
	}

	@Test
	public void expandConfiguration() {
		Map<String, VariableItem> config = new LinkedHashMap<>();
		config.put("1.var", new VariableItemDto("var", 100));

		repo.expandConfiguration(module, config);

		assertEquals(config.get("1.var"), repo.getConfiguration(module).get("1.var"));
	}

	@Test
	public void replaceConfiguration() {
		Map<String, VariableItem> config = new LinkedHashMap<>();
		config.put("1.var", new VariableItemDto("var", 200));
		config.put("2.var", new VariableItemDto("var2", "Hi"));
		config.put("3.var", new VariableItemDto("var3", new Date()));

		assertEquals(0, repo.getConfiguration(module).size());

		repo.replaceConfiguration(module, config);

		assertEquals(3, repo.getConfiguration(module).size());
	}

}