package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.TabelaAID;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.TabelaCAPK;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.TabelaCertificadosRevogados;
import br.com.setis.sunmi.bibliotecapinpad.entradas.EntradaComandoTableLoad;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.utilities.DataUtility;
import io.cloudwalk.pos.pinpadservice.PinpadAbstractionLayer;
import io.cloudwalk.pos.pinpadservice.utilities.ManufacturerUtility;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TLE {
    private static final String TAG_LOGCAT = TLE.class.getSimpleName();

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadAbstractionLayer.getInstance().getPinpad();
    }

    private static TabelaAID parseAPPL(int TAB_LEN, int TAB_ID, int TAB_ACQ,
                                       String TAB_DATA)
            throws Exception {
        TabelaAID.Builder builder = new TabelaAID.Builder();

        int     TAB_RECIDX      = Integer.parseInt(TAB_DATA.substring(0, 2));
                TAB_DATA        = TAB_DATA.substring(2);

        Log.d(TAG_LOGCAT, "TAB_RECIDX [" + TAB_RECIDX + "]");

        int     T1_AIDLEN       = Integer.parseInt(TAB_DATA.substring(0, 2));
                TAB_DATA        = TAB_DATA.substring(2);

        Log.d(TAG_LOGCAT, "T1_AIDLEN [" + T1_AIDLEN + "]");

        byte[]  T1_AID          = DataUtility.toByteArray(TAB_DATA.substring(0, T1_AIDLEN * 2));
                TAB_DATA        = TAB_DATA.substring(32);

        Log.d(TAG_LOGCAT, "T1_AID [" + (new String(T1_AID, UTF_8)) + "]");

        int     T1_APPTYPE      = Integer.parseInt(TAB_DATA.substring(0, 2));
                TAB_DATA        = TAB_DATA.substring(2);

        Log.d(TAG_LOGCAT, "T1_APPTYPE [" + T1_APPTYPE + "]");

        String  T1_DEFLABEL     = TAB_DATA.substring(0, 16);
                TAB_DATA        = TAB_DATA.substring(16);

        Log.d(TAG_LOGCAT, "T1_DEFLABEL [" + T1_DEFLABEL + "]");

        int     T1_ICCSTD       = Integer.parseInt(TAB_DATA.substring(0, 2));
                TAB_DATA        = TAB_DATA.substring(2);

        Log.d(TAG_LOGCAT, "T1_ICCSTD [" + T1_ICCSTD + "]");

        byte[]  T1_APPVER1      = DataUtility.toByteArray(TAB_DATA.substring(0,  4));
        byte[]  T1_APPVER2      = DataUtility.toByteArray(TAB_DATA.substring(4,  8));
        byte[]  T1_APPVER3      = DataUtility.toByteArray(TAB_DATA.substring(8, 12));

                TAB_DATA        = TAB_DATA.substring(12);

        Log.d(TAG_LOGCAT, "T1_APPVER1 [" + (new String(T1_APPVER1, UTF_8)) + "]");
        Log.d(TAG_LOGCAT, "T1_APPVER2 [" + (new String(T1_APPVER2, UTF_8)) + "]");
        Log.d(TAG_LOGCAT, "T1_APPVER3 [" + (new String(T1_APPVER3, UTF_8)) + "]");

        int     T1_TRMCNTRY     = Integer.parseInt(TAB_DATA.substring(0, 3));
                TAB_DATA        = TAB_DATA.substring(3);

        Log.d(TAG_LOGCAT, "T1_TRMCNTRY [" + T1_TRMCNTRY + "]");

        int     T1_TRNCURR      = Integer.parseInt(TAB_DATA.substring(0, 3));
                TAB_DATA        = TAB_DATA.substring(3);

        Log.d(TAG_LOGCAT, "T1_TRNCURR [" + T1_TRNCURR + "]");

        int     T1_TRNCRREXP    = Integer.parseInt(TAB_DATA.substring(0, 1));
                TAB_DATA        = TAB_DATA.substring(1);

        Log.d(TAG_LOGCAT, "T1_TRNCRREXP [" + T1_TRNCRREXP + "]");

        String  T1_MERCHID      = TAB_DATA.substring(0, 15);
                TAB_DATA        = TAB_DATA.substring(15);

        Log.d(TAG_LOGCAT, "T1_MERCHID [" + T1_MERCHID + "]");

        int     T1_MCC          = Integer.parseInt(TAB_DATA.substring(0, 4));
                TAB_DATA        = TAB_DATA.substring(4);

        Log.d(TAG_LOGCAT, "T1_MCC [" + T1_MCC + "]");

        String  T1_TRMID        = TAB_DATA.substring(0, 8);
                TAB_DATA        = TAB_DATA.substring(8);

        Log.d(TAG_LOGCAT, "T1_TRMID [" + T1_TRMID + "]");

        byte[]  T1_TRMCAPAB     = DataUtility.toByteArray(TAB_DATA.substring(0, 6));
                TAB_DATA        = TAB_DATA.substring(6);

        Log.d(TAG_LOGCAT, "T1_TRMCAPAB [" + (new String(T1_TRMCAPAB, UTF_8)) + "]");

        byte[]  T1_ADDTRMCP     = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
                TAB_DATA        = TAB_DATA.substring(10);

        Log.d(TAG_LOGCAT, "T1_ADDTRMCP [" + (new String(T1_ADDTRMCP, UTF_8)) + "]");

        int     T1_TRMTYP       = Integer.parseInt(TAB_DATA.substring(0, 2));
                TAB_DATA        = TAB_DATA.substring(2);

        Log.d(TAG_LOGCAT, "T1_TRMTYP [" + T1_TRMTYP + "]");

        byte[]  T1_TACDEF       = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
                TAB_DATA        = TAB_DATA.substring(10);

        Log.d(TAG_LOGCAT, "T1_TACDEF [" + (new String(T1_TACDEF, UTF_8)) + "]");

        byte[]  T1_TACDEN       = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
                TAB_DATA        = TAB_DATA.substring(10);

        Log.d(TAG_LOGCAT, "T1_TACDEN [" + (new String(T1_TACDEN, UTF_8)) + "]");

        byte[]  T1_TACONL       = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
                TAB_DATA        = TAB_DATA.substring(10);

        Log.d(TAG_LOGCAT, "T1_TACONL [" + (new String(T1_TACONL, UTF_8)) + "]");

        byte[]  T1_FLRLIMIT     = DataUtility.toByteArray(TAB_DATA.substring(0, 8));
                TAB_DATA        = TAB_DATA.substring(8);

        Log.d(TAG_LOGCAT, "T1_FLRLIMIT [" + (new String(T1_FLRLIMIT, UTF_8)) + "]");

        char    T1_TCC          = TAB_DATA.charAt(0);
        char    T1_CTLSZEROAM   = TAB_DATA.charAt(1);
        char    T1_CTLSMODE     = TAB_DATA.charAt(2);

                TAB_DATA        = TAB_DATA.substring(3);

        Log.d(TAG_LOGCAT, "T1_TCC [" + T1_TCC + "]");
        Log.d(TAG_LOGCAT, "T1_CTLSZEROAM [" + T1_CTLSZEROAM + "]");
        Log.d(TAG_LOGCAT, "T1_CTLSMODE [" + T1_CTLSMODE + "]");

        byte[]  T1_CTLSTRNLIM   = DataUtility.toByteArray(TAB_DATA.substring(0, 8));
                TAB_DATA        = TAB_DATA.substring(8);

        Log.d(TAG_LOGCAT, "T1_CTLSTRNLIM [" + (new String(T1_CTLSTRNLIM, UTF_8)) + "]");

        byte[]  T1_CTLSFLRLIM   = DataUtility.toByteArray(TAB_DATA.substring(0, 8));
                TAB_DATA        = TAB_DATA.substring(8);

        Log.d(TAG_LOGCAT, "T1_CTLSFLRLIM [" + (new String(T1_CTLSFLRLIM, UTF_8)) + "]");

        byte[]  T1_CTLSCVMLIM   = DataUtility.toByteArray(TAB_DATA.substring(0, 8));
                TAB_DATA        = TAB_DATA.substring(8);

        Log.d(TAG_LOGCAT, "T1_CTLSCVMLIM [" + (new String(T1_CTLSCVMLIM, UTF_8)) + "]");

        byte[]  T1_CTLSAPPVER   = DataUtility.toByteArray(TAB_DATA.substring(0, 4));
                TAB_DATA        = TAB_DATA.substring(4 + 1); /* + T1_RUF1 */

        Log.d(TAG_LOGCAT, "T1_CTLSAPPVER [" + (new String(T1_CTLSAPPVER, UTF_8)) + "]");

        byte[]  T1_TDOLDEF      = DataUtility.toByteArray(TAB_DATA.substring(0, 40));
                TAB_DATA        = TAB_DATA.substring(40);

        Log.d(TAG_LOGCAT, "T1_TDOLDEF [" + (new String(T1_TDOLDEF, UTF_8)) + "]");

        byte[]  T1_DDOLDEF      = DataUtility.toByteArray(TAB_DATA.substring(0, 40));
                TAB_DATA        = TAB_DATA.substring(40);

        Log.d(TAG_LOGCAT, "T1_DDOLDEF [" + (new String(T1_DDOLDEF, UTF_8)) + "]");

        String  T1_ARCOFFLN     = TAB_DATA.substring(0, 8);
                TAB_DATA        = TAB_DATA.substring(8);

        Log.d(TAG_LOGCAT, "T1_ARCOFFLN [" + T1_ARCOFFLN + "]");

        byte[]  T1_CTLSTACDEF   = T1_TACDEF;
        byte[]  T1_CTLSTACDEN   = T1_TACDEN;
        byte[]  T1_CTLSTACONL   = T1_TACONL;
        byte[]  T1_CTLSTRMCP    = { }; /* T1_TRMCAPAB; */
        char    T1_MOBCVM       = '0';
        byte[]  T1_CTLSADDTC    = { }; /* T1_ADDTRMCP; */
        byte[]  T1_CTLSMBTLIM   = T1_CTLSTRNLIM;
        char    T1_CTLSISSSCR   = '0';

        if (!TAB_DATA.isEmpty()) {
            T1_CTLSTACDEF       = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
            TAB_DATA            = TAB_DATA.substring(10);
        }

        Log.d(TAG_LOGCAT, "T1_CTLSTACDEF [" + (new String(T1_CTLSTACDEF, UTF_8)) + "]");

        if (!TAB_DATA.isEmpty()) {
            T1_CTLSTACDEN       = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
            TAB_DATA            = TAB_DATA.substring(10);
        }

        Log.d(TAG_LOGCAT, "T1_CTLSTACDEN [" + (new String(T1_CTLSTACDEN, UTF_8)) + "]");

        if (!TAB_DATA.isEmpty()) {
            T1_CTLSTACONL       = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
            TAB_DATA            = TAB_DATA.substring(10);
        }

        Log.d(TAG_LOGCAT, "T1_CTLSTACONL [" + (new String(T1_CTLSTACONL, UTF_8)) + "]");

        if (!TAB_DATA.isEmpty()) {
            T1_CTLSTRMCP        = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
            TAB_DATA            = TAB_DATA.substring(10);
        }

        Log.d(TAG_LOGCAT, "T1_CTLSTRMCP [" + (new String(T1_CTLSTRMCP, UTF_8)) + "]");

        if (!TAB_DATA.isEmpty()) {
            T1_MOBCVM           = TAB_DATA.charAt(0);
            TAB_DATA            = TAB_DATA.substring(1);
        }

        Log.d(TAG_LOGCAT, "T1_MOBCVM [" + T1_MOBCVM + "]");

        if (!TAB_DATA.isEmpty()) {
            T1_CTLSADDTC        = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
            TAB_DATA            = TAB_DATA.substring(10);
        }

        Log.d(TAG_LOGCAT, "T1_CTLSADDTC [" + (new String(T1_CTLSADDTC, UTF_8)) + "]");

        if (!TAB_DATA.isEmpty()) {
            T1_CTLSMBTLIM       = DataUtility.toByteArray(TAB_DATA.substring(0, 8));
            TAB_DATA            = TAB_DATA.substring(8);
        }

        Log.d(TAG_LOGCAT, "T1_CTLSMBTLIM [" + (new String(T1_CTLSMBTLIM, UTF_8)) + "]");

        if (!TAB_DATA.isEmpty()) {
            T1_CTLSISSSCR       = TAB_DATA.charAt(0);
            TAB_DATA            = TAB_DATA.substring(1);
        }

        Log.d(TAG_LOGCAT, "T1_CTLSISSSCR [" + T1_CTLSISSSCR + "]");

        builder.informaIdentificadorRedeCredenciadora                   (TAB_ACQ);
        builder.informaIndiceRegistroTabela                             (TAB_RECIDX);
        builder.informaApplicationIdentifier                            (T1_AID);
        builder.informaTipoAplicacaoCartao                              (T1_APPTYPE);
        builder.informaEtiquetaDefaultAplicacao                         (T1_DEFLABEL);
                                                                     /* (T1_ICCSTD); */
        builder.informaTerminalApplicationVer1                          (T1_APPVER1);
        builder.informaTerminalApplicationVer2                          (T1_APPVER2);
        builder.informaTerminalApplicationVer3                          (T1_APPVER3);
        builder.informaTerminalCountryCode                              (T1_TRMCNTRY);
        builder.informaTransactionCurrencyCode                          (T1_TRNCURR);
        builder.informaTransactionCurrencyExponent                      (T1_TRNCRREXP);
        builder.informaMerchantIdentifier                               (T1_MERCHID);
        builder.informaMerchantCategoryCode                             (T1_MCC);
        builder.informaTerminalIdentification                           (T1_TRMID);
        builder.informaTerminalCapababilities                           (T1_TRMCAPAB);
        builder.informaAdditionalTerminalCapabilities                   (T1_ADDTRMCP);
        builder.informaTerminalType                                     (T1_TRMTYP);
        builder.informaTerminalDefaultActionCode                        (T1_TACDEF);
        builder.informaTerminalDenialActionCode                         (T1_TACDEN);
        builder.informaTerminalOnlineActionCode                         (T1_TACONL);
        builder.informaTerminalFloorLimit                               (T1_FLRLIMIT);
        builder.informaTransactionCategoryCode                          (T1_TCC);
        builder.informaContactlessZeroAction                            (T1_CTLSZEROAM);
        builder.informaContactlessMode                                  (T1_CTLSMODE);
        builder.informaContactlessTransactionLimit                      (T1_CTLSTRNLIM);
        builder.informaContactlessFloorLimit                            (T1_CTLSFLRLIM);
        builder.informaContactlessCvmRequiredLimit                      (T1_CTLSCVMLIM);
        builder.informaMagStripeApplicationVersionNumber                (T1_CTLSAPPVER);
        builder.informaDefaultTransactionCertificateDataObjectList      (T1_TDOLDEF);
        builder.informaDefaultDynamicDataAuthenticationDataObjectList   (T1_DDOLDEF);
                                                                     /* (T1_ARCOFFLN); */
        builder.informaTerminalDefaultActionCodeContactless             (T1_CTLSTACDEF);
        builder.informaTerminalDenialActionCodeContactless              (T1_CTLSTACDEN);
        builder.informaTerminalOnlineActionCodeContactless              (T1_CTLSTACONL);

        if (T1_CTLSTRMCP.length != 0) {
            builder.informaTerminalCapababilitiesContactless            (T1_CTLSTRMCP);
        }

        builder.informaMobileCVMSupport                                 (T1_MOBCVM);

        if (T1_CTLSADDTC.length != 0) {
            builder.informaAdditionalTerminalCapabilitiesContactless    (T1_CTLSADDTC);
        }

        builder.informaContactlessTransactionLimitMobile                (T1_CTLSMBTLIM);
        builder.informaContactlessIssuerScriptsSupport                  (T1_CTLSISSSCR);

        return builder.build();
    }

    private static TabelaCAPK parseCAPK(int TAB_LEN, int TAB_ID, int TAB_ACQ,
                                        String TAB_DATA)
            throws Exception {
        TabelaCAPK.Builder builder = new TabelaCAPK.Builder();

        int     TAB_RECIDX      = Integer.parseInt(TAB_DATA.substring(0, 2));
                TAB_DATA        = TAB_DATA.substring(2);

        Log.d(TAG_LOGCAT, "TAB_RECIDX [" + TAB_RECIDX + "]");

        byte[]  T2_RID          = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
                TAB_DATA        = TAB_DATA.substring(10);

        Log.d(TAG_LOGCAT, "T2_RID [" + (new String(T2_RID, UTF_8)) + "]");

        byte[]  T2_CAPKIDX      = DataUtility.toByteArray(TAB_DATA.substring(0, 2));
                TAB_DATA        = TAB_DATA.substring(2 + 2); /* + T2_RUF1 */

        Log.d(TAG_LOGCAT, "T2_CAPKIDX [" + (new String(T2_CAPKIDX, UTF_8)) + "]");

        int     T2_EXPLEN       = Integer.parseInt(TAB_DATA.substring(0, 1));
                TAB_DATA        = TAB_DATA.substring(1);

        Log.d(TAG_LOGCAT, "T2_EXPLEN [" + T2_EXPLEN + "]");

        byte[]  T2_EXP          = DataUtility.toByteArray(TAB_DATA.substring(0, 6));
                TAB_DATA        = TAB_DATA.substring(6);

        Log.d(TAG_LOGCAT, "T2_EXP [" + (new String(T2_EXP, UTF_8)) + "]");

        int     T2_MODLEN       = Integer.parseInt(TAB_DATA.substring(0, 3));
                TAB_DATA        = TAB_DATA.substring(3);

        Log.d(TAG_LOGCAT, "T2_MODLEN [" + T2_MODLEN + "]");

        byte[]  T2_MOD          = DataUtility.toByteArray(TAB_DATA.substring(0, 496));
                TAB_DATA        = TAB_DATA.substring(496);

        Log.d(TAG_LOGCAT, "T2_MOD [" + (new String(T2_MOD, UTF_8)) + "]");

        int     T2_CHKSTAT      = Integer.parseInt(TAB_DATA.substring(0, 1));
                TAB_DATA        = TAB_DATA.substring(1);

        Log.d(TAG_LOGCAT, "T2_CHKSTAT [" + T2_CHKSTAT + "]");

        byte[]  T2_CHECKSUM     = DataUtility.toByteArray(TAB_DATA.substring(0, 40));
                TAB_DATA        = TAB_DATA.substring(40);

        Log.d(TAG_LOGCAT, "T2_CHECKSUM [" + (new String(T2_CHECKSUM, UTF_8)) + "]");

        builder.informaIdentificadorRedeCredenciadora                   (TAB_ACQ);
        builder.informaIndiceRegistroTabela                             (TAB_RECIDX);
        builder.informaRegisteredApplicationProviderIdentifier          (T2_RID);
        builder.informaCertificationAuthorityPublicKeyIndex             (T2_CAPKIDX);
                                                                     /* (T2_EXPLEN); */
        builder.informaPublicKeyExponent                                (T2_EXP);
                                                                     /* (T2_MODLEN); */
        builder.informaPublicKeyModulus                                 (T2_MOD);
                                                                     /* (T2_CHKSTAT); */
        builder.informaPublicKeyChecksum                                (T2_CHECKSUM);

        return builder.build();
    }

    private static
    TabelaCertificadosRevogados parseCERT(int TAB_LEN, int TAB_ID, int TAB_ACQ, String TAB_DATA)
            throws Exception {
        TabelaCertificadosRevogados.Builder builder = new TabelaCertificadosRevogados.Builder();

        int     TAB_RECIDX      = Integer.parseInt(TAB_DATA.substring(0, 2));
                TAB_DATA        = TAB_DATA.substring(2);

        Log.d(TAG_LOGCAT, "TAB_RECIDX [" + TAB_RECIDX + "]");

        byte[]  T3_RID          = DataUtility.toByteArray(TAB_DATA.substring(0, 10));
                TAB_DATA        = TAB_DATA.substring(10);

        Log.d(TAG_LOGCAT, "T3_RID [" + (new String(T3_RID, UTF_8)) + "]");

        byte[]  T3_CAPKIDX      = DataUtility.toByteArray(TAB_DATA.substring(0, 2));
                TAB_DATA        = TAB_DATA.substring(2 + 2); /* + T2_RUF1 */

        Log.d(TAG_LOGCAT, "T3_CAPKIDX [" + (new String(T3_CAPKIDX, UTF_8)) + "]");

        byte[]  T3_CERTSN       = DataUtility.toByteArray(TAB_DATA.substring(0, 6));
                TAB_DATA        = TAB_DATA.substring(6);

        Log.d(TAG_LOGCAT, "T3_CERTSN [" + (new String(T3_CERTSN, UTF_8)) + "]");

        builder.informaIdentificadorRedeCredenciadora                   (TAB_ACQ);
        builder.informaIndiceRegistroTabela                             (TAB_RECIDX);
        builder.informaRegisteredApplicationProviderIdentifier          (T3_RID);
        builder.informaCertificationAuthorityPublicKeyIndex             (T3_CAPKIDX);
        builder.informaCertificateSerialNumber                          (T3_CERTSN);

        return builder.build();
    }

    public static Bundle tle(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        int     TLI_ACQIDX      = TLI.getTLI_ACQIDX();
        String  TLI_TABVER      = TLI.getTLI_TABVER();
        int     TLR_NREC        = TLR.getTLR_NREC();
        String  TLR_DATA        = TLR.getTLR_DATA().toString();

        TLR.setTLR_DATA(new StringBuilder());
        TLR.setTLR_NREC(0);

        if (TLI_TABVER.equals("0000000000") || TLR_NREC <= 0) {
            output[0].putString(ABECS.RSP_ID,   ABECS.TLE);
            output[0].putInt   (ABECS.RSP_STAT, ABECS.STAT.ST_INVCALL.ordinal());

            return output[0];
        }

        List<TabelaAID>  appl = new ArrayList<>(0);
        List<TabelaCAPK> capk = new ArrayList<>(0);
        List<TabelaCertificadosRevogados> cert = new ArrayList<>(0);

        int i = 0;

        while (i++ < TLR_NREC) {
            int     TAB_LEN     = Integer.parseInt(TLR_DATA.substring(0, 3));
            int     TAB_ID      = Integer.parseInt(TLR_DATA.substring(3, 4));
            int     TAB_ACQ     = Integer.parseInt(TLR_DATA.substring(4, 6));

            String  TAB_DATA    = TLR_DATA.substring(6, TAB_LEN);

            switch (TAB_ID) {
                case 1:
                    appl.add(parseAPPL(TAB_LEN, TAB_ID, TAB_ACQ, TAB_DATA));
                    break;

                case 2:
                    capk.add(parseCAPK(TAB_LEN, TAB_ID, TAB_ACQ, TAB_DATA));
                    break;

                case 3:
                    cert.add(parseCERT(TAB_LEN, TAB_ID, TAB_ACQ, TAB_DATA));
                    break;

                default:
                    /* Nothing to do */
                    break;
            }

            TLR_DATA = TLR_DATA.substring(TAB_LEN);
        }

        EntradaComandoTableLoad entradaComandoTableLoad =
                new EntradaComandoTableLoad(TLI_ACQIDX, TLI_TABVER, appl, capk, cert);

        getPinpad().tableLoad(entradaComandoTableLoad, response -> {
            ABECS.STAT status = ManufacturerUtility.toSTAT(response);

            output[0].putString(ABECS.RSP_ID,   ABECS.TLE);
            output[0].putInt   (ABECS.RSP_STAT, status.ordinal());

            semaphore[0].release();
        });

        semaphore[0].acquireUninterruptibly();

        Log.d(TAG_LOGCAT, ABECS.TLE + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");

        return output[0];
    }
}
