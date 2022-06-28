package io.cloudwalk.pos.demo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.Application;
import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class DEMO {
    private static final String
            TAG = DEMO.class.getSimpleName();

    private DEMO() {
        Log.d(TAG, "DEMO");

        /* Nothing to do */
    }

    public static String OPN()
            throws Exception {
        Log.d(TAG, "OPN");

        JSONObject request = new JSONObject();

        String OPN_MOD = "A82A660B3C49226EFCDABA7FC68066B83D23D0560EDA3A12B63E9132F299FBF340A5AE" +
                         "BC4CD5DC1F14873F83A80BA9A88D3FEABBAB41DFFC1944BBBAA89F26AF9CC28FF31C49" +
                         "7EB91D82F8613E7463C47529FBD1925FD3326A8DC027704DA68860E68BD0A1CEA8DE6E" +
                         "C75604CD3D9A6AF38822DE45AAA0C9FBF2BD4783B0F9A81F6350C0188156F908FAB1F5" +
                         "59CFCE1F91A393431E8BF2CD78C04BD530DB441091CDFFB400DAC08B1450DB65C00E2D" +
                         "4AF4E9A85A1A19B61F550F0C289B14BD63DF8A1539A8CF629F98F88EA944D905667500" +
                         "0F95BFD0FEFC56F9D9D66E2701BDBD71933191AE9928F5D623FE8B99ECC777444FFAA8" +
                         "3DE456F5C8D3C83EC511AF";

        request.put(ABECS.CMD_ID, ABECS.OPN);
        request.put(ABECS.OPN_OPMODE, "0");
        request.put(ABECS.OPN_MOD, OPN_MOD);
        request.put(ABECS.OPN_EXP, "0D");

        return request.toString();
    }

    public static String GIX()
            throws Exception {
        Log.d(TAG, "GIX");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.GIX);

        return request.toString();
    }

    public static String CLX()
            throws Exception {
        Log.d(TAG, "CLX");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.CLX);

        return request.toString();
    }

    public static String CEX()
            throws Exception {
        Log.d(TAG, "CEX");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.CEX);
        request.put(ABECS.SPE_CEXOPT, "011000");
        request.put(ABECS.SPE_TIMEOUT, "0F");
        request.put(ABECS.SPE_PANMASK, "0404");

        return request.toString();
    }

    public static String CHP()
            throws Exception {
        Log.d(TAG, "CHP");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.CHP);
        request.put(ABECS.CHP_SLOT, "0");
        request.put(ABECS.CHP_OPER, "1");

        return request.toString();
    }

    public static String EBX()
            throws Exception {
        Log.d(TAG, "EBX");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.EBX);
        request.put(ABECS.SPE_DATAIN, "00010203040506070809101112131415");
        request.put(ABECS.SPE_MTHDDAT, "50");
        request.put(ABECS.SPE_KEYIDX, "11");

        return request.toString();
    }

    public static String GCD()
            throws Exception {
        Log.d(TAG, "GCD");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.GCD);
        request.put(ABECS.SPE_MSGIDX, "0033");
        request.put(ABECS.SPE_MINDIG, "01");
        request.put(ABECS.SPE_MAXDIG, "02");
        request.put(ABECS.SPE_TIMEOUT, "0F");

        return request.toString();
    }

    public static String GPN()
            throws Exception {
        Log.d(TAG, "GPN");

        JSONObject request = new JSONObject();

        String GPN_WKENC = "00000000000000000000000000000000";

        request.put(ABECS.CMD_ID, ABECS.GPN);
        request.put(ABECS.GPN_METHOD, "3");
        request.put(ABECS.GPN_KEYIDX, "10");
        request.put(ABECS.GPN_WKENC, GPN_WKENC);
        request.put(ABECS.GPN_PAN, "5502092192096336   ");
        request.put(ABECS.GPN_ENTRIES, "1");
        request.put(ABECS.GPN_MIN1, "04");
        request.put(ABECS.GPN_MAX1, "08");
        request.put(ABECS.GPN_MSG1, "GPN_MSG1       \n                ");

        return request.toString();
    }

    public static String GTK()
            throws Exception {
        Log.d(TAG, "GTK");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.GTK);

        return request.toString();
    }

    public static String MNU()
            throws Exception {
        Log.d(TAG, "MNU");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.MNU);
        request.put(ABECS.SPE_TIMEOUT, "0F");
        request.put(ABECS.SPE_DSPMSG, "SPE_DSPMSG      ");

        JSONArray SPE_MNUOPT = new JSONArray();

        SPE_MNUOPT.put("OPT 1");
        SPE_MNUOPT.put("OPT 2");
        SPE_MNUOPT.put("OPT 3");

        request.put(ABECS.SPE_MNUOPT, SPE_MNUOPT);

        return request.toString();
    }

    public static String RMC()
            throws Exception {
        Log.d(TAG, "RMC");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.RMC);
        request.put(ABECS.RMC_MSG, "RMC_MSG        \n                ");

        return request.toString();
    }

    public static String TLI()
            throws Exception {
        Log.d(TAG, "TLI");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.TLI);
        request.put(ABECS.TLI_ACQIDX, "00");
        request.put(ABECS.TLI_TABVER, "0123456789");

        return request.toString();
    }

    public static List<String> TLR()
            throws Exception {
        Log.d(TAG, "TLR");

        BufferedReader reader  = new BufferedReader(new InputStreamReader(Application.getContext().getAssets().open("TLI_R_E")));

        ArrayList<String> list = new ArrayList<>(0);

        while (reader.ready()) {
            list.add(reader.readLine());
        }

        reader.close();

        ArrayList<String> request = new ArrayList<>(0);

        for (String record: list) {
            JSONObject entry = new JSONObject();

            entry.put(ABECS.CMD_ID, ABECS.TLR);
            entry.put(ABECS.TLR_NREC, "01");
            entry.put(ABECS.TLR_DATA, record);

            request.add(entry.toString());
        }

        return request;
    }

    public static String TLE()
            throws Exception {
        Log.d(TAG, "TLE");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.TLE);

        return request.toString();
    }

    public static String GCX()
            throws Exception {
        Log.d(TAG, "GCX");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.GCX);
        request.put(ABECS.SPE_TRNTYPE, "00");
        request.put(ABECS.SPE_ACQREF, "04");
        request.put(ABECS.SPE_APPTYPE, "0102");
        request.put(ABECS.SPE_AMOUNT, "000000000999");
        request.put(ABECS.SPE_CASHBACK, "000000000000");
        request.put(ABECS.SPE_TRNCURR, "986");
        request.put(ABECS.SPE_TRNDATE, "210909");
        request.put(ABECS.SPE_TRNTIME, "163800");
        request.put(ABECS.SPE_GCXOPT, "10000");
        request.put(ABECS.SPE_PANMASK, "0404");
        request.put(ABECS.SPE_TAGLIST, "5F285F24");
        request.put(ABECS.SPE_TIMEOUT, "0F");
        request.put(ABECS.SPE_DSPMSG, "SPE_DSPMSG      ");

        return request.toString();
    }

    public static String GED()
            throws Exception {
        Log.d(TAG, "GED");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.GED);
        request.put(ABECS.SPE_TAGLIST, "5F285F24");

        return request.toString();
    }

    public static String GOX()
            throws Exception {
        Log.d(TAG, "GOX");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.GOX);
        request.put(ABECS.SPE_ACQREF, "04");
        request.put(ABECS.SPE_TRNTYPE, "00");
        request.put(ABECS.SPE_AMOUNT, "000000000999");
        request.put(ABECS.SPE_CASHBACK, "000000000000");
        request.put(ABECS.SPE_TRNCURR, "986");
        request.put(ABECS.SPE_GOXOPT, "11100");
        request.put(ABECS.SPE_MTHDPIN, "3");
        request.put(ABECS.SPE_KEYIDX, "10");
        request.put(ABECS.SPE_DSPMSG, "SPE_DSPMSG     \n                ");
        request.put(ABECS.SPE_TRMPAR, "00000000250000000025");
        request.put(ABECS.SPE_TAGLIST, "5F285F24");
        request.put(ABECS.SPE_TIMEOUT, "0F");

        return request.toString();
    }

    public static String FCX()
            throws Exception {
        Log.d(TAG, "FCX");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, ABECS.FCX);
        request.put(ABECS.SPE_FCXOPT, "0000");
        request.put(ABECS.SPE_ARC, "00");
        request.put(ABECS.SPE_TAGLIST, "5F285F24");
        request.put(ABECS.SPE_TIMEOUT, "0F");

        return request.toString();
    }
}
