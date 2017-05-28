package com.akouki.rssreader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsManager {

    private static String PREFS_LAST_UPDATE_NAME = "rssreader.lastupdate";
    private static String PREFS_LAST_CLEAN_NAME = "rssreader.lastclean";

    public static void saveLastRun(Context context, long currentTimeMillis) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_LAST_UPDATE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong("lastupdate", currentTimeMillis);
        editor.commit();
    }

    public static long getLastUpdate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_LAST_UPDATE_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("lastupdate", 0);
    }

    public static void saveLastClean(Context context, long currentTimeMillis) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_LAST_CLEAN_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong("lastclean", currentTimeMillis);
        editor.commit();
    }

    public static long getLastClean(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_LAST_CLEAN_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("lastclean", 0);
    }

    // ============================= Settings =============================

    public static int getSyncAutomatic(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(sp.getString("syncAutomatic", "0"));
    }

    public static long getSyncInterval(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Long.valueOf(sp.getString("syncInterval", "3600000"));
    }

    public static boolean syncOnStartup(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("syncStartup", false);
    }

    public static long getCleanInterval(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Long.valueOf(sp.getString("cleanAutomatic", "172800000"));
    }

    public static boolean showNotifications(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("notificationsShow", true);
    }

    public static boolean downloadThumbnails(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("downloadThumbnails", true);
    }

}