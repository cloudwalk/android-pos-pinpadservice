package io.cloudwalk.pos.pinpadservice.utilities;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import br.com.setis.sunmi.bibliotecapinpad.definicoes.CodigosRetorno;
import br.com.setis.sunmi.bibliotecapinpad.saidas.SaidaComandoGetCard;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.utilities.DataUtility;

public class ManufacturerUtility {
    private static final String TAG_LOGCAT = ManufacturerUtility.class.getSimpleName();

    private ManufacturerUtility() {
        /* Nothing to do */
    }

    public static ABECS.STAT toSTAT(@NotNull CodigosRetorno input) {
        Log.d(TAG_LOGCAT, "toSTAT");

        switch (input) {
            case OK:
                return ABECS.STAT.ST_OK;
            case CHAMADA_INVALIDA:
                return ABECS.STAT.ST_INVCALL;
            case PARAMETRO_INVALIDO:
                return ABECS.STAT.ST_INVPARM;
            case TIMEOUT:
                return ABECS.STAT.ST_TIMEOUT;

            case OPERACAO_CANCELADA:
            case OPERACAO_ABORTADA:
                return ABECS.STAT.ST_CANCEL;

            case PINPAD_NAO_INICIALIZADO:
                return ABECS.STAT.PP_NOTOPEN;
            case MODELO_INVALIDO:
                return ABECS.STAT.PP_INVMODEL;
            case OPERACAO_NAO_SUPORTADA:
                return ABECS.STAT.PP_NOFUNC;
            case TABELAS_EXPIRADAS:
                return ABECS.STAT.ST_TABVERDIF;
            case ERRO_GRAVACAO_TABELAS:
                return ABECS.STAT.ST_TABERR;
            case ERRO_LEITURA_CARTAO_MAG:
                return ABECS.STAT.ST_MCDATAERR;
            case CHAVE_PIN_AUSENTE:
                return ABECS.STAT.ST_ERRKEY;
            case CARTAO_AUSENTE:
                return ABECS.STAT.ST_NOCARD;
            case PINPAD_OCUPADO:
                return ABECS.STAT.ST_PINBUSY;
            case ERRO_MODULO_SAM:
                return ABECS.STAT.PP_SAMERR;
            case SAM_AUSENTE:
                return ABECS.STAT.PP_NOSAM;
            case SAM_INVALIDO:
                return ABECS.STAT.PP_SAMINV;
            case CARTAO_MUDO:
                return ABECS.STAT.ST_DUMBCARD;
            case ERRO_COMUNICACAO_CARTAO:
                return ABECS.STAT.ST_ERRCARD;
            case CARTAO_INVALIDADO:
                return ABECS.STAT.ST_CARDINVALIDAT;
            case CARTAO_COM_PROBLEMAS:
                return ABECS.STAT.ST_CARDPROBLEMS;
            case CARTAO_COM_DADOS_INVALIDOS:
                return ABECS.STAT.ST_CARDINVDATA;
            case CARTAO_SEM_APLICACAO:
                return ABECS.STAT.ST_CARDAPPNAV;
            case APLICACAO_NAO_UTILIZADA:
                return ABECS.STAT.ST_CARDAPPNAUT;
            case ERRO_FALLBACK:
                return ABECS.STAT.ST_ERRFALLBACK;
            case VALOR_INVALIDO:
                return ABECS.STAT.ST_INVAMOUNT;
            case EXCEDE_CAPACIDADE_AID:
                return ABECS.STAT.ST_ERRMAXAID;
            case CARTAO_BLOQUEADO:
                return ABECS.STAT.ST_CARDBLOCKED;
            case MULTIPLOS_CTLSS:
                return ABECS.STAT.ST_CTLSMULTIPLE;
            case ERRO_COMUNICACAO_CTLSS:
                return ABECS.STAT.ST_CTLSCOMMERR;
            case CTLSS_INVALIDADO:
                return ABECS.STAT.ST_CTLSINVALIDAT;
            case CTLSS_COM_PROBLEMAS:
                return ABECS.STAT.ST_CTLSPROBLEMS;
            case CTLSS_SEM_APLICACAO:
                return ABECS.STAT.ST_CTLSAPPNAV;
            case CTLSS_APLICACAO_NAO_SUPORTADA:
                return ABECS.STAT.ST_CTLSAPPNAUT;
            case CTLSS_DISPOSITIVO_EXTERNO:
                return ABECS.STAT.ST_CTLSEXTCVM;
            case CTLSS_MUDA_INTERFACE:
                return ABECS.STAT.ST_CTLSIFCHG;

            default:
                return ABECS.STAT.ST_INTERR;
        }
    }

    public static String getPP_PAN(@NotNull String SPE_PANMASK,
                                   @NotNull SaidaComandoGetCard.DadosCartao data) {
        Log.d(TAG_LOGCAT, "getPP_PAN");

        try {
            int LL = Integer.parseInt(SPE_PANMASK.substring(0, 2));
            int RR = Integer.parseInt(SPE_PANMASK.substring(2, 4));

            String PP_PAN  = data.obtemPan();
            int    track   = 1;

            String PP_TRK;

            if (PP_PAN == null) {
                PP_TRK = data.obtemTrilha1();

                if (PP_TRK == null) {
                    PP_TRK = data.obtemTrilha2();
                    track++;
                }

                if (PP_TRK != null) {
                    char separator = (track != 1) ? '=' : '^';

                    if (PP_TRK.contains("" + separator) && PP_TRK.length() > 1) {
                        PP_PAN = PP_TRK.substring((track != 1) ? 0 : 1, PP_TRK.indexOf(separator));
                    }
                }
            }

            if (PP_PAN != null) {
                return DataUtility.mask(PP_PAN, LL, RR);
            }
        } catch (Exception exception) {
            Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
        }

        return null;
    }

    public static String getPP_TRKx(@NotNull String SPE_PANMASK,
                                    @NotNull SaidaComandoGetCard.DadosCartao data,
                                    boolean truncate, int track) {
        Log.d(TAG_LOGCAT, "getPP_TRKx");

        String PP_TRK;
        String PP_TRKINC;

        switch (track) {
            case 1:
                PP_TRK = data.obtemTrilha1();

                if (PP_TRK != null) {
                    if (PP_TRK.length() <= 79) {
                        break;
                    }

                    StringBuilder TRK = new StringBuilder();

                    for (int i = 0, j = 2; i < PP_TRK.length(); i += 2) {
                        TRK.append(
                                String.format(
                                        Locale.getDefault(), "%c",
                                        Integer.parseInt(PP_TRK.substring(i, i + j), 16)));
                    }

                    PP_TRK = TRK.toString();
                }
                break;

            case 2:
                PP_TRK = data.obtemTrilha2();
                break;

            case 3:
                PP_TRK = data.obtemTrilha3();
                break;

            default:
                return null;
        }

        if (PP_TRK != null) {
            PP_TRKINC = PP_TRK.substring(0, Math.min(PP_TRK.length(), 19));

            switch (track) {
                case 1:
                case 2:
                    char separator = (track != 1) ? '=' : '^';

                    if (truncate) {
                        if (PP_TRK.contains("" + separator)) {
                            PP_TRK = PP_TRK.substring(0, Math.min(PP_TRK.lastIndexOf(separator) + 7, PP_TRK.length()));
                        } else {
                            PP_TRK = PP_TRKINC;
                        }
                    }

                    String PP_PAN = getPP_PAN("9999", data);

                    if (PP_PAN != null) {
                        int index = PP_TRK.indexOf(PP_PAN);

                        if (index >= 0) {
                            PP_PAN = getPP_PAN(SPE_PANMASK, data);

                            PP_TRK = PP_TRK.substring(0, index) + PP_PAN + PP_TRK.substring(PP_PAN.length() + 1);
                        }
                    }
                    break;

                default:
                    return (truncate) ? PP_TRKINC : PP_TRK;
            }
        }

        return PP_TRK;
    }
}
