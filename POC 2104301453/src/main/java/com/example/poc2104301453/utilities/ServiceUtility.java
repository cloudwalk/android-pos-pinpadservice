package com.example.poc2104301453.utilities;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.example.poc2104301453.IServiceCallback;
import com.example.poc2104301453.commands.*;
import com.example.poc2104301453.exceptions.PendingDevelopmentException;

import java.util.ArrayList;
import java.util.List;

import static com.example.poc2104301453.IServiceMap.*;

/**
 *
 */
public class ServiceUtility {
    private static final String TAG_LOGCAT = ServiceUtility.class.getSimpleName();

    private static final List<Pair<String, Runner>> sRequestList = new ArrayList<>(0);

    private static final ServiceUtility S_SERVICE_UTILITY = new ServiceUtility();

    /**
     * Constructor.
     */
    private ServiceUtility() {
        /*
         * 3.2 Comandos de controle
         */

        sRequestList.add(new Pair<>(VALUE_REQUEST_OPN, OPN::opn));
        sRequestList.add(new Pair<>(VALUE_REQUEST_GIN, GIN::gin));
        sRequestList.add(new Pair<>(VALUE_REQUEST_CLO, CLO::clo));

        /*
         * 3.3 Comandos básicos
         */

        // sRequestList.add(new Pair<>(VALUE_REQUEST_CKE, CKE::cke));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_ENB, ENB::enb));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_GDU, GDU::gdu));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_GPN, GPN::gpn));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_MNU, MNU::mnu));

        /*
         * 3.5 Comandos para manutenção de Tabelas EMV
         */

        // sRequestList.add(new Pair<>(VALUE_REQUEST_GTS, GTS::gts));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_TLI, TLI::tls));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_TLR, TLR::tlr));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_TLE, TLE::tle));

        /*
         * 3.6 Comandos de processamento de cartão (obsoletos)
         */

        // sRequestList.add(new Pair<>(VALUE_REQUEST_GCR, GCR::gcr));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_CNG, CNG::cng));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_GOC, GOC::goc));
        // sRequestList.add(new Pair<>(VALUE_REQUEST_FNC, FNC::fnc));
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
        return S_SERVICE_UTILITY;
    }

    /**
     *
     * @param callback
     * @return
     * @throws PendingDevelopmentException
     */
    public Bundle register(IServiceCallback callback)
            throws PendingDevelopmentException {
        throw new PendingDevelopmentException("Pending development");
    }

    /**
     *
     * @param input
     * @return
     * @throws PendingDevelopmentException
     */
    public Bundle run(Bundle input) {

        Bundle output = new Bundle();

        try {
            String key = input.getString(KEY_REQUEST);

            if (key == null) {
                throw new Exception("Mandatory key \"" + KEY_REQUEST + "\" not found");
            }

            for (Pair<String, Runner> request : sRequestList) {
                if (key.equals(request.first)) {
                    /* TODO: deal with parallel processing several commands */

                    return request.second.run(input);
                }
            }

            StringBuilder log = new StringBuilder("Be sure to run one of the known requests:\r\n");

            for (Pair<String, Runner> request : sRequestList) {
                log.append("\t ").append(request.first).append(";\r\n");
            }

            Log.e(TAG_LOGCAT, log.toString());

            throw new Exception("Unknown input: { " + KEY_REQUEST + ": \"" + key + "\" }");
        } catch (Exception exception) {
            output.putInt("status", 40);
            output.putSerializable("exception", exception);
        }

        return output;
    }
}
