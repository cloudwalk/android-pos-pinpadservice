package com.example.poc2104301453.pinpadservice;

import com.example.poc2104301453.pinpadservice.IServiceCallback;

/**
 * ABECS Service Interface.<br>
 * Wraps the obsolete set of commands from specification v2.12 under a simplified key-value
 * interface.
 */
interface IABECS {
    /**
     * @see com.example.poc2104301453.pinpadlibrary.ABECS#run(Bundle)
     */
    Bundle run(in String caller, in IServiceCallback callback, in Bundle input);
}
