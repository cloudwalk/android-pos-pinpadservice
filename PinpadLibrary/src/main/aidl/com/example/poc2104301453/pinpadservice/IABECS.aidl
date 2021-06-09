package com.example.poc2104301453.pinpadservice;

import com.example.poc2104301453.pinpadservice.IServiceCallback;

/**
 * ABECS Service Interface.<br>
 * Wraps the obsolete set of commands from specification v2.12 under a simplified key-value
 * interface, supporting both sync. and async. operation.
 */
interface IABECS {
    /**
     * @see com.example.poc2104301453.library.ABECS#run(Bundle)
     */
    Bundle run(in String caller, in IServiceCallback callback, in Bundle input);
}
