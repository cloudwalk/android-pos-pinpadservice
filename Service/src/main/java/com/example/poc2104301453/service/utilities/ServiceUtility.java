package com.example.poc2104301453.service.utilities;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import com.example.poc2104301453.service.IStatusCallback;
import com.example.poc2104301453.service.commands.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.example.poc2104301453.library.ABECS.*;

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
    private static final Callback[] serviceCallback = { null, null };

    private static final List<Pair<String, Runner>> sCommandList = new ArrayList<>(0);

    private static final Semaphore sSemaphoreCallback = new Semaphore(1, true);

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

        Callback.Process process = new Callback.Process() {
            /* TODO */
        };

        Callback.Status status = new Callback.Status() {
            @Override
            public void onFailure(Bundle output) {
                callRemoteStatusCallback(false, output);
            }

            @Override
            public void onSuccess(Bundle output) {
                callRemoteStatusCallback(true, output);
            }
        };

        serviceCallback[0] = new Callback(process, status);
    }

    /* TODO: (2) private void callRemoteProcessingCallback() { ... } */

    private void callRemoteStatusCallback(boolean success, Bundle output) {
        sSemaphoreCallback.acquireUninterruptibly();

        if (serviceCallback[1] != null) {
            Log.d(TAG_LOGCAT, "Calling remote callback");

            Callback.Status remoteInstance = serviceCallback[1].status;

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
                    } catch (Exception exception) {
                        Log.d(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
                    } finally {
                        Log.d(TAG_LOGCAT, "Returning from remote callback execution");
                    }
                }
            }.start();
        }

        sSemaphoreCallback.release();
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

    public Bundle register(IStatusCallback callback) {
        Log.d(TAG_LOGCAT, "register::callback [" + callback + "]");

        Bundle output = new Bundle();
        int status = 40;

        try {
            sSemaphoreCallback.acquireUninterruptibly();

            Callback.Process remoteProcess = new Callback.Process() {
                /* TODO */
            };

            Callback.Status remoteStatus = new Callback.Status() {
                @Override
                public void onFailure(Bundle output) {
                    try {
                        callback.onFailure(output);
                    } catch (RemoteException exception) {
                        Log.d(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
                    }
                }

                @Override
                public void onSuccess(Bundle output) {
                    try {
                        callback.onSuccess(output);
                    } catch (RemoteException exception) {
                        Log.d(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
                    }
                }
            };

            Callback remoteCallback = null;

            if (callback != null) {
                remoteCallback = new Callback(remoteProcess, remoteStatus);
            }

            serviceCallback[1] = remoteCallback;

            status = 0;
        } catch (Exception exception) {
            output.putSerializable("exception", exception);
        } finally {
            sSemaphoreCallback.release();

            output.putInt("status", status);
        }

        return output;
    }

    /**
     *
     * @param input
     * @return
     */
    public Bundle run(IStatusCallback callback, Bundle input) {
        if (input != null) {
            input.get(null); /* 2021-05-05: just to force the parcelable data printable */
        }

        Log.d(TAG_LOGCAT, "run::input [" + ((input != null) ? input.toString() : null) + "]");

        Bundle output = new Bundle();

        try {
            String request = input.getString(KEY_REQUEST);

            if (request == null) {
                throw new Exception("Mandatory key \"" + KEY_REQUEST + "\" not found");
            }

            output = register(callback);

            if (output.getInt("status") != 0) {
                return output;
            }

            for (Pair<String, Runner> command : sCommandList) {
                if (request.equals(command.first)) {
                    return command.second.run(input);
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
            if (!input.getBoolean("synchronous_operation")) {
                try {
                    if (output.getInt("status") != 0) {
                        serviceCallback[0].status.onFailure(output);
                    } else {
                        serviceCallback[0].status.onSuccess(output);
                    }
                } catch (Exception exception) {
                    Log.d(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
                }
            }
        }

        return output;
    }
}
