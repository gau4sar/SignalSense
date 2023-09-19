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

    SignalStrength defaultSignalStrength = null;

    ActiveSignalStrength activeSignalStrength = null;

    int secondsCount = 0;
    private boolean isGpsDialogOpen = false;
    private static final long PERIOD_MILLIS = 1000; // 1 second

    private RecyclerView recyclerView;
    private CellInfoAdapter cellInfoAdapter;

    private MutableLiveData<Integer> rsrqLiveData = new MutableLiveData<>();

    TextView ratTypeTextView;
    TextView defaultRssiTextView;
    TextView defaultRsrpTextView;
    TextView defaultRsrqTextView;
    TextView defaultSnrTextView;
    TextView defaultSSRsrpTextView;
    TextView defaultSSrsrqTextView;
    TextView defaultSSSnrTextView;

    String defaultRssiValue;
    String defaultRsrpValue;
    String defaultRsrqValue;
    String defaultSnrValue;
    String defaultSSRsrpValue;
    String defaultSSrsrqValue;
    String defaultSSSnrValue;

    String ratTypeString = "";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ratTypeTextView = findViewById(R.id.tv_rat_type);
        defaultRssiTextView = findViewById(R.id.tv_default_rssi);
        defaultRsrpTextView = findViewById(R.id.tv_default_rsrp);
        defaultRsrqTextView = findViewById(R.id.tv_default_rsrq);
        defaultSnrTextView = findViewById(R.id.tv_default_snr);
        defaultSSRsrpTextView = findViewById(R.id.tv_default_ssrsrp);
        defaultSSrsrqTextView = findViewById(R.id.tv_default_ssrsrq);
        defaultSSSnrTextView = findViewById(R.id.tv_default_sssnr);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pass the additional string to the adapter
        cellInfoAdapter = new CellInfoAdapter(new ArrayList<>(),activeSignalStrength);
        recyclerView.setAdapter(cellInfoAdapter);

        // Check for and request permissions
        checkAndRequestPermissions();

        checkIfGpsEnabled();

        // Register the BroadcastReceiver
        registerReceiver(gpsStatusReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

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

                // Place your code to be executed every 1 second here
                // This code runs on the UI thread and can safely update UI elements
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("SignalSenseLog", "runEvery1Second 3");

                        Log.d("SignalSenseLog", "runEvery1Second 4 seconds->" + ++secondsCount);

                        ratTypeTextView.setText(ratTypeString);

                        /*defaultRssiTextView.setText(defaultRssiValue);
                        defaultRsrpTextView.setText(defaultRsrpValue);
                        defaultRsrqTextView.setText(defaultRsrqValue);
                        defaultSnrTextView.setText(defaultSnrValue);

                        defaultSSRsrpTextView.setText(defaultSSRsrpValue);
                        defaultSSrsrqTextView.setText(defaultSSrsrqValue);
                        defaultSSSnrTextView.setText(defaultSSSnrValue);*/

                        cellInfoAdapter.setData(iCellWithNetworkTypeList,activeSignalStrength);
                        cellInfoAdapter.notifyDataSetChanged();

                    }
                });
            }
        }, 0, PERIOD_MILLIS); // Schedule task every 1 second
    }

    private void populateCellList() {


        /**
         * This is for the native library
         * */

        Log.d("SignalSenseLog", "Code requires both permissions");

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        List<CellInfo> cellInfoList = null;

        if (telephonyManager != null) {
            cellInfoList = telephonyManager.getAllCellInfo();


            defaultSignalStrength = telephonyManager.getSignalStrength();



            Log.d("SignalSenseLog", "signalStrength-> " + defaultSignalStrength);


            List<CellSignalStrength> signalStrengthList = defaultSignalStrength.getCellSignalStrengths();

            for (CellSignalStrength cellSignalStrength:signalStrengthList)
            {
                // If the signal strength is from 5G NR network
                if (cellSignalStrength instanceof CellSignalStrengthNr nrSignalStrength) {
                    defaultSSRsrpValue= String.valueOf(nrSignalStrength.getSsRsrp()); // Extract NR RSSI value
                    defaultSSrsrqValue = String.valueOf(nrSignalStrength.getSsRsrq()); // Extract NR
                    defaultSSSnrValue = String.valueOf(nrSignalStrength.getSsSinr()); // Extract NR RSSI value

                    Log.d("SignalSenseLog", "defaultSSrsrqValue-> " + defaultSSrsrqValue);
                }
                else if (cellSignalStrength instanceof CellSignalStrengthLte lteSignalStrength) {

                    defaultRssiValue = String.valueOf(lteSignalStrength.getRssi()); // Extract NR RSSI value
                    defaultRsrpValue = String.valueOf(lteSignalStrength.getRsrp()); // Extract NR RSSI value
                    defaultRsrqValue = String.valueOf(lteSignalStrength.getRsrq()); // Extract NR RSSI value
                    defaultSnrValue = String.valueOf(lteSignalStrength.getRssnr()); // Extract NR RSSI value

                }
            }


            activeSignalStrength = new ActiveSignalStrength(defaultRssiValue,defaultRsrpValue,defaultRsrqValue,defaultSnrValue,defaultSSRsrpValue,defaultSSrsrqValue,defaultSSSnrValue);


            List<CellInfo> registeredCells = new ArrayList<>();
            List<Integer> registeredCellIds = new ArrayList<>();
            List<RegisteredCellIdWithAlphaLong> registeredCellIdWithAlphaLongList = new ArrayList<>();


            if (cellInfoList != null) {
                Log.d("SignalSenseLog", "cellInfo != null " + cellInfoList);


            /*for(CellLocation cellLocation : cellLocations){
                Log.d("SignalSenseLog", "getCellLocation-> " + cellLocation);
            }*/

                if (cellInfoList.size() > 1) {
                    Log.d("SignalSenseLog", "cellInfo last value " + cellInfoList.get(cellInfoList.size() - 1));
                }

                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo.isRegistered()) {
                        // This cell is connected to your SIM card
                        registeredCells.add(cellInfo);
                    }
                }


                Log.d("SignalSenseLog", "registeredCells -> " + registeredCells);

                Log.d("SignalSenseLog", "total cellInfoList size-> " + cellInfoList.size() + "  Registered cell info size" + registeredCells.size());

                for (CellInfo info : registeredCells) {
                    Log.d("SignalSenseLog", "info in getCellIdentity " + info.getCellIdentity());


                    if (info instanceof CellInfoLte cellInfoLte) {
                        CellSignalStrengthLte cellSignalStrength = (CellSignalStrengthLte) ((CellInfoLte) info).getCellSignalStrength();
                        CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
                        cellSignalStrength.getRssnr();
                        int pci = cellIdentity.getPci();
                        Log.d("SignalSenseLog", "CellInfoLteTest  " + "getPci-> " + pci + " info-> " + info);
                        registeredCellIdWithAlphaLongList.add(new RegisteredCellIdWithAlphaLong(pci, (String) cellIdentity.getOperatorAlphaLong()));
                    } else if (info instanceof CellInfoNr cellInfoNr) {
                        // Handle CellInfoNr (5G) similarly
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


            // Create an instance of the Kotlin class
            NetMonsterHelper netMonsterHelper = new NetMonsterHelper(this);

            // Set a callback to receive network information
            netMonsterHelper.setCallback(new NetMonsterHelper.NetworkInfoCallback() {
                @Override
                public void getCellList(List<ICellWithNetworkType> list, String ratTypes) {


                    ratTypeString = ratTypes;

                    iCellWithNetworkTypeList = list;

                    Log.d("SignalSenseLog", "getCellList " + iCellWithNetworkTypeList);
                }

            });

            // Request network information
            netMonsterHelper.getCellList(registeredCellIdWithAlphaLongList,defaultSignalStrength);

        } else {
            Toast.makeText(this, "Telephone manager not initialized", Toast.LENGTH_LONG).show();
        }
    }


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

    // Define a BroadcastReceiver to listen for GPS status changes
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
