package io.cloudwalk.pos.pinpadlibrary;

import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;

interface IPinpadManager {
    // TODO: split 'request' into 'send' and 'recv'
    byte[] request(in byte[] input);
}
