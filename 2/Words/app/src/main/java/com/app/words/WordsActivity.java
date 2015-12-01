package com.app.words;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class WordsActivity extends AppCompatActivity {

    public final static String PARAM_START_NUM = "PARAM_START_NUM";
    public final static String PARAM_LAST_NUM = "PARAM_LAST_NUM";;
    public final static String PARAM_CURRENT_NUM = "PARAM_CURRENT_NUM";
    public final static String BROADCAST_ACTION = "service_MainService";

    final String LOG_TAG = "wordLogs";

    TextView _textView;
    EditText _firstWord;
    EditText _lastWord;
    String startNum;
    String lastNum;
    Button button;

    BroadcastReceiver br;    // это для обратной связи от сервиса MainService


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




        // создаем BroadcastReceiver
        br = new BroadcastReceiver() {

            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                String currentNum = intent.getStringExtra(PARAM_CURRENT_NUM);    // извлекаем значение параметра PARAM_CURRENT_NUM из intent, который пришел от MainService
                 _textView.setText(currentNum);
            }
        };

        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);

        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, intFilt);

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


        Log.d(LOG_TAG, "startNum from Activity = " + startNum);
        Log.d(LOG_TAG, "lastNum from Activity = " + lastNum);


        // Создаем сервис
        Intent WordIntent = new Intent(this, MainService.class);


        //Запустить \ остановить сервис
        if (ServiceRunning(MainService.class)) {

            stopService(WordIntent);
            button.setText("Go !!!");

        } else {

            WordIntent.putExtra(PARAM_START_NUM, startNum);    // "putExtra"  вкладывает параметры в "intent". Их потом подберем в сервисе
            WordIntent.putExtra(PARAM_LAST_NUM, lastNum);

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



    @Override
    protected void onDestroy() {
        super.onDestroy();

        // дерегистрируем (выключаем) BroadcastReceiver
        unregisterReceiver(br);
    }


}