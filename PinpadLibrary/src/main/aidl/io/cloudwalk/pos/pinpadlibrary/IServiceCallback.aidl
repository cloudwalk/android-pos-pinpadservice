package io.cloudwalk.pos.pinpadlibrary;

interface IServiceCallback {
    const String NTF_MSG                        = "NTF_MSG";
    const String NTF_OPTLST                     = "NTF_OPTLST";
    const String NTF_PIN                        = "NTF_PIN";
    const String NTF_TIMEOUT                    = "NTF_TIMEOUT";
    const String NTF_TITLE                      = "NTF_TITLE";
    const String NTF_TYPE                       = "NTF_TYPE";

    const int    NTF                            =  0;
    const int    NTF_2x16                       =  1;
    const int    NTF_PROCESSING                 =  2;
    const int    NTF_INSERT_SWIPE_CARD          =  3;
    const int    NTF_TAP_INSERT_SWIPE_CARD      =  4;
    const int    NTF_SELECT                     =  5;
    const int    NTF_SELECTED                   =  6;
    const int    NTF_AID_INVALID                =  7;
    const int    NTF_PIN_START                  =  8;
    const int    NTF_PIN_ENTRY                  =  9;
    const int    NTF_PIN_FINISH                 = 10;
    const int    NTF_PIN_INVALID                = 11;
    const int    NTF_PIN_LAST_TRY               = 12;
    const int    NTF_PIN_BLOCKED                = 13;
    const int    NTF_PIN_VERIFIED               = 14;
    const int    NTF_CARD_BLOCKED               = 15;
    const int    NTF_REMOVE_CARD                = 16;
    const int    NTF_UPDATING                   = 17;
    const int    NTF_SECOND_TAP                 = 18;

    int onServiceCallback(inout Bundle bundle);
}
