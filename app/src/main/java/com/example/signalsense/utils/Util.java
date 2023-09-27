package com.example.signalsense.utils;

public class Util {

    public static String get4gCqiQamMapping(int cqi) {
        switch (cqi) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return "QPSK";
            case 7:
            case 8:
            case 9:
                return "16-QAM";
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return "64-QAM";
            default:
                return "Out of Range for " + cqi;
        }
    }

    public static String get5gCqiQamMapping(int cqi) {
        if (cqi >= 1 && cqi <= 6) {
            return "QPSK";
        } else if (cqi >= 7 && cqi <= 9) {
            return "16-QAM";
        } else if (cqi >= 10 && cqi <= 15) {
            return "64-QAM";
        } else {
            return "Out of Range for " + cqi;
        }
    }
}
