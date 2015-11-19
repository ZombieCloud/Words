package com.app.words;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class Word {

    final String LOG_TAG = "wordLogs";

    public String _en = "";
    public String _ru = "";
    public File _enSound;
    public File _ruSound;



    //Конструктор
    public Word(String num) {
        String filename;

        URL url;
        HttpURLConnection urlConnection = null;
        InputStream in;
        int data;
        InputStreamReader isw;
        _ru = "Something ru";
        _en = "Something en";



        // Слово EN (_en) ------------------
        try {
            url = new URL("http://tests.progmans.net/index.php?NUM_EN=" + num);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = urlConnection.getInputStream();
            isw = new InputStreamReader(in);
            data = isw.read();
            while (data != -1) {
                char currentSymbol = (char) data;
                data = isw.read();
                _en = _en + currentSymbol;
            }
            isw = null;

        } catch (Exception e) {     //  Нет интернета
            isw = null;
            _en = "Something wrong with internet :(  en_";
            Log.d(LOG_TAG, "Something wrong with internet :(  en_");
            e.printStackTrace();
        }  finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }



        // Слово RU (_ru) -----------------------
        try {
            url = new URL("http://tests.progmans.net/index.php?NUM_RU=" + num);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = urlConnection.getInputStream();
            isw = new InputStreamReader(in);
            data = isw.read();
            while (data != -1) {
                char currentSymbol = (char) data;
                data = isw.read();
                _ru = _ru + currentSymbol;
            }
            isw = null;

        } catch (Exception e) {     // Нет интернета
            isw = null;
            _ru = "Something wrong with internet :(  ru_";
            Log.d(LOG_TAG, "Something wrong with internet :(  ru_");
            e.printStackTrace();
        }  finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }



        // Звуки качаем только если локальное хранилище доступно
        if (isExternalStorageWritable()) {

            // Звук EN (_enSound) --------------------------------
            try {
                filename = "en_" + num + ".wav";
                _enSound = new File(getSoundStorageDir("app_words"), filename);
                if (!_enSound.exists()) {
                    url = new URL("http://tests.progmans.net/index.php?NUM_EN_SOUND=" + num);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = urlConnection.getInputStream();
                    copyInputStreamToFile(in, _enSound);
                }

            } catch (Exception e) {    // Нет интернета
                _en = "Something wrong with sound files or internet:(";
                _enSound = null;
                Log.d(LOG_TAG, "Something wrong with sound files or internet:(");
                e.printStackTrace();

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }



            // Звук RU (_ruSound) -----------------------------
            try {
                filename = "ru_" + num + ".wav";
                _ruSound = new File(getSoundStorageDir("app_words"), filename);
                if (!_ruSound.exists()) {
                    url = new URL("http://tests.progmans.net/index.php?NUM_RU_SOUND=" + num);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = urlConnection.getInputStream();
                    copyInputStreamToFile(in, _ruSound);
                }

            } catch (Exception e) {    // Нет интернета
                _ru = "Something wrong with sound files or internet:(";
                _ruSound = null;
                Log.d(LOG_TAG, "Something wrong with sound files or internet:(");
                e.printStackTrace();

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }


        } else {
            Log.d(LOG_TAG, "Storage is not available");
            _en = "Storage is not available :(";
            _ru = "Storage is not available :(";
        }
    }




    // Get the directory for the app's private pictures directory.
    public File getSoundStorageDir(String albumName) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), albumName);
        if (!file.mkdirs()) {
            Log.d(LOG_TAG, "Directory not created");
        }
        return file;
    }



//     Checks if external storage is available for read and write
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
            buf = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
