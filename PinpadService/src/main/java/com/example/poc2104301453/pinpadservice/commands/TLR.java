package com.example.poc2104301453.pinpadservice.commands;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.pinpadlibrary.ABECS;
import com.example.poc2104301453.pinpadlibrary.exceptions.MissingArgumentException;
import com.example.poc2104301453.pinpadservice.PinpadAbstractionLayer;
import com.example.poc2104301453.pinpadservice.utilities.ManufacturerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.definicoes.TabelaAID;
import br.com.verifone.bibliotecapinpad.definicoes.TabelaCAPK;
import br.com.verifone.bibliotecapinpad.definicoes.TabelaCertificadosRevogados;
import br.com.verifone.bibliotecapinpad.entradas.EntradaComandoTableLoad;

public class TLR {
    private static final String TAG_LOGCAT = TLR.class.getSimpleName();

    private static TabelaAID parseAPPL(int TAB_LEN, int TAB_ID, int TAB_ACQ,
                                       String TAB_DATA)
            throws Exception {
        TabelaAID.Builder builder = new TabelaAID.Builder();

        return builder.build();
    }

    private static TabelaCAPK parseCAPK(int TAB_LEN, int TAB_ID, int TAB_ACQ,
                                        String TAB_DATA)
            throws Exception {
        TabelaCAPK.Builder builder = new TabelaCAPK.Builder();

        return builder.build();
    }

    private static TabelaCertificadosRevogados
            parseCERT(int TAB_LEN, int TAB_ID, int TAB_ACQ, String TAB_DATA)
            throws Exception {
        TabelaCertificadosRevogados.Builder builder = new TabelaCertificadosRevogados.Builder();

        return builder.build();
    }

    public static Bundle tlr(Bundle input)
            throws Exception {
        AcessoFuncoesPinpad pinpad = PinpadAbstractionLayer.getInstance().getPinpad();
        String CMD_ID = input.getString(ABECS.CMD_ID);

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        int     TLI_ACQIDX      = input.getInt   (ABECS.TLI_ACQIDX, -1);
        String  TLI_TABVER      = input.getString(ABECS.TLI_TABVER);
        int     TLR_NREC        = input.getInt   (ABECS.TLR_NREC, -1);
        String  TLR_DATA        = input.getString(ABECS.TLR_DATA);

        if ((TLI_ACQIDX == -1) || (TLI_TABVER == null) || (TLR_NREC == -1)
                || (TLR_DATA == null)) {
            throw new MissingArgumentException();
        }

        List<TabelaAID>  appl = new ArrayList<>(TLR_NREC);
        List<TabelaCAPK> capk = new ArrayList<>(TLR_NREC);
        List<TabelaCertificadosRevogados> cert = new ArrayList<>(TLR_NREC);

        int i = 0;

        while (i++ < TLR_NREC) {
            int     TAB_LEN     = Integer.parseInt(TLR_DATA.substring(0, 3));
            int     TAB_ID      = Integer.parseInt(TLR_DATA.substring(3, 4));
            int     TAB_ACQ     = Integer.parseInt(TLR_DATA.substring(4, 6));

            String  TAB_DATA    = TLR_DATA.substring(6, TAB_LEN - 3 + 1);

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

        pinpad.tableLoad(entradaComandoTableLoad, codigosRetorno -> {
            ABECS.STAT status = ManufacturerUtility.toSTAT(codigosRetorno);

            output[0].putString(ABECS.RSP_ID, CMD_ID);
            output[0].putInt   (ABECS.RSP_STAT, status.ordinal());

            semaphore[0].release();
        });

        semaphore[0].acquireUninterruptibly();

        return output[0];
    }
}
