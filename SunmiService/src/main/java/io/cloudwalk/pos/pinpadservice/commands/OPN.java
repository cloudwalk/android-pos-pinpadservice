package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.utilities.ManufacturerUtility;
import io.cloudwalk.pos.pinpadservice.PinpadAbstractionLayer;

import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.setis.sunmi.bibliotecapinpad.InterfaceUsuarioPinpad;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.LedsContactless;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.Menu;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.NotificacaoCapturaPin;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.TipoNotificacao;
import br.com.setis.sunmi.bibliotecapinpad.entradas.EntradaComandoOpen;

public class OPN {
    private static final String TAG_LOGCAT = OPN.class.getSimpleName();

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadAbstractionLayer.getInstance().getPinpad();
    }

    public static Bundle opn(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        InterfaceUsuarioPinpad callback = new InterfaceUsuarioPinpad() {
            @Override
            public void mensagemNotificacao(String s, TipoNotificacao tipoNotificacao) {
                Log.d(TAG_LOGCAT, "mensagemNotificacao::s [" + s + "], tipoNotificacao [" + tipoNotificacao + "]");
            }

            @Override
            public void notificacaoCapturaPin(NotificacaoCapturaPin notificacaoCapturaPin) {
                Log.d(TAG_LOGCAT, "notificacaoCapturaPin::notificacaoCapturaPin [" + notificacaoCapturaPin + "]");
            }

            @Override
            public void menu(Menu menu) {
                Log.d(TAG_LOGCAT, "menu::menu [" + menu + "]");
            }

            @Override
            public void ledsProcessamentoContactless(LedsContactless ledsContactless) {
                Log.d(TAG_LOGCAT, "ledsProcessamentoContactless::ledsContactless [" + ledsContactless + "]");
            }
        };

        EntradaComandoOpen entradaComandoOpen = new EntradaComandoOpen(callback);

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        getPinpad().open(entradaComandoOpen, response -> {
            ABECS.STAT status = ManufacturerUtility.toSTAT(response);

            output[0].putString(ABECS.RSP_ID,   ABECS.OPN);
            output[0].putInt   (ABECS.RSP_STAT, status.ordinal());

            semaphore[0].release();
        });

        semaphore[0].acquireUninterruptibly();

        Log.d(TAG_LOGCAT, ABECS.OPN + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");

        return output[0];
    }
}
