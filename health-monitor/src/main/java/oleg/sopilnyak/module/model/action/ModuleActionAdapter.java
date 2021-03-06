/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.action;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.dto.ModuleDto;

import java.time.Instant;

/**
 * Type: parent of any action of module
 */
@Data
@ToString(of = {"name", "id", "hostName", "state", "module", "started", "duration", "description", "parent"})
@NoArgsConstructor
public class ModuleActionAdapter implements ModuleAction {
	private ModuleBasics module;
	private ModuleAction parent;
	private String id;
	private String name;
	private String description;
	private Instant started;
	private Long duration;
	private String hostName;
	private State state;

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

