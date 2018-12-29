package oleg.sopilnyak.service;

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
}
