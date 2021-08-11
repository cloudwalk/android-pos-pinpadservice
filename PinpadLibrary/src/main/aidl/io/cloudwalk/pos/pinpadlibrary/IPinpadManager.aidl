package io.cloudwalk.pos.pinpadlibrary;

import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;

interface IPinpadManager {
    /**
     *
     */
    int recv(out byte[] output,
              in long timeout);

    /**
     *
     */
    int send( in String application,
              in IServiceCallback callback,
              in byte[] input,
              in  int length);
}
