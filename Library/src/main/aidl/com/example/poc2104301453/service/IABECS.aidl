package com.example.poc2104301453.service;

import com.example.poc2104301453.service.IServiceCallback;

/**
 * ABECS Service Interface.<br>
 * Wraps the obsolete set of commands from Protocolo de Comunicação e Funcionamento v2.12 under a
 * simplified key-value interface, supporting both sync. and async. operation.
 */
interface IABECS {
    /**
     * @see com.example.library.ABECS#run(Bundle)
     */
    Bundle run(in IServiceCallback callback, in Bundle input);
}
