package com.app.words;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class MainService extends Service {

    final String LOG_TAG = "wordLogs";
    int n;     // Счетчик слов
    boolean stop_thread;   // Переменная-флаг, которая остановит поток. (По-людски поток вообще остановить нельзя?)
    MediaPlayer mediaPlayer_en;
    MediaPlayer mediaPlayer_ru;
    Uri myUri;
    Word newWord;
    NotificationManager nm;
    PowerManager.WakeLock wakeLock;


    public MainService() {
    }


    public void onCreate() {
        super.onCreate();

        //Включить WakeLock
        PowerManager devicePowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = devicePowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        if(wakeLock != null){
            if(wakeLock.isHeld() == false){
                wakeLock.acquire();
            }
        }

        //Уведомлялка
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Log.d(LOG_TAG, "onCreate");
        stop_thread = false;
    }



    // Срабатывает, когда сервис запущен методом "startService". В нем мы запускаем то, что нам нужно
    // У "onStartCommand" на вход и на выход идут параметры
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        Log.d(LOG_TAG, "startId of service = " + startId);
        Log.d(LOG_TAG, "startNum = " + intent.getStringExtra("startNum"));
        Log.d(LOG_TAG, "lastNum = " + intent.getStringExtra("lastNum"));


        // Запускаем сервис в режиме Foreground
        Notification noti = new Notification();
        startForeground(startId, noti);


        // Перебор слов. Номера первого и последнего слов и интервал приезжают сюда вместе с intent
        Start_Fetching_Of_The_Words(Integer.valueOf(intent.getStringExtra("startNum")), Integer.valueOf(intent.getStringExtra("lastNum")),  startId);

        return START_STICKY;
    }




    public void onDestroy() {

        //Отключить WakeLock
        if (wakeLock != null) {
            if(wakeLock.isHeld()){
                wakeLock.release();
                wakeLock = null;
            }
        }

        stopForeground(true);

        stop_thread = true;   //Это остановит поток
        newWord = null;
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



    // Достать/произнести слово.
    void Start_Fetching_Of_The_Words(final int startNum,  final int lastNum,  final int startId) {
        new Thread(new Runnable() {
            public void run() {

                n = startNum;
                while ((n >= startNum) && (n <= lastNum)) {

                    //Стоп потоку
                    if (stop_thread) break;

                    Log.d(LOG_TAG, "n = " + n);

                    // Отослать уведомление
                    sendNotif(String.valueOf(n), startId);

                    //Достать новое слово
                    newWord = new Word(String.valueOf(n));

                    // Проиграть слово
                    PlayWords(newWord);

                    newWord = null;

                    //Следующее слово
                    n++;

                    // Заново
                    if (n > lastNum) n = startNum;
                }
                stopSelf();  // Останавливает сервис, в котором был вызван поток
            }
        }).start();
    }



    //Произнести слово
    private void PlayWords(Word _word) {

        int pause_en = 1;
        int pause_ru = 1;

        KillPlayer();

        //Новый плеер en
        mediaPlayer_en = new MediaPlayer();
        mediaPlayer_en.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            myUri = Uri.parse(_word._enSound.getAbsolutePath());             // "/mnt/sdcard/app_words/en_1.wav"
            mediaPlayer_en.setDataSource(getApplicationContext(), myUri);
            mediaPlayer_en.prepare();
            pause_en = (int) ((mediaPlayer_en.getDuration() / 1000 + 1) * 2.5);

            Log.d(LOG_TAG, "pause_en  =  " + pause_en);

        } catch (IOException e) {
            e.printStackTrace();
            KillPlayer();
        }


        //Новый плеер ru
        mediaPlayer_ru = new MediaPlayer();
        mediaPlayer_ru.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            myUri = Uri.parse(_word._ruSound.getAbsolutePath());
            mediaPlayer_ru.setDataSource(getApplicationContext(), myUri);
            mediaPlayer_ru.prepare();
            pause_ru = mediaPlayer_ru.getDuration() / 1000 + 1;

            Log.d(LOG_TAG, "pause_ru  =  " + pause_ru);

        } catch (IOException e) {
            e.printStackTrace();
            KillPlayer();
        }


        //Начинаем играть
        mediaPlayer_ru.start();
        Pause(pause_ru + pause_en);
        mediaPlayer_en.start();
        Pause(pause_en + 3);


        KillPlayer();
    }




    //Пауза
    private void Pause(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }




    //Попытаемся убить плеера. Вдруг играет или еще чего случилось
    private void KillPlayer() {
        //Убить англ.плеер
        if (mediaPlayer_en != null) {
            try {
                mediaPlayer_en.release();
                mediaPlayer_en = null;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Can't kill player");
                e.printStackTrace();
            }
        }

        //Убить рус.плеер
        if (mediaPlayer_ru != null) {
            try {
                mediaPlayer_ru.release();
                mediaPlayer_ru = null;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Can't kill player");
                e.printStackTrace();
            }
        }
    }





    public class MyBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }




    // Уведомление
    void sendNotif(String n, int startId) {
        Notification notif = new Notification(R.drawable.notification_template_icon_bg, "now " + n, System.currentTimeMillis());

        Intent intent = new Intent(this, WordsActivity.class);
        intent.putExtra(WordsActivity.WORD_NUM, n);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notif.setLatestEventInfo(this, "words", "now " + n, pIntent);

        // ставим флаг, чтобы уведомление пропало после нажатия
        notif.flags |= Notification.FLAG_AUTO_CANCEL;

        // отправляем
        nm.notify(startId, notif);
    }


}