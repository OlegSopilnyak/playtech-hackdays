/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.bean;

import lombok.*;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.model.dto.ModuleDto;

import java.time.Instant;

/**
 * Type: parent of any action of module
 */
@Data
@ToString(of = {"name", "id", "hostName", "state", "module", "started", "duration", "description", "parent"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleActionAdapter implements ModuleAction {
	protected ModuleBasics module;
	protected ModuleAction parent;
	protected String id;
	protected String name;
	protected String description;
	protected Instant started;
	protected Long duration;
	protected String hostName;
	protected State state;

	public ModuleActionAdapter(ModuleBasics module, ModuleAction parent, String name) {
		this.module = new ModuleDto(module);
		this.parent = parent;
		this.name = name;
		this.state = State.INIT;
	}

	public ModuleActionAdapter(ModuleBasics module, String name) {
		this(module, null, name);
	}

	public String valueAsString() {
		return "action :" + name
				+ " id :" + id
				+ " host :" + hostName
				+ " state :" + state
				+ " module :" + module.primaryKey()
				+ " started :" + started
				+ " duration :" + duration
				+ " description : " + description;

	}
}

