package io.cloudwalk.pos.pinpadservice.commands;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.utilities.DataUtility;
import io.cloudwalk.pos.pinpadservice.PinpadAbstractionLayer;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;
import io.cloudwalk.pos.pinpadservice.utilities.ManufacturerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.entradas.EntradaComandoGetInfoEx;
import br.com.verifone.bibliotecapinpad.saidas.SaidaComandoGetInfo;
import br.com.verifone.bibliotecapinpad.saidas.SaidaComandoGetInfoEx;

import static android.content.Context.ACTIVITY_SERVICE;
import static br.com.verifone.bibliotecapinpad.entradas.EntradaComandoGetInfoEx.TipoInfo.*;

public class GIX {
    private static final String TAG_LOGCAT = GIX.class.getSimpleName();

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadManager.getInstance().getPinpad();
    }

    private static Bundle parseRSP(SaidaComandoGetInfoEx response) {
        Bundle output = new Bundle();

        switch (response.obtemTipoInformacao()) {
            case INFO_GERAL:
                break;

            case INFO_VERSAO_TABELAS_EMV:
                try {
                    List<String> list = response
                            .obtemInformacoes(SaidaComandoGetInfoEx.VersaoTabelasEMV.class)
                            .obtemVersaoTabelasEMV();

                    for (int i = 0; i < list.size(); i++) {
                        String key = ABECS.PP_TABVERnn
                                .replace("nn", String.format(Locale.getDefault(), "%02d", i));

                        output.putString(key, list.get(i));
                    }
                } catch (Exception exception) {
                    Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
                }

                return output;

            case INFO_KEYMAP_MK3DES_DATA:
            case INFO_KEYMAP_MK3DES_PIN:
            case INFO_KEYMAP_DUKPT3DES_DATA:
            case INFO_KEYMAP_DUKPT3DES_PIN:
                try {
                    List<SaidaComandoGetInfoEx.InformacaoChave> list = response
                            .obtemInformacoes(SaidaComandoGetInfoEx.MapaDeChaves.class)
                            .obtemListaMapaChaves();

                    StringBuilder map = new StringBuilder();

                    for (int i = 0; i < list.size(); i++) {
                        switch (list.get(i).obtemStatusChave()) {
                            case CHAVE_AUSENTE:
                                map.append('0');
                                break;

                            case CHAVE_PRESENTE:
                                String key = null;

                                switch (response.obtemTipoInformacao()) {
                                    case INFO_KEYMAP_DUKPT3DES_DATA:
                                        if (key == null) {
                                            key = ABECS.PP_KSNTDESDnn;
                                        }
                                        /* no break */

                                    case INFO_KEYMAP_DUKPT3DES_PIN:
                                        if (key == null) {
                                            key = ABECS.PP_KSNTDESPnn;
                                        }

                                        key = key.replace("nn", String.format(Locale.getDefault(), "%02d", i));

                                        byte[] PP_KSNTDESxnn = list.get(i).obtemKSN();

                                        if (PP_KSNTDESxnn != null) {
                                            map.append('1');

                                            output.putString(key, DataUtility.toHex(PP_KSNTDESxnn));
                                        } else {
                                            map.append('0');
                                        }
                                        break;

                                    default:
                                        map.append('1');
                                        break;
                                }
                                break;

                            default:
                                map.append('2');
                                break;
                        }
                    }

                    switch (response.obtemTipoInformacao()) {
                        case INFO_KEYMAP_MK3DES_DATA:
                            output.putString(ABECS.PP_MKTDESD,   map.toString());
                            break;

                        case INFO_KEYMAP_MK3DES_PIN:
                            output.putString(ABECS.PP_MKTDESP,   map.toString());
                            break;

                        case INFO_KEYMAP_DUKPT3DES_DATA:
                            output.putString(ABECS.PP_DKPTTDESD, map.toString());
                            break;

                        case INFO_KEYMAP_DUKPT3DES_PIN:
                            output.putString(ABECS.PP_DKPTTDESP, map.toString());
                            break;

                        default:
                            /* Nothing to do */
                            break;
                    }
                } catch (Exception exception) {
                    Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
                }

                return output;

            default:
                Log.e(TAG_LOGCAT, "response.obtemTipoInformacao() [" + response.obtemTipoInformacao() + "]");

                return output;
        }

        SaidaComandoGetInfo data = response.obtemInformacoes(SaidaComandoGetInfo.class);

        output.putString(ABECS.PP_SERNUM,       data.obtemNumeroSeriePinpad());
        output.putString(ABECS.PP_PARTNBR,      data.obtemPartNumberPinpad());
        output.putString(ABECS.PP_MODEL,        data.obtemModeloPinpad());

        char   contactless = data.obtemCapacidadesPinpad().suportaContactless() ? '1' : '0';

        String PP_CAPAB = "x200000000";
               PP_CAPAB = PP_CAPAB.replace('x', contactless);

        output.putString(ABECS.PP_CAPAB,        PP_CAPAB);

        output.putString(ABECS.PP_SOVER,        data.obtemVersaoSistemaOperacionalPinpad());
        output.putString(ABECS.PP_SPECVER,      data.obtemVersaoEspecificacao());
        output.putString(ABECS.PP_MANVERS,      data.obtemVersaoAplicacaoGerenciadora());
        output.putString(ABECS.PP_APPVERS,      data.obtemVersaoAplicacaoAbecs());
        output.putString(ABECS.PP_GENVERS,      data.obtemVersaoAplicacaoExtensao());
        output.putString(ABECS.PP_KRNLVER,      data.obtemVersaoKernelEMV());
        output.putString(ABECS.PP_CTLSVER,      data.obtemVersaoKernelCtls());
        output.putString(ABECS.PP_MCTLSVER,     data.obtemVersaoKernelCtlsMasterPayPass());
        output.putString(ABECS.PP_VCTLSVER,     data.obtemVersaoKernelCtlsVisaPayWave());
        output.putString(ABECS.PP_AECTLSVER,    data.obtemVersaoKernelCtlsAmex());
        output.putString(ABECS.PP_DPCTLSVER,    data.obtemVersaoKernelCtlsDiscover());
        output.putString(ABECS.PP_PUREVER,      data.obtemVersaoKernelCtlsPure());

        output.putString(ABECS.PP_TLRMEM,       getPP_TLRMEM());

        /*
         * "N/A" property list:
         *
         * - ABECS.PP_DSPTXTSZ  - ABECS.PP_DSPGRSZ  - ABECS.PP_MFSUP
         *
         * - ABECS.PP_BIGRAND
         */

        for (String key : output.keySet()) {
            if (output.get(key) == null) {
                output.putString(key, "");
            }
        }

        return output;
    }

    private static String getPP_TLRMEM() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());

        long flash  = (statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong());
        flash /= (1024 * 1024);

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

        Context context = DataUtility.getApplicationContext();

        ((ActivityManager) context
                .getSystemService(ACTIVITY_SERVICE))
                .getMemoryInfo(memoryInfo);

        long PP_TLRMEM = Math.min(memoryInfo.availMem / 0x100000L, flash) * 1000000;

        if (PP_TLRMEM > 4294967295L) { /* 0xFFFFFFFF */
            PP_TLRMEM = 4294967295L;
        }

        String output = Long.toHexString(PP_TLRMEM).toUpperCase(Locale.getDefault());

        if (output.length() < 8) {
            return "00000000".substring(0, 8 - output.length()) + output;
        } else {
            return output;
        }
    }

    public static Bundle gix(Bundle input)
            throws Exception {
        final long overhead = SystemClock.elapsedRealtime();

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { null };

        List<EntradaComandoGetInfoEx.TipoInfo> typeList = new ArrayList<>(6);

        switch (input.getString(ABECS.CMD_ID)) {
            case ABECS.GIN:
                typeList.add(INFO_GERAL);
                break;

            case ABECS.GTS:
                typeList.add(INFO_VERSAO_TABELAS_EMV);
                break;

            default:
                typeList.add(INFO_GERAL);
                typeList.add(INFO_VERSAO_TABELAS_EMV);

                typeList.add(INFO_KEYMAP_MK3DES_DATA);
                typeList.add(INFO_KEYMAP_MK3DES_PIN);

                typeList.add(INFO_KEYMAP_DUKPT3DES_DATA);
                typeList.add(INFO_KEYMAP_DUKPT3DES_PIN);
                break;
        }

        semaphore[0] = new Semaphore((typeList.size() - 1) * -1, true);

        long timestamp = SystemClock.elapsedRealtime();

        for (EntradaComandoGetInfoEx.TipoInfo type : typeList) {
            getPinpad().getInfoEx(new EntradaComandoGetInfoEx(type, -1), response -> {
                    ABECS.STAT status = ManufacturerUtility.toSTAT(response.obtemResultadoOperacao());

                    output[0].putString (ABECS.RSP_ID,   ABECS.GIX);
                    output[0].putInt    (ABECS.RSP_STAT, status.ordinal());

                    try {
                        if (status != ABECS.STAT.ST_OK) {
                            return;
                        }

                        output[0].putAll(parseRSP(response));
                    } finally {
                        semaphore[0].release();
                    }
                });
        }

        semaphore[0].acquireUninterruptibly();

        timestamp = SystemClock.elapsedRealtime() - timestamp;

        Log.d(TAG_LOGCAT, ABECS.GIX + "::timestamp [" + timestamp + "ms] [" + ((SystemClock.elapsedRealtime() - overhead) - timestamp) + "ms]");

        return output[0];
    }
}
