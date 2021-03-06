package com.uca;

public enum PCode {
    LIT,
    OPR,
    CAR,
    ALM,
    LLA,
    INS,
    SAL,
    SAC,
    SAI,
    ALO,
    CAO,
    PAR,
    RET;

    static PCode get(String s){
        switch (s){
            case "LIT":
                return LIT;
            case "OPR":
                return OPR;
            case "CAR":
                return CAR;
            case "ALM":
                return ALM;
            case "LLA":
                return LLA;
            case "INS":
                return INS;
            case "SAL":
                return SAL;
            case "SAC":
                return SAC;
            case "SAI":
                return SAI;
            case "ALO":
                return ALO;
            case "CAO":
                return CAO;
            case "PAR":
                return PAR;
            case "RET":
                return RET;
        }
        return null;
    }
}
