package com.example.library;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.service.IABECS;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ABECS {
    private static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    public static final String KEY_REQUEST = "request";

    /*
     * 3.2 Comandos de controle
     */

    public static final String VALUE_REQUEST_OPN = "OPN";

    public static final String VALUE_REQUEST_GIN = "GIN";

    // public static final String VALUE_REQUEST_GIX = "GIX";

    // public static final String VALUE_REQUEST_DWK = "DWK";

    public static final String VALUE_REQUEST_CLO = "CLO";

    // public static final String VALUE_REQUEST_CLX = "CLX";

    /*
     * 3.3 Comandos básicos
     */

    public static final String VALUE_REQUEST_CKE = "CKE";

    // public static final String VALUE_REQUEST_CHP = "CHP";

    // public static final String VALUE_REQUEST_DEX = "DEX";

    // public static final String VALUE_REQUEST_DSP = "DSP";

    // public static final String VALUE_REQUEST_EBX = "EBX";

    public static final String VALUE_REQUEST_ENB = "ENB";

    // public static final String VALUE_REQUEST_GCD = "GCD";

    public static final String VALUE_REQUEST_GDU = "GDU";

    // public static final String VALUE_REQUEST_GKY = "GKY";

    public static final String VALUE_REQUEST_GPN = "GPN";

    // public static final String VALUE_REQUEST_GTK = "GTK";

    public static final String VALUE_REQUEST_MNU = "MNU";

    // public static final String VALUE_REQUEST_RMC = "RMC";

    /*
     * 3.4 Comandos multimídia
     */

    // public static final String VALUE_REQUEST_MLI = "MLI";

    // public static final String VALUE_REQUEST_MLR = "MLR";

    // public static final String VALUE_REQUEST_MLE = "MLE";

    // public static final String VALUE_REQUEST_LMF = "LMF";

    // public static final String VALUE_REQUEST_DMF = "DMF";

    // public static final String VALUE_REQUEST_DSI = "DSI";

    /*
     * 3.5 Comandos para manutenção de Tabelas EMV
     */

    public static final String VALUE_REQUEST_GTS = "GTS";

    public static final String VALUE_REQUEST_TLI = "TLI";

    public static final String VALUE_REQUEST_TLR = "TLR";

    public static final String VALUE_REQUEST_TLE = "TLE";

    /*
     * 3.6 Comandos de processamento de cartão (obsoletos)
     */

    public static final String VALUE_REQUEST_GCR = "GCR";

    public static final String VALUE_REQUEST_CNG = "CNG";

    public static final String VALUE_REQUEST_GOC = "GOC";

    public static final String VALUE_REQUEST_FNC = "FNC";

    /*
     * 3.7 Comandos ABECS de processamento de cartão
     */

    // public static final String VALUE_REQUEST_GCX = "GCX";

    // public static final String VALUE_REQUEST_GED = "GED";

    // public static final String VALUE_REQUEST_GOX = "GOX";

    // public static final String VALUE_REQUEST_FCX = "FCX";

    /*
     * 3.8 Comandos genéricos
     */

    // public static final String VALUE_REQUEST_GEN = "GEN";

    /**
     * Callback interface for async. methods.
     */
    public static class Callback {
        public Process process;

        /*
         * Note: mandatory for async. operation.
         */
        public Status status;

        public Callback(Process process, Status status) {
            if (process == null && status == null) {
                throw new NullPointerException("Both arguments cannot be null");
            }

            this.process = process;

            this.status = status;
        }

        public static interface Process {
            /* TODO: see Verifone's OPN command */
        }

        public static interface Status {
            /**
             * Status callback.<br>
             * As the name states, its called upon a processing failure.<br>
             * {@code output} will return the "status" key. The "exception" key may also be present,
             * providing further details on the failure.
             *
             * @param output {@link Bundle}
             */
            void onFailure(Bundle output);

            /**
             * Status callback.<br>
             * As the name states, its called when the processing of a request finishes
             * successfully.<br>
             * {@code output} will return the "status" key. Other keys may be present, conditionally to
             * the request that was just processed.<br>
             * See the specification v2.12 from ABECS for further details.
             *
             * @param output {@link Bundle}
             */
            void onSuccess(Bundle output);
        }
    }

    /**
     * Parses and processes a {@link Bundle} {@code input}.<br>
     * <br>
     * Mandatory key(s):<br>
     * <ul>
     *     <li>{@code request}</li>
     * </ul>
     * Conditional and optional keys: every request may have its own mandatory, conditional and/or
     * optional keys (e.g. "OPN" may be requested along with the "callback" key).<br>
     * See the specification v2.12 from ABECS for further details.
     *
     * @param context application context
     * @param input {@link Bundle}
     * @return {@link Bundle} (or {@code null} when {@code sync} is {@code false}).
     */
    public static Bundle run(@NotNull Context context, @NotNull Bundle input) {
        final Bundle[] output = { null };
        final boolean sync = input.getBoolean("operation_mode");

        Lock lock = new ReentrantLock(true);

        if (sync) {
            lock.lock();
        }

        Intent intent = new Intent("com.example.poc2104301453");

        intent.setClassName("com.example.poc2104301453", "com.example.poc2104301453.PinpadService");

        boolean serviceBind = context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(ABECS.TAG_LOGCAT, "onServiceConnected::name [" + name + "], service [" + service + "]");

                try {
                    Bundle input = new Bundle();

                    input.putString("request", "OPN");

                    output[0] = IABECS.Stub.asInterface(service).run(input);
                } catch (Exception exception) {
                    Log.d(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
                } finally {
                    context.unbindService(this);

                    if (sync) {
                        lock.unlock();
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG_LOGCAT, "onServiceDisconnected::name [" + name + "]");

                if (sync) {
                    lock.unlock();
                }
            }
        }, Context.BIND_AUTO_CREATE);

        Log.d(TAG_LOGCAT, "onCreate::serviceBind [" + serviceBind + "]");

        if (sync) {
            lock.lock();
        }

        return output[0];
    }
}
