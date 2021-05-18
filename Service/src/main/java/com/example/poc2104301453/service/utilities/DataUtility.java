package com.example.poc2104301453.service.utilities;

import android.util.Log;
import android.util.Pair;

import com.example.poc2104301453.library.ABECS;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import br.com.verifone.bibliotecapinpad.definicoes.CodigosRetorno;

import static br.com.verifone.bibliotecapinpad.definicoes.CodigosRetorno.*;
import static com.example.poc2104301453.library.ABECS.RSP_STAT.*;

public class DataUtility {
    private static final String TAG_LOGCAT = DataUtility.class.getSimpleName();

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    private static final DataUtility sDataUtility = new DataUtility();

    /**
     * Constructor.
     */
    private DataUtility() {
        /* Nothing to do */
    }

    /**
     * @return {@link DataUtility}
     */
    public static DataUtility getInstance() {
        Log.d(TAG_LOGCAT, "getInstance");

        return sDataUtility;
    }

    /**
     * See <a href="https://bit.ly/2RWydoS">https://bit.ly/2RWydoS</a>
     *
     * @param input {@link byte} array
     * @return {@link String}
     */
    public String toHex(@NotNull byte[] input) {
        Log.d(TAG_LOGCAT, "toHex");

        byte[] output = new byte[input.length * 2];

        for (int j = 0; j < input.length; j++) {
            int value = input[j] & 0xFF;
            output[j * 2] = HEX_ARRAY[value >>> 4];
            output[j * 2 + 1] = HEX_ARRAY[value & 0x0F];
        }

        return new String(output, StandardCharsets.UTF_8);
    }

    /**
     * Translates the manufacturer return codes to those specified in the ABECS documentation.
     *
     * @param input {@link CodigosRetorno}
     * @return {@link int} (see {@link ABECS.RSP_STAT}
     */
    public int toInt(@NotNull CodigosRetorno input) {
        Log.d(TAG_LOGCAT, "toInt::input [" + input + "]");

        List<Pair<CodigosRetorno, ABECS.RSP_STAT>> list = new ArrayList<>(0);

        /*
         * Spec. 2.12
         */

        list.add(new Pair<>(OK, ST_OK));
        list.add(new Pair<>(CHAMADA_INVALIDA, ST_INVCALL));
        list.add(new Pair<>(PARAMETRO_INVALIDO, ST_INVPARM));
        list.add(new Pair<>(TIMEOUT, ST_TIMEOUT));
        list.add(new Pair<>(OPERACAO_CANCELADA, ST_CANCEL));
        list.add(new Pair<>(TABELAS_EXPIRADAS, ST_TABVERDIF));
        list.add(new Pair<>(ERRO_GRAVACAO_TABELAS, ST_TABERR));
        list.add(new Pair<>(ERRO_LEITURA_CARTAO_MAG, ST_MCDATAERR));
        list.add(new Pair<>(CHAVE_PIN_AUSENTE, ST_ERRKEY));
        list.add(new Pair<>(CARTAO_AUSENTE, ST_NOCARD));
        list.add(new Pair<>(PINPAD_OCUPADO, ST_PINBUSY));
        list.add(new Pair<>(CARTAO_MUDO, ST_DUMBCARD));
        list.add(new Pair<>(ERRO_COMUNICACAO_CARTAO, ST_ERRCARD));
        list.add(new Pair<>(CARTAO_INVALIDADO, ST_CARDINVALIDAT));
        list.add(new Pair<>(CARTAO_COM_PROBLEMAS, ST_CARDPROBLEMS));
        list.add(new Pair<>(CARTAO_COM_DADOS_INVALIDOS, ST_CARDINVDATA));
        list.add(new Pair<>(CARTAO_SEM_APLICACAO, ST_CARDAPPNAV));
        list.add(new Pair<>(APLICACAO_NAO_UTILIZADA, ST_CARDAPPNAUT));
        list.add(new Pair<>(ERRO_FALLBACK, ST_ERRFALLBACK));
        list.add(new Pair<>(VALOR_INVALIDO, ST_INVAMOUNT));
        list.add(new Pair<>(EXCEDE_CAPACIDADE_AID, ST_ERRMAXAID));
        list.add(new Pair<>(CARTAO_BLOQUEADO, ST_CARDBLOCKED));
        list.add(new Pair<>(MULTIPLOS_CTLSS, ST_CTLSMULTIPLE));
        list.add(new Pair<>(ERRO_COMUNICACAO_CTLSS, ST_CTLSCOMMERR));
        list.add(new Pair<>(CTLSS_INVALIDADO, ST_CTLSINVALIDAT));
        list.add(new Pair<>(CTLSS_COM_PROBLEMAS, ST_CTLSPROBLEMS));
        list.add(new Pair<>(CTLSS_SEM_APLICACAO, ST_CTLSAPPNAV));
        list.add(new Pair<>(CTLSS_APLICACAO_NAO_SUPORTADA, ST_CTLSAPPNAUT));
        list.add(new Pair<>(CTLSS_DISPOSITIVO_EXTERNO, ST_CTLSEXTCVM));
        list.add(new Pair<>(CTLSS_MUDA_INTERFACE, ST_CTLSIFCHG));
        list.add(new Pair<>(ERRO_INTERNO, ST_INTERR));
        list.add(new Pair<>(OPERACAO_ABORTADA, ST_CANCEL));

        /*
         * Spec. 1.08a
         */

        list.add(new Pair<>(PINPAD_NAO_INICIALIZADO, PP_NOTOPEN));
        list.add(new Pair<>(MODELO_INVALIDO, PP_INVMODEL));
        list.add(new Pair<>(OPERACAO_NAO_SUPORTADA, PP_NOFUNC));
        list.add(new Pair<>(ERRO_MODULO_SAM, PP_SAMERR));
        list.add(new Pair<>(SAM_AUSENTE, PP_NOSAM));
        list.add(new Pair<>(SAM_INVALIDO, PP_SAMINV));

        for (Pair<CodigosRetorno, ABECS.RSP_STAT> item : list) {
            if (item.first == input) {
                return item.second.getValue();
            }
        }

        return ST_INTERR.getValue();
    }
}
