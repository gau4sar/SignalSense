package com.example.signalsense;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import cz.mroczis.netmonster.core.db.model.NetworkType;
import cz.mroczis.netmonster.core.model.cell.CellLte;
import cz.mroczis.netmonster.core.model.cell.CellNr;
import cz.mroczis.netmonster.core.model.cell.ICell;

public class CellInfoAdapter extends RecyclerView.Adapter<CellInfoAdapter.CellInfoViewHolder> {

    static ActiveSignalStrength activeSignalStrength;
    private List<ICellWithNetworkType> cellInfoList;

    public CellInfoAdapter(List<ICellWithNetworkType> cellInfoList, ActiveSignalStrength activeSignalStrength) {
        this.cellInfoList = cellInfoList;
        this.activeSignalStrength = activeSignalStrength;
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


        // Get the current time in milliseconds
        long currentMilliseconds = System.currentTimeMillis();

        holder.populateCellData(iCellWithNetworkType, iCellWithNetworkType.getAlphaLong(), iCellWithNetworkType.getNetworkType(), currentMilliseconds);


/*
        if (iCell instanceof CellLte cellInfoLte) {

            // Populate TextViews with LTE cell data
            holder.populateCellData(cellInfoLte, iCellWithNetworkType.getAlphaLong(), iCellWithNetworkType.getNetworkType(), currentMilliseconds);
        } else if (iCell instanceof CellNr cellInfoLte) {

            // Populate TextViews with NR cell data
            holder.populateNrData(cellInfoLte, iCellWithNetworkType.getAlphaLong(), iCellWithNetworkType.getNetworkType(), currentMilliseconds);

        }*/
    }

    @Override
    public int getItemCount() {
        return cellInfoList.size();
    }

    public void setData(List<ICellWithNetworkType> cellInfoList, ActiveSignalStrength activeSignalStrength) {
        this.cellInfoList = cellInfoList;
        this.activeSignalStrength = activeSignalStrength;
    }

    static class CellInfoViewHolder extends RecyclerView.ViewHolder {
        TextView rsrqTextView;
        TextView rsrpTextView;
        TextView snrTextView;

        TextView ssRsrqTextView;
        TextView ssRsrpTextView;
        TextView ssSinrTextView;

        TextView eNBTextView;
        TextView networkTypeTextView;
        TextView nsaNetworkTypeTextView;
        TextView cellIdTextView;
        TextView networkTechnologyTextView;
        TextView timestampTextView;
        TextView nsaCellIdTextView;
        TextView nsaTimestampTextView;
        LinearLayout strengthValuesFor5g;

        public CellInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            rsrqTextView = itemView.findViewById(R.id.tv_rsrq);
            rsrpTextView = itemView.findViewById(R.id.tv_rsrp);
            snrTextView = itemView.findViewById(R.id.tv_snr);

            ssRsrqTextView = itemView.findViewById(R.id.tv_ssrsrq);
            ssRsrpTextView = itemView.findViewById(R.id.tv_ssrsrp);
            ssSinrTextView = itemView.findViewById(R.id.tv_sssinr);
            nsaCellIdTextView = itemView.findViewById(R.id.tv_nsa_cellId);
            nsaTimestampTextView = itemView.findViewById(R.id.tv_nsa_timestamp);
            nsaNetworkTypeTextView = itemView.findViewById(R.id.tv_networkType_nsa);

            eNBTextView = itemView.findViewById(R.id.tv_enb);
            networkTypeTextView = itemView.findViewById(R.id.tv_networkType);
            networkTechnologyTextView = itemView.findViewById(R.id.tv_network_technology);
            cellIdTextView = itemView.findViewById(R.id.tv_cellId);
            timestampTextView = itemView.findViewById(R.id.tv_timestamp);

            strengthValuesFor5g = itemView.findViewById(R.id.ll_5g_strength_values);
        }

        public void populateCellData(ICellWithNetworkType iCellWithNetworkType, String alphaLong, NetworkType networkType, long currentMilliseconds) {

            Log.d("Signalsense", "populateCellData NetworkType" + networkType);
            Log.d("Signalsense", "populateCellData activeSignalStrength" + activeSignalStrength);

            CellLte cellLte = iCellWithNetworkType.getCellLte();
            CellNr _5gSignalsCellNr = iCellWithNetworkType.get_5gSignals();

            if (iCellWithNetworkType.getRatType().equals(NetMonsterHelper.RatType._5G_NSA.getValue())) {

                strengthValuesFor5g.setVisibility(View.VISIBLE);

                if (_5gSignalsCellNr != null) {

                    if (_5gSignalsCellNr.getSignal().getSsRsrp() != null) {
                        ssRsrpTextView.setText(_5gSignalsCellNr.getSignal().getSsRsrp().toString());

                    } else if (activeSignalStrength.getSsRsrp() != null) {
                        ssRsrpTextView.setText(activeSignalStrength.getSsRsrp());
                    } else {
                        ssRsrpTextView.setText("loading");
                    }

                    if (_5gSignalsCellNr.getSignal().getSsRsrq() != null) {
                        ssRsrqTextView.setText(_5gSignalsCellNr.getSignal().getSsRsrq().toString());

                    } else if (activeSignalStrength.getSsRsrq() != null) {
                        ssRsrqTextView.setText(activeSignalStrength.getSsRsrq());
                    } else {
                        ssRsrqTextView.setText("loading");
                    }

                    if (_5gSignalsCellNr.getSignal().getSsSinr() != null) {
                        ssSinrTextView.setText(_5gSignalsCellNr.getSignal().getSsSinr().toString());
                    } else if (activeSignalStrength.getSsSinr() != null) {

                        if (Objects.equals(activeSignalStrength.getSsSinr(), "2147483647")) {
                            ssSinrTextView.setText("N/A");
                        } else {
                            ssSinrTextView.setText(activeSignalStrength.getSsSinr());
                        }
                    } else {
                        ssSinrTextView.setText("loading");
                    }

                } else {
                    if (activeSignalStrength.getSsRsrp() != null) {
                        ssRsrpTextView.setText(activeSignalStrength.getSsRsrp());
                    }
                    else{
                        ssRsrpTextView.setText("loading");
                    }
                    if (activeSignalStrength.getSsRsrq() != null) {
                        ssRsrqTextView.setText(activeSignalStrength.getSsRsrq());
                    }
                    else{
                        ssRsrqTextView.setText("loading");
                    }
                    if (activeSignalStrength.getSsSinr() != null) {
                        ssSinrTextView.setText(activeSignalStrength.getSsSinr());
                    }
                    else{
                        ssSinrTextView.setText("loading");
                    }
                }


                nsaCellIdTextView.setText("CellId: " + cellLte.getPci());
                nsaTimestampTextView.setText("Timestamp: " + convertMillisecondsToCDT(currentMilliseconds));

                nsaNetworkTypeTextView.setText(alphaLong + " 5G");
            } else {

                strengthValuesFor5g.setVisibility(View.GONE);

            }


            if (cellLte.getSignal().getRsrp() != null) {
                rsrpTextView.setText(cellLte.getSignal().getRsrp().toString());
            } else if (activeSignalStrength.getSsRsrp() != null) {
                rsrpTextView.setText(activeSignalStrength.getRsrp());
            }

            if (cellLte.getSignal().getRsrq() != null) {
                rsrqTextView.setText(cellLte.getSignal().getRsrq().toString());
            } else if (activeSignalStrength.getSsRsrp() != null) {
                rsrqTextView.setText(activeSignalStrength.getRsrq());
            }

            if (cellLte.getSignal().getSnr() != null) {
                snrTextView.setText(cellLte.getSignal().getSnr().toString());
            } else if (activeSignalStrength.getSnr() != null) {
                snrTextView.setText(activeSignalStrength.getSnr());
            }


            cellIdTextView.setText("CellId: " + cellLte.getPci());
            networkTypeTextView.setText(alphaLong + " 4G");
            timestampTextView.setText("Timestamp: " + convertMillisecondsToCDT(currentMilliseconds));

            eNBTextView.setText("" + cellLte.getEnb());


            //networkTechnologyTextView.setText("Network technology: " + networkType);
            networkTechnologyTextView.setVisibility(View.GONE);
        }

        public void populateNrData(CellNr cellNr, String alphaLong, NetworkType networkType, long currentMilliseconds) {

            cellIdTextView.setText("CellId: " + cellNr.getPci());


            if (cellNr.getSignal().getSsRsrq() != null) {
                rsrqTextView.setText("" + cellNr.getSignal().getSsRsrq());
            }

            if (cellNr.getSignal().getSsRsrp() != null) {
                rsrpTextView.setText("" + +cellNr.getSignal().getSsRsrp());
            }

            if (cellNr.getSignal().getSsSinr() != null) {
                snrTextView.setText("" + +cellNr.getSignal().getSsSinr());
            }

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