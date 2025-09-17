package com.quitbuddy.ui.achievements;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.quitbuddy.R;
import com.quitbuddy.data.model.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {

    private final List<Achievement> items = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = items.get(position);
        holder.textTitle.setText(achievement.name);
        holder.textDescription.setText(achievement.description);
        holder.checkStatus.setChecked(achievement.achieved);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<Achievement> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCheckBox checkStatus;
        final TextView textTitle;
        final TextView textDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkStatus = itemView.findViewById(R.id.checkStatus);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
        }
    }
}
