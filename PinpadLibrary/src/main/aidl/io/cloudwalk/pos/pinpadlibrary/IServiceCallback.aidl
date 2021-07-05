package io.cloudwalk.pos.pinpadlibrary;

interface IServiceCallback {
    int onSelectionRequired(inout Bundle output);

    void onNotificationThrow(inout Bundle output, in int type);
}
