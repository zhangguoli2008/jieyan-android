package com.quitbuddy.ui.craving;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quitbuddy.R;
import com.quitbuddy.data.model.CravingEventEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CravingHistoryAdapter extends RecyclerView.Adapter<CravingHistoryAdapter.ViewHolder> {

    private final List<CravingEventEntity> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_craving, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CravingEventEntity event = items.get(position);
        holder.textTimestamp.setText(dateFormat.format(event.timestamp));
        holder.textTrigger.setText(event.trigger + " · 强度 " + event.intensity);
        String details = (event.didSmoke ? "已吸烟" : "成功抵抗") + " · " + (event.note == null ? "" : event.note);
        holder.textDetails.setText(details.trim());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<CravingEventEntity> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textTimestamp;
        final TextView textTrigger;
        final TextView textDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
            textTrigger = itemView.findViewById(R.id.textTrigger);
            textDetails = itemView.findViewById(R.id.textDetails);
        }
    }
}
