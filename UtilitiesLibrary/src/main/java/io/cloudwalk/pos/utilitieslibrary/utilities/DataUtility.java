package io.cloudwalk.pos.utilitieslibrary.utilities;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.cloudwalk.pos.loglibrary.Log;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DataUtility {
    private static final String
            TAG = DataUtility.class.getSimpleName();

    private static final byte[]
            HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    /**
     * Converts given {@link List} to {@link JSONArray}.
     *
     * @param list {@link List}
     * @return {@link JSONArray}
     */
    private static JSONArray getJSONArrayFromList(List list) {
        JSONArray jsonArray = new JSONArray();

        for (Object object : list) {
            if (object instanceof List) {
                jsonArray.put(getJSONArrayFromList((List) object));
            } else if (object instanceof Bundle) {
                jsonArray.put(getJSONObjectFromBundle((Bundle) object));
            } else {
                jsonArray.put(object);
            }
        }

        return jsonArray;
    }

    /**
     * See {@link DataUtility#getJSONObjectFromBundle(Bundle, boolean)}.
     *
     * @param input {@link Bundle}
     * @return {@link JSONObject}
     */
    public static JSONObject getJSONObjectFromBundle(@NotNull Bundle input) {
        return getJSONObjectFromBundle(input, false);
    }

    /**
     * Converts given {@link Bundle} to {@link JSONObject}.
     *
     * @param input {@link Bundle}
     * @param sort indicates whether the collection of keys must be sorted lexicographically
     * @return {@link JSONObject}
     */
    public static JSONObject getJSONObjectFromBundle(@NotNull Bundle input, boolean sort) {
        List<String> keySet = new ArrayList<>(0);

        if (sort) {
            keySet.addAll(input.keySet());

            Collections.sort(keySet);
        }

        JSONObject output = new JSONObject();

        for (String key : (sort) ? keySet : input.keySet()) {
            try {
                Object item = input.get(key);

                if (item instanceof Bundle) {
                    output.put(key, getJSONObjectFromBundle((Bundle) item, sort));
                } else if (item instanceof List) {
                    output.put(key, getJSONArrayFromList((List) item));
                } else {
                    output.put(key, item);
                }
            } catch (Exception exception) {
                Log.e(TAG, Log.getStackTraceString(exception));

                output = null;
            }
        }

        return output;
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
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        return output.toString();
    }

    /**
     * See <a href="https://bit.ly/2RWydoS">https://bit.ly/2RWydoS</a> for details.
     *
     * @param input {@code byte} array
     * @return {@link String}
     */
    public static String getHexStringFromByteArray(@NotNull byte[] input) {
        byte[] output = new byte[input.length * 2];

        for (int j = 0; j < input.length; j++) {
            int value = input[j] & 0xFF;
            output[j * 2] = HEX_ARRAY[value >>> 4];
            output[j * 2 + 1] = HEX_ARRAY[value & 0x0F];
        }

        return new String(output, UTF_8);
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
     * See <a href="https://bit.ly/3xY5mAv">https://bit.ly/3xY5mAv</a> for details.
     *
     * @param input {@code byte[]}
     * @return {@code byte[]}
     */
    public static byte[] CRC16_XMODEM(byte[] input) {
        int wCRCin = 0x0000;
        int wCPoly = 0x1021;

        for (byte b : input) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((wCRCin >> 15 & 1) == 1);

                wCRCin <<= 1;

                if (c15 ^ bit) {
                    wCRCin ^= wCPoly;
                }
            }
        }

        wCRCin &= 0xffff;
        wCRCin ^= 0x0000;

        String trace = Integer.toHexString(wCRCin);

        return getByteArrayFromHexString((trace.length() % 2 != 0) ? "0" + trace : trace);
    }

    /**
     * Concatenates variable number of given {@code byte[]} arguments into single new
     * {@code byte[]}.
     *
     * @param input one or more {@code byte[]}
     * @return {@code byte[]}
     */
    public static byte[] concatByteArray(byte[]... input) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            for (byte[] array : input) {
                stream.write(array);
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            return null;
        }

        return stream.toByteArray();
    }

    /**
     * Converts given hexadecimal {@link String} to {@code byte} array.
     * See <a href="https://bit.ly/3Bvd7jU">https://bit.ly/3Bvd7jU</a> for details.
     *
     * @param input {@link String}
     * @return {@code byte}
     */
    public static byte[] getByteArrayFromHexString(String input) {
        int len = input.length();

        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(input.charAt(i), 16) << 4) + Character.digit(input.charAt(i+1), 16));
        }

        return data;
    }

    /**
     * See {@link String#getBytes(Charset)}.
     *
     * @param charset {@link StandardCharsets}
     * @param input {@link String}
     * @return {@code byte[]}
     */
    public static byte[] getByteArrayFromString(Charset charset, String input) {
        return input.getBytes(charset);
    }

    /**
     * See {@link String#getBytes(Charset)}, taking into consideration
     * {@link StandardCharsets#UTF_8}.
     *
     * @param input {@link String}
     * @return {@code byte[]}
     */
    public static byte[] getByteArrayFromString(String input) {
        return input.getBytes(UTF_8);
    }

    /**
     * See <a href="https://bit.ly/3rs0Bgj">https://bit.ly/3rs0Bgj</a> for details.
     *
     * @param input {@code byte[]}
     * @return {@code byte[]}
     */
    public static byte[] trimByteArray(byte[] input) {
        int i = input.length - 1;

        while (i >= 0 && input[i] == 0) {
            --i;
        }

        return Arrays.copyOf(input, i + 1);
    }

    /**
     * Converts given {@code byte[]} to {@code int}.<br>
     * One must consider the possibility of overflow according to given {@code length}.
     *
     * @param input {@code byte[]}
     * @param length {@code int}
     * @return {@code int}
     */
    public static int getIntFromByteArray(byte[] input, int length) {
        int output = 0;

        for (int i = length - 1, j = 0; i >= 0; i--, j++) {
            output += (input[j] - 0x30) * ((i > 0) ? (Math.pow(10, i)) : 1);
        }

        return output;
    }

    /**
     * {@link String} binary search.<br>
     * Bear in mind binary searches require a {@code haystack} properly sorted.
     *
     * @param haystack self-describing
     * @param needle self-describing
     * @return either the position of {@code needle} on the {@code haystack} or -1
     */
    public static int stringBinarySearch(@NotNull String[] haystack, @NotNull String needle) {
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
