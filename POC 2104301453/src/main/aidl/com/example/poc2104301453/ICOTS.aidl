package com.example.poc2104301453;

import com.example.poc2104301453.IServiceCallback;

/**
 * POC on handling isolated features using the service approach.
 */
interface ICOTS {
    /**
     * Echoes the {@link Bundle} {@code input} as an JSON output through {@code callback}.<br>
     * <br>
     * Only one bundle key is currently known and up for parsing: "COTS" (arbitrary {@link String})<br>
     * Possible callback output keys are:
     * <ul>
     *     <li>For failure: "exception" and "status"</li>
     *     <li>For a status update: "status"</li>
     *     <li>For success: "echo" and "status"</li>
     * </ul>
     * @see IServiceCallback
     *
     * @type asynchronous
     * @param input {@link Bundle}
     * @param callback {@link IServiceCallback} (Stub)
     */
    void parse(in Bundle input, in IServiceCallback callback);
}
