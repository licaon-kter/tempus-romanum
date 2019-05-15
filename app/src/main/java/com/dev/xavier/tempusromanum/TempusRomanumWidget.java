package com.dev.xavier.tempusromanum;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class TempusRomanumWidget extends AppWidgetProvider {

    private static Date currentDate;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Chargement des préférences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultFontSize = context.getResources().getString(R.string.default_font_size);
        int fontSize = Integer.valueOf(pref.getString(context.getString(R.string.saved_font_size), defaultFontSize));

        // Faut-il calculer la mise à jour ?
        if(currentDate != null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            Calendar newCal = Calendar.getInstance();
            if(cal.get(Calendar.DAY_OF_MONTH) != newCal.get(Calendar.DAY_OF_MONTH) || cal.get(Calendar.MONTH) != newCal.get(Calendar.MONTH) || cal.get(Calendar.YEAR) != newCal.get(Calendar.YEAR))
            {
                currentDate = newCal.getTime();
            }
        }
        else
        {
            currentDate = new Date();
        }

        // Calcul de la date en latin
        CharSequence widgetText = Calendarium.tempus(currentDate);


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tempus_romanum_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        // Set font size
        views.setTextViewTextSize(R.id.appwidget_text, TypedValue.COMPLEX_UNIT_SP,fontSize);
        // Set font color
        // TODO views.setTextColor();

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Permettre l'ouverture de l'application sur clic du widget
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.setComponent(new ComponentName(context.getPackageName(), MainActivity.class.getName()));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            // RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tempus_romanum_widget);
            views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context.getApplicationContext(),"There was a problem loading the application: ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of the
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}
