package com.example.poc2104301453.pinpadservice;

import com.example.poc2104301453.pinpadservice.IServiceCallback;

interface IServiceCallback {
    /**
     * @see com.example.library.ABECS.Callback.Status#onFailure(Bundle)
     */
     void onFailure(inout Bundle output);

    /**
     * @see com.example.library.ABECS.Callback.Status#onSuccess(Bundle)
     */
    void onSuccess(inout Bundle output);
}
