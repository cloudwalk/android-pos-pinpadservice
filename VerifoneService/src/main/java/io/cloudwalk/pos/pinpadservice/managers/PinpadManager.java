package io.cloudwalk.pos.pinpadservice.managers;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.GestaoBibliotecaPinpad;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadservice.commands.CEX;
import io.cloudwalk.pos.pinpadservice.commands.CKE;
import io.cloudwalk.pos.pinpadservice.commands.CLO;
import io.cloudwalk.pos.pinpadservice.commands.GCR;
import io.cloudwalk.pos.pinpadservice.commands.GCX;
import io.cloudwalk.pos.pinpadservice.commands.GIN;
import io.cloudwalk.pos.pinpadservice.commands.GIX;
import io.cloudwalk.pos.pinpadservice.commands.GOX;
import io.cloudwalk.pos.pinpadservice.commands.GTS;
import io.cloudwalk.pos.pinpadservice.commands.OPN;
import io.cloudwalk.pos.pinpadservice.commands.TLE;
import io.cloudwalk.pos.pinpadservice.commands.TLI;
import io.cloudwalk.pos.pinpadservice.commands.TLR;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String TAG_LOGCAT = PinpadManager.class.getSimpleName();

    private static final
            IServiceCallback sLocalCallback = new IServiceCallback.Stub() {
        @Override
        public int onSelectionRequired(Bundle output)
                throws RemoteException {
            Log.d(TAG_LOGCAT, "onSelectionRequired");

            if (!getInstance().getCallbackStatus()) {
                return 0;
            }

            IServiceCallback callback;

            sSemaphore[2].acquireUninterruptibly();

            callback = sExternalCallback;

            sSemaphore[2].release();

            return (callback != null) ? callback.onSelectionRequired(output) : 0;
        }

        @Override
        public void onNotificationThrow(Bundle output, int type)
                throws RemoteException {
            Log.d(TAG_LOGCAT, "onNotificationThrow");

            if (!getInstance().getCallbackStatus()) {
                return;
            }

            IServiceCallback callback;

            sSemaphore[2].acquireUninterruptibly();

            callback = sExternalCallback;

            sSemaphore[2].release();

            if (callback != null){
                callback.onNotificationThrow(output, type);
            }
        }
    };

    private static final
            List<Pair<String, Runnable>> sCommandList = new ArrayList<>(0);

    private static final
            PinpadManager sPinpadManager = new PinpadManager();

    private static final
            Semaphore[] sSemaphore = {
                    new Semaphore(1, true), /* Public (external) */
                    new Semaphore(1, true), /* Public (internal) */
                    new Semaphore(1, true)  /* Public (internal) */
            };

    private static AcessoFuncoesPinpad sAcessoFuncoesPinpad = null;

    private static IServiceCallback sExternalCallback = null;

    private static boolean sCallbackStatus = false;

    /**
     * Runnable interface.
     */
    public static interface Runnable {
        /**
         * @return {@link Bundle}
         * @throws Exception self-describing
         */
        Bundle run(Bundle input)
                throws Exception;
    }

    private PinpadManager() {
        Log.d(TAG_LOGCAT, "PinpadManager");

        sCommandList.add(new Pair<>(ABECS.OPN, OPN::opn));
        sCommandList.add(new Pair<>(ABECS.GIN, GIN::gin));
        sCommandList.add(new Pair<>(ABECS.GIX, GIX::gix));
        sCommandList.add(new Pair<>(ABECS.CLO, CLO::clo));

        sCommandList.add(new Pair<>(ABECS.CEX, CEX::cex));
        sCommandList.add(new Pair<>(ABECS.CKE, CKE::cke));

        sCommandList.add(new Pair<>(ABECS.GTS, GTS::gts));
        sCommandList.add(new Pair<>(ABECS.TLI, TLI::tli));
        sCommandList.add(new Pair<>(ABECS.TLR, TLR::tlr));
        sCommandList.add(new Pair<>(ABECS.TLE, TLE::tle));

        sCommandList.add(new Pair<>(ABECS.GCR, GCR::gcr));
        sCommandList.add(new Pair<>(ABECS.GCX, GCX::gcx));
        sCommandList.add(new Pair<>(ABECS.GOX, GOX::gox));
    }

    private void setCallback(@NotNull IServiceCallback callback) {
        Log.d(TAG_LOGCAT, "setCallback");

        sSemaphore[2].acquireUninterruptibly();

        sExternalCallback = callback;

        sSemaphore[2].release();
    }

    public static PinpadManager getInstance() {
        Log.d(TAG_LOGCAT, "getInstance");

        return sPinpadManager;
    }

    public AcessoFuncoesPinpad getPinpad() {
        Log.d(TAG_LOGCAT, "getPinpad");

        sSemaphore[1].acquireUninterruptibly();

        if (sAcessoFuncoesPinpad == null) {
            try {
                sAcessoFuncoesPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoFuncoesPinpad();
            } catch (Exception exception) {
                Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
            }
        }

        sSemaphore[1].release();

        return sAcessoFuncoesPinpad;
    }

    public IServiceCallback getCallback() {
        Log.d(TAG_LOGCAT, "getCallback");

        return sLocalCallback;
    }

    @Override
    public Bundle request(Bundle input) {
        Log.d(TAG_LOGCAT, "request");

        try {
            switch (input.getString(ABECS.CMD_ID)) {
                // case ABECS.GOC:
                case ABECS.GOX:
                // case ABECS.FNC:
                // case ABECS.FNX:
                    break;

                default:
                    getPinpad().abort(); /* ABECS v2.12; 2.2.2.3 */
            }
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
                    PinpadManager.getInstance().setCallbackStatus(true);

                    return command.second.run(input);
                }
            }

            if (request.equals("CAN")) {
                throw new Exception("Interruption requested: { CMD_ID: \"" + request + "\" }");
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

    public boolean getCallbackStatus() {
        Log.d(TAG_LOGCAT, "getCallbackStatus");

        boolean enabled;

        sSemaphore[2].acquireUninterruptibly();

        enabled = sCallbackStatus;

        sSemaphore[2].release();

        return enabled;
    }

    @Override
    public void registerCallback(IServiceCallback input) {
        Log.d(TAG_LOGCAT, "registerCallback");

        sSemaphore[0].acquireUninterruptibly();

        setCallback(input);

        sSemaphore[0].release();
    }

    public void setCallbackStatus(boolean enabled) {
        Log.d(TAG_LOGCAT, "setCallbackStatus");

        sSemaphore[2].acquireUninterruptibly();

        sCallbackStatus = enabled;

        sSemaphore[2].release();
    }
}
