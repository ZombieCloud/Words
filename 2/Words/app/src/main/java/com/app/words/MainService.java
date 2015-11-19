package com.app.words;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
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


    public MainService() {
    }


    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        stop_thread = false;
    }



    // Срабатывает, когда сервис запущен методом "startService". В нем мы запускаем то, что нам нужно
    // У "onStartCommand" на вход и на выход идут параметры
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        Log.d(LOG_TAG, "startNum = " + intent.getStringExtra("startNum"));
        Log.d(LOG_TAG, "lastNum = " + intent.getStringExtra("lastNum"));

        // Перебор слов. Номера первого и последнего слов и интервал приезжают сюда вместе с intent
        Start_Fetching_Of_The_Words(Integer.valueOf(intent.getStringExtra("startNum")), Integer.valueOf(intent.getStringExtra("lastNum")));

        Notification noti = new Notification();
        startForeground(666, noti);
        return START_STICKY;
    }




    public void onDestroy() {
        stop_thread = true;   //Это остановит поток
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
    void Start_Fetching_Of_The_Words(final int startNum,   final int lastNum) {
        new Thread(new Runnable() {
            public void run() {

                n = startNum;
                while ((n >= startNum) && (n <= lastNum)) {
                    Log.d(LOG_TAG, "n = " + n);

                    //Достать новое слово
                    newWord = new Word(String.valueOf(n));

//                    _textView.setText(newWord._ru);
                    PlayWords(newWord);
//                    _textView.setText(newWord._en);

                    newWord = null;

                    //Следующее слово
                    n++;

                    // Заново
                    if (n > lastNum) n = startNum;

                    //Стоп потоку
                    if (stop_thread) break;
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
            pause_en = (mediaPlayer_en.getDuration() / 1000 + 1) * 2;

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



}
