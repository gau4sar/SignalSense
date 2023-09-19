package com.example.signalsense;

import android.telephony.SignalStrength;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cz.mroczis.netmonster.core.db.model.NetworkType;
import cz.mroczis.netmonster.core.model.cell.CellLte;
import cz.mroczis.netmonster.core.model.cell.CellNr;
import cz.mroczis.netmonster.core.model.cell.ICell;

public class CellInfoAdapter extends RecyclerView.Adapter<CellInfoAdapter.CellInfoViewHolder> {

    SignalStrength activeSignalStrength ;
    private List<ICellWithNetworkType> cellInfoList;

    public CellInfoAdapter(List<ICellWithNetworkType> cellInfoList,SignalStrength activeSignalStrength) {
        this.cellInfoList = cellInfoList;
        this.activeSignalStrength =activeSignalStrength;
    }

    @NonNull
    @Override
    public CellInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_info_item, parent, false);
        return new CellInfoViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull CellInfoViewHolder holder, int position) {
        ICellWithNetworkType iCellWithNetworkType = cellInfoList.get(position);



        ICell iCell = iCellWithNetworkType.getICell();

        // Get the current time in milliseconds
        long currentMilliseconds = System.currentTimeMillis();


        if (iCell instanceof CellLte) {
            CellLte cellInfoLte = (CellLte) iCell;

            // Populate TextViews with LTE cell data
            holder.populateLteData(cellInfoLte, iCellWithNetworkType.getAlphaLong(), iCellWithNetworkType.getNetworkType(), currentMilliseconds);
        } else if (iCell instanceof CellNr) {
            CellNr cellInfoLte = (CellNr) iCell;

            // Populate TextViews with NR cell data
            holder.populateNrData(cellInfoLte, iCellWithNetworkType.getAlphaLong(), iCellWithNetworkType.getNetworkType(), currentMilliseconds);

        }
    }

    @Override
    public int getItemCount() {
        return cellInfoList.size();
    }

    public void setData(List<ICellWithNetworkType> cellInfoList,SignalStrength activeSignalStrength) {
        this.cellInfoList = cellInfoList;
        this.activeSignalStrength =activeSignalStrength;
    }

    static class CellInfoViewHolder extends RecyclerView.ViewHolder {
        TextView rsrqTextView;
        TextView rsrpTextView;
        TextView sinrTextView;
        TextView eNBTextView;
        TextView networkTypeTextView;
        TextView cellIdTextView;
        TextView networkTechnologyTextView;
        TextView timestampTextView;

        public CellInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            rsrqTextView = itemView.findViewById(R.id.tv_rsrq);
            rsrpTextView = itemView.findViewById(R.id.tv_rsrp);
            sinrTextView = itemView.findViewById(R.id.tv_sinr);
            eNBTextView = itemView.findViewById(R.id.tv_enb);
            networkTypeTextView = itemView.findViewById(R.id.tv_networkType);
            networkTechnologyTextView = itemView.findViewById(R.id.tv_network_technology);
            cellIdTextView = itemView.findViewById(R.id.tv_cellId);
            timestampTextView = itemView.findViewById(R.id.tv_timestamp);
        }

        public void populateLteData(CellLte cellLte, String alphaLong, NetworkType networkType, long currentMilliseconds) {
            cellIdTextView.setText("CellId: " + cellLte.getPci());

            rsrqTextView.setText("RSRQ: " + cellLte.getSignal().getRsrq());
            rsrpTextView.setText("RSRP: " + cellLte.getSignal().getRsrp());
            sinrTextView.setText("SNR: " + cellLte.getSignal().getSnr());
            eNBTextView.setText("eNB: " + cellLte.getEnb());
            networkTypeTextView.setText(alphaLong + " 4G");
            timestampTextView.setText("Timestamp: " + convertMillisecondsToCDT(currentMilliseconds));
            //networkTechnologyTextView.setText("Network technology: " + networkType);
            networkTechnologyTextView.setVisibility(View.GONE);
        }

        public void populateNrData(CellNr cellNr, String alphaLong, NetworkType networkType, long currentMilliseconds) {

            activeSignalStrength

            cellIdTextView.setText("CellId: " + cellNr.getPci());

            rsrqTextView.setText("RSRQ: " + cellNr.getSignal().getSsRsrq());
            rsrpTextView.setText("RSRP: " + cellNr.getSignal().getSsRsrp());

            sinrTextView.setText("SINR: " + cellNr.getSignal().getSsSinr());
            eNBTextView.setVisibility(View.GONE);
            networkTypeTextView.setText(alphaLong + " 5G");
            timestampTextView.setText("Timestamp: " + convertMillisecondsToCDT(currentMilliseconds));
            //networkTechnologyTextView.setText("Network technology: " + networkType);
            networkTechnologyTextView.setVisibility(View.GONE);
        }
    }

    public static String formatMilliseconds(long milliseconds) {
        long seconds = milliseconds / 1000;

        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + " minutes " + remainingSeconds + " seconds";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;
            return hours + " hours " + remainingMinutes + " minutes " + remainingSeconds + " seconds";
        } else {
            long days = seconds / 86400;
            long remainingHours = (seconds % 86400) / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;
            return days + " days " + remainingHours + " hours " + remainingMinutes + " minutes " + remainingSeconds + " seconds";
        }
    }

    private static String convertMillisecondsToCDT(long milliseconds) {
        // Create a SimpleDateFormat instance with CDT timezone
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        TimeZone cdtTimeZone = TimeZone.getTimeZone("America/Chicago");
        dateFormat.setTimeZone(cdtTimeZone);

        // Convert milliseconds to a Date object
        Date date = new Date(milliseconds);

        // Format the Date object to CDT datetime format
        return dateFormat.format(date);
    }


    // Decimal -> hexadecimal
    public String DecToHex(int dec) {
        return String.format("%x", dec);
    }

    // hex -> decimal
    public int HexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }
}