package io.cloudwalk.pos.pinpadlibrary;

interface IServiceCallback {
    /**
     * Notification types
     */
    const int NTF                           =  0;
    const int NTF_2x16                      =  1;
    const int NTF_PROCESSING                =  2;
    const int NTF_INSERT_SWIPE_CARD         =  3;
    const int NTF_TAP_INSERT_SWIPE_CARD     =  4;
    const int NTF_SELECT                    =  5;
    const int NTF_SELECTED                  =  6;
    const int NTF_AID_INVALID               =  7;
    const int NTF_PIN_START                 =  8;
    const int NTF_PIN_ENTRY                 =  9;
    const int NTF_PIN_FINISH                = 10;
    const int NTF_PIN_INVALID               = 11;
    const int NTF_PIN_LAST_TRY              = 12;
    const int NTF_PIN_BLOCKED               = 13;
    const int NTF_PIN_VERIFIED              = 14;
    const int NTF_CARD_BLOCKED              = 15;
    const int NTF_REMOVE_CARD               = 16;
    const int NTF_UPDATING                  = 17;
    const int NTF_RETAP_CARD                = 18;

    int onSelectionRequired(inout Bundle output);

    void onNotificationThrow(inout Bundle output, in int type);
}
