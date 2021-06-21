package com.example.poc2104301453.pinpadservice.commands;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.pinpadlibrary.ABECS;
import com.example.poc2104301453.pinpadservice.PinpadAbstractionLayer;
import com.example.poc2104301453.pinpadservice.utilities.ManufacturerUtility;

import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.InterfaceUsuarioPinpad;
import br.com.verifone.bibliotecapinpad.definicoes.LedsContactless;
import br.com.verifone.bibliotecapinpad.definicoes.Menu;
import br.com.verifone.bibliotecapinpad.definicoes.NotificacaoCapturaPin;
import br.com.verifone.bibliotecapinpad.definicoes.TipoNotificacao;
import br.com.verifone.bibliotecapinpad.entradas.EntradaComandoOpen;

public class OPN {
    private static final String TAG_LOGCAT = OPN.class.getSimpleName();

    public static Bundle opn(Bundle input)
            throws Exception {
        AcessoFuncoesPinpad pinpad = PinpadAbstractionLayer.getInstance().getPinpad();
        String CMD_ID  = input.getString(ABECS.CMD_ID);

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

        pinpad.open(entradaComandoOpen, codigosRetorno -> {
            output[0].putString(ABECS.RSP_ID, CMD_ID);
            output[0].putInt   (ABECS.RSP_STAT, ManufacturerUtility.toSTAT(codigosRetorno).ordinal());

            semaphore[0].release();
        });

        semaphore[0].acquireUninterruptibly();

        return output[0];
    }
}