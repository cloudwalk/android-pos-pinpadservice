package com.example.poc2104301453.pinpadservice.commands;

import android.os.Bundle;

import com.example.poc2104301453.pinpadlibrary.ABECS;
import com.example.poc2104301453.pinpadservice.PinpadAbstractionLayer;
import com.example.poc2104301453.pinpadservice.utilities.ManufacturerUtility;

import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;

public class GIN {
    private static final String TAG_LOGCAT = GIN.class.getSimpleName();

    public static Bundle gin(Bundle input)
            throws Exception {
        AcessoFuncoesPinpad pinpad = PinpadAbstractionLayer.getInstance().getPinpad();
        String CMD_ID = input.getString(ABECS.CMD_ID);

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        pinpad.getInfo(saidaComandoGetInfo -> {
            ABECS.STAT status = ManufacturerUtility.toSTAT(saidaComandoGetInfo.obtemResultadoOperacao());

            output[0].putString(ABECS.RSP_ID, CMD_ID);
            output[0].putInt   (ABECS.RSP_STAT, status.ordinal());

            try {
                if (status != ABECS.STAT.ST_OK) {
                    return;
                }

                String GIN_MNAME    = saidaComandoGetInfo.obtemFabricantePinpad();
                String GIN_MODEL    = saidaComandoGetInfo.obtemModeloPinpad();
                String GIN_CTLSSUP  = (saidaComandoGetInfo.obtemCapacidadesPinpad().suportaContactless()) ? "C" : " ";
                String GIN_SOVER    = saidaComandoGetInfo.obtemVersaoSistemaOperacionalPinpad();
                String GIN_SPECVER  = saidaComandoGetInfo.obtemVersaoEspecificacao();
                String GIN_MANVER   = saidaComandoGetInfo.obtemVersaoAplicacaoGerenciadora();
                String GIN_SERNUM   = saidaComandoGetInfo.obtemNumeroSeriePinpad();

                String GIN_ACQNAM   = saidaComandoGetInfo.obtemNomeAdquirente();
                       GIN_ACQNAM   = (GIN_ACQNAM != null) ? GIN_ACQNAM : "ABECS";

                String GIN_APPVERS  = saidaComandoGetInfo.obtemVersaoAplicacaoAbecs();

                int    GIN_ACQIDX   = input.getInt(ABECS.GIN_ACQIDX);

                switch (GIN_ACQIDX) {
                    case 0:
                        output[0].putString(ABECS.GIN_MNAME, GIN_MNAME);
                        output[0].putString(ABECS.GIN_MODEL, GIN_MODEL);
                        output[0].putString(ABECS.GIN_CTLSSUP, GIN_CTLSSUP);
                        output[0].putString(ABECS.GIN_SOVER, GIN_SOVER);
                        output[0].putString(ABECS.GIN_MANVER, GIN_MANVER);
                        output[0].putString(ABECS.GIN_SERNUM, GIN_SERNUM);
                        break;

                    default:
                        output[0].putString(ABECS.GIN_ACQNAM, GIN_ACQNAM);
                        output[0].putString(ABECS.GIN_APPVERS, GIN_APPVERS);
                        break;
                }

                output[0].putString(ABECS.GIN_SPECVER, GIN_SPECVER);
            } finally {
                semaphore[0].release();
            }
        });

        semaphore[0].acquireUninterruptibly();

        return output[0];
    }
}
