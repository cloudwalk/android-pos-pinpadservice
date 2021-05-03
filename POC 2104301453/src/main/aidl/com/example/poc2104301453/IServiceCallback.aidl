package com.example.poc2104301453;

import com.example.poc2104301453.IServiceCallback;

interface IServiceCallback {
    void onFailure(in Bundle output);
    void onStatusUpdate(in Bundle output);
    void onSuccess(in Bundle output);
}
