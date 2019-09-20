/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import oleg.sopilnyak.service.action.ActionContext;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Type: context for atomic-module-action
 */
@Data
@AllArgsConstructor
@Builder
@ToString(of = {"criteria", "input", "output"})
class AtomicActionContext<IN, OUT> implements ActionContext<IN,OUT> {
	private Map<String, Object> criteria;
	private IN input;
	private OUT output;
	private Callable<OUT> action;

	@Override
	public boolean addCriteria(String criteriaName, Object criteriaValue){
		return Objects.isNull(criteria.putIfAbsent(criteriaName, criteriaValue));
	}

	/**
	 * To save the result of successful operation
	 *
	 * @param result the result of operation
	 */
	@Override
	public void saveResult(OUT result) {
		this.output = result;
	}
}
