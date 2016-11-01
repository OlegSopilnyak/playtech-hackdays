package com.mobenga.health.model.transport;

import com.mobenga.health.model.ModuleOutput;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for ModuleOutput criteria basic
 */
public class ModuleOutputCriteriaBaseTest {

    private ModuleOutputCriteriaBase criteriaBase = new ModuleOutputCriteriaBase();
    private ModuleOutput message = mock(ModuleOutput.class);

    @Test
    public void isNotSuitable() throws Exception {
        assertFalse(criteriaBase.isSuitable(null));
    }

    @Test
    public void isSuitableByOutputId() throws Exception {
        when(message.getId()).thenReturn("AAA");
        criteriaBase.setOutputId("AAA");
        assertFalse(!criteriaBase.isSuitable(message));
    }

    @Test
    public void isSuitableByType() throws Exception {
        when(message.getId()).thenReturn("AAA");
        when(message.getMessageType()).thenReturn("BBB");
        criteriaBase.setType("BBB");
        assertFalse(!criteriaBase.isSuitable(message));
    }

    @Test
    public void isSuitableByModulePK() throws Exception {
        when(message.getId()).thenReturn("AAA");
        when(message.getMessageType()).thenReturn("BBB");
        when(message.getModulePK()).thenReturn("CCC");
        criteriaBase.setModulePK("CCC");
        assertFalse(!criteriaBase.isSuitable(message));
    }

    @Test
    public void isSuitableByActionId() throws Exception {
        when(message.getId()).thenReturn("AAA");
        when(message.getMessageType()).thenReturn("BBB");
        when(message.getModulePK()).thenReturn("CCC");
        when(message.getActionId()).thenReturn("DDD");
        criteriaBase.setActionIds(new String[]{"CCC","DDD"});
        assertFalse(!criteriaBase.isSuitable(message));
    }

    @Test
    public void isSuitableByMoreThen() throws Exception {
        when(message.getId()).thenReturn("AAA");
        when(message.getMessageType()).thenReturn("BBB");
        when(message.getModulePK()).thenReturn("CCC");
        when(message.getActionId()).thenReturn("DDD");
        when(message.getWhenOccurred()).thenReturn(new Date());

        criteriaBase.setMoreThan(new Date(System.currentTimeMillis() + 1000));
        assertFalse(!criteriaBase.isSuitable(message));
    }

    @Test
    public void isSuitableByLessThen() throws Exception {
        when(message.getId()).thenReturn("AAA");
        when(message.getMessageType()).thenReturn("BBB");
        when(message.getModulePK()).thenReturn("CCC");
        when(message.getActionId()).thenReturn("DDD");
        when(message.getWhenOccurred()).thenReturn(new Date());

        criteriaBase.setLessThan(new Date(System.currentTimeMillis() - 1000));
        assertFalse(!criteriaBase.isSuitable(message));
    }

    @Test
    public void isSuitable() throws Exception {
        when(message.getId()).thenReturn("AAA");
        when(message.getMessageType()).thenReturn("BBB");
        when(message.getModulePK()).thenReturn("CCC");
        when(message.getActionId()).thenReturn("DDD");
        when(message.getWhenOccurred()).thenReturn(new Date());

        criteriaBase.setOutputId("AAA");
        criteriaBase.setType("BBB");
        criteriaBase.setModulePK("CCC");
        criteriaBase.setActionIds(new String[]{"CCC","DDD"});
        criteriaBase.setMoreThan(new Date(System.currentTimeMillis() + 1000));
        criteriaBase.setLessThan(new Date(System.currentTimeMillis() - 1000));
        assertFalse(!criteriaBase.isSuitable(message));
    }
}