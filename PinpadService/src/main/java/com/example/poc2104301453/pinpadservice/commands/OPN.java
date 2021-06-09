package com.example.poc2104301453.pinpadservice.commands;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.pinpadlibrary.ABECS;
import com.example.poc2104301453.pinpadlibrary.utilities.DataUtility;

import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.InterfaceUsuarioPinpad;
import br.com.verifone.bibliotecapinpad.definicoes.CodigosRetorno;
import br.com.verifone.bibliotecapinpad.definicoes.LedsContactless;
import br.com.verifone.bibliotecapinpad.definicoes.NotificacaoCapturaPin;
import br.com.verifone.bibliotecapinpad.definicoes.TipoNotificacao;
import br.com.verifone.bibliotecapinpad.entradas.EntradaComandoOpen;

import static com.example.poc2104301453.pinpadlibrary.ABECS.KEY_ENUM.*;

public class OPN {
    private static final String TAG_LOGCAT = OPN.class.getSimpleName();

    public static Bundle opn(Bundle input)
            throws Exception {
        String CMD_ID  = input.getString(ABECS.KEY_ENUM.REQUEST.getValue());
        String OPN_MOD = input.getString("OPN_MOD");
        String OPN_EXP = input.getString("OPN_EXP");

        Log.d(TAG_LOGCAT, "opn::CMD_ID [" + CMD_ID + "]");

        if (OPN_MOD != null || OPN_EXP != null) {
            if (OPN_MOD == null || OPN_EXP == null) {
                Log.e(TAG_LOGCAT, "Mandatory key(s) \"" + "OPN_MOD and/or OPN_EXP" + "\" not found.");
            }

            Log.w(TAG_LOGCAT, "ABECS OPN pending development. Using classic OPN instead.");
        }

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
            public void menu(br.com.verifone.bibliotecapinpad.definicoes.Menu menu) {
                Log.d(TAG_LOGCAT, "menu::menu [" + menu + "]");
            }

            @Override
            public void ledsProcessamentoContactless(LedsContactless ledsContactless) {
                Log.d(TAG_LOGCAT, "ledsProcessamentoContactless::ledsContactless [" + ledsContactless + "]");
            }
        };

        EntradaComandoOpen entradaComandoOpen = new EntradaComandoOpen(callback);

        final Bundle output[] = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        AcessoFuncoesPinpad pinpad = com.example.poc2104301453.pinpadservice.ABECS.getInstance().getPinpad();

        pinpad.open(entradaComandoOpen, new EntradaComandoOpen.OpenCallback() {
            @Override
            public void comandoOpenEncerrado(CodigosRetorno codigosRetorno) {
                output[0].putInt(STATUS.getValue(), codigosRetorno.ordinal());

                semaphore[0].release();
            }
        });

        semaphore[0].acquireUninterruptibly();

        return output[0];
    }
}
