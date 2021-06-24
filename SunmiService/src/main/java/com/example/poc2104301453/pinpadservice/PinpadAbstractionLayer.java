package com.example.poc2104301453.pinpadservice;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.example.poc2104301453.pinpadlibrary.ABECS;
import com.example.poc2104301453.pinpadservice.commands.CEX;
import com.example.poc2104301453.pinpadservice.commands.CKE;
import com.example.poc2104301453.pinpadservice.commands.CLO;
import com.example.poc2104301453.pinpadservice.commands.GIN;
import com.example.poc2104301453.pinpadservice.commands.GIX;
import com.example.poc2104301453.pinpadservice.commands.OPN;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.setis.sunmi.bibliotecapinpad.GestaoBibliotecaPinpad;

/**
 *
 */
public class PinpadAbstractionLayer extends IABECS.Stub {
    private static final String TAG_LOGCAT = PinpadAbstractionLayer.class.getSimpleName();

    private static final List<Pair<String, Runnable>> sCommandList =
            new ArrayList<>(0);

    private static final Semaphore[] sSemaphore = {
                new Semaphore(1, true),
                new Semaphore(1, true)
            };

    private static final PinpadAbstractionLayer sPinpadAbstractionLayer =
            new PinpadAbstractionLayer();

    private static AcessoFuncoesPinpad sPinpad = null;

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
    private PinpadAbstractionLayer() {
        Log.d(TAG_LOGCAT, "PinpadAbstractionLayer");

        sCommandList.add(new Pair<>(ABECS.OPN, OPN::opn));
        sCommandList.add(new Pair<>(ABECS.GIN, GIN::gin));
        sCommandList.add(new Pair<>(ABECS.GIX, GIX::gix));
        sCommandList.add(new Pair<>(ABECS.CLO, CLO::clo));

        sCommandList.add(new Pair<>(ABECS.CEX, CEX::cex));
        sCommandList.add(new Pair<>(ABECS.CKE, CKE::cke));
    }

    /**
     * @return {@link PinpadAbstractionLayer}
     */
    public static PinpadAbstractionLayer getInstance() {
        Log.d(TAG_LOGCAT, "getInstance");

        return sPinpadAbstractionLayer;
    }

    /**
     * @return {@link AcessoFuncoesPinpad}
     */
    public AcessoFuncoesPinpad getPinpad() {
        sSemaphore[1].acquireUninterruptibly();

        if (sPinpad == null) {
            try {
                sPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoFuncoesPinpad();
            } catch (Exception exception) {
                Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
            }
        }

        sSemaphore[1].release();

        return sPinpad;
    }

    /**
     *
     * @param input
     * @return
     */
    @Override
    public Bundle request(Bundle input) {
        Log.d(TAG_LOGCAT, "request");

        try {
            getPinpad().abort(); /* "É importante que o SPE sempre inicie o
                                  * fluxo de comunicação com o pinpad enviando
                                  * um <CAN>, de forma a abortar qualquer
                                  * comando blocante que eventualmente esteja
                                  * em processamento." - ABECS v2.12; 2.2.2.3 */
        } catch (Exception exception) {
            Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
        }

        Bundle output = new Bundle();

        sSemaphore[0].acquireUninterruptibly();

        try {
            input.get(null);

            Log.d(TAG_LOGCAT, "run::input [" + input.toString() + "]");

            String request = input.getString(ABECS.CMD_ID);

            if (request == null) {
                throw new Exception("Mandatory key \"CMD_ID\" not found");
            }

            for (Pair<String, Runnable> command : sCommandList) {
                if (request.equals(command.first)) {
                    return command.second.run(input);
                }
            }

            StringBuilder log = new StringBuilder("Be sure to run one of the known commands:\r\n");

            for (Pair<String, Runnable> cmd : sCommandList) {
                log.append("\t ").append(cmd.first).append(";\r\n");
            }

            Log.e(TAG_LOGCAT, log.toString());

            throw new Exception("Unknown input: { CMD_ID: \"" + request + "\" }");
        } catch (Exception exception) {
            output.putInt(ABECS.RSP_STAT, ABECS.STAT.ST_INTERR.ordinal());
            output.putSerializable("EXCEPTION", exception);
        } finally {
            sSemaphore[0].release();
        }

        return output;
    }
}
