package com.example.poc2104301453.service.utilities;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.example.poc2104301453.service.IServiceCallback;
import com.example.poc2104301453.service.commands.*;

import java.util.ArrayList;
import java.util.List;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.GestaoBibliotecaPinpad;
import br.com.verifone.bibliotecapinpad.RegistroBibliotecaPinpad;
import br.com.verifone.bibliotecapinpad.comum.AcessoFuncoesImplementacao;
import br.com.verifone.ppcompX990.PPCompX990;

import static com.example.poc2104301453.library.ABECS.*;
import static com.example.poc2104301453.library.ABECS.RSP_STAT.*;

/**
 *
 */
public class ServiceUtility {
    private static final String TAG_LOGCAT = ServiceUtility.class.getSimpleName();

    private static AcessoFuncoesPinpad sPinpad = null;

    private static IServiceCallback sServiceCallback = null;

    private static final List<Pair<String, Runner>> sCommandList = new ArrayList<>(0);

    private static final ServiceUtility sServiceUtility = new ServiceUtility();

    /**
     * Constructor.
     */
    private ServiceUtility() {
        sCommandList.add(new Pair<>(VAL_ENUM.OPN.getValue(), OPN::opn));
        sCommandList.add(new Pair<>(VAL_ENUM.GIN.getValue(), GIN::gin));
        sCommandList.add(new Pair<>(VAL_ENUM.CLO.getValue(), CLO::clo));
        // sCommandList.add(new Pair<>(VAL_ENUM.CKE.getValue(), CKE::cke));
        // sCommandList.add(new Pair<>(VAL_ENUM.ENB.getValue(), ENB::enb));
        // sCommandList.add(new Pair<>(VAL_ENUM.GDU.getValue(), GDU::gdu));
        // sCommandList.add(new Pair<>(VAL_ENUM.GPN.getValue(), GPN::gpn));
        // sCommandList.add(new Pair<>(VAL_ENUM.MNU.getValue(), MNU::mnu));
        // sCommandList.add(new Pair<>(VAL_ENUM.GTS.getValue(), GTS::gts));
        // sCommandList.add(new Pair<>(VAL_ENUM.TLI.getValue(), TLI::tls));
        // sCommandList.add(new Pair<>(VAL_ENUM.TLR.getValue(), TLR::tlr));
        // sCommandList.add(new Pair<>(VAL_ENUM.TLE.getValue(), TLE::tle));
        // sCommandList.add(new Pair<>(VAL_ENUM.GCR.getValue(), GCR::gcr));
        // sCommandList.add(new Pair<>(VAL_ENUM.CNG.getValue(), CNG::cng));
        // sCommandList.add(new Pair<>(VAL_ENUM.GOC.getValue(), GOC::goc));
        // sCommandList.add(new Pair<>(VAL_ENUM.FNC.getValue(), FNC::fnc));

        try {
            RegistroBibliotecaPinpad.informaClassesBiblioteca(PPCompX990.getInstance());

            sPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoFuncoesPinpad();
        } catch (Exception exception) {
            Log.e(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
        }
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

    public AcessoFuncoesPinpad getPinpad() {
        Log.d(TAG_LOGCAT, "getPinpad");

        return sPinpad;
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

            String request = input.getString(KEY_ENUM.REQUEST.getValue());

            if (request == null) {
                throw new Exception("Mandatory key \"" + KEY_ENUM.REQUEST.getValue() + "\" not found");
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

            throw new Exception("Unknown input: { " + KEY_ENUM.REQUEST.getValue() + ": \"" + request + "\" }");
        } catch (Exception exception) {
            output.putInt(KEY_ENUM.STATUS.getValue(), ST_INTERR.getValue());
            output.putSerializable(KEY_ENUM.EXCEPTION.getValue(), exception);
        } finally {
            if (!input.getBoolean(KEY_ENUM.SYNCHRONOUS_OPERATION.getValue())) {
                try {
                    if (output.getInt(KEY_ENUM.STATUS.getValue()) != 0) {
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
