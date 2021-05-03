package com.example.poc2104301453;

import com.example.poc2104301453.IServiceCallback;
import com.example.poc2104301453.IServiceMap;

/**
 * ABECS Service Interface by CloudWalk, Inc.<br>
 * Wraps the obsolete set of commands from Protocolo de Comunicação e Funcionamento v2.12 under a
 * simplified key-value interface, supporting both sync. and async. operation.
 */
interface IABECS {
    /**
     * Registers the service callbacks.<br>
     * See {@link IServiceCallback} for further details.
     *
     * @param sync indicates the type of operation. @{code false} is recommended (furthermore
     * mandatory if the application intends to call
     * {@link IABECS#register(boolean, IServiceCallback)} from its main thread).
     * @return {@link Bundle} or {@code null} when {@code sync} is {@code false}.
     */
    Bundle register(in boolean sync, in IServiceCallback callback);

    /**
     * Parses and processes a {@link Bundle} {@code input}.<br>
     * <br>
     * Mandatory key(s):<br>
     * <ul>
     *     <li>{@code request}</li>
     * </ul>
     * Conditional and optional keys: every request may have its own mandatory, conditional and
     * optional keys. See the specification v2.12 from ABECS for further details.
     *
     * @param sync indicates the type of operation. @{code false} is recommended (furthermore
     * mandatory if the application intends to call {@link IABECS#run(boolean, Bundle)} from its
     * main thread).
     * @param input {@link Bundle}
     * @return {@link Bundle} or {@code null} when {@code sync} is {@code false}.
     */
    Bundle run(in boolean sync, in Bundle input);
}
