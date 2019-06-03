/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.storage.simpletext.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.service.action.ModuleActionsRepository;
import oleg.sopilnyak.service.dto.ModuleDto;
import org.springframework.util.StringUtils;

import java.io.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Realization for trivial modules-action operations
 */
@Slf4j
public class SimpleTextModuleActionsRepositoryImpl implements ModuleActionsRepository {

    static final String ACTIONS_DATA_FILE = "actions.data";
    static final String DELIMITER = "^";
    private static final ReadWriteLock DATA_LOCK = new ReentrantReadWriteLock();

    /**
     * To store action to repository
     *
     * @param action item to store
     */
    @Override
    public void persist(ModuleAction action) {
        log.debug("Persisting {}", action);
        DATA_LOCK.writeLock().lock();
        try (final PrintWriter dataOutput = new PrintWriter(new FileWriter(ACTIONS_DATA_FILE, true))) {
            final String dataLine = transform(action);
            log.debug("action '{}'", dataLine);
            dataOutput.println(dataLine);
        } catch (IOException e) {
            log.error("Cannot store data to file '{}'", ACTIONS_DATA_FILE, e);
        } finally {
            DATA_LOCK.writeLock().unlock();
        }
    }

    /**
     * To get ModuleAction by Id
     *
     * @param actionId id of action
     * @return action instance or null if not exists
     */
    @Override
    public ModuleAction getById(String actionId) {
        log.debug("Getting action by id: {}", actionId);
        DATA_LOCK.readLock().lock();
        try {
            final ModuleActionAdapter action = findAndRestore(actionId);
            return Objects.isNull(action) ? null : postProcess(action);
        } catch (Exception e) {
            log.error("Cannot process request", e);
            return null;
        } finally {
            DATA_LOCK.readLock().unlock();
        }
    }

    // private methods
    static ModuleActionAdapter findAndRestore(String actionId) throws Exception {
        final String prefix = actionId + DELIMITER;
        try (final BufferedReader reader = new BufferedReader(new FileReader(ACTIONS_DATA_FILE))) {
            String actionData;
            while (!StringUtils.isEmpty(actionData = reader.readLine())) {
                if (actionData.startsWith(prefix)) {
                    return restore(actionData);
                }
            }
        }
        return null;
    }

    static String transform(ModuleAction action) {
        final StringBuilder builder = new StringBuilder(action.getId());
        builder
                .append(DELIMITER).append(action.getName())
                .append(DELIMITER).append(action.getModule().primaryKey())
                .append(DELIMITER).append(action.getParent() == null ? "null" : action.getParent().getId())
                .append(DELIMITER).append(action.getHostName())
                .append(DELIMITER).append(action.getState().name())
                .append(DELIMITER).append(action.getStarted())
                .append(DELIMITER).append(action.getDuration())
                .append(DELIMITER).append(action.getDescription());
        return builder.toString();
    }

    ModuleAction postProcess(ModuleActionAdapter action) throws Exception {
        assert action != null : "Cannot post-process empty action.";

        if (Objects.isNull(action.getParent())) {
            return action;
        }
        final ModuleActionAdapter parent = loadParent(action.getParent().getId(), null);
        action.setParent(parent);
        return action;
    }

    static ModuleActionAdapter restore(String actionData) throws Exception {
        assert !StringUtils.isEmpty(actionData) : "actionData line cannot be empty";

        final StringTokenizer st = new StringTokenizer(actionData, DELIMITER);
        if (st.countTokens() != 9) {
            throw new InvalidObjectException("Wrong quantity of fields " + st.countTokens());
        }
        return ModuleActionAdapter.builder()
                .id(st.nextToken())
                .name(st.nextToken())
                .module(new ModuleDto(st.nextToken()))
                .parent(toAction(st.nextToken()))
                .hostName(st.nextToken())
                .state(toState(st.nextToken()))
                .started(toInstance(st.nextToken()))
                .duration(toLong(st.nextToken()))
                .description(st.nextToken())
                .build();
    }

    private static Long toLong(String duration) {
        return "null".equalsIgnoreCase(duration) ? null : Long.parseLong(duration);
    }

    private static Instant toInstance(String started) {
        return "null".equalsIgnoreCase(started) ? null : Instant.parse(started);
    }

    private static ModuleAction.State toState(String state) {
        return "null".equalsIgnoreCase(state) ? null : ModuleAction.State.valueOf(state);
    }

    static ModuleAction toAction(String actionId) {
        if ("null".equalsIgnoreCase(actionId)) {
            return null;
        }
        return ModuleActionAdapter.builder().id(actionId).build();
    }

    ModuleActionAdapter loadParent(String actionId, Map<String, ModuleActionAdapter> parents) throws Exception {
        if (Objects.isNull(parents)) {
            parents = new HashMap<>();
        }
        if (parents.containsKey(actionId)) {
            return parents.get(actionId);
        }
        final ModuleActionAdapter action = findAndRestore(actionId);
        if (Objects.isNull(action) || Objects.isNull(action.getParent())) {
            return action;
        }
        actionId = action.getParent().getId();
        log.debug("Recursive call for {}", actionId);
        final ModuleActionAdapter parent = loadParent(actionId, parents);
        action.setParent(parent);
        parents.put(actionId, action);
        return action;
    }

}
