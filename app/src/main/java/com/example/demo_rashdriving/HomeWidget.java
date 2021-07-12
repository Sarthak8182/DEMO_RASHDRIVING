package com.example.demo_rashdriving;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;


public class HomeWidget extends AppWidgetProvider  {

    private String xx;
    private static RemoteViews views;
    private String lat;


    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object





        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras =intent.getExtras();
        String pitch = extras.getString("pitch");
        String roll=extras.getString("roll");

       int key = extras.getInt("key");
       if(key==1){

           String data = extras.getString("data");


        if (data != null) {
            xx = extras.getString("data", AppWidgetManager.EXTRA_APPWIDGET_ID);
            xx="Current Shake speed: \n"+xx+"\n"+"Pitch:- "+pitch+"\n"+"Roll:- "+roll;

        }
        }
       else if(key==2){
           String data1 = extras.getString("info1");
           String data2 = extras.getString("info2");

           String x=extras.getString("info1", AppWidgetManager.EXTRA_APPWIDGET_ID);
           String y=extras.getString("info2",AppWidgetManager.EXTRA_APPWIDGET_ID);
           xx="Latitude:- "+x+"\n"+"Longitude:- "+y;



       }


        views = new RemoteViews(context.getPackageName(), R.layout.homewidget);
        views.setTextViewText(R.id.appwidget_text,xx);
      if(key==1)
        views.setTextViewText(R.id.appwidget_status,"Service Activated");
      else
          views.setTextViewText(R.id.appwidget_status,"Service Deactivated");
      super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent appIntent = new Intent(context, MainActivity.class);
            PendingIntent intent = PendingIntent.getActivity(context, 43, appIntent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.homewidget);
            views.setOnClickPendingIntent(R.id.lay, intent);
            appWidgetManager.updateAppWidget(appWidgetId, views);

            views.setTextViewText(R.id.appwidget_text, xx);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {

        // Enter relevant functionality for when the last widget is disabled
    }

}

