/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import oleg.sopilnyak.module.ModuleBasics;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Type to transport Module's properties
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class ModuleDto implements ModuleBasics, Serializable {
	private String systemId;
	private String moduleId;
	private String versionId;
	private String description;

	public ModuleDto(String primaryKey) {
		final StringTokenizer st = new StringTokenizer(primaryKey, "::");
		systemId = st.nextToken();
		moduleId = st.nextToken();
		versionId= st.nextToken();
	}
}
