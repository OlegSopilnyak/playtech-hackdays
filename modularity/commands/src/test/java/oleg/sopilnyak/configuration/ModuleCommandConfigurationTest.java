package oleg.sopilnyak.configuration;

import oleg.sopilnyak.commands.ModuleCommand;
import oleg.sopilnyak.commands.ModuleCommandFactory;
import oleg.sopilnyak.commands.impl.ListModuleCommand;
import oleg.sopilnyak.commands.impl.RestartModuleCommand;
import oleg.sopilnyak.commands.impl.StartModuleCommand;
import oleg.sopilnyak.commands.impl.StopModuleCommand;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		ModuleCommandConfiguration.class,
		ModuleUtilityConfiguration.class,
		ModuleCommandConfigurationTest.TestConfiguration.class
})
public class ModuleCommandConfigurationTest {
	@Autowired
	private ApplicationContext context;

	@Test
	public void getModuleCommandFactory() {
		assertNotNull(context.getBean(ModuleCommandFactory.class));
	}

	@Test
	public void commandPrototypesTest() {
		ModuleCommand command = context.getBean(ListModuleCommand.class);
		assertNotEquals(command, context.getBean(ListModuleCommand.class));

		command = context.getBean(StartModuleCommand.class);
		assertNotEquals(command, context.getBean(StartModuleCommand.class));

		command = context.getBean(StartModuleCommand.class);
		assertNotEquals(command, context.getBean(StopModuleCommand.class));

		command = context.getBean(StopModuleCommand.class);
		assertNotEquals(command, context.getBean(StartModuleCommand.class));

		command = context.getBean(RestartModuleCommand.class);
		assertNotEquals(command, context.getBean(RestartModuleCommand.class));
	}
	// inner classes
	@Configuration
	static class TestConfiguration{

		@Bean
		public ModulesRegistryService mockModulesRegistry(){
			ModulesRegistryService registry = mock(ModulesRegistryService.class);
			return registry;
		}
	}
}