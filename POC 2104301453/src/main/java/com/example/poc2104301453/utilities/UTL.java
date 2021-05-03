package com.example.poc2104301453.utilities;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.example.poc2104301453.IServiceCallback;
import com.example.poc2104301453.commands.CLO;
import com.example.poc2104301453.commands.OPN;
import com.example.poc2104301453.exceptions.PendingDevelopmentException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class UTL {
    private static final String TAG_LOGCAT = UTL.class.getSimpleName();

    private static final List<Pair<String, Runner>> sList = new ArrayList<>(0);

    private static final UTL sUTL = new UTL();

    /**
     * Constructor.
     */
    private UTL() {
        sList.add(new Pair<>(VALUE_OPN, OPN::opn));
        sList.add(new Pair<>(VALUE_CLO, CLO::clo));
    }

    public static final String KEY_COMMAND = "command";

    public static final String VALUE_OPN = "OPN";

    public static final String VALUE_CLO = "CLO";


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
     * @return {@link UTL}
     */
    public static UTL getInstance() {
        return sUTL;
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
            String key = input.getString(KEY_COMMAND);

            if (key == null) {
                throw new Exception("Mandatory key \"" + KEY_COMMAND + "\" not found");
            }

            for (Pair<String, Runner> command : sList) {
                if (key.equals(command.first)) {
                    /* TODO: deal with parallel processing several commands */

                    return command.second.run(input);
                }
            }

            StringBuilder log = new StringBuilder("Be sure to run one of the known commands:\r\n");

            for (Pair<String, Runner> command : sList) {
                log.append("\t ").append(command.first).append(";\r\n");
            }

            Log.e(TAG_LOGCAT, log.toString());

            throw new Exception("Unknown input: { " + KEY_COMMAND + ": \"" + key + "\" }");
        } catch (Exception exception) {
            output.putInt("status", 40);
            output.putSerializable("exception", exception);
        }

        return output;
    }
}
