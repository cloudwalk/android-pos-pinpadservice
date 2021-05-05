package com.example.service.utilities;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import com.example.service.IServiceCallback;
import com.example.service.commands.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.service.IServiceMap.*;

/**
 *
 */
public class ServiceUtility {
    private static final String TAG_LOGCAT = ServiceUtility.class.getSimpleName();

    /**
     * <ul>
     *     <li>[0]: local instance (always available)</li>
     *     <li>[1]: remote instance</li>
     * </ul>
     */
    private static final IServiceCallback[] serviceCallback = { null, null };

    private static final List<Pair<String, Runner>> sCommandList = new ArrayList<>(0);

    private static final Lock sLockCallback = new ReentrantLock(true);

    private static final ServiceUtility sServiceUtility = new ServiceUtility();

    /**
     * Constructor.
     */
    private ServiceUtility() {
        /*
         * 3.2 Comandos de controle
         */

        sCommandList.add(new Pair<>(VALUE_REQUEST_OPN, OPN::opn));
        sCommandList.add(new Pair<>(VALUE_REQUEST_GIN, GIN::gin));
        sCommandList.add(new Pair<>(VALUE_REQUEST_CLO, CLO::clo));

        /*
         * 3.3 Comandos básicos
         */

        // sCommandList.add(new Pair<>(VALUE_REQUEST_CKE, CKE::cke));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_ENB, ENB::enb));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_GDU, GDU::gdu));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_GPN, GPN::gpn));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_MNU, MNU::mnu));

        /*
         * 3.5 Comandos para manutenção de Tabelas EMV
         */

        // sCommandList.add(new Pair<>(VALUE_REQUEST_GTS, GTS::gts));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_TLI, TLI::tls));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_TLR, TLR::tlr));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_TLE, TLE::tle));

        /*
         * 3.6 Comandos de processamento de cartão (obsoletos)
         */

        // sCommandList.add(new Pair<>(VALUE_REQUEST_GCR, GCR::gcr));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_CNG, CNG::cng));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_GOC, GOC::goc));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_FNC, FNC::fnc));

        serviceCallback[0] = new IServiceCallback.Stub() {
            @Override
            public void onFailure(Bundle output) {
                callRemoteStatusCallback(false, output);
            }

            @Override
            public void onSuccess(Bundle output) {
                callRemoteStatusCallback(true, output);
            }
        };
    }

    /* TODO: (2) private void callRemoteProcessingCallback() { ... } */

    /**
     *
     * @param success
     * @param output
     */
    private void callRemoteStatusCallback(boolean success, Bundle output) {
        sLockCallback.lock();

        if (serviceCallback[1] != null) {
            Log.d(TAG_LOGCAT, "Calling remote callback");

            IServiceCallback remoteInstance = serviceCallback[1];

            new Thread() {
                @Override
                public void run() {
                    super.run();

                    try {
                        if (success) {
                            remoteInstance.onSuccess(output);
                        } else {
                            remoteInstance.onFailure(output);
                        }
                    } catch (RemoteException exception) {
                        Log.w(TAG_LOGCAT, exception);
                    } finally {
                        Log.d(TAG_LOGCAT, "Returning from remote callback");
                    }
                }
            }.start();
        }

        sLockCallback.unlock();
    }

    /**
     * Runner interface.
     */
    public interface Runner {
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
     * @return {@link ServiceUtility}
     */
    public static ServiceUtility getInstance() {
        Log.d(TAG_LOGCAT, "getInstance");

        return sServiceUtility;
    }

    /**
     *
     * @param sync
     * @param callback
     * @return
     */
    public Bundle register(boolean sync, IServiceCallback callback) {
        Log.d(TAG_LOGCAT, "register::callback [" + callback + "]");

        Bundle output = new Bundle();
        int status = 40;

        try {
            sLockCallback.lock();

            serviceCallback[1] = callback;

            status = 0;
        } catch (Exception exception) {
            output.putSerializable("exception", exception);
        } finally {
            sLockCallback.unlock();

            output.putInt("status", status);

            if (!sync) {
                try {
                    if (status != 0) {
                        serviceCallback[0].onFailure(output);
                    } else {
                        serviceCallback[0].onSuccess(output);
                    }
                } catch (RemoteException exception) {
                    Log.w(TAG_LOGCAT, exception);
                }
            }
        }

        return output;
    }

    /**
     *
     * @param sync
     * @param input
     * @return
     */
    public Bundle run(boolean sync, Bundle input) {
        Log.d(TAG_LOGCAT, "run::sync [" + sync + "], input [" + ((input != null) ? input.toString() : null) + "]");

        Bundle output = new Bundle();

        try {
            String request = input.getString(KEY_REQUEST);

            if (request == null) {
                throw new Exception("Mandatory key \"" + KEY_REQUEST + "\" not found");
            }

            /* TODO: (1) make the callback stub a parcelable to be retrieved from the input bundle?! */

            for (Pair<String, Runner> command : sCommandList) {
                if (request.equals(command.first)) {
                    /* TODO: (3) ensure safe and limited parallel processing */

                    output = command.second.run(input);

                    return output;
                }
            }

            StringBuilder log = new StringBuilder("Be sure to run one of the known commands:\r\n");

            for (Pair<String, Runner> command : sCommandList) {
                log.append("\t ").append(command.first).append(";\r\n");
            }

            Log.e(TAG_LOGCAT, log.toString());

            throw new Exception("Unknown input: { " + KEY_REQUEST + ": \"" + request + "\" }");
        } catch (Exception exception) {
            output.putInt("status", 40);
            output.putSerializable("exception", exception);
        } finally {
            if (!sync) {
                try {
                    if (output.getInt("status") != 0) {
                        serviceCallback[0].onFailure(output);
                    } else {
                        serviceCallback[0].onSuccess(output);
                    }
                } catch (RemoteException exception) {
                    Log.w(TAG_LOGCAT, exception);
                }
            }
        }

        return output;
    }
}
