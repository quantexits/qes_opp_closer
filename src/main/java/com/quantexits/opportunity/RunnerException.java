package com.quantexits.opportunity;

/**
 * Exception in runner.
 */
public class RunnerException extends Exception {
    /**
     * Constructs a RunnerException with no detail message.
     */
    public RunnerException() {
        super();
    }

    /**
     * Constructs a RunnerException with the specified detail message.
     *
     * @param message The detail message.
     */
    public RunnerException(final String message) {
        super(message);
    }

    /**
     * Constructs a RunnerException with the specified cause.
     *
     * @param cause The cause.
     */
    public RunnerException(final Exception cause) {
        super(cause);
    }

    /**
     * Constructs a RunnerException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause The cause.
     */
    public RunnerException(final String message, final Exception cause) {
        super(message, cause);
    }

}
