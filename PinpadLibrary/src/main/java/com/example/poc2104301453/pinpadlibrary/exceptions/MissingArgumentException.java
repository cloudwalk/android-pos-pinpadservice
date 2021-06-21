package com.example.poc2104301453.pinpadlibrary.exceptions;

public class MissingArgumentException extends Exception {
    /**
     * Constructor.
     */
    public MissingArgumentException() {
        super("Check mandatory command arguments");
    }
}
