/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.control.ModuleCommandFactory;
import oleg.sopilnyak.service.control.model.ModuleCommandState;
import oleg.sopilnyak.service.control.model.ModuleCommandType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.PROCESS;
import static oleg.sopilnyak.service.control.model.ModuleCommandState.SUCCESS;
import static oleg.sopilnyak.service.control.model.ModuleCommandType.HELP;

/**
 * Command: help modules command
 */
@Slf4j
public class HelpModuleCommand implements ModuleCommand {

	@Autowired
	private ObjectMapper jsonMapper;
	@Autowired
	private ModuleCommandFactory commandsFactory;
	// map of commands helps
	private static final Map<ModuleCommandType, String[]> detailedHelp = new LinkedHashMap<>();

	/**
	 * Constructor to register helps for commands
	 *
	 * @param detailedHelp
	 */
	public HelpModuleCommand(Map<ModuleCommandType, String[]> detailedHelp) {
		HelpModuleCommand.detailedHelp.putAll(detailedHelp);
	}

	/**
	 * To execute command for registry
	 *
	 * @param parameters parameters for command
	 * @return Execution result
	 */
	@Override
	public CommandResult execute(Object... parameters) {
		log.debug("Trying to help.");
		final Result result = new Result();
		result.setState(PROCESS);
		result.setData(helpByParameters(parameters));
		result.setState(SUCCESS);
		return result;
	}

	/**
	 * To get description of command for help
	 *
	 * @return value
	 */
	@Override
	public String description() {
		return "Modules help command";
	}

	/**
	 * To get the type of command
	 *
	 * @return value
	 */
	@Override
	public ModuleCommandType type() {
		return HELP;
	}

	// private methods
	private Object helpByParameters(Object[] parameters) {
		final List<Command> explanation = new ArrayList<>();
		if (Objects.isNull(parameters) || parameters.length == 0) {
			log.debug("The list of commands");
			listCommands(explanation);
		}else{
			final String command = (String) parameters[0];
			log.debug("Making help for '{}' command", command);
			particularCommandHelp(command, explanation);
		}
		return explanation.toArray(new Command[0]);
	}

	private void particularCommandHelp(String command, List<Command> explanation) {
		final ModuleCommandType type = restoreByName(command);
		if (Objects.nonNull(type)){
			final String[] help = detailedHelp.get(type);
			if (Objects.isNull(help)){
				log.error("No detailed help for command-type '{}'", type);
				return;
			}
			log.debug("Prepare detailed information.");
			final AtomicBoolean firstTime = new AtomicBoolean(true);
			Stream.of(help).forEach((line)->{
				if (firstTime.get()){
					explanation.add(Command.builder().name(command.toLowerCase()).description(line).build());
					firstTime.getAndSet(false);
				}else {
					explanation.add(Command.builder().name("").description(line).build());
				}
			});
		}
	}

	private void listCommands(List<Command> explanation) {
		commandsFactory.availableCommands().stream()
				.forEach((name) -> {
					final ModuleCommandType type;
					if (Objects.nonNull(type = restoreByName(name))) {
						final ModuleCommand cmd = commandsFactory.create(type);
						explanation.add(Command.builder().name(name).description(cmd.description()).build());
					}
				});
	}

	private ModuleCommandType restoreByName(String name) {
		return Stream.of(ModuleCommandType.values())
				.filter(type -> type.name().equalsIgnoreCase(name))
				.findFirst().orElse(null);
	}


	// inner classes
	@Data
	@AllArgsConstructor
	@Builder
	static class Command {
		private String name;
		private String description;
	}

	@Data
	class Result implements CommandResult {
		private ModuleCommandState state;
		private Object data;

		/**
		 * To get result's data as string for console output
		 *
		 * @return data as tty string
		 */
		@Override
		public String dataAsTTY() {
			final Command[] details = (Command[]) data;
			if (details.length == 0){
				return "Nothing to show";
			}
			final boolean command = isCommandHelp((Command[]) data);
			final StringBuilder builder = command ?
					new StringBuilder()
					:new StringBuilder("The list of available commands\n").append("-----------\n")

					;
			Stream.of(details).forEach(detail -> {
				builder.append(detail.name).append("\t").append(detail.description).append("\n");
			});
			if (!command) {
				builder
						.append("-----------\n")
						.append("\nTo get more information about command, use\n$<command> help\nwhere <command> is modules command you need help about.\n");
			}
			return builder.toString();
		}


		/**
		 * To get result's data as string for JS communication
		 *
		 * @return data as json string
		 */
		@Override
		public String dataAsJSON() {
			try {
				return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
			} catch (Throwable t) {
				return "{\"status\": \"failed :" + t.getClass().getSimpleName() + " - " + t.getMessage() + "\"}";
			}
		}
		// private methods
		private boolean isCommandHelp(Command[] details){
			return Stream.of(details).anyMatch(detail-> StringUtils.isEmpty(detail.getName()));
		}
	}

}
