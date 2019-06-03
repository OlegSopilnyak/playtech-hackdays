/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import oleg.sopilnyak.module.ModuleBasics;

import java.util.StringTokenizer;

/**
 * Type to transport Module's properties
 */
@Data
@NoArgsConstructor
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

	public ModuleDto(String primaryKey) {
		final StringTokenizer st = new StringTokenizer(primaryKey, "::");
		systemId = st.nextToken();
		moduleId = st.nextToken();
		versionId= st.nextToken();
	}
}
