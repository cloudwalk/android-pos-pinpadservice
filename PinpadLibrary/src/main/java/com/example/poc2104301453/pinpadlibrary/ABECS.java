package com.example.poc2104301453.pinpadlibrary;

public class ABECS {
    public static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    public static final String CMD_ID           = "CMD_ID";

    public static final String RSP_ID           = "RSP_ID";
    public static final String RSP_STAT         = "RSP_STAT";

    public static final String OPN              = "OPN";
    public static final String OPN_OPMODE       = "OPN_OPMODE";
    public static final String OPN_MOD          = "OPN_MOD";
    public static final String OPN_EXP          = "OPN_EXP";

    public static final String GIN              = "GIN";
    public static final String GIN_ACQIDX       = "GIN_ACQIDX";
    public static final String GIN_MNAME        = "GIN_MNAME";
    public static final String GIN_MODEL        = "GIN_MODEL";
    public static final String GIN_CTLSSUP      = "GIN_CTLSSUP";
    public static final String GIN_SOVER        = "GIN_SOVER";
    public static final String GIN_SPECVER      = "GIN_SPECVER";
    public static final String GIN_MANVER       = "GIN_MANVER";
    public static final String GIN_SERNUM       = "GIN_SERNUM";
    public static final String GIN_ACQNAM       = "GIN_ACQNAM";
    public static final String GIN_KRNLVER      = "GIN_KRNLVER";
    public static final String GIN_APPVERS      = "GIN_APPVERS";
    public static final String GIN_CTLSVER      = "GIN_CTLSVER";
    public static final String GIN_MCTLSVER     = "GIN_MCTLSVER";
    public static final String GIN_VCTLSVER     = "GIN_VCTLSVER";
    public static final String GIN_DUKPT        = "GIN_DUKPT";

    public static final String CLO              = "CLO";

    public static final String CKE              = "CKE";
    public static final String CKE_KEY          = "CKE_KEY";
    public static final String CKE_MAG          = "CKE_MAG";
    public static final String CKE_ICC          = "CKE_ICC";
    public static final String CKE_CTLS         = "CKE_CTLS";
    public static final String CKE_EVENT        = "CKE_EVENT";
    public static final String CKE_KEYCODE      = "CKE_KEYCODE";
    public static final String CKE_TRK1         = "CKE_TRK1";
    public static final String CKE_TRK2         = "CKE_TRK2";
    public static final String CKE_TRK3         = "CKE_TRK3";
    public static final String CKE_ICCSTAT      = "CKE_ICCSTAT";
    public static final String CKE_CTLSSTAT     = "CKE_CTLSSTAT";

    /* 2021-006-21: TLI, TLR and TLE were grouped into a single command to
     * match the manufacturer's abstraction.
     * TODO: review ABECS spec. and make them individual requests? */

    public static final String TLI              = "TLI";
    public static final String TLI_ACQIDX       = "TLI_ACQIDX";
    public static final String TLI_TABVER       = "TLI_TABVER";

    public static final String TLR              = "TLR";
    public static final String TLR_NREC         = "TLR_NREC";
    public static final String TLR_DATA         = "TLR_DATA";

    public static final String TLE              = "TLE";

    public static final String GCR              = "GCR";
    public static final String GCR_ACQIDXREQ    = "GCR_ACQIDXREQ";
    public static final String GCR_APPTYPREQ    = "GCR_APPTYPREQ";
    public static final String GCR_AMOUNT       = "GCR_AMOUNT";
    public static final String GCR_DATE         = "GCR_DATE";
    public static final String GCR_TIME         = "GCR_TIME";
    public static final String GCR_TABVER       = "GCR_TABVER";
    public static final String GCR_QTDAPP       = "GCR_QTDAPP";
    public static final String GCR_IDAPPn       = "GCR_IDAPPn";
    public static final String GCR_CTLSON       = "GCR_CTLSON";
    public static final String GCR_CARDTYPE     = "GCR_CARDTYPE";
    public static final String GCR_STATCHIP     = "GCR_STATCHIP";
    public static final String GCR_APPTYPE      = "GCR_APPTYPE";
    public static final String GCR_ACQIDX       = "GCR_ACQIDX";
    public static final String GCR_RECIDX       = "GCR_RECIDX";
    public static final String GCR_TRK1         = "GCR_TRK1";
    public static final String GCR_TRK2         = "GCR_TRK2";
    public static final String GCR_TRK3         = "GCR_TRK3";
    public static final String GCR_PAN          = "GCR_PAN";
    public static final String GCR_PANSEQNO     = "GCR_PANSEQNO";
    public static final String GCR_APPLABEL     = "GCR_APPLABEL";
    public static final String GCR_SRVCODE      = "GCR_SRVCODE";
    public static final String GCR_CHNAME       = "GCR_CHNAME";
    public static final String GCR_CARDEXP      = "GCR_CARDEXP";
    public static final String GCR_ISSCNTRY     = "GCR_ISSCNTRY";
    public static final String GCR_ACQRD        = "GCR_ACQRD";

    public static enum STAT {
        ST_OK,              PP_PROCESSING,      PP_NOTIFY,
        ST_NOSEC,           ST_F1,              ST_F2,
        ST_F3,              ST_F4,              ST_BACKSP,
        ST_ERRPKTSEC,       ST_INVCALL,         ST_INVPARM,
        ST_TIMEOUT,         ST_CANCEL,          PP_ALREADYOPEN,
        PP_NOTOPEN,         PP_EXECERR,         PP_INVMODEL,
        PP_NOFUNC,          ST_MANDAT,          ST_TABVERDIF,
        ST_TABERR,          PP_NOAPPLIC,

        RFU_23, RFU_24, RFU_25, RFU_26, RFU_27, RFU_28, RFU_29,

        PP_PORTERR,         PP_COMMERR,         PP_UNKNOWNSTAT,
        PP_RSPERR,          PP_COMMTOUT,

        RFU_35, RFU_36, RFU_37, RFU_38, RFU_39,

        ST_INTERR,          ST_MCDATAERR,       ST_ERRKEY,
        ST_NOCARD,          ST_PINBUSY,         ST_RSPOVRFL,
        ST_ERRCRYPT,

        RFU_47, RFU_48, RFU_49,

        PP_SAMERR,          PP_NOSAM,           PP_SAMINV,

        RFU_53, RFU_54, RFU_55, RFU_56, RFU_57, RFU_58, RFU_59,

        ST_DUMBCARD,        ST_ERRCARD,         PP_CARDINV,
        PP_CARDBLOCKED,     PP_CARDNAUTH,       PP_CARDEXPIRED,
        PP_CARDERRSTRUCT,   ST_CARDINVALIDAT,   ST_CARDPROBLEMS,
        ST_CARDINVDATA,     ST_CARDAPPNAV,      ST_CARDAPPNAUT,
        PP_NOBALANCE,       PP_LIMITEXC,        PP_CARDNOTEFFECT,
        PP_VCINVCURR,       ST_ERRFALLBACK,     ST_INVAMOUNT,
        ST_ERRMAXAID,       ST_CARDBLOCKED,     ST_CTLSMULTIPLE,
        ST_CTLSCOMMERR,     ST_CTLSINVALIDAT,   ST_CTLSPROBLEMS,
        ST_CTLSAPPNAV,      ST_CTLSAPPNAUT,     ST_CTLSEXTCVM,
        ST_CTLSIFCHG,

        RFU_88, RFU_89,
        RFU_90, RFU_91, RFU_92, RFU_93, RFU_94, RFU_95, RFU_96, RFU_97, RFU_98, RFU_99,

        ST_MFNFOUND,        ST_MFERRFMT,        ST_MFERR;

        private STAT() {
            /* Nothing to do */
        }
    }
}
