package com.example.signalsense.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.example.signalsense.data.CpuGridItem;
import com.example.signalsense.R;

import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CpuFrequencyGridAdapter extends ArrayAdapter<CpuGridItem> {

    public CpuFrequencyGridAdapter(@NonNull Context context, ArrayList<CpuGridItem> courseModelArrayList) {
        super(context, 0, courseModelArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listitemView = convertView;
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.cpu_frequency_item, parent, false);
        }

        CpuGridItem courseModel = getItem(position);
        TextView cpuFrequencyTextView = listitemView.findViewById(R.id.tv_cpu_frequency);

        cpuFrequencyTextView.setText(courseModel.getCpuUsagePercentage());

        return listitemView;
    }
}