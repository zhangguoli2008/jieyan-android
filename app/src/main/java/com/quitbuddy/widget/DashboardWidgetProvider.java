package com.quitbuddy.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.quitbuddy.R;
import com.quitbuddy.data.AppDatabase;
import com.quitbuddy.data.AppExecutors;
import com.quitbuddy.data.dao.CravingEventDao;
import com.quitbuddy.data.dao.QuitPlanDao;
import com.quitbuddy.data.model.QuitPlanEntity;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

public class DashboardWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateWidget(context);
    }

    public static void updateWidget(Context context) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            QuitPlanDao planDao = AppDatabase.getInstance(context).quitPlanDao();
            CravingEventDao eventDao = AppDatabase.getInstance(context).cravingEventDao();
            QuitPlanEntity plan = planDao.getPlanSync();
            long days = 0;
            double money = 0;
            if (plan != null) {
                ZonedDateTime start = ZonedDateTime.ofInstant(plan.startDate.toInstant(), ZoneId.systemDefault());
                ZonedDateTime now = ZonedDateTime.now();
                days = Math.max(0, Duration.between(start.toLocalDate().atStartOfDay(), now).toDays());
                int smoked = eventDao.getSmokedCount();
                long avoided = (long) plan.dailyBaseline * Math.max(0, days + 1) - smoked;
                if (plan.cigsPerPack > 0) {
                    money = (double) avoided / plan.cigsPerPack * plan.pricePerPack;
                }
            }
            long finalDays = days;
            double finalMoney = money;
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
