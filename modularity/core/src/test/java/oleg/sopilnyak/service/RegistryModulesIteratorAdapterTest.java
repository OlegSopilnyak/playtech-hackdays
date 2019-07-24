package oleg.sopilnyak.service;

import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.action.storage.ModuleActionStorageStub;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class RegistryModulesIteratorAdapterTest {
	@Autowired
	private ModuleActionStorage actionStorage;
	@Autowired
	private ModuleConfigurationStorage configurationStorage;
	@Autowired
	private ModulesRegistryService registry;

	@Autowired
	private  RegistryModulesIteratorAdapterTest.TestRegistryModulesIterator service;

	@Before
	public void setUp() {
		service.moduleStart();
	}

	@After
	public void tearDown() {
		service.moduleStop();
	}

	@Test
	public void iterateRegisteredModules() {
		String label = "test";
		reset(service);

		service.iterateRegisteredModules(label);

		verify(service, times(1)).inspectModule(eq(label), any(ModuleAction.class), eq(service));
		verify(service, times(1)).getMetricsContainer();
	}

	@Test
	public void inspectModule() {
		String label = "test";
		ModuleAction action = mock(ModuleAction.class);
		Module module = mock(Module.class);
		reset(service);

		service.inspectModule(label, action, module);

		verify(service, times(1)).inspectModuleInternal(eq(label), eq(action), eq(module));
	}
	//inner classes
	static class TestRegistryModulesIterator extends RegistryModulesIteratorAdapter{

		@Override
		protected void inspectModule(String label, ModuleAction action, Module module) {
			inspectModuleInternal(label, action, module);
		}
		void inspectModuleInternal(String label, ModuleAction action, Module module){

		}
		@Override
		public String getSystemId() {
			return "modules";
		}
		@Override
		public String getModuleId() {
			return "just-test";
		}
		@Override
		public String getVersionId() {
			return "0.8";
		}
		@Override
		public String getDescription() {
			return "Test service for deep testing.";
		}
	}
	@Configuration
	@Import({ModuleSystemConfiguration.class})
	static class Config{

		@Bean
		public RegistryModulesIteratorAdapterTest.TestRegistryModulesIterator makeTestService(){
			return spy(new RegistryModulesIteratorAdapterTest.TestRegistryModulesIterator());
		}
		@Bean
		public ModuleActionStorage mockModuleActionStorage(){
			return spy(new ModuleActionStorageStub());
		}
		@Bean
		public ModuleConfigurationStorage mockModuleConfigurationStorage(){
			return mock(ModuleConfigurationStorage.class);
		}
		@Bean
		public ModuleMetricStorage mockModuleMetricStorage(){
			return mock(ModuleMetricStorage.class);
		}
	}
}