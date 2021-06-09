package com.example.poc2104301453.pinpadservice;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.GestaoBibliotecaPinpad;
import br.com.verifone.bibliotecapinpad.RegistroBibliotecaPinpad;
import br.com.verifone.ppcompX990.PPCompX990;

import static com.example.poc2104301453.pinpadlibrary.ABECS.KEY_ENUM.*;
import static com.example.poc2104301453.pinpadlibrary.ABECS.RSP_STAT.*;
import static com.example.poc2104301453.pinpadlibrary.ABECS.VAL_ENUM.*;

/**
 *
 */
public class ABECS extends IABECS.Stub {
    private static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    private static AcessoFuncoesPinpad sPinpad = null;

    private static final List<Pair<String, Runnable>> sCommandList = new ArrayList<>(0);

    private static final Semaphore sSemaphore = new Semaphore(1, true);

    private static final ABECS sABECS = new ABECS();

    /**
     * Runnable interface.
     */
    public static interface Runnable {
        /**
         * Runs a known command.
         *
         * @return {@link Bundle}
         * @throws Exception self-describing
         */
        Bundle run(Bundle input)
                throws Exception;
    }

    /**
     * Constructor.
     */
    private ABECS() {
        Log.d(TAG_LOGCAT, "ABECS");

        sCommandList.add(new Pair<>(OPN.getValue(), com.example.poc2104301453.pinpadservice.commands.OPN::opn));
        sCommandList.add(new Pair<>(GIN.getValue(), com.example.poc2104301453.pinpadservice.commands.GIN::gin));
        sCommandList.add(new Pair<>(CLO.getValue(), com.example.poc2104301453.pinpadservice.commands.CLO::clo));

        try {
            RegistroBibliotecaPinpad.informaClassesBiblioteca(PPCompX990.getInstance());

            sPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoFuncoesPinpad();
        } catch (Exception exception) {
            Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
        }
    }

    private Bundle run(Bundle input) {
        Bundle output = new Bundle();

        sSemaphore.acquireUninterruptibly();

        try {
            input.get(null);

            Log.d(TAG_LOGCAT, "run::input [" + input.toString() + "]");

            String request = REQUEST.getValue();

            String command = input.getString(request);

            if (command == null) {
                throw new Exception("Mandatory key \"" + request + "\" not found");
            }

            for (Pair<String, Runnable> cmd : sCommandList) {
                if (command.equals(cmd.first)) {
                    return cmd.second.run(input);
                }
            }

            StringBuilder log = new StringBuilder("Be sure to run one of the known commands:\r\n");

            for (Pair<String, Runnable> cmd : sCommandList) {
                log.append("\t ").append(cmd.first).append(";\r\n");
            }

            Log.e(TAG_LOGCAT, log.toString());

            throw new Exception("Unknown input: { " + request + ": \"" + command + "\" }");
        } catch (Exception exception) {
            output.putInt(STATUS.getValue(), ST_INTERR.getValue());
            output.putSerializable(EXCEPTION.getValue(), exception);
        } finally {
            sSemaphore.release();
        }

        return output;
    }

    /**
     * @return {@link ABECS}
     */
    public static ABECS getInstance() {
        Log.d(TAG_LOGCAT, "getInstance");

        return sABECS;
    }

    /**
     * @return {@link AcessoFuncoesPinpad}
     */
    public AcessoFuncoesPinpad getPinpad() {
        return sPinpad;
    }

    /**
     *
     * @param input
     * @return
     */
    @Override
    public Bundle run(String caller, IServiceCallback callback, Bundle input) {
        Log.d(TAG_LOGCAT, "run");

        try {
            sPinpad.abort();
        } catch (Exception exception) {
            Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
        }

        return run(input);
    }
}
