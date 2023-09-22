package com.example.signalsense;

import android.util.Log;
import com.example.signalsense.data.CpuGridItem;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class provides various methods to retrieve CPU-related information on an Android device.
 */
public class CpuInfo {

    // File path to read CPU temperature from
    private static final String CPU_TEMP_FILE = "/sys/class/thermal/thermal_zone0/temp";

    /**
     * Retrieves the CPU temperature in degrees Celsius.
     *
     * @return The CPU temperature, or -1 if unable to read the temperature.
     */
    public static float getCpuTemperature() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(CPU_TEMP_FILE));
            String line = br.readLine();
            br.close();

            // Convert the temperature to an integer value
            if (line != null) {
                float temp = Float.parseFloat(line) / 1000; // Convert to Celsius
                DecimalFormat decimalFormat = new DecimalFormat("#.##"); // Format to two decimal places
                String formattedTemp = decimalFormat.format(temp);
                return Float.parseFloat(formattedTemp);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if the temperature couldn't be read
    }

    /**
     * Estimates the overall CPU usage percentage by analyzing core frequencies.
     *
     * @return The estimated CPU usage percentage (0 to 100).
     */
    public static int getCpuUsagePercentage() {
        return getOverallCpuUsage(getCoreUsageFromAllTheCoreFreq());
    }

    /**
     * Calculates the total CPU usage percentage from an array of core usage values.
     *
     * @param coresUsage An array of core usage values.
     * @return The total CPU usage percentage (0 to 100).
     */
    public static int getOverallCpuUsage(int[] coresUsage) {
        if (coresUsage.length < 2)
            return 0;

        //Overall cpu usage is stored in index 0
        return coresUsage[0];
    }

    /**
     * Estimates core usage based on core frequencies.
     *
     * @return An array of core usage values (0 to 100) with the first element representing global CPU usage.
     */
    public static synchronized int[] getCoreUsageFromAllTheCoreFreq() {
        initCoresFreq();
        int nbCores = mCoresFreq.size() + 1;
        int[] coresUsage = new int[nbCores];
        coresUsage[0] = 0;
        for (byte i = 0; i < mCoresFreq.size(); i++) {
            coresUsage[i + 1] = mCoresFreq.get(i).getCurrentUsage();

            //Overall cpu usage is stored in index 0
            coresUsage[0] += coresUsage[i + 1];
        }
        if (mCoresFreq.size() > 0)
            //Overall cpu usage is stored in index 0
            coresUsage[0] /= mCoresFreq.size();
        return coresUsage;
    }

    /**
     * Initializes core frequency information.
     */
    public static void initCoresFreq() {
        if (mCoresFreq == null) {
            int nbCores = getNbCores();
            mCoresFreq = new ArrayList<>();
            for (byte i = 0; i < nbCores; i++) {
                mCoresFreq.add(new CoreFreq(i));
            }
        }
    }

    /**
     * Retrieves a list of CPU cores with their current frequencies.
     *
     * @param totalCores The total number of CPU cores.
     * @return A list of CpuGridItem objects containing CPU core information.
     */
    public static List<CpuGridItem> getAllCoresWithFrequency(int totalCores) {
        List<CpuGridItem> updatedItemList = new ArrayList<>();
        for (byte i = 0; i < totalCores; i++) {
            int currentFrequency = getCurCpuFreq(i);
            Log.d("signalsense", "currentFrequency " + i + " " + currentFrequency);
            String formattedInGhz = formatGhz(currentFrequency);
            Log.d("signalsense", "formattedInGhz " + i + " " + formattedInGhz);
            updatedItemList.add(new CpuGridItem("CPU" + i + ": " + formattedInGhz));
        }
        return updatedItemList;
    }

    /**
     * Formats CPU frequency in GHz.
     *
     * @param hertz CPU frequency in Hertz.
     * @return The formatted CPU frequency in GHz.
     */
    public static String formatGhz(long hertz) {
        double ghz = hertz / 1e6; // Convert Hertz to GHz
        return new DecimalFormat("0.00").format(ghz) + " GHz";
    }

    /**
     * Retrieves the current CPU frequency of a specific core.
     *
     * @param coreIndex Index of the CPU core.
     * @return The current CPU frequency in Hertz.
     */
    public static int getCurCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/scaling_cur_freq");
    }

    /**
     * Retrieves the minimum CPU frequency of a specific core.
     *
     * @param coreIndex Index of the CPU core.
     * @return The minimum CPU frequency in Hertz.
     */
    private static int getMinCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/cpuinfo_min_freq");
    }

    /**
     * Retrieves the maximum CPU frequency of a specific core.
     *
     * @param coreIndex Index of the CPU core.
     * @return The maximum CPU frequency in Hertz.
     */
    private static int getMaxCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/cpuinfo_max_freq");
    }

    /**
     * Reads an integer value from a file.
     *
     * @param path The path to the file containing the integer value.
     * @return The integer value read from the file, or 0 if any error occurs.
     */
    private static int readIntegerFile(String path) {
        int ret = 0;
        try {
            RandomAccessFile reader = new RandomAccessFile(path, "r");
            try {
                String line = reader.readLine();
                ret = Integer.parseInt(line);
            } finally {
                reader.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    /**
     * Gets the number of CPU cores available in the device.
     *
     * @return The number of CPU cores, or 1 if unable to retrieve the count.
     */
    public static int getNbCores() {
        // Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by one or more digits
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            // Default to return 1 core
            return 1;
        }
    }

    /**
     * A helper class to store core frequency information.
     */
    private static class CoreFreq {
        int num, cur, min = 0, max = 0;

        CoreFreq(int num) {
            this.num = num;
            min = getMinCpuFreq(num);
            max = getMaxCpuFreq(num);
        }

        void updateCurFreq() {
            cur = getCurCpuFreq(num);
            // min & max CPU frequencies could not have been properly initialized if the core was offline
            if (min == 0)
                min = getMinCpuFreq(num);
            if (max == 0)
                max = getMaxCpuFreq(num);
        }

        /**
         * Calculates the current core usage percentage.
         *
         * @return The current core usage percentage (0 to 100).
         */
        int getCurrentUsage() {
            updateCurFreq();
            int cpuUsage = 0;
            if (max - min > 0 && max > 0 && cur > 0) {
                cpuUsage = (cur - min) * 100 / (max - min);
            }
            return cpuUsage;
        }
    }

    // Stores current core frequencies
    private static ArrayList<CoreFreq> mCoresFreq;
}
