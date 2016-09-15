package com.mobenga.hm.openbet.service;

import com.mobenga.hm.openbet.dto.MonitorCriteria;
import com.mobenga.hm.openbet.dto.MonitorOperation;

import java.util.List;

/**
 * The main storage of OpenBet operations
 */
public interface OpenbetOperationsManipulationService {
    /**
     * To get operations that fit the criteria
     *
     * @param criteria selection criteria
     * @return the list of DTO objects
     */
    List<MonitorOperation> selectOperationsByCriteria(MonitorCriteria criteria);

    /**
     * To get the list monitored OpenBet operations
     *
     * @return the names list
     */
    List<String> supportedOperationTypes();

    /**
     * To get the quantity of operations that fit the criteria
     *
     * @param criteria selection criteria
     * @return the quantity of operations
     */
    long countOperationByCriteria(MonitorCriteria criteria);
}
