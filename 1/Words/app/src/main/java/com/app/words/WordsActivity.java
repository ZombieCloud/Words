package com.app.words;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;



class Word {
    public String _en = "";
    public String _ru = "";
    public File _enSound;
    public File _ruSound;

    private String _num = "";
    private String filename;


    //Конструктор
    public Word(String num) {
        _num = num;

        URL url;
        HttpURLConnection urlConnection = null;
        InputStream in;
        int data;
        InputStreamReader isw;
        _ru = "Something ru";
        _en = "Something en";
        //Слово EN (_en) ------------------
        try {
            url = new URL("http://tests.progmans.net/index.php?NUM_EN=" + _num);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = urlConnection.getInputStream();
            isw = new InputStreamReader(in);
            data = isw.read();
            while (data != -1) {
                char currentSymbol = (char) data;
                data = isw.read();
                _en = _en + currentSymbol;
            }

        } catch (Exception e) {     //  Нет интернета
            _en = "Something wrong with internet :(  en_" + _num;      //  +_num  - чтоб приложение не ставилось на паузу, когда экран гаснет
            e.printStackTrace();
        }  finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }



        //Слово RU (_ru) -----------------------
        try {
            url = new URL("http://tests.progmans.net/index.php?NUM_RU=" + _num);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = urlConnection.getInputStream();
            isw = new InputStreamReader(in);
            data = isw.read();
            while (data != -1) {
                char currentSymbol = (char) data;
                data = isw.read();
                _ru = _ru + currentSymbol;
            }

        } catch (Exception e) {     // Нет интернета
            _ru = "Something wrong with internet :(  ru_" + _num;      //  +_num  - чтоб приложение не ставилось на паузу, когда экран гаснет
            e.printStackTrace();
        }  finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }



        if (isExternalStorageWritable()) {

            //Звук EN (_enSound) --------------------------------
            try {
                filename = "en_" + _num + ".wav";
                _enSound = new File(getSoundStorageDir("app_words"), filename);
                if (!_enSound.exists()) {
                    url = new URL("http://tests.progmans.net/index.php?NUM_EN_SOUND=" + _num);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = urlConnection.getInputStream();
                    copyInputStreamToFile(in, _enSound);
                }

            } catch (Exception e) {    // Нет интернета
                _en = "Something wrong with sound files or internet:(";
                _enSound = null;
                e.printStackTrace();

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }



            //Звук RU (_ruSound) -----------------------------
            try {
                filename = "ru_" + _num + ".wav";
                _ruSound = new File(getSoundStorageDir("app_words"), filename);
                if (!_ruSound.exists()) {
                    url = new URL("http://tests.progmans.net/index.php?NUM_RU_SOUND=" + _num);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = urlConnection.getInputStream();
                    copyInputStreamToFile(in, _ruSound);
                }

            } catch (Exception e) {    // Нет интернета
                _ru = "Something wrong with sound files or internet:(";
                _ruSound = null;
                e.printStackTrace();

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }


        } else {
            _en = "Storage is not available :(";
            _ru = "Storage is not available :(";
        }
    }




    // Get the directory for the app's private pictures directory.
    public File getSoundStorageDir(String albumName) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), albumName);
        if (!file.mkdirs()) {
//            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }



    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    //Копирует InputStream из URL в файл (http://stackoverflow.com/questions/10854211/android-store-inputstream-in-file)
    private void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
};




public class WordsActivity extends AppCompatActivity {

    TextView _textView;
    EditText _firstWord;
    EditText _lastWord;
    EditText _timeInterval;

    boolean weHaveWord;
    boolean ProgramAlredyStarted;

    Word newWord;

    int startNum;
    int lastNum;
    int currentNum;
    int Interval;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();
    Button button;
    Uri myUri;

    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        _textView = (TextView) findViewById(R.id.textView);
        _firstWord = (EditText) findViewById(R.id.firstWord);
        _lastWord = (EditText) findViewById(R.id.lastWord);
        _timeInterval = (EditText) findViewById(R.id.timeInterval);

        weHaveWord = false;              //Эта переменная для того, чтоб англ. и рус. чередовались
        ProgramAlredyStarted = false;      //Чтобы после паузы счетчик слов не начинался с самого начала интервала
    }



    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();

        button.setText("Stop");

        //starts the timer
        timer.schedule(timerTask, Interval, Interval);
    }


    public void stopTimerTask() {
        //stop the timer
        if (timer != null) {
            timer.cancel();
            timer = null;
            button.setText("Go !!!");
        }
    }



    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run
                handler.post(new Runnable() {
                    public void run() {

                        if (weHaveWord) {
                            //У нас есть слово. Крутим его

                            _textView.setText(newWord._en);
                            PlayWord(newWord._enSound);

                            weHaveWord = false;
                            currentNum = currentNum + 1;

                        } else {
                            //Нет слова. Достанем новое
                            stopTimerTask();

                            //Достать новое слово
                            newWord = new Word(String.valueOf(currentNum));

                            _textView.setText(newWord._ru);
                            PlayWord(newWord._ruSound);

                            weHaveWord = true;
                            startTimer();
                        }

                        //Закончились слова. Заново
                        if (currentNum > lastNum){
                            stopTimerTask();
                            currentNum = startNum;
                            startTimer();
                        }
                    }
                });
            }
        };
    }



    public void buttonOnClick(View v) throws InterruptedException {
        button = (Button) v;

        if (button.getText() == "Stop") {
            stopTimerTask();

        } else {
            if (!ProgramAlredyStarted) {
                startNum = Integer.valueOf(_firstWord.getText().toString());
                lastNum = Integer.valueOf(_lastWord.getText().toString());
                Interval = Integer.valueOf(_timeInterval.getText().toString()) * 1000;
                currentNum = startNum;

                ProgramAlredyStarted = true;
            }

            startTimer();
        }
    }



    private void PlayWord(File fileToPlay) {

        //Попытаемся убить плеер. Вдруг играет
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
        }
    }

}
