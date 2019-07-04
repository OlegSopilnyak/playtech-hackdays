package oleg.sopilnyak.service;

import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.action.storage.ModuleActionStorageStub;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		ModuleSystemConfiguration.class
		, ModuleServiceAdapterTest.Config.class
})
public class ModuleServiceAdapterTest {

	@Autowired
	private TestService service;

	@Before
	public void setUp() {
		service.moduleStart();
	}

	@After
	public void tearDown() {
		service.moduleStop();
	}

	@Test
	public void moduleStart() {
	}

	@Test
	public void moduleStop() {
	}

	@Test
	public void getMainAction() {
	}

	@Test
	public void isActive() {
	}

	@Test
	public void getCondition() {
	}

	@Test
	public void healthGoUp() {
	}

	@Test
	public void healthGoLow() {
	}

	@Test
	public void lastThrown() {
	}

	@Test
	public void canRestart() {
	}

	@Test
	public void getConfiguration() {
	}

	@Test
	public void configurationChanged() {
	}

	@Test
	public void getMetricsContainer() {
	}

	@Test
	public void configurationVariableOf() {
	}

	@Test
	public void activateMainModuleAction() {
	}

	@Test
	public void finishModuleAction() {
	}

	@Test
	public void initAsService() {
	}

	@Test
	public void shutdownAsService() {
	}

	@Test
	public void configurationItemChanged() {
	}

	@Test
	public void executeAtomicAction() {
	}

	@Test
	public void setupModuleConfiguration() {
	}

	//inner classes
	static class TestService extends ModuleServiceAdapter{
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
	static class Config{
		@Bean
		public TestService makeTestService(){
			return new TestService();
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