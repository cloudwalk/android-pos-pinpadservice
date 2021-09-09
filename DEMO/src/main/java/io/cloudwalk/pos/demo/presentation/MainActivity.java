package io.cloudwalk.pos.demo.presentation;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.pos.demo.PinpadServer;
import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.demo.adapters.MainAdapter;
import io.cloudwalk.pos.demo.databinding.ActivityMainBinding;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class MainActivity extends AppCompatActivity {
    private static final String
            TAG = MainActivity.class.getSimpleName();

    private static final Semaphore[]
            SEMAPHORE = {
                    new Semaphore(1, true), /* mAutoScroll and mServerTraceOn */
                    new Semaphore(1, true), /* mPinpadServer */
            };

    private static final int
            MAIN_ADAPTER_CONTENT_LIMIT = 2000;

    private AlertDialog
            mAboutAlertDialog = null;

    private MainAdapter
            mMainAdapter = null;

    private PinpadServer
            mPinpadServer = null;

    private RecyclerView
            mRecyclerView = null;

    private boolean
            mAutoScroll = true;

    private boolean
            mServerTraceOn = false;

    private SpannableString getBullet(@ColorInt int color) {
        Log.d(TAG, "getBullet::color [" + color + "]");

        SpannableString output = new SpannableString("  ");

        output.setSpan(new BulletSpan(7, color), 0, 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return output;
    }

    private boolean getAutoScroll() {
        Log.d(TAG, "getAutoScroll");

        boolean autoScroll;

        acquire(0);

        autoScroll = mAutoScroll;

        release(0);

        return autoScroll;
    }

    private boolean getServerTraceStatus() {
        Log.d(TAG, "getServerTraceStatus");

        boolean serverTraceStatus;

        acquire(0);

        serverTraceStatus = mServerTraceOn;

        release(0);

        return serverTraceStatus;
    }

    private void acquire(int index) {
        Log.d(TAG, "acquire::index [" + index + "]");

        SEMAPHORE[index].acquireUninterruptibly();
    }

    private void release(int index) {
        Log.d(TAG, "release::index [" + index + "]");

        SEMAPHORE[index].release();
    }

    private void setAutoScroll(boolean autoScroll) {
        Log.d(TAG, "setAutoScroll::autoScroll [" + autoScroll + "]");

        acquire(0);

        mAutoScroll = autoScroll;

        release(0);
    }

    private void setServerTraceStatus(boolean serverTraceStatus) {
        Log.d(TAG, "setServerTraceStatus::serverTraceStatus [" + serverTraceStatus + "]");

        acquire(0);

        mServerTraceOn = serverTraceStatus;

        release(0);
    }

    private void updateContentScrolling(String delim, String message) {
        Log.d(TAG, "updateContentScrolling:: delim [" + delim + "]");

        String[] trace = null;

        if (delim != null && !delim.isEmpty()) {
            trace = message.split(delim);
        } else {
            trace = new String[] { message };
        }

        Semaphore[] semaphore = { new Semaphore(0, true) };

        for (String chunk : trace) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int limit = MAIN_ADAPTER_CONTENT_LIMIT;

                        mMainAdapter.push(chunk);

                        int count = mMainAdapter.getItemCount();

                        if (!getAutoScroll()) {
                            return;
                        }

                        if (count >= limit) {
                            mMainAdapter.clear(0, count - (limit / 2));

                            count = mMainAdapter.getItemCount();
                        }

                        mRecyclerView.scrollToPosition(count - 1);
                    } finally {
                        semaphore[0].release();
                    }
                }
            });

            semaphore[0].acquireUninterruptibly();
        }
    }

    private void updateStatus(int status, String message) {
        Log.d(TAG, "updateStatus::status [" + status + "]");

        SpannableStringBuilder[] content = { new SpannableStringBuilder() };

        switch (status) {
            case 0: /* SUCCESS */
                content[0].append(getBullet(Color.GREEN));
                break;

            case 1: /* FAILURE */
                content[0].append(getBullet(Color.RED));
                break;

            case 2: /* PROCESSING */
                content[0].append(getBullet(Color.BLUE));
                break;

            default:
                content[0].append(getBullet(Color.GRAY));
                break;
        }

        content[0].append(message);

        Semaphore[] semaphore = { new Semaphore(0, true) };

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.tv_app_status)).setText(content[0]);

                semaphore[0].release();
            }
        });

        semaphore[0].acquireUninterruptibly();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.fab.setEnabled(false);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAutoScroll(true);

                        int limit = MAIN_ADAPTER_CONTENT_LIMIT;

                        int count = mMainAdapter.getItemCount();

                        if (count >= limit) {
                            mMainAdapter.clear(0, count - (limit / 2));

                            count = mMainAdapter.getItemCount();
                        }

                        mRecyclerView.scrollToPosition(count - 1);

                        binding.fab.setEnabled(false);

                        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_pause_24, null);

                        binding.fab.setImageDrawable(drawable);
                    }
                });
            }
        });

        mRecyclerView = findViewById(R.id.rv_main_content);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setScrollContainer(true);
        mRecyclerView.setItemAnimator(null);

        mMainAdapter = new MainAdapter();

        mRecyclerView.setAdapter(mMainAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                Log.d(TAG, "onInterceptTouchEvent");

                setAutoScroll(false);

                binding.fab.setEnabled(true);

                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_play_arrow_24, null);

                binding.fab.setImageDrawable(drawable);

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                Log.d(TAG, "onTouchEvent");
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                Log.d(TAG, "onRequestDisallowInterceptTouchEvent");
            }
        });

        mAboutAlertDialog = new AboutAlertDialog(this);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        finish();

        /* 'onPause' runs on the UI thread, hence the new thread not to block it */
        new Thread() {
            @Override
            public void run() {
                super.run();

                PinpadManager.abort();

                acquire(1);

                if (mPinpadServer != null) {
                    mPinpadServer.close();
                }

                release(1);
            }
        }.start();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        overridePendingTransition(0, 0);

        super.onResume();

        /* 'onResume' runs on the UI thread, hence the new thread not to block it */
        new Thread() {
            @Override
            public void run() {
                super.run();

                IServiceCallback serviceCallback = new IServiceCallback.Stub() {
                    @Override
                    public int onSelectionRequired(Bundle output) {
                        Log.d(TAG, "onSelectionRequired");

                        // TODO: AlertDialog!?

                        return 0;
                    }

                    @Override
                    public void onNotificationThrow(Bundle output, int type) {
                        Log.d(TAG, "onNotificationThrow");

                        String msg = output.getString("NTF_MSG").replace("\n", "\\n");

                        Log.d(TAG, "onNotificationThrow::type [" + type + "] msg [" + msg + "]");

                        // TODO: AlertDialog!?
                    }
                };

                updateStatus(2, getString(R.string.warning_local_processing));

                Bundle request = new Bundle();

                List<Bundle> requestList = new ArrayList<>(0);

                request.putString(ABECS.CMD_ID, ABECS.CLX);

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.OPN);
                request.putString(ABECS.OPN_OPMODE, "0");
                request.putString(ABECS.OPN_MOD, "A82A660B3C49226EFCDABA7FC68066B83D23D0560EDA3A12B63E9132F299FBF340A5AEBC4CD5DC1F14873F83A80BA9A88D3FEABBAB41DFFC1944BBBAA89F26AF9CC28FF31C497EB91D82F8613E7463C47529FBD1925FD3326A8DC027704DA68860E68BD0A1CEA8DE6EC75604CD3D9A6AF38822DE45AAA0C9FBF2BD4783B0F9A81F6350C0188156F908FAB1F559CFCE1F91A393431E8BF2CD78C04BD530DB441091CDFFB400DAC08B1450DB65C00E2D4AF4E9A85A1A19B61F550F0C289B14BD63DF8A1539A8CF629F98F88EA944D9056675000F95BFD0FEFC56F9D9D66E2701BDBD71933191AE9928F5D623FE8B99ECC777444FFAA83DE456F5C8D3C83EC511AF");
                request.putString(ABECS.OPN_EXP, "0D");

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.GIX);
                request.putString(ABECS.SPE_IDLIST, "800180028003800480058006800780088009800A80108011801280138014801580168032803380358036910A920B9300");

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.TLI);
                request.putString(ABECS.TLI_ACQIDX, "00");
                request.putString(ABECS.TLI_TABVER, "0123456789");

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.TLR);
                request.putString(ABECS.TLR_NREC, "03");
                request.putString(ABECS.TLR_DATA,
                        "3141040107A000000494201000000000000000000002ELO DEBITO      03000100010001076986200000000000000000000000000060D0E87000F0A00122FC408480000010000000FC6084900000000000R093B9AC9FF00001388000013880000100000000000000000000000000000000000000009F37040000000000000000000000000000000000Y1Z1Y3Z3FC408480000010000000FC60849000"
                      + "3141040210A0000000041010D0761300000000000001Master CREDITO  030002000200020769862000000000000000000000000000E0F8C8F000F0A00122FE50BCA0000000000000FE50BCF80000000000R143B9AC9FF0000000000001388000019F02065F2A029A039C0195059F370400000000009F37040000000000000000000000000000000000Y1Z1Y3Z3F45084800C0000000000F45084800C"
                      + "3141040310A0000000041010D0761200000000000002Master DEBITO   030002000200020769862000000000000000000000000000E0F8C8F000F0A00122FE50BCA0000000000000FE50BCF80000000000R143B9AC9FF0000000000001388000019F02065F2A029A039C0195059F370400000000009F37040000000000000000000000000000000000Y1Z1Y3Z3F45084800C0000000000F45084800C");

                requestList.add(request);

                ArrayList<String> list = new ArrayList<>(0);

                list.add("3141040407A000000004101000000000000000000001Mastercard      030002000200020769862000000000000000000000000000E0F8C8F000F0A00122FE50BCA0000000000000FE50BCF80000000000R143B9AC9FF0000000000001388000019F02065F2A029A039C0195059F370400000000009F37040000000000000000000000000000000000Y1Z1Y3Z3F45084800C0000000000F45084800C");
                list.add("3141040507A000000004306000000000000000000002Maestro         030002000200020769862000000000000000000000000000E0F8C87000F0A00122FE50BCA0000000000000FE50BCF80000000000R143B9AC9FF0000000000001388000019F02065F2A029A039C0195059F370400000000009F37040000000000000000000000000000000000Y1Z1Y3Z3F45004800C0000800000F45004800C");
                list.add("3141040607A000000003201000000000000000000002VISA DEBIT      03008C008C008C076986200000000000000000000000000060D0E87000F0A00122DC4000A8000010000000DC4004F80000000000R123B9AC9FF0000000000001388000019F02065F2A029A039C0195059F370400000000009F37040000000000000000000000000000000000Y1Z1Y3Z3000000000000000000000000000000");
                list.add("3141040707A000000004600000000000000000000001Cirrus          03000200020002076986200000000000000000000000000060D0E8F000F0A00122FC50ACA0000400000000F850ACF80000000000R00000000000000000000000000000009F02065F2A029A039C0195059F370400000000009F37040000000000000000000000000000000000Y1Z1Y3Z3000000000000000000000000000000");
                list.add("3141040807A000000494101000000000000000000001ELO CREDITO     03000200020002076986200000000000000000000000000060D0E87000F0F00122FC408480000010000000FC6084900000000000R093B9AC9FF0000138800001388000019F02065F2A029A039C0195059F370400000000009F37040000000000000000000000000000000000Y1Z1Y3Z3FC408480000010000000FC60849000");
                list.add("3141040906A000000025010000000000000000000001AMEX            03000100010001076986200000000000000000000000000060D0E87000F0A00122DC50FC98000010000000DE00FC980000000000R16900000000000000000005000000019F02065F2A029A039C0195059F370400000000009F37040000000000000000000000000000000000Y1Z1Y3Z3000000000000000000000000000000");
                list.add("3141041007A000000003101000000000000000000001VISA            03008C008C008C076986200000000000000000000000000060D0E87000F0A00122DC4000A8000010000000DC4004F80000000000R123B9AC9FF0000000000001388000019F02065F2A029A039C0195059F370400000000009F37040000000000000000000000000000000000Y1Z1Y3Z3000000000000000000000000000000");
                list.add("61120411A00000000395001030000144BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001EE1511CEC71020A9B90443B37B1D5F6E703030F6000000000000000000000000000000000000000000");
                list.add("61120412A00000000392001030000176996AF56F569187D09293C14810450ED8EE3357397B18A2458EFAA92DA3B6DF6514EC060195318FD43BE9B8F0CC669E3F844057CBDDF8BDA191BB64473BC8DC9A730DB8F6B4EDE3924186FFD9B8C7735789C23A36BA0B8AF65372EB57EA5D89E7D14E9C7B6B557460F10885DA16AC923F15AF3758F0F03EBD3C5C2C949CBA306DB44E6A2C076C5F67E281D7EF56785DC4D75945E491F01918800A9E2DC66F60080566CE0DAF8D17EAD46AD8E30A247C9F0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001429C954A3859CEF91295F663C963E582ED6EB253000000000000000000000000000000000000000000");
                list.add("61120413A00000000399001030000128AB79FCC9520896967E776E64444E5DCDD6E13611874F3985722520425295EEA4BD0C2781DE7F31CD3D041F565F747306EED62954B17EDABA3A6C5B85A1DE1BEB9A34141AF38FCF8279C9DEA0D5A6710D08DB4124F041945587E20359BAB47B7575AD94262D4B25F264AF33DEDCF28E09615E937DE32EDC03C54445FE7E38277700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000014ABFFD6B1C51212D05552E431C5B17007D2F5E6D000000000000000000000000000000000000000000");
                list.add("61120414A00000000394001030000248ACD2B12302EE644F3F835ABD1FC7A6F62CCE48FFEC622AA8EF062BEF6FB8BA8BC68BBF6AB5870EED579BC3973E121303D34841A796D6DCBC41DBF9E52C4609795C0CCF7EE86FA1D5CB041071ED2C51D2202F63F1156C58A92D38BC60BDF424E1776E2BC9648078A03B36FB554375FC53D57C73F5160EA59F3AFC5398EC7B67758D65C9BFF7828B6B82D4BE124A416AB7301914311EA462C19F771F31B3B57336000DFF732D3B83DE07052D730354D297BEC72871DCCF0E193F171ABA27EE464C6A97690943D59BDABB2A27EB71CEEBDAFA1176046478FD62FEC452D5CA393296530AA3F41927ADFE434A2DF2AE3054F8840657A26E0FC6171C4A3C43CCF87327D136B804160E47D43B60E6E0F000000000000000000000000000000000000000000");
                list.add("61120415A000000004FE001030000128A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000019A295B05FB390EF7923F57618A9FDA2941FC34E0000000000000000000000000000000000000000000");
                list.add("61120416A000000004F1001030000176A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE70000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB000000000000000000000000000000000000000000");
                list.add("61120417A000000004F300103000014498F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001A69AC7603DAF566E972DEDC2CB433E07E8B01A9A000000000000000000000000000000000000000000");
                list.add("61120418A000000004F8001030000128A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001F06ECC6D2AAEBF259B7E755A38D9A9B24E2FF3DD000000000000000000000000000000000000000000");
                list.add("61120419A000000004EF001030000248A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B121766EBB0EE122AFB65D7845B73DB46BAB65427A000000000000000000000000000000000000000000");
                list.add("61120420A000000004FA001030000144A90FCD55AA2D5D9963E35ED0F440177699832F49C6BAB15CDAE5794BE93F934D4462D5D12762E48C38BA83D8445DEAA74195A301A102B2F114EADA0D180EE5E7A5C73E0C4E11F67A43DDAB5D55683B1474CC0627F44B8D3088A492FFAADAD4F42422D0E7013536C3C49AD3D0FAE96459B0F6B1B6056538A3D6D44640F94467B108867DEC40FAAECD740C00E2B7A8852D000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000015BED4068D96EA16D2D77E03D6036FC7A160EA99C000000000000000000000000000000000000000000");
                list.add("61120421A000000494E0001030000144D8D8A754F12A90EB3D1A6828F65300F1E938877732AC1CBB75E74EE9129398F62B8DE7812873442B5FA5CB27A42D17170E4F45B22AF25B03F36246ACD8C682292363C9C9FA470D4F60C2500E70732D19346E50097558E85428BF23C04D6907769E4B3236C84E4D43B3E07BD2CC8F8334022EDA760086BD4DD5088179A7EA3A42B4AC82C971287A0DD5E985DA6D8DF4991BC045C73C4213E11A431E1B419E1FFD725733E220000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
                list.add("61120422A000000494E1001030000176BEFA49A34C81522C4499745F2FFEDC5597A8049D8C75D80F3AE606DCEF62D2DD9AA97A031AFA0FDC80737C031D09729E25E9DF1A5C05E88040F7C4CEE2AFC0398A0BCF2BB491CD36F4F5C359B9BF7B12958C2E79E06BBB2F37BF748177E1AF6662F9EDEFED3A15A6E5E87A6258DE4F83B1AC75757526C9961C29DEB7E5C67B18A81FEB2F0E4E62DF86B75B2DE834EB4CA838EB486C18C161AED45159DCDEA9F0CF724F3DD6B7222A539F037E52910845184EC43E67F4219779771DC7E5FD8700307E3FD24000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
                list.add("61120423A000000494E2001030000248B303169291494ED69263243D1A617A15F943413D2ECFF0A98292884D15826B494E7925A087BC1FE54FCA44DAACB0A91A8F384F1AA9189F5EC7B15211C66129E2640A75313D584C6A992E04521B70E25DB49E35E65959F136ACE71602C954EB83E4223BECCD5DFF089AF5A5B444BF914463EF855E6DD642EBC6CA6CE662AFCA3DFFA32A44AB0D0C8CE1DBCEFFCA56CEBE31BDDCCB9DBD1C3BD00C099BBFBEEADC2672B809AFFDF2D1571AA4CC8AF96D41B0CF72369CEF6B15AED7930E21CC95AD32EEEB55210800942CDF82DB5B3B3B4896B15D1899114ED33DA43900B6DAF941BBD69431E83BE4D01E4FC36922C497E9F370309D51D667071589D31C994A3720AB01292353FC45DDB46CF88B5000000000000000000000000000000000000000000");
                list.add("61120424A000000025C8001030000144BF0CFCED708FB6B048E3014336EA24AA007D7967B8AA4E613D26D015C4FE7805D9DB131CED0D2A8ED504C3B5CCD48C33199E5A5BF644DA043B54DBF60276F05B1750FAB39098C7511D04BABC649482DDCF7CC42C8C435BAB8DD0EB1A620C31111D1AAAF9AF6571EEBD4CF5A08496D57E7ABDBB5180E0A42DA869AB95FB620EFF2641C3702AF3BE0B0C138EAEF202E21D133BD7A059FAB094939B90A8F35845C9DC779BD500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
                list.add("61120425A000000025CA001030000248C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E316BDA32B1AA171444C7E8F88075A74FBFE845765F000000000000000000000000000000000000000000");
                list.add("61120426A000000025C9001030000176B362DB5733C15B8797B8ECEE55CB1A371F760E0BEDD3715BB270424FD4EA26062C38C3F4AAA3732A83D36EA8E9602F6683EECC6BAFF63DD2D49014BDE4D6D603CD744206B05B4BAD0C64C63AB3976B5C8CAAF8539549F5921C0B700D5B0F83C4E7E946068BAAAB5463544DB18C63801118F2182EFCC8A1E85E53C2A7AE839A5C6A3CABE73762B70D170AB64AFC6CA482944902611FB0061E09A67ACB77E493D998A0CCF93D81A4F6C0DC6B7DF22E62DB18E8DFF443D78CD91DE88821D70C98F0638E51E49000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");

                for (String item: list) {
                    request = new Bundle();

                    request.putString(ABECS.CMD_ID, ABECS.TLR);
                    request.putString(ABECS.TLR_NREC, "01");
                    request.putString(ABECS.TLR_DATA, item);

                    requestList.add(request);
                }

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.TLE);

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.CEX);
                request.putString(ABECS.SPE_CEXOPT, "011000");
                request.putString(ABECS.SPE_TIMEOUT, "3C");
                request.putString(ABECS.SPE_PANMASK, "0404");

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.GTK);

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.RMC);
                request.putString(ABECS.RMC_MSG, "HAVE FAITH...                   ");

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.EBX);
                request.putString(ABECS.SPE_DATAIN, "00010203040506070809101112131415");
                request.putString(ABECS.SPE_MTHDDAT, "51");
                request.putString(ABECS.SPE_KEYIDX, "11");

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.GCX);
                request.putString(ABECS.SPE_TRNTYPE, "00");
                request.putString(ABECS.SPE_ACQREF, "04");
                request.putString(ABECS.SPE_APPTYPE, "02");
                request.putString(ABECS.SPE_AMOUNT, "000000000999");
                request.putString(ABECS.SPE_CASHBACK, "000000000000");
                request.putString(ABECS.SPE_TRNCURR, "986");
                request.putString(ABECS.SPE_TRNDATE, "210909");
                request.putString(ABECS.SPE_TRNTIME, "163800");
                request.putString(ABECS.SPE_GCXOPT, "10000");
                request.putString(ABECS.SPE_PANMASK, "0404");
                request.putString(ABECS.SPE_TAGLIST, "5F285F24");
                request.putString(ABECS.SPE_TIMEOUT, "3C");
                request.putString(ABECS.SPE_DSPMSG, "HAVE FAITH...");

                requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.GED);
                request.putString(ABECS.SPE_TAGLIST, "5F285F24");

                requestList.add(request);

                for (Bundle TX : requestList) {
                    try {
                        updateContentScrolling(null, "\"TX\": " + DataUtility.getJSONObjectFromBundle(TX).toString(4));

                        Bundle RX = PinpadManager.request(serviceCallback, TX);

                        updateContentScrolling(null, "\"RX\": " + DataUtility.getJSONObjectFromBundle(RX).toString(4));

                        if (wasPaused()) {
                            return;
                        }
                    } catch (Exception exception) {
                        updateContentScrolling(null, Log.getStackTraceString(exception));
                    }
                }

                updateStatus(0, "Finished processing local requests");
            }
        }.start();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        super.onStop();

        mAboutAlertDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement

        if (id == R.id.action_about) {
            mAboutAlertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
