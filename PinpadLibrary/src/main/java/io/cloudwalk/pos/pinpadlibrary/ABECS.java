package io.cloudwalk.pos.pinpadlibrary;

public class ABECS {
    public static final String
            TAG = ABECS.class.getSimpleName();

    /*
     * Basic data fields.
     */

    public static final String CMD_ID           = "CMD_ID";
    public static final String RSP_ID           = "RSP_ID";
    public static final String RSP_EXCEPTION    = "RSP_EXCEPTION";
    public static final String RSP_STAT         = "RSP_STAT";

    /*
     * "Unofficial" callback fields.
     */

    public static final String NTF_MSG          = "NTF_MSG";
    public static final String NTF_OPTLST       = "NTF_OPTLST";
    public static final String NTF_PIN          = "NTF_PIN";
    public static final String NTF_TIMEOUT      = "NTF_TIMEOUT";
    public static final String NTF_TITLE        = "NTF_TITLE";

    /*
     * Supported requests.
     */

    public static final String OPN              = "OPN";
    public static final String GIX              = "GIX";
    public static final String CLX              = "CLX";
    public static final String CEX              = "CEX";
    public static final String CHP              = "CHP";
    public static final String EBX              = "EBX";
    public static final String GCD              = "GCD";
    public static final String GPN              = "GPN";
    public static final String GTK              = "GTK";
    public static final String MNU              = "MNU";
    public static final String RMC              = "RMC";
    public static final String TLI              = "TLI";
    public static final String TLR              = "TLR";
    public static final String TLE              = "TLE";
    public static final String GCX              = "GCX";
    public static final String GED              = "GED";
    public static final String GOX              = "GOX";
    public static final String FCX              = "FCX";

    /*
     * Untagged data fields.
     */

    public static final String OPN_OPMODE       = "OPN_OPMODE";
    public static final String OPN_MOD          = "OPN_MOD";
    public static final String OPN_EXP          = "OPN_EXP";
    public static final String OPN_CRKSEC       = "OPN_CRKSEC";
    public static final String CHP_SLOT         = "CHP_SLOT";
    public static final String CHP_OPER         = "CHP_OPER";
    public static final String CHP_CMD          = "CHP_CMD";
    public static final String CHP_PINFMT       = "CHP_PINFMT";
    public static final String CHP_PINMSG       = "CHP_PINMSG";
    public static final String CHP_RSP          = "CHP_RSP";
    public static final String GPN_METHOD       = "GPN_METHOD";
    public static final String GPN_KEYIDX       = "GPN_KEYIDX";
    public static final String GPN_WKENC        = "GPN_WKENC";
    public static final String GPN_PAN          = "GPN_PAN";
    public static final String GPN_ENTRIES      = "GPN_ENTRIES";
    public static final String GPN_MIN1         = "GPN_MIN1";
    public static final String GPN_MAX1         = "GPN_MAX1";
    public static final String GPN_MSG1         = "GPN_MSG1";
    public static final String GPN_PINBLK       = "GPN_PINBLK";
    public static final String GPN_KSN          = "GPN_KSN";
    public static final String RMC_MSG          = "RMC_MSG";
    public static final String TLI_ACQIDX       = "TLI_ACQIDX";
    public static final String TLI_TABVER       = "TLI_TABVER";
    public static final String TLR_NREC         = "TLR_NREC";
    public static final String TLR_DATA         = "TLR_DATA";

    /*
     * Request data fields.
     */

    public static final String SPE_IDLIST       = "SPE_IDLIST";
    public static final String SPE_MTHDPIN      = "SPE_MTHDPIN";
    public static final String SPE_MTHDDAT      = "SPE_MTHDDAT";
    public static final String SPE_TAGLIST      = "SPE_TAGLIST";
    public static final String SPE_EMVDATA      = "SPE_EMVDATA";
    public static final String SPE_CEXOPT       = "SPE_CEXOPT";
    public static final String SPE_TRACKS       = "SPE_TRACKS";
    public static final String SPE_OPNDIG       = "SPE_OPNDIG";
    public static final String SPE_KEYIDX       = "SPE_KEYIDX";
    public static final String SPE_WKENC        = "SPE_WKENC";
    public static final String SPE_MSGIDX       = "SPE_MSGIDX";
    public static final String SPE_TIMEOUT      = "SPE_TIMEOUT";
    public static final String SPE_MINDIG       = "SPE_MINDIG";
    public static final String SPE_MAXDIG       = "SPE_MAXDIG";
    public static final String SPE_DATAIN       = "SPE_DATAIN";
    public static final String SPE_ACQREF       = "SPE_ACQREF";
    public static final String SPE_APPTYPE      = "SPE_APPTYPE";
    public static final String SPE_AIDLIST      = "SPE_AIDLIST";
    public static final String SPE_AMOUNT       = "SPE_AMOUNT";
    public static final String SPE_CASHBACK     = "SPE_CASHBACK";
    public static final String SPE_TRNDATE      = "SPE_TRNDATE";
    public static final String SPE_TRNTIME      = "SPE_TRNTIME";
    public static final String SPE_GCXOPT       = "SPE_GCXOPT";
    public static final String SPE_GOXOPT       = "SPE_GOXOPT";
    public static final String SPE_FCXOPT       = "SPE_FCXOPT";
    public static final String SPE_TRMPAR       = "SPE_TRMPAR";
    public static final String SPE_DSPMSG       = "SPE_DSPMSG";
    public static final String SPE_ARC          = "SPE_ARC";
    public static final String SPE_IVCBC        = "SPE_IVCBC";
    public static final String SPE_MFNAME       = "SPE_MFNAME";
    public static final String SPE_MFINFO       = "SPE_MFINFO";
    public static final String SPE_MNUOPT       = "SPE_MNUOPT";
    public static final String SPE_TRNTYPE      = "SPE_TRNTYPE";
    public static final String SPE_TRNCURR      = "SPE_TRNCURR";
    public static final String SPE_PANMASK      = "SPE_PANMASK";
    public static final String SPE_PBKMOD       = "SPE_PBKMOD";
    public static final String SPE_PBKEXP       = "SPE_PBKEXP";

    /*
     * Response data fields.
     */

    public static final String PP_SERNUM        = "PP_SERNUM";
    public static final String PP_PARTNBR       = "PP_PARTNBR";
    public static final String PP_MODEL         = "PP_MODEL";
    public static final String PP_MNNAME        = "PP_MNNAME";
    public static final String PP_CAPAB         = "PP_CAPAB";
    public static final String PP_SOVER         = "PP_SOVER";
    public static final String PP_SPECVER       = "PP_SPECVER";
    public static final String PP_MANVERS       = "PP_MANVERS";
    public static final String PP_APPVERS       = "PP_APPVERS";
    public static final String PP_GENVERS       = "PP_GENVERS";
    public static final String PP_KRNLVER       = "PP_KRNLVER";
    public static final String PP_CTLSVER       = "PP_CTLSVER";
    public static final String PP_MCTLSVER      = "PP_MCTLSVER";
    public static final String PP_VCTLSVER      = "PP_VCTLSVER";
    public static final String PP_AECTLSVER     = "PP_AECTLSVER";
    public static final String PP_DPCTLSVER     = "PP_DPCTLSVER";
    public static final String PP_PUREVER       = "PP_PUREVER";
    public static final String PP_DSPTXTSZ      = "PP_DSPTXTSZ";
    public static final String PP_DSPGRSZ       = "PP_DSPGRSZ";
    public static final String PP_MFSUP         = "PP_MFSUP";
    public static final String PP_MKTDESP       = "PP_MKTDESP";
    public static final String PP_MKTDESD       = "PP_MKTDESD";
    public static final String PP_DKPTTDESP     = "PP_DKPTTDESP";
    public static final String PP_DKPTTDESD     = "PP_DKPTTDESD";
    public static final String PP_EVENT         = "PP_EVENT";
    public static final String PP_TRK1INC       = "PP_TRK1INC";
    public static final String PP_TRK2INC       = "PP_TRK2INC";
    public static final String PP_TRK3INC       = "PP_TRK3INC";
    public static final String PP_TRACK1        = "PP_TRACK1";
    public static final String PP_TRACK2        = "PP_TRACK2";
    public static final String PP_TRACK3        = "PP_TRACK3";
    public static final String PP_TRK1KSN       = "PP_TRK1KSN";
    public static final String PP_TRK2KSN       = "PP_TRK2KSN";
    public static final String PP_TRK3KSN       = "PP_TRK3KSN";
    public static final String PP_ENCPAN        = "PP_ENCPAN";
    public static final String PP_ENCPANKSN     = "PP_ENCPANKSN";
    public static final String PP_KSN           = "PP_KSN";
    public static final String PP_VALUE         = "PP_VALUE";
    public static final String PP_DATAOUT       = "PP_DATAOUT";
    public static final String PP_CARDTYPE      = "PP_CARDTYPE";
    public static final String PP_ICCSTAT       = "PP_ICCSTAT";
    public static final String PP_AIDTABINFO    = "PP_AIDTABINFO";
    public static final String PP_PAN           = "PP_PAN";
    public static final String PP_PANSEQNO      = "PP_PANSEQNO";
    public static final String PP_EMVDATA       = "PP_EMVDATA";
    public static final String PP_CHNAME        = "PP_CHNAME";
    public static final String PP_GOXRES        = "PP_GOXRES";
    public static final String PP_PINBLK        = "PP_PINBLK";
    public static final String PP_FCXRES        = "PP_FCXRES";
    public static final String PP_ISRESULTS     = "PP_ISRESULTS";
    public static final String PP_BIGRAND       = "PP_BIGRAND";
    public static final String PP_LABEL         = "PP_LABEL";
    public static final String PP_ISSCNTRY      = "PP_ISSCNTRY";
    public static final String PP_CARDEXP       = "PP_CARDEXP";
    public static final String PP_MFNAME        = "PP_MFNAME";
    public static final String PP_DEVTYPE       = "PP_DEVTYPE";
    public static final String PP_TLRMEM        = "PP_TLRMEM";
    public static final String PP_ENCKRAND      = "PP_ENCKRAND";
    public static final String PP_KSNTDESPnn    = "PP_KSNTDESPnn";
    public static final String PP_KSNTDESDnn    = "PP_KSNTDESDnn";
    public static final String PP_TABVERnn      = "PP_TABVERnn";

    /*
     * Response status enumerator.
     */

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

    /*
     * Data field types.
     */

    public static enum TYPE {
        A, S, N, H, X, B;

        private TYPE() {
            /* Nothing to do */
        }
    }
}
