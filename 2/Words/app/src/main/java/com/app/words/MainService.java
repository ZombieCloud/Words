package com.app.words;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainService extends Service {

    final String LOG_TAG = "wordLogs";
    int n;     // Счетчик слов
    boolean stop_thread;   // Переменная-флаг, которая остановит поток. (По-людски поток вообще остановить нельзя?)
    MediaPlayer mediaPlayer;
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
        Log.d(LOG_TAG, "Interval = " + intent.getStringExtra("Interval"));

        // Перебор слов. Номера первого и последнего слов и интервал приезжают сюда вместе с intent
        Start_Fetching_Of_The_Words(Integer.valueOf(intent.getStringExtra("startNum")),     Integer.valueOf(intent.getStringExtra("lastNum")),     Integer.valueOf(intent.getStringExtra("Interval")));

        return super.onStartCommand(intent, flags, startId);
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
    void Start_Fetching_Of_The_Words(final int startNum,   final int lastNum,   final int interval) {
        new Thread(new Runnable() {
            public void run() {

                n = startNum;
                while ((n >= startNum) && (n <= lastNum)) {
                    Log.d(LOG_TAG, "n = " + n);

                    //Достать новое слово
                    newWord = new Word(String.valueOf(n));

//                    _textView.setText(newWord._ru);
                    PlayWord(newWord._ruSound);
                    Pause(interval);

//                    _textView.setText(newWord._en);
                    PlayWord(newWord._enSound);
                    Pause(interval);

                    newWord = null;

                    KillPlayer();

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



    //Произнести слово. Запускаем в отдельном потоке, иначе замерзает при отключении экрана.
    //Видимо, плеер работает в потоке приложения, поэтому замерзает
    private void PlayWord(final File fileToPlay) {
        new Thread(new Runnable() {
            public void run() {

                KillPlayer();

                //Новый плеер
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    myUri = Uri.parse(fileToPlay.getAbsolutePath());             // "/mnt/sdcard/app_words/en_1.wav"
                    mediaPlayer.setDataSource(getApplicationContext(), myUri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    KillPlayer();

                }
            }
        }).start();
    }



    //Пауза
    private void Pause(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    //Попытаемся убить плеер. Вдруг играет
    private void KillPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Can't kill player");
                e.printStackTrace();
            }
        }
    }



}
