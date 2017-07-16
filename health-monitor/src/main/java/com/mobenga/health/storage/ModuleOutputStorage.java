package com.mobenga.health.storage;

import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.out.ModuleOutputMessage;
import com.mobenga.health.model.business.out.SelectOutputCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The service to manage module's output stuff
 */
public interface ModuleOutputStorage {
    /**
     * To create module's output item for particular module
     * @param module owner of output
     * @param type the type of output
     * @return a new instance of output
     */
    ModuleOutputMessage createModuleOutput(ModuleKey module, String type);

    /**
     * To save changed module's output
     *
     * @param message output to save
     */
    void saveModuleOutput(ModuleOutputMessage message);

    /**
     * To select entities suit the criteria
     *
     * @param criteria criteria to select
     * @param offset required part of selection
     * @return required
     */
    Page<ModuleOutputMessage> select(SelectOutputCriteria criteria, Pageable offset);

    /**
     * To delete unnecessary entities
     * @param criteria criteria of selection
     * @return the quantity of deleted entities
     */
    int delete(SelectOutputCriteria criteria);
}
