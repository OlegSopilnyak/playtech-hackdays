/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action;

import java.util.Objects;

/**
 * Type context for executing atomic-action-activity
 */
public interface ActionContext<IN,OUT> {

	/**
	 * To add new criteria value to context
	 *
	 * @param criteriaName  name of criteria
	 * @param criteriaValue value of criteria
	 * @return true if added
	 */
	boolean addCriteria(String criteriaName, Object criteriaValue);

	/**
	 * To get access to context's criteria
	 *
	 * @return map name->value of criteria's map
	 */
	java.util.Map<String, Object> getCriteria();

	/**
	 * To get input of action's call
	 *
	 * @return the value or null if not applied
	 */
	IN getInput();

	/**
	 * To get result of execution
	 *
	 * @return
	 */
	OUT getOutput();

	/**
	 * To save the result of successful operation
	 *
	 * @param result the result of operation
	 */
	void saveResult(OUT result);

	/**
	 * To get callable instance of action
	 *
	 * @return callable instance
	 */
	java.util.concurrent.Callable<OUT> getAction();

	/**
	 * To check if no input and empty criteria
	 *
	 * @return true if only callable presents
	 */
	default boolean isTrivial(){
		return Objects.isNull(getInput())
				|| Objects.isNull(getCriteria())
				|| getCriteria().isEmpty();
	}
}
