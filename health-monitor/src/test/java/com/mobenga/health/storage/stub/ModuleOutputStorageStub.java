package com.mobenga.health.storage.stub;

import com.mobenga.health.model.LogMessage;
import com.mobenga.health.model.ModuleOutput;
import com.mobenga.health.storage.ModuleOutputStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import com.mobenga.health.model.ModulePK;

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
    public ModuleOutput createModuleOutput(ModulePK module, String type) {
        if (LogMessage.OUTPUT_TYPE.equals(type))  return mock(LogMessage.class);
        return null;
    }

    /**
     * To save changed module's output
     *
     * @param message output to save
     */
    @Override
    public void saveModuleOutput(ModuleOutput message) {
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
    public Page<ModuleOutput> select(ModuleOutput.Criteria criteria, Pageable offset) {
        return new PageImpl<ModuleOutput>(Collections.EMPTY_LIST);
    }

    /**
     * To delete unnecessary entities
     *
     * @param criteria criteria of selection
     * @return the quantity of deleted entities
     */
    @Override
    public int delete(ModuleOutput.Criteria criteria) {
        return 0;
    }
}
