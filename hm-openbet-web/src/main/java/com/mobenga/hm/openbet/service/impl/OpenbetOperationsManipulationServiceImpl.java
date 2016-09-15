package com.mobenga.hm.openbet.service.impl;

import com.mobenga.health.model.MonitoringOperationContext;
import com.mobenga.health.monitor.strategy.BetServiceStrategyFactory;
import com.mobenga.health.storage.impl.OpenBetOperationStorageImpl;
import com.mobenga.hm.openbet.dto.MonitorCriteria;
import com.mobenga.hm.openbet.dto.MonitorOperation;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.util.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.text.CollationKey;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * realization of OpenbetOperationsManipulationService for ES
 */
public class OpenbetOperationsManipulationServiceImpl implements OpenbetOperationsManipulationService {
    private static final Logger LOG = LoggerFactory.getLogger(OpenbetOperationsManipulationServiceImpl.class);

    @Autowired
    private OpenBetOperationStorageImpl operationStorage;

    public void initStorage() {
        LOG.info("Init storage.");
    }

    /**
     * To get operations that fit the criteria
     *
     * @param criteria selection criteria
     * @return the list of DTO objects
     */
    @Override
    public List<MonitorOperation> selectOperationsByCriteria(MonitorCriteria criteria) {
        LOG.debug("Selecting operations for {}", criteria);
        Map<String, Object> openbetCriteria = operationCriteria(criteria);
        Map<String, Object> actionsCriteria = monitorCriteria(criteria);

        return
                operationStorage
                        .selectOperations(openbetCriteria, actionsCriteria)
                        .stream()
                        .map(c -> new MonitorOperation(c.getOperation(), c.getMonitoredAction()))
                        .collect(Collectors.toList());
    }

    /**
     * To get the list monitored OpenBet operations
     *
     * @return the names list
     */
    @Override
    public List<String> supportedOperationTypes() {
        return operationStorage.supportedOperationTypes();
    }

    /**
     * To get the quantity of operations that fit the criteria
     *
     * @param criteria selection criteria
     * @return the quantity of operations
     */
    @Override
    public long countOperationByCriteria(MonitorCriteria criteria) {
        LOG.debug("Counting operations for {}", criteria);
        Map<String, Object> openbetCriteria = operationCriteria(criteria);
        Map<String, Object> actionsCriteria = monitorCriteria(criteria);

        return operationStorage.countOperations(openbetCriteria, actionsCriteria);
    }

    // private methods
    private Map<String, Object> monitorCriteria(MonitorCriteria uiCriteria) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        if (!StringUtils.isEmpty(uiCriteria.getFromDate())) {
            criteria = transformFromDate(uiCriteria.getFromDate(), criteria);
        }
        if (!StringUtils.isEmpty(uiCriteria.getToDate())) {
            criteria = transformToDate(uiCriteria.getToDate(), criteria);
        }
        return criteria;
    }

    private Map<String, Object> transformFromDate(String fromDate, Map<String, Object> criteria) {
        if (StringUtils.isEmpty(fromDate = fromDate.trim())) {
            // empty criteria fromDate field only spaces
            return criteria;
        }
        try {
            Date from = DatatypeConverter.parseDateTime(fromDate).getTime();
            criteria.put("from", from.getTime());
        } catch (Throwable t) {
//            t.printStackTrace();
        }
        return criteria;
    }

    private Map<String, Object> transformToDate(String toDate, Map<String, Object> criteria) {
        if (StringUtils.isEmpty(toDate = toDate.trim())) {
            // empty criteria toDate field only spaces
            return criteria;
        }
        try {
            Date to = DatatypeConverter.parseDateTime(toDate).getTime();
            criteria.put("to", to.getTime());
        } catch (Throwable t) {
//            t.printStackTrace();
        }
        return criteria;
    }

    private Map<String, Object> operationCriteria(MonitorCriteria uiCriteria) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        if (!StringUtils.isEmpty(uiCriteria.getBet())) {
            criteria = transformBet(uiCriteria.getBet(), criteria);
        }
        if (!StringUtils.isEmpty(uiCriteria.getOperationType())) {
            criteria = transformOperation(uiCriteria.getOperationType(), criteria);
        }
        if (!StringUtils.isEmpty(uiCriteria.getCustomer())) {
            criteria = transformCustomer(uiCriteria.getCustomer(), criteria);
        }
        return criteria;
    }

    private Map<String, Object> transformCustomer(String customer, Map<String, Object> criteria) {
        if (StringUtils.isEmpty(customer = customer.trim())) {
            // empty criteria operationType field only spaces
            return criteria;
        }
        try {
            Long.parseLong(customer);
            criteria.put("customerId", customer);
        } catch (NumberFormatException e) {
        }
        return criteria;
    }

    private Map<String, Object> transformOperation(String operationType, Map<String, Object> criteria) {
        if (StringUtils.isEmpty(operationType = operationType.trim())) {
            // empty criteria operationType field only spaces
            return criteria;
        }
        criteria.put("typeName", operationType);
        return criteria;
    }

    private Map<String, Object> transformBet(String criteriaBet, Map<String, Object> criteria) {
        if (StringUtils.isEmpty(criteriaBet = criteriaBet.trim())) {
            // empty criteria bet field only spaces
            return criteria;
        }
        try {
            Long.parseLong(criteriaBet);
            criteria.put("betId", criteriaBet);
        } catch (NumberFormatException e) {
        }
        criteria.put("receipt", criteriaBet);
        return criteria;
    }
}
