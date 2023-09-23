package com.example.signalsense.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.signalsense.ActiveSignalStrength;
import com.example.signalsense.ICellWithNetworkType;
import com.example.signalsense.NetMonsterHelper;
import com.example.signalsense.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import cz.mroczis.netmonster.core.db.model.NetworkType;
import cz.mroczis.netmonster.core.model.cell.CellLte;
import cz.mroczis.netmonster.core.model.cell.CellNr;

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

        TextView lteENBTextView;
        TextView networkTypeTextView;
        TextView nsaNetworkTypeTextView;
        TextView networkTechnologyTextView;
        TextView timestampTextView;
        TextView nsaTimestampTextView;
        TextView lteECITextView;
        TextView lteCIDTextView;
        TextView lteTACTextView;
        TextView ltePCITextView;
        TextView nrNCITextView;
        TextView nrCIDTextView;
        TextView nrGNBTextView;
        TextView nrTACTextView;
        TextView nrPCITextView;
        LinearLayout strengthValuesFor5g;

        public CellInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            rsrqTextView = itemView.findViewById(R.id.tv_rsrq);
            rsrpTextView = itemView.findViewById(R.id.tv_rsrp);
            snrTextView = itemView.findViewById(R.id.tv_snr);

            ssRsrqTextView = itemView.findViewById(R.id.tv_ssrsrq);
            ssRsrpTextView = itemView.findViewById(R.id.tv_ssrsrp);
            ssSinrTextView = itemView.findViewById(R.id.tv_sssinr);

            lteECITextView = itemView.findViewById(R.id.tv_lte_eci);
            lteCIDTextView = itemView.findViewById(R.id.tv_lte_cid);
            lteENBTextView = itemView.findViewById(R.id.tv_enb);
            lteTACTextView = itemView.findViewById(R.id.tv_lte_tac);
            ltePCITextView = itemView.findViewById(R.id.tv_lte_pci);


            nrNCITextView = itemView.findViewById(R.id.tv_nr_nci);
            nrCIDTextView= itemView.findViewById(R.id.tv_nr_cid);
            nrGNBTextView = itemView.findViewById(R.id.tv_gnb);
            nrTACTextView = itemView.findViewById(R.id.tv_nr_tac);
            nrPCITextView = itemView.findViewById(R.id.tv_nr_pci);

            nsaTimestampTextView = itemView.findViewById(R.id.tv_nsa_timestamp);
            nsaNetworkTypeTextView = itemView.findViewById(R.id.tv_networkType_nsa);

            networkTypeTextView = itemView.findViewById(R.id.tv_networkType);
            networkTechnologyTextView = itemView.findViewById(R.id.tv_network_technology);
            timestampTextView = itemView.findViewById(R.id.tv_timestamp);

            strengthValuesFor5g = itemView.findViewById(R.id.ll_5g_strength_values);
        }

        public void populateCellData(ICellWithNetworkType iCellWithNetworkType, String alphaLong, NetworkType networkType, long currentMilliseconds) {

            CellLte cellLte = iCellWithNetworkType.getCellLte();
            CellNr _5gSignalsCellNr = iCellWithNetworkType.get_5gSignals();

            if (iCellWithNetworkType.getRatType().equals(NetMonsterHelper.RatType._5G_NSA.getValue())) {

                // If it's a 5G NSA network, show 5g signal strength values
                strengthValuesFor5g.setVisibility(View.VISIBLE);

                if (_5gSignalsCellNr != null) {

                    // Populate 5G signal strength values if available
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

                    if (_5gSignalsCellNr.getNci() != null) {
                        nrNCITextView.setText(_5gSignalsCellNr.getNci().toString());
                    } else if (iCellWithNetworkType.getCellIdentityNr() != null && iCellWithNetworkType.getCellIdentityNr().getNci() != -1) {
                        nrNCITextView.setText(String.valueOf(iCellWithNetworkType.getCellIdentityNr().getNci()));
                    }
                    if (_5gSignalsCellNr.getTac() != null) {
                        nrTACTextView.setText(_5gSignalsCellNr.getTac().toString());
                    } else if (iCellWithNetworkType.getCellIdentityNr() != null && iCellWithNetworkType.getCellIdentityNr().getTac() != -1) {
                        nrTACTextView.setText(String.valueOf(iCellWithNetworkType.getCellIdentityNr().getTac()));
                    }
                    if (_5gSignalsCellNr.getPci() != null) {
                        nrPCITextView.setText(_5gSignalsCellNr.getPci().toString());
                    } else if (iCellWithNetworkType.getCellIdentityNr() != null && iCellWithNetworkType.getCellIdentityNr().getPci() != -1) {
                        nrPCITextView.setText(String.valueOf(iCellWithNetworkType.getCellIdentityNr().getPci()));
                    }

                } else if (activeSignalStrength.getSsRsrp() != null || activeSignalStrength.getSsRsrq() != null || activeSignalStrength.getSsSinr() != null) {

                    // Populate signal strength values from the active signal if available
                    if (activeSignalStrength.getSsRsrp() != null) {
                        ssRsrpTextView.setText(activeSignalStrength.getSsRsrp());
                    } else {
                        ssRsrpTextView.setText("loading");
                    }
                    if (activeSignalStrength.getSsRsrq() != null) {
                        ssRsrqTextView.setText(activeSignalStrength.getSsRsrq());
                    } else {
                        ssRsrqTextView.setText("loading");
                    }
                    if (activeSignalStrength.getSsSinr() != null) {
                        ssSinrTextView.setText(activeSignalStrength.getSsSinr());
                    } else {
                        ssSinrTextView.setText("loading");
                    }

                    if (iCellWithNetworkType.getCellIdentityNr() != null && iCellWithNetworkType.getCellIdentityNr().getNci() != -1) {
                        nrNCITextView.setText(String.valueOf(iCellWithNetworkType.getCellIdentityNr().getNci()));
                    } else {
                        nrNCITextView.setText("N/A");
                    }
                    if (iCellWithNetworkType.getCellIdentityNr() != null && iCellWithNetworkType.getCellIdentityNr().getTac() != -1) {
                        nrTACTextView.setText(String.valueOf(iCellWithNetworkType.getCellIdentityNr().getTac()));
                    } else {
                        nrTACTextView.setText("N/A");
                    }
                    if (iCellWithNetworkType.getCellIdentityNr() != null && iCellWithNetworkType.getCellIdentityNr().getPci() != -1) {
                        nrPCITextView.setText(String.valueOf(iCellWithNetworkType.getCellIdentityNr().getPci()));
                    } else {
                        nrPCITextView.setText("N/A");
                    }
                } else {
                    // Hide strength values if no data is available
                    strengthValuesFor5g.setVisibility(View.GONE);
                }


                nsaTimestampTextView.setText("Timestamp: " + convertMillisecondsToCDT(currentMilliseconds));

                nsaNetworkTypeTextView.setText(alphaLong + " 5G");
            } else {

                // Hide 5G NSA-related views
                strengthValuesFor5g.setVisibility(View.GONE);
            }


            // Populate 4G LTE cell data
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

            if (cellLte.getEci() != null) {
                lteECITextView.setText(cellLte.getEci().toString());
            }
            if (cellLte.getTac() != null) {
                lteTACTextView.setText(cellLte.getTac().toString());
            }
            if (cellLte.getPci() != null) {
                ltePCITextView.setText(cellLte.getPci().toString());
            }
            if (cellLte.getCid() != null) {
                lteCIDTextView.setText(cellLte.getCid().toString());
            }
            if (cellLte.getEnb() != null) {
                lteENBTextView.setText("" + cellLte.getEnb());
            }


            networkTypeTextView.setText(alphaLong + " 4G");
            timestampTextView.setText("Timestamp: " + convertMillisecondsToCDT(currentMilliseconds));


            //networkTechnologyTextView.setText("Network technology: " + networkType);
            networkTechnologyTextView.setVisibility(View.GONE);
        }

        public void populateNrData(CellNr cellNr, String alphaLong, NetworkType networkType, long currentMilliseconds) {


            if (cellNr.getSignal().getSsRsrq() != null) {
                rsrqTextView.setText("" + cellNr.getSignal().getSsRsrq());
            }

            if (cellNr.getSignal().getSsRsrp() != null) {
                rsrpTextView.setText("" + +cellNr.getSignal().getSsRsrp());
            }

            if (cellNr.getSignal().getSsSinr() != null) {
                snrTextView.setText("" + +cellNr.getSignal().getSsSinr());
            }

            lteENBTextView.setVisibility(View.GONE);
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