package com.example.poc2104301453.service.utilities;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.example.poc2104301453.service.IServiceCallback;
import com.example.poc2104301453.service.commands.*;

import java.util.ArrayList;
import java.util.List;

import static com.example.poc2104301453.library.ABECS.*;

/**
 *
 */
public class ServiceUtility {
    private static final String TAG_LOGCAT = ServiceUtility.class.getSimpleName();

    private static IServiceCallback sServiceCallback = null;

    private static final List<Pair<String, Runner>> sCommandList = new ArrayList<>(0);

    private static final ServiceUtility sServiceUtility = new ServiceUtility();

    /**
     * Constructor.
     */
    private ServiceUtility() {
        sCommandList.add(new Pair<>(VALUE_REQUEST_OPN, OPN::opn));
        sCommandList.add(new Pair<>(VALUE_REQUEST_GIN, GIN::gin));
        sCommandList.add(new Pair<>(VALUE_REQUEST_CLO, CLO::clo));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_CKE, CKE::cke));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_ENB, ENB::enb));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_GDU, GDU::gdu));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_GPN, GPN::gpn));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_MNU, MNU::mnu));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_GTS, GTS::gts));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_TLI, TLI::tls));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_TLR, TLR::tlr));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_TLE, TLE::tle));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_GCR, GCR::gcr));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_CNG, CNG::cng));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_GOC, GOC::goc));
        // sCommandList.add(new Pair<>(VALUE_REQUEST_FNC, FNC::fnc));
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
     * @param input
     * @return
     */
    public Bundle run(IServiceCallback callback, Bundle input) {
        Bundle output = new Bundle();

        try {
            input.get(null);

            Log.d(TAG_LOGCAT, "run::input [" + input.toString() + "]");

            String request = input.getString(KEY_REQUEST);

            if (request == null) {
                throw new Exception("Mandatory key \"" + KEY_REQUEST + "\" not found");
            }

            sServiceCallback = callback;

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
                        sServiceCallback.onFailure(output);
                    } else {
                        sServiceCallback.onSuccess(output);
                    }
                } catch (Exception exception) {
                    Log.d(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
                }
            }
        }

        return output;
    }
}
