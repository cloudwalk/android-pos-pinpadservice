package io.cloudwalk.pos.pinpadlibrary;

import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;

interface IPinpadManager {
    Bundle request(inout Bundle input);

    void registerCallback(in IServiceCallback input);
}
