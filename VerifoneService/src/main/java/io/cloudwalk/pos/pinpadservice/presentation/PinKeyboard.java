package io.cloudwalk.pos.pinpadservice.presentation;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import br.com.verifone.bibliotecapinpad.definicoes.IdentificacaoTeclaPIN;
import io.cloudwalk.pos.pinpadservice.R;

public class PinKeyboard extends LinearLayout implements View.OnClickListener {
    private static final String TAG_LOGCAT = PinKeyboard.class.getSimpleName();

    public PinKeyboard(Context context) {
        this(context, null, 0, 0);
    }

    public PinKeyboard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public PinKeyboard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PinKeyboard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.default_keyboard_pin, this, true);
    }

    /**
     *
     * @return
     */
    public Map<IdentificacaoTeclaPIN, Integer> getPINViewMap() {
        Map<IdentificacaoTeclaPIN, Integer> map = new HashMap<>();

        map.put(IdentificacaoTeclaPIN.TECLA_0,          R.id.keyboard_custom_btn0);
        map.put(IdentificacaoTeclaPIN.TECLA_1,          R.id.keyboard_custom_btn1);
        map.put(IdentificacaoTeclaPIN.TECLA_2,          R.id.keyboard_custom_btn2);
        map.put(IdentificacaoTeclaPIN.TECLA_3,          R.id.keyboard_custom_btn3);
        map.put(IdentificacaoTeclaPIN.TECLA_4,          R.id.keyboard_custom_btn4);
        map.put(IdentificacaoTeclaPIN.TECLA_5,          R.id.keyboard_custom_btn5);
        map.put(IdentificacaoTeclaPIN.TECLA_6,          R.id.keyboard_custom_btn6);
        map.put(IdentificacaoTeclaPIN.TECLA_7,          R.id.keyboard_custom_btn7);
        map.put(IdentificacaoTeclaPIN.TECLA_8,          R.id.keyboard_custom_btn8);
        map.put(IdentificacaoTeclaPIN.TECLA_9,          R.id.keyboard_custom_btn9);
        map.put(IdentificacaoTeclaPIN.TECLA_CONFIRMA,   R.id.keyboard_custom_btnE);
        map.put(IdentificacaoTeclaPIN.TECLA_CANCELA,    R.id.keyboard_custom_btnC);
        map.put(IdentificacaoTeclaPIN.TECLA_VOLTA,      R.id.keyboard_custom_btnD);

        return map;
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG_LOGCAT, "onClick::view.getId() [" + view.getId() + "]");
    }
}
