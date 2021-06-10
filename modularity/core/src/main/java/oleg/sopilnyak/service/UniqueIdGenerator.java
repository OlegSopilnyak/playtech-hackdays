package oleg.sopilnyak.service;

import java.util.UUID;

/**
 * Service to generate unique Ids
 */
public interface UniqueIdGenerator {
    /**
     * To generate unique id
     *
     * @return unique id
     */
    String generate();

    /**
     * To generated UUID
     *
     * @return the instance
     */
    UUID generateId();
}
