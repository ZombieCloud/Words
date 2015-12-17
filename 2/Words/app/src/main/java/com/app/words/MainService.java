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
            if(!wakeLock.isHeld()){
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
        Log.d(LOG_TAG, "startNum = " + intent.getStringExtra(WordsActivity.PARAM_START_NUM));
        Log.d(LOG_TAG, "lastNum = " + intent.getStringExtra(WordsActivity.PARAM_LAST_NUM));


        // Запускаем сервис в режиме Foreground
        startForeground(startId, new Notification());

        // Перебор слов. Номера первого и последнего слов и интервал приезжают сюда вместе с intent
        Start_Fetching_Of_The_Words(Integer.valueOf(intent.getStringExtra(WordsActivity.PARAM_START_NUM)), Integer.valueOf(intent.getStringExtra(WordsActivity.PARAM_LAST_NUM)),  Integer.valueOf(intent.getStringExtra(WordsActivity.PARAM_CURRENT_NUM)));

        // Отослать уведомление
        sendNotif(startId);

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
    void Start_Fetching_Of_The_Words(final int startNum,  final int lastNum,  final int currentNum) {
        new Thread(new Runnable() {
            public void run() {

                n = currentNum;
                while ((n >= startNum) && (n <= lastNum)) {

                    //Стоп потоку
                    if (stop_thread) break;

                    Log.d(LOG_TAG, "n = " + n);

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
            pause_en = (int) ((mediaPlayer_en.getDuration() / 1000 + 1) * 2.4);

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
        sendToActivity(_word._ru, _word.n);  //Текст на экран
        mediaPlayer_ru.start();     //Проговорить
        Pause(pause_ru + pause_en);

        sendToActivity(_word._en, _word.n);  //Текст на экран
        mediaPlayer_en.start();     //Проговорить
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




    // Уведомление для Activity
    void sendNotif(int startId) {

        // Отослать уведомление в WordActiity
        Notification notif = new Notification(R.mipmap.ic_launcher, "", System.currentTimeMillis());     // R.mipmap.ic_launcher - это иконка
        Intent intent = new Intent(this, WordsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notif.setLatestEventInfo(this, "Words", "hi !!!", pIntent);
        notif.flags |= Notification.FLAG_AUTO_CANCEL;   // ставим флаг, чтобы уведомление пропало после нажатия
        nm.notify(startId, notif);                      // отправляем уведомление

        intent = null;
    }



    // Инфомация для Activity
    void sendToActivity(String str, String num) {

        // Отослать текущий номер слова в WordActiity
        Intent intent = new Intent(WordsActivity.BROADCAST_ACTION);   // По значению BROADCAST_ACTION   BroadcastReceiver в WordActiity найдет сообщение. Это значение фильтра
        intent.putExtra(WordsActivity.PARAM_CURRENT_WORD, str);       // Поместить str в intent
        intent.putExtra(WordsActivity.PARAM_CURRENT_NUM, num);        // Поместить num в intent
        sendBroadcast(intent);

        intent = null;
    }


}