package com.mobenga.health.storage.stub;

import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.out.ModuleOutputMessage;
import com.mobenga.health.model.business.out.SelectOutputCriteria;
import com.mobenga.health.model.business.out.log.ModuleLoggerMessage;
import com.mobenga.health.storage.ModuleOutputStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.mockito.Mockito.mock;

/**
 * The stub for ModuleOutputStorage
 */
public class ModuleOutputStorageStub implements ModuleOutputStorage {
    /**
     * To create module's output item for particular module
     *
     * @param module owner of output
     * @param type   the type of output
     * @return a new instance of output
     */
    @Override
    public ModuleOutputMessage createModuleOutput(ModuleKey module, String type) {
        if (ModuleLoggerMessage.LOG_OUTPUT_TYPE.equals(type))  return mock(ModuleLoggerMessage.class);
        return null;
    }

    /**
     * To save changed module's output
     *
     * @param message output to save
     */
    @Override
    public void saveModuleOutput(ModuleOutputMessage message) {
        //
    }

    /**
     * To select entities suit the criteria
     *
     * @param criteria criteria to select
     * @param offset   required part of selection
     * @return required
     */
    @Override
    public Page<ModuleOutputMessage> select(SelectOutputCriteria criteria, Pageable offset) {
        return new PageImpl<ModuleOutputMessage>(Collections.EMPTY_LIST);
    }

    /**
     * To delete unnecessary entities
     *
     * @param criteria criteria of selection
     * @return the quantity of deleted entities
     */
    @Override
    public int delete(SelectOutputCriteria criteria) {
        return 0;
    }
}
