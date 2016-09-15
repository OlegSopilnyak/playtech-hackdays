package com.mobenga.health.model.persistence;

/**
 * Interface to keep state of entity in a consistent state
 */
public interface ValidatingEntity {
    /**
     * To validate internal state of entity if state invalid throws EntityInvalidState
     *
     */
    void validate();

    /**
     * Class runtime-exception for validating
     */
    class EntityInvalidState extends RuntimeException {
        private final String attributeName;
        /**
         * Constructs a new runtime exception with {@code null} as its
         * detail message.  The cause is not initialized, and may subsequently be
         * initialized by a call to {@link #initCause}.
         */
        public EntityInvalidState(String attributeName) {
            this.attributeName = attributeName;
        }

        /**
         * Constructs a new runtime exception with the specified detail message.
         * The cause is not initialized, and may subsequently be initialized by a
         * call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public EntityInvalidState(String attributeName, String message) {
            super(message);
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
}
