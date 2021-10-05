package io.cloudwalk.pos.pinpadlibrary;

import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;

interface IPinpadManager {
    int recv(inout Bundle bundle);

    int send(inout Bundle bundle, in IServiceCallback callback);
}
