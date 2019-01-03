/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.action;

import lombok.Data;
import lombok.NoArgsConstructor;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.dto.ModuleDto;

import java.time.Instant;

/**
 * Type: parent of any action of module
 */
@Data
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

	public void setStarted(Instant started) {
		this.started = started;
		this.duration = 0L;
	}

	public String valueAsString() {
		return name
				+ " id:" + id
				+ " host:" + hostName
				+ " state:" + state
				+ " module:" + module.primaryKey()
				+ " started:" + started
				+ " duration:" + duration
				+ " description: " + description;

	}
}

