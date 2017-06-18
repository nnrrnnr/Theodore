package com.github.watanabear.sampleapplication.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.github.watanabear.sampleapplication.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ryo on 2017/06/17.
 */

public class Theodore {

    private static final String THEODORE_SERVICE_NAME = "com.github.watanabear.sampleapplication.service.PostAppOpenedService";

    private static final String THEODORE_SERVICE = "com.github.watanabear.sampleapplication.service.TheodoreService";

    private static final String THEODORE_ACTIVITY = "com.github.watanabear.theodore.MainActivity";

    private static final String EXTRA_KEY_REPOSITORY_NAME = "com.github.watanabear.theodore.EXTRA_KEY_REPOSITORY_NAME";

    private static final String EXTRA_KEY_SCREENSHOT = "com.github.watanabear.sampleapplication.EXTRA_KEY_SCREENSHOT";

    private static final String ACTION_SCREENSHOT = "com.github.watanabear.sampleapplication.ACTION_THEODORE_SCREENSHOT";


    public static void init(final Application application) {
        if (0 == (application.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            return;
        }

        application.registerActivityLifecycleCallbacks(new MyLifecycleCallbacks());

        AssetManager am = application.getResources().getAssets();
        InputStream is;

        try {
            is = am.open("theodore.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            JSONObject jo = new JSONObject(json);
            String repositoryName = jo.getString("repositoryName");

            if (repositoryName == null) {
                return;
            }

            Intent service = new Intent();
            service.setClassName(BuildConfig.THEODORE_PACKAGE_NAME, THEODORE_SERVICE_NAME);
            service.putExtra(EXTRA_KEY_REPOSITORY_NAME, repositoryName);
            application.startService(service);

            SharedPreferences pref = application
                    .getSharedPreferences("com.github.watanabear.sampleapplication", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(EXTRA_KEY_REPOSITORY_NAME, repositoryName);
            editor.apply();
        } catch (IOException e) {
            Log.d("", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static class MyLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        private int running = 0;

        private Intent theodoreIntent;

        private final Map<Activity, BroadcastReceiver> screenShotEventReceiverMap = new HashMap<>();


        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(final Activity activity) {
            running++;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                SharedPreferences pref = activity.getSharedPreferences("com.github.watanabear.sampleapplication", Context.MODE_PRIVATE);
                final String repositoryName = pref.getString(EXTRA_KEY_REPOSITORY_NAME, null);
                if (repositoryName == null) {
                    return;
                }
                BroadcastReceiver screenshotEventReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Rect rect = new Rect();
                        Window window = activity.getWindow();
                        View decorView = window.getDecorView();
                        decorView.getWindowVisibleDisplayFrame(rect);
                        int statusBarHeight = rect.top;

                        View view = decorView;
                        view.setDrawingCacheEnabled(true);
                        view.buildDrawingCache(true);
                        Bitmap bitmap = view.getDrawingCache();
                        Bitmap b = Bitmap.createBitmap(bitmap, 0, statusBarHeight, bitmap.getWidth(),
                                bitmap.getHeight() - statusBarHeight);
                        view.setDrawingCacheEnabled(false);
                        view.destroyDrawingCache();

                        Intent i = new Intent();
                        i.setClassName(BuildConfig.THEODORE_PACKAGE_NAME, THEODORE_ACTIVITY);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.putExtra(EXTRA_KEY_REPOSITORY_NAME, repositoryName);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        i.putExtra(EXTRA_KEY_SCREENSHOT, byteArray);
                        context.startActivity(i);
                    }
                };

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION_SCREENSHOT);
                activity.registerReceiver(screenshotEventReceiver, intentFilter);
                screenShotEventReceiverMap.put(activity, screenshotEventReceiver);
            } else {
                detectScreenShotService(activity);
            }

        }

        private void detectScreenShotService(final Activity activity) {

            final Handler h = new Handler();
            final int delay = 3000; //milliseconds
            final ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

            h.postDelayed(new Runnable() {
                public void run() {

                    List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(200);

                    for (ActivityManager.RunningServiceInfo ar : rs) {
                        if (ar.process.equals("com.android.systemui:screenshot")) {
                            Toast.makeText(activity, "Screenshot captured!!", Toast.LENGTH_LONG).show();

                            SharedPreferences pref = activity.getSharedPreferences("com.github.watanabear.sampleapplication", Context.MODE_PRIVATE);

                            final String repositoryName = pref.getString(EXTRA_KEY_REPOSITORY_NAME, null);
                            if (repositoryName == null) {
                                return;
                            }

                            Rect rect = new Rect();
                            Window window = activity.getWindow();
                            View decorView = window.getDecorView();
                            decorView.getWindowVisibleDisplayFrame(rect);
                            int statusBarHeight = rect.top;

                            View view = decorView;
                            view.setDrawingCacheEnabled(true);
                            view.buildDrawingCache(true);
                            Bitmap bitmap = view.getDrawingCache();
                            Bitmap b = Bitmap.createBitmap(bitmap, 0, statusBarHeight, bitmap.getWidth(),
                                    bitmap.getHeight() - statusBarHeight);
                            view.setDrawingCacheEnabled(false);
                            view.destroyDrawingCache();

                            Intent i = new Intent();
                            i.setClassName(BuildConfig.THEODORE_PACKAGE_NAME, THEODORE_ACTIVITY);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            i.putExtra(EXTRA_KEY_REPOSITORY_NAME, repositoryName);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            b.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            i.putExtra(EXTRA_KEY_SCREENSHOT, byteArray);
                            activity.startActivity(i);
                        }
                    }
                    h.postDelayed(this, delay);
                }
            }, delay);

        }

        @Override
        public void onActivityResumed(Activity activity) {

            SharedPreferences sharedPreferences = activity.getSharedPreferences("com.github.watanabear.sampleapplication", Context.MODE_PRIVATE);
            final String repositoryName = sharedPreferences.getString(EXTRA_KEY_REPOSITORY_NAME, null);
            if (repositoryName != null) {
                theodoreIntent = new Intent();
                theodoreIntent.setClassName(BuildConfig.THEODORE_PACKAGE_NAME, THEODORE_SERVICE);
                theodoreIntent.putExtra(EXTRA_KEY_REPOSITORY_NAME, repositoryName);
                activity.startService(theodoreIntent);
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (--running == 0) {
                if (theodoreIntent != null) {
                    activity.stopService(theodoreIntent);
                    theodoreIntent = null;
                }
            }

            BroadcastReceiver screenshotEventReceiver = screenShotEventReceiverMap.get(activity);
            if (screenshotEventReceiver != null) {
                activity.unregisterReceiver(screenshotEventReceiver);
                screenShotEventReceiverMap.remove(activity);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}
