package com.mobenga.hm.openbet.service.impl;

import com.mobenga.health.adviser.OpenBetOperationsMonitorAdviser;
import com.mobenga.health.configuration.FactoryConfiguration;
import com.mobenga.health.configuration.OpenbetPersistenceConfiguration;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.model.OpenBetOperation;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.persistence.es.StructureModuleEntity;
import com.mobenga.health.storage.impl.MonitorCoreStorageImpl;
import com.mobenga.health.storage.impl.OpenBetOperationStorageImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Date;

/**
 * Class to generate test data
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
        @ContextConfiguration(classes = OpenbetPersistenceConfiguration.class, loader = AnnotationConfigContextLoader.class)
        , @ContextConfiguration(locations = {
        "classpath:/META-INF/spring/com/mobenga/health/monitor-persistence.xml"
        , "classpath:spring/com/mobenga/hm/openbet/service/impl/test-real-monitor-storage-openbet.xml"
})
})
public class GenerateOpenbetTestData {
    @Autowired
    private MonitorCoreStorageImpl storage;
    @Autowired
    private TimeService timer;

    @Autowired
    private OpenBetOperationStorageImpl obStorage;

    @Autowired
    OpenBetOperationsMonitorAdviser adviser;

    @Test
    public void shouldStoreOpenbetOperation() throws Exception {
        System.out.println("Start inserting");
        HealthItemPK pk = createHealthItemPK(1);
        String moduleId = ((StructureModuleEntity) pk).getId();

        for (int i = 1; i < 15; i++)
            createOpenbetCashoutOperation(pk, i);

    }

    // private methods
    private void createOpenbetCashoutOperation(HealthItemPK pk, int order) {
        Date moment = timer.now();
        moment.setMinutes(moment.getMinutes() + order);
        MonitoredAction action = storage.createMonitoredAction();
        action.setStart(moment);
        action.setState(MonitoredAction.State.INIT);
        action.setDescription("################");
        storage.saveActionState(pk, action);

        OpenBetOperation operation = obStorage.createOpenBetOperation();
        operation.setActionId(action.getId());
        operation.setUserToken("#########################");
        operation.setType(OpenBetOperation.Type.cashoutBet);
        operation.setBetId("" + order);
        operation.setCustomerId("3");
        operation.setInputXML("<input><data>" + order + "</data></input>");
        obStorage.storeOperation(operation);

        action.setState(MonitoredAction.State.PROGRESS);
        storage.saveActionState(pk, action);

        operation.setOutputXML("<output><data>" + order + "</data></output>");
        operation.setReceipt("O/1/0000" + order);
        action.setDuration(1000);
        moment.setTime(moment.getTime() + 1000);
        action.setFinish(moment);
        action.setState(order % 3 == 0 ? MonitoredAction.State.SUCCESS : MonitoredAction.State.FAIL);
        storage.saveActionState(pk, action);
        obStorage.storeOperation(operation);
    }

    private HealthItemPK createHealthItemPK(int index) {
        StructureModuleEntity entity = new StructureModuleEntity();
        entity.setSystemId("sys" + index);
        entity.setApplicationId("app" + index);
        entity.setVersionId("ver" + index);
        entity.setDescription("descr" + index);
        return storage.getModulePK(entity);

    }
}
