package com.kinggo.app.shiftworker.view.calendar.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkInfo;

import java.util.ArrayList;

public class WorkInfoRecyclerAdapter extends RecyclerView.Adapter<WorkInfoRecyclerAdapter.CustomViewHolder> {

    private ArrayList<WorkInfo> workInfos;
    private OnWorkInfoListClickEvent onWorkInfoListClickEvent;

    public WorkInfoRecyclerAdapter(ArrayList<WorkInfo> workInfos) {
        this.workInfos = workInfos;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_workinfo, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.workInfo_img.setColorFilter(Color.parseColor(workInfos.get(position).getColor()));
        holder.workInfo_tx.setText(workInfos.get(position).getName().substring(0, 1));

        holder.workInfo_img.setOnClickListener(view -> {
            onWorkInfoListClickEvent.clickWorkInfoEvent(position);
        });
    }


    @Override
    public int getItemCount() {
        return workInfos.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView workInfo_img;
        TextView workInfo_tx;

        CustomViewHolder(View view) {
            super(view);
            workInfo_img = view.findViewById(R.id.item_workInfo_icon);
            workInfo_tx = view.findViewById(R.id.item_workInfo_text);
        }
    }

    public interface OnWorkInfoListClickEvent {
        void clickWorkInfoEvent(int position);
    }

    public void setOnWorkInfoListClickEvent(WorkInfoRecyclerAdapter.OnWorkInfoListClickEvent onWorkInfoListClickEvent) {
        this.onWorkInfoListClickEvent = onWorkInfoListClickEvent;
    }
}