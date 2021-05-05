package com.example.service;

/**
 * ABECS Service Interface.<br>
 * Wraps the obsolete set of commands from Protocolo de Comunicação e Funcionamento v2.12 under a
 * simplified key-value interface, supporting both sync. and async. operation.
 */
interface IABECS {
    /**
     * Parses and processes a {@link Bundle} {@code input}.<br>
     * <br>
     * Mandatory key(s):<br>
     * <ul>
     *     <li>{@code request}</li>
     * </ul>
     * Conditional and optional keys: every request may have its own mandatory, conditional and
     * optional keys (e.g. "OPN" may be requested alongside the "callback" key).<br>
     * See the specification v2.12 from ABECS for further details.
     *
     * @param sync indicates the type of operation. @{code false} is recommended (furthermore
     * mandatory if the application intends to call {@link IABECS#run(boolean, Bundle)} from its
     * main thread).
     * @param input {@link Bundle}
     * @return {@link Bundle} or {@code null} when {@code sync} is {@code false}.
     */
    Bundle run(in boolean sync, in Bundle input);
}
