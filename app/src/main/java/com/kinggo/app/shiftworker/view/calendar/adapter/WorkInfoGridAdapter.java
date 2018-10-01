package com.kinggo.app.shiftworker.view.calendar.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkInfo;

import org.w3c.dom.Text;

import java.util.List;

public class WorkInfoGridAdapter extends BaseAdapter {

    private List<WorkInfo> workInfo_list;
    private LayoutInflater inflater;

    private ViewHolder holder;

    public WorkInfoGridAdapter(List<WorkInfo> workInfo_list, Context context) {
        this.workInfo_list = workInfo_list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return workInfo_list.size();
    }

    @Override
    public Object getItem(int position) {
        return workInfo_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.item_workinfo, viewGroup, false);
            holder = new ViewHolder();

            holder.workInfo_icon = view.findViewById(R.id.item_workInfo_icon);
            holder.workInfo_text = view.findViewById(R.id.item_workInfo_text);
            holder.workInfo_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);

            view.setTag(holder);
        } else
            holder = (ViewHolder) view.getTag();

        resetView(position);
        return view;
    }

    private void resetView(int position) {
        holder.workInfo_icon.setColorFilter(Color.parseColor(workInfo_list.get(position).getColor()));
        holder.workInfo_text.setText(workInfo_list.get(position).getName().substring(0, 1));
    }

    private static class ViewHolder {
        ImageView workInfo_icon;
        TextView workInfo_text;
    }
}
