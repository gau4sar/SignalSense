package com.example.signalsense.utils;

public class Util {

    public static String get4gCqiQamMapping(int cqi) {
        return switch (cqi) {
            case 1, 2, 3, 4, 5, 6 -> "QPSK";
            case 7, 8, 9 -> "16-QAM";
            case 10, 11, 12, 13, 14, 15 -> "64-QAM";
            default -> "Out of Range for " + cqi;
        };
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
