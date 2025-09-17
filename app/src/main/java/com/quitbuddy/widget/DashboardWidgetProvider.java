package com.quitbuddy.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.quitbuddy.R;
import com.quitbuddy.data.AppExecutors;
import com.quitbuddy.data.model.DashboardSnapshot;
import com.quitbuddy.data.repo.QuitBuddyRepository;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateWidget(context);
    }

    public static void updateWidget(Context context) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            DashboardSnapshot snapshot = QuitBuddyRepository.getInstance(context).getSnapshotSync();
            long finalDays = snapshot.smokeFreeDays;
            double finalMoney = snapshot.moneySaved;
            AppExecutors.getInstance().mainThread().execute(() -> {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dashboard);
                views.setTextViewText(R.id.widgetDays, finalDays + " 天");
                views.setTextViewText(R.id.widgetMoney, NumberFormat.getCurrencyInstance(Locale.getDefault()).format(finalMoney));
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                ComponentName widget = new ComponentName(context, DashboardWidgetProvider.class);
                manager.updateAppWidget(widget, views);
            });
        });
    }
}
