/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.FailModuleAction;
import oleg.sopilnyak.module.model.action.ModuleMainAction;
import oleg.sopilnyak.module.model.action.ModuleRegularAction;
import oleg.sopilnyak.module.model.action.SuccessModuleAction;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
public class ModuleActionsConfiguration {
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ModuleMainAction createModuleMainAction(ModuleBasics module){
		return new ModuleMainAction(module);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ModuleRegularAction createModuleRegularAction(ModuleBasics module, String name){
		return new ModuleRegularAction(module, name);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public FailModuleAction makeFailModuleAction(ModuleAction action, Throwable exception){
		return new FailModuleAction(action, exception);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public SuccessModuleAction makeSuccessModuleAction(ModuleAction action){
		return new SuccessModuleAction(action);
	}
}
