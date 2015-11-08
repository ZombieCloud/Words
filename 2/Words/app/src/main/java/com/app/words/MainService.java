package com.app.words;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class MainService extends Service {

    final String LOG_TAG = "myLogs";

    public MainService() {
    }


    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }


    // Срабатывает, когда сервис запущен методом "startService". В нем мы запускаем метод "someTask"
    // У "onStartCommand" на вход и на выход идут параметры
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");

        // Достать/произнести слово
        OneWord();

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");

        Log.d(LOG_TAG, "onBind");
        return null;

    }


    // Достать/произнести слово. Запускаем в отдельном потоке
    void OneWord() {
        new Thread(new Runnable() {
            public void run() {
                int n = 0;
                while (true) {
                    n++;
                    Log.d(LOG_TAG, "n = " + n);
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//                stopSelf();  // Останавливает сервис, в котором был вызван поток
            }
        }).start();
    }
}
