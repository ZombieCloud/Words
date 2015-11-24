package com.app.words;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
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
    NotificationManager nm;
    Thread myThred;


    public MainService() {
    }


    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Start_Fetching_Of_The_Words(4, 10);
        Log.d(LOG_TAG, "onCreate");
        stop_thread = false;
    }



    // Срабатывает, когда сервис запущен методом "startService". В нем мы запускаем то, что нам нужно
    // У "onStartCommand" на вход и на выход идут параметры
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        Log.d(LOG_TAG, "startId of service = " + startId);
        Log.d(LOG_TAG, "startNum = " + intent.getStringExtra("startNum"));
        Log.d(LOG_TAG, "lastNum = " + intent.getStringExtra("lastNum"));


        Notif("fuck");

        myThred = new MyThred();
        myThred.start();





        // Перебор слов. Номера первого и последнего слов и интервал приезжают сюда вместе с intent
//        Start_Fetching_Of_The_Words(Integer.valueOf(intent.getStringExtra("startNum")), Integer.valueOf(intent.getStringExtra("lastNum")));



//        Notification noti = new Notification();


/*        Notification notification = new Notification(R.drawable.icon, getText(R.string.ticker_text), System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, WordsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getText(R.string.notification_title),
                getText(R.string.notification_message), pendingIntent);
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        startForeground(startId, notification);   */

//        sendNotif();

/*        Notification noti = new Notification(R.drawable.notification_template_icon_bg, "Text in status bar", System.currentTimeMillis());
        Intent wa_intent = new Intent(this, WordsActivity.class);
        wa_intent.putExtra(WordsActivity.WORD_NUM, "word_num");
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        noti.setLatestEventInfo(this, "Notification's title", "Notification's text", pIntent);
        noti.flags |= Notification.FLAG_AUTO_CANCEL;    */


//        return super.onStartCommand(intent, flags, startId);

//        startForeground(startId, noti);
        return START_STICKY;
    }




    public void onDestroy() {
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









    public class MyBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }





    void Notif(String n) {
        Notification noti = new Notification(R.drawable.notification_template_icon_bg, "now " + n, System.currentTimeMillis());

        Intent intent = new Intent(this, WordsActivity.class);
        intent.putExtra(WordsActivity.WORD_NUM, "n");
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        noti.setLatestEventInfo(this, "words", "now " + n, pIntent);

        // ставим флаг, чтобы уведомление пропало после нажатия
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        nm.notify(1, noti);
        startForeground(1, noti);
    }


/*
    private void addNotification() {
        // create the notification
        Notification.Builder m_notificationBuilder = new Notification.Builder(this)
                .setContentTitle(getText(R.string.service_name))
                .setContentText(getResources().getText(R.string.service_status_monitor))
                .setSmallIcon(R.drawable.notification_small_icon);

        // create the pending intent and add to the notification
        Intent intent = new Intent(this, MainService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        m_notificationBuilder.setContentIntent(pendingIntent);

        // send the notification
        m_notificationManager.notify(NOTIFICATION_ID, m_notificationBuilder.build());
    }
*/

}




class MyThred extends Thread {

    final String LOG_TAG = "wordLogs";
    int n;     // Счетчик слов
    boolean stop_thread;   // Переменная-флаг, которая остановит поток. (По-людски поток вообще остановить нельзя?)
    MediaPlayer mediaPlayer_en;
    MediaPlayer mediaPlayer_ru;
    Uri myUri;
    Word newWord;


    @Override
    public void run() {


        int startNum = 1;
        int lastNum = 42;


        n = startNum;
        while ((n >= startNum) && (n <= lastNum)) {
            Log.d(LOG_TAG, "n = " + n);

            //Достать новое слово
            newWord = new Word(String.valueOf(n));

//                    _textView.setText(newWord._ru);
            PlayWords(newWord);
//                    _textView.setText(newWord._en);

//                    Notif(String.valueOf(n));

            newWord = null;

            //Следующее слово
            n++;

            // Заново
            if (n > lastNum) n = startNum;

            //Стоп потоку
            if (stop_thread) break;
        }
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
                mediaPlayer_en.setDataSource(myUri.getPath());
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
//                mediaPlayer_ru.setDataSource(getApplicationContext(), myUri);
                mediaPlayer_ru.setDataSource(myUri.getPath());
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

