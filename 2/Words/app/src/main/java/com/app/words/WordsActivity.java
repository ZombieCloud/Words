package com.app.words;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;




public class WordsActivity extends AppCompatActivity {

    final String LOG_TAG = "wordLogs";

    TextView _textView;
    EditText _firstWord;
    EditText _lastWord;

    String startNum;
    String lastNum;

    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        //Устанавливаем заголовок кнопки в зависимости от того, запущен ли сервис
        button = (Button) findViewById(R.id.button);
        if (ServiceRunning(MainService.class)) {
            button.setText("Stop");
        } else {
            button.setText("Go !!!");
        }

        _textView = (TextView) findViewById(R.id.textView);
        _firstWord = (EditText) findViewById(R.id.firstWord);
        _lastWord = (EditText) findViewById(R.id.lastWord);
    }




    public void buttonOnClick(View v) throws InterruptedException {
//        button = (Button) v;

        try {
            startNum = (Integer.valueOf(_firstWord.getText().toString())).toString();
            lastNum = (Integer.valueOf(_lastWord.getText().toString())).toString();

        } catch (Exception e) {

            Log.d(LOG_TAG, "WRONG NUMBERS !!!");
            startNum = "100000";
            lastNum = "100000";
        }


        // Создаем сервис
        Intent WordIntent = new Intent(this, MainService.class);
        WordIntent.putExtra("startNum", startNum);    // "putExtra"  вкладывает параметры в "intent". Их потом подберем в сервисе
        WordIntent.putExtra("lastNum", lastNum);



        if (ServiceRunning(MainService.class)) {
            stopService(WordIntent);
            button.setText("Go !!!");
        } else {
            startService(WordIntent);
            button.setText("Stop");
        }
    }



    //Проверяет, запущен ли сервис
    private boolean ServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}

