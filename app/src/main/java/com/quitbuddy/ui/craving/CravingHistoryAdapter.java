package com.quitbuddy.ui.craving;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.quitbuddy.R;
import com.quitbuddy.data.model.CravingEventEntity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CravingHistoryAdapter extends ListAdapter<CravingEventEntity, CravingHistoryAdapter.ViewHolder> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    private String query = "";

    public CravingHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_craving, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CravingEventEntity event = getItem(position);
        holder.textTimestamp.setText(dateFormat.format(event.timestamp));
        holder.textTrigger.setText(highlight(event.trigger));
        String note = event.note == null ? "" : event.note;
        String status = event.didSmoke ? holder.itemView.getContext().getString(R.string.craving_history_filter_smoked_yes)
                : holder.itemView.getContext().getString(R.string.craving_history_filter_smoked_no);
        String details = status + (TextUtils.isEmpty(note) ? "" : " · " + note);
        holder.textDetails.setText(highlight(details));
    }

    public void setSearchQuery(String query) {
        String normalized = query == null ? "" : query.trim();
        if (!TextUtils.equals(this.query, normalized)) {
            this.query = normalized;
            notifyDataSetChanged();
        }
    }

    private CharSequence highlight(String text) {
        if (TextUtils.isEmpty(query) || TextUtils.isEmpty(text)) {
            return text;
        }
        String lower = text.toLowerCase(Locale.getDefault());
        String target = query.toLowerCase(Locale.getDefault());
        int start = lower.indexOf(target);
        if (start < 0) {
            return text;
        }
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + target.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
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

    private static final DiffUtil.ItemCallback<CravingEventEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<CravingEventEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull CravingEventEntity oldItem, @NonNull CravingEventEntity newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CravingEventEntity oldItem, @NonNull CravingEventEntity newItem) {
            return oldItem.id == newItem.id
                    && oldItem.didSmoke == newItem.didSmoke
                    && oldItem.intensity == newItem.intensity
                    && TextUtils.equals(oldItem.trigger, newItem.trigger)
                    && TextUtils.equals(oldItem.note, newItem.note)
                    && oldItem.timestamp.equals(newItem.timestamp);
        }
    };
}
