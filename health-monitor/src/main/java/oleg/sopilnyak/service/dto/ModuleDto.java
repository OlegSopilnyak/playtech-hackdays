/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.dto;

import lombok.Data;
import oleg.sopilnyak.module.ModuleBasics;

/**
 * Type to transport Module's properties
 */
@Data
public class ModuleDto implements ModuleBasics {
	private String systemId;
	private String moduleId;
	private String versionId;
	private String description;

	public ModuleDto(ModuleBasics module) {
		systemId = module.getSystemId();
		moduleId = module.getModuleId();
		versionId = module.getVersionId();
		description = module.getDescription();
	}
}
