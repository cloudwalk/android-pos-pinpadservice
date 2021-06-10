package com.example.poc2104301453.pinpadlibrary.exceptions;

public class ServiceInstanceException extends Exception {

    /**
     * Constructor.
     */
    public ServiceInstanceException() {
        super("Unable to get instance: service may be disconnected, unbound or missing.");
    }

    /**
     * Constructor.
     */
    public ServiceInstanceException(String message) {
        super(message);
    }
}
