package io.cloudwalk.pos.pinpadlibrary.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;

import io.cloudwalk.pos.pinpadlibrary.PinpadAbstractionLayer;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DataUtility {
    private static final String TAG_LOGCAT = DataUtility.class.getSimpleName();

    private static final byte[] HEX_ARRAY =
            "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    /**
     * @return {@link Context}
     */
    public static Context getApplicationContext() {
        return PinpadAbstractionLayer.getContext();
    }

    /**
     * @param id {@link DrawableRes} id
     * @return {@link Drawable}
     */
    public static Drawable getDrawableById(@DrawableRes int id) {
        return ResourcesCompat.getDrawable(getApplicationContext().getResources(), id, null);
    }

    public static byte[] toByteArray(Charset charset, String input) {
        return input.getBytes(charset);
    }

    public static byte[] toByteArray(String input) {
        return input.getBytes(UTF_8);
    }

    /**
     * Converts a given {@link Bundle} to a {@link JSONObject}.<br>
     * If the conversion fails by any reason, the return will be an empty {@link JSONObject}.
     *
     * @param input {@link Bundle}
     * @param sort indicates whether the collection of keys must be sorted lexicographically
     * @return {@link JSONObject}
     */
    public static JSONObject toJSON(@NotNull Bundle input, boolean sort) {
        JSONObject output = new JSONObject();

        List<String> keySet = new ArrayList<>(0);

        if (sort) {
            keySet.addAll(input.keySet());

            Collections.sort(keySet);
        }

        for (String key : (sort) ? keySet : input.keySet()) {
            try {
                Object item = input.get(key);

                if (item instanceof Bundle) {
                    output.put(key, toJSON((Bundle) item, sort));
                } else if (item instanceof List) {
                    JSONArray jsonArray = new JSONArray();

                    for (Object object : (List) item) {
                        if (object instanceof Bundle) {
                            jsonArray.put(toJSON((Bundle) object));
                        } else { /* 2021-06-04: may be lacking a recursion of type
                                  * 'toJSONArray(List)' for inner lists */
                            jsonArray.put(object);
                        }
                    }

                    output.put(key, jsonArray);
                } else {
                    output.put(key, item);
                }
            } catch (Exception exception) {
                Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));

                output = new JSONObject();
            }
        }

        return output;
    }

    /**
     * See {@link DataUtility#toJSON(Bundle, boolean)}.
     *
     * @param input {@link Bundle}
     * @return {@link JSONObject}
     */
    public static JSONObject toJSON(@NotNull Bundle input) {
        return toJSON(input, false);
    }

    /**
     * Digests an {@code input} using the specified {@code algorithm}.<br>
     * If the algorithm isn't available or known, the return will be an empty {@link String}.
     *
     * @param algorithm e.g. "MD5", "SHA", "SHA-256", etc.
     * @param input {@link String}
     * @return {@link String}
     */
    public static String digest(@NotNull String algorithm, @NotNull String input) {
        StringBuilder output = new StringBuilder();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);

            byte[] byteInput = input.getBytes(UTF_8);

            messageDigest.update(byteInput, 0, byteInput.length);

            byte[] byteDigest = messageDigest.digest();

            for (int i = 0; i < byteDigest.length; i++) {
                output.append(Integer.toString(( byteDigest[i] & 0xFF ) + 0x100, 16).substring(1));
            }
        } catch (Exception exception) {
            Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
        }

        return output.toString();
    }

    /**
     * Masks data from given {@code input} between indexes {@code ll} and {@code rr}.<br>
     * Ignores whitespaces.
     *
     * @param input {@link String}
     * @param rr {@code int}
     * @param ll {@code int}
     * @return {@link String}
     */
    public static String mask(@NotNull String input, int ll, int rr) {
        String output = new String(input);

        if (ll < 0 || rr < 0) {
            return output;
        }

        if ((output.length() - ll - rr) <= 0) {
            return output;
        }

        for (int i = ll; i < output.length() - rr; i++) {
            char candidate = output.charAt(i);

            if (candidate != ' ') {
                output = output.substring(0, i) + '*' + output.substring(i + 1);
            }
        }

        return output;
    }

    /**
     * See <a href="https://bit.ly/2RWydoS">https://bit.ly/2RWydoS</a> for details.
     *
     * @param input {@code byte} array
     * @return {@link String}
     */
    public static String toHex(@NotNull byte[] input) {
        byte[] output = new byte[input.length * 2];

        for (int j = 0; j < input.length; j++) {
            int value = input[j] & 0xFF;
            output[j * 2] = HEX_ARRAY[value >>> 4];
            output[j * 2 + 1] = HEX_ARRAY[value & 0x0F];
        }

        return new String(output, UTF_8);
    }

    /**
     * {@link String} binary search.<br>
     * Bear in mind binary searches require a {@code haystack} properly sorted.
     *
     * @param haystack self-describing
     * @param needle self-describing
     * @return either the position of {@code needle} on the {@code haystack} or -1
     */
    public static int binarySearch(@NotNull String[] haystack, @NotNull String needle) {
        int left = 0, right = haystack.length - 1;

        while (left <= right) {
            int middle = left + (right - left) / 2;

            int res = needle.compareTo(haystack[middle]);

            if (res == 0) {
                return middle;
            }
            if (res > 0) {
                left = middle + 1;
            }
            else {
                right = middle - 1;
            }
        }

        return -1;
    }
}
