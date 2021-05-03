package com.example.poc2104301453;

import com.example.poc2104301453.IServiceCallback;

/**
 *
 */
interface IABECS {
    /**
     *
     */
    Bundle register(in boolean sync, in IServiceCallback callback);

    /**
     *
     */
    Bundle run(in boolean sync, in Bundle input);
}
