package com.example.signalsense;


import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.mroczis.netmonster.core.db.model.NetworkType;
import cz.mroczis.netmonster.core.model.cell.ICell;


public class MainActivity extends ComponentActivity {

    private Timer timer;
    private List<ICellWithNetworkType> iCellWithNetworkTypeList = new ArrayList<>();
    private ActiveSignalStrength activeSignalStrength = null;
    private int secondsCount = 0;
    private boolean isGpsDialogOpen = false;
    private static final long PERIOD_MILLIS = 1000; // 1 second

    private CellInfoAdapter cellInfoAdapter;

    // UI TextViews
    private TextView ratTypeTextView;

    // String representing the Radio Access Technology (RAT) type
    private String ratTypeString = "";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI TextViews
        ratTypeTextView = findViewById(R.id.tv_rat_type);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cellInfoAdapter = new CellInfoAdapter(new ArrayList<>(),activeSignalStrength);
        recyclerView.setAdapter(cellInfoAdapter);

        // Check for and request permissions
        checkAndRequestPermissions();

        checkIfGpsEnabled();

        // Register the BroadcastReceiver for GPS status changes
        registerReceiver(gpsStatusReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }


    // Periodically run tasks
    private void runEvery1Second() {

        Log.d("SignalSenseLog", "runEvery1Second 1 ");
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("SignalSenseLog", "runEvery1Second 2");

                // Replace with your logic
                populateCellList();

                // Update UI elements
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("SignalSenseLog", "runEvery1Second 4 seconds->" + ++secondsCount);

                        ratTypeTextView.setText(ratTypeString);

                        cellInfoAdapter.setData(iCellWithNetworkTypeList,activeSignalStrength);
                        cellInfoAdapter.notifyDataSetChanged();

                    }
                });
            }
        }, 0, PERIOD_MILLIS); // Schedule task every 1 second
    }


    // This method is used to gather information about the cell network.
    private void populateCellList() {


        /**
         * This is for the native library
         * */

        Log.d("SignalSenseLog", "Code requires both permissions");

        // Get the TelephonyManager service to access cell network information.
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        List<CellInfo> cellInfoList = null;

        // Check if TelephonyManager is available.
        if (telephonyManager != null) {
            cellInfoList = telephonyManager.getAllCellInfo();


            SignalStrength defaultSignalStrength = telephonyManager.getSignalStrength();


            Log.d("SignalSenseLog", "signalStrength-> t " + defaultSignalStrength);

            // Retrieve the default signal strength.
            List<CellSignalStrength> signalStrengthList = defaultSignalStrength.getCellSignalStrengths();

            // Initialize variables to store signal strength values.
            String defaultSSRsrpValue = null;
            String defaultSSrsrqValue = null;
            String defaultSSSnrValue = null;

            String defaultRssiValue = null;
            String defaultRsrpValue = null;
            String defaultRsrqValue = null;
            String defaultSnrValue = null;

            // Iterate through the list of signal strengths.
            for (CellSignalStrength cellSignalStrength:signalStrengthList)
            {
                // If the signal strength is from 5G NR network
                if (cellSignalStrength instanceof CellSignalStrengthNr nrSignalStrength) {
                    defaultSSRsrpValue = String.valueOf(nrSignalStrength.getSsRsrp());

                    // Check and handle invalid values.
                    if(defaultSSRsrpValue.equals("2147483647"))
                    {
                        Log.d("SignalSenseLog", "signalStrength-> defaultSSRsrpValue.equals(2147483647)" + defaultSSRsrpValue);
                        defaultSSRsrpValue =null;
                    }

                    defaultSSrsrqValue = String.valueOf(nrSignalStrength.getSsRsrq()); // Extract NR
                    if(defaultSSrsrqValue.equals("2147483647"))
                    {
                        defaultSSrsrqValue =null;
                    }

                    defaultSSSnrValue = String.valueOf(nrSignalStrength.getSsSinr());
                    if(defaultSSSnrValue.equals("2147483647"))
                    {
                        defaultSSSnrValue =null;
                    }

                    Log.d("SignalSenseLog", "defaultSSrsrqValue-> " + defaultSSrsrqValue);
                }
                else if (cellSignalStrength instanceof CellSignalStrengthLte lteSignalStrength) {

                    defaultRssiValue = String.valueOf(lteSignalStrength.getRssi());

                    // Check and handle invalid values.
                    if(defaultRssiValue.equals("2147483647"))
                    {
                        defaultRssiValue =null;
                    }

                    defaultRsrpValue = String.valueOf(lteSignalStrength.getRsrp());
                    if(defaultRsrpValue.equals("2147483647"))
                    {
                        defaultRsrpValue =null;
                    }

                    defaultRsrqValue = String.valueOf(lteSignalStrength.getRsrq());
                    if(defaultRsrqValue.equals("2147483647"))
                    {
                        defaultRsrqValue =null;
                    }

                    defaultSnrValue = String.valueOf(lteSignalStrength.getRssnr());
                    if(defaultSnrValue.equals("2147483647"))
                    {
                        defaultSnrValue =null;
                    }

                }
            }

            // Create an instance of ActiveSignalStrength with extracted values.
            activeSignalStrength = new ActiveSignalStrength(defaultRssiValue, defaultRsrpValue, defaultRsrqValue, defaultSnrValue, defaultSSRsrpValue, defaultSSrsrqValue, defaultSSSnrValue);


            // Create lists to store registered cell information.
            List<CellInfo> registeredCells = new ArrayList<>();
            List<Integer> registeredCellIds = new ArrayList<>();
            List<RegisteredCellIdWithAlphaLong> registeredCellIdWithAlphaLongList = new ArrayList<>();


            if (cellInfoList != null) {

                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo.isRegistered()) {
                        // This cell is connected to your SIM card
                        registeredCells.add(cellInfo);
                    }
                }

                Log.d("SignalSenseLog", "registeredCells -> " + registeredCells);

                // Iterate through cellInfoList to find registered cells.
                for (CellInfo info : registeredCells) {

                    if (info instanceof CellInfoLte cellInfoLte) {
                        // Extract LTE cell information.
                        CellSignalStrengthLte cellSignalStrength = (CellSignalStrengthLte) ((CellInfoLte) info).getCellSignalStrength();
                        CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
                        cellSignalStrength.getRssnr();
                        int pci = cellIdentity.getPci();
                        Log.d("SignalSenseLog", "CellInfoLteTest  " + "getPci-> " + pci + " info-> " + info);
                        registeredCellIdWithAlphaLongList.add(new RegisteredCellIdWithAlphaLong(pci, (String) cellIdentity.getOperatorAlphaLong()));
                    } else if (info instanceof CellInfoNr cellInfoNr) {
                        // Extract CellInfoNr (5G) cell information.
                        CellSignalStrengthNr cellSignalStrength = (CellSignalStrengthNr) ((CellInfoNr) info).getCellSignalStrength();
                        CellIdentityNr cellIdentity = (CellIdentityNr) cellInfoNr.getCellIdentity();
                        int pci = cellIdentity.getPci();
                        cellSignalStrength.getSsSinr();
                        Log.d("SignalSenseLog", "CellInfoNrTest  " + "getPci-> " + pci + " info-> " + info);
                        registeredCellIdWithAlphaLongList.add(new RegisteredCellIdWithAlphaLong(pci, (String) cellIdentity.getOperatorAlphaLong()));
                    }
                }

                Log.d("SignalSenseLog", "registerdcellids =>  " + registeredCellIds);
            }


            /**
             * This is for the new net-monster library
             * */

            // Create an instance of the Kotlin class NetMonsterHelper.
            NetMonsterHelper netMonsterHelper = new NetMonsterHelper(this);

            // Set a callback to receive network information
            netMonsterHelper.setCallback(new NetMonsterHelper.NetworkInfoCallback() {
                @Override
                public void getCellList(List<ICellWithNetworkType> list, String ratTypes) {

                    // Store the received RAT (Radio Access Technology) types.
                    ratTypeString = ratTypes;

                    // Store the received list of cell network information.
                    iCellWithNetworkTypeList = list;

                    Log.d("SignalSenseLog", "getCellList " + iCellWithNetworkTypeList);
                }
            });

            // Request network information from the netMonsterHelper.
            netMonsterHelper.getCellList(registeredCellIdWithAlphaLongList,activeSignalStrength);

        } else {
            // Show a toast message if TelephonyManager is not initialized.
            Toast.makeText(this, "Telephone manager not initialized", Toast.LENGTH_LONG).show();
        }
    }


    // Check and request necessary permissions
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void checkAndRequestPermissions() {
        String fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        String phoneStatePermission = Manifest.permission.READ_PHONE_STATE;

        int fineLocationPermissionResult = ContextCompat.checkSelfPermission(this, fineLocationPermission);
        int phoneStatePermissionResult = ContextCompat.checkSelfPermission(this, phoneStatePermission);

        boolean fineLocationPermissionGranted = fineLocationPermissionResult == PackageManager.PERMISSION_GRANTED;
        boolean phoneStatePermissionGranted = phoneStatePermissionResult == PackageManager.PERMISSION_GRANTED;

        if (fineLocationPermissionGranted && phoneStatePermissionGranted) {

            runEvery1Second();

        } else {
            // Request permissions
            ActivityCompat.requestPermissions(this, new String[]{fineLocationPermission, phoneStatePermission}, 0);
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // Permissions granted, proceed with your code
                Log.d("SignalSenseLog", "Permissions granted after request.");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    checkAndRequestPermissions();
                }
            } else {
                // Permissions denied, handle accordingly
                Log.d("SignalSenseLog", "Permissions denied after request.");
            }
        }
    }

    // BroadcastReceiver to listen for GPS status changes
    private BroadcastReceiver gpsStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                // GPS status has changed, check if it's enabled

                Log.d("SignalSenseLog", "PROVIDERS_CHANGED_ACTION BroadcastReceiver");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkIfGpsEnabled();
                    }
                }, 3500);

            }
        }
    };

    private void checkIfGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGpsEnabled) {
            Log.d("SignalSenseLog", "isGpsEnabled false");
            // GPS is not enabled, show a dialog or prompt the user to enable it
            showGpsDisabledDialog();
        }
    }

    // Show a dialog to enable GPS
    private void showGpsDisabledDialog() {
        if (!isGpsDialogOpen) {
            isGpsDialogOpen = true; // Set the flag to indicate that the dialog is open

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("GPS is disabled. Do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Open location settings to enable GPS
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Close the app or handle it as needed
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    isGpsDialogOpen = false; // Reset the flag when the dialog is dismissed
                }
            });
            alert.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Resume the periodic task when the app is resumed
        if (timer != null) {
            runEvery1Second();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Pause the periodic task when the app goes into the background
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop the timer when the activity goes into the background
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the BroadcastReceiver to prevent leaks
        unregisterReceiver(gpsStatusReceiver);
        // Clean up any resources when the activity is destroyed
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

}
