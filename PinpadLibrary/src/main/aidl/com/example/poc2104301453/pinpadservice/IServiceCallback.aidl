package com.example.poc2104301453.pinpadservice;

import com.example.poc2104301453.pinpadservice.IServiceCallback;

interface IServiceCallback {
     void onFailure(inout Bundle output);

    void onSuccess(inout Bundle output);
}
