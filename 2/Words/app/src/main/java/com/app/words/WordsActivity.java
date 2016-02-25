package com.app.words;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;



public class WordsActivity extends AppCompatActivity {

    public final static String PARAM_START_NUM = "PARAM_START_NUM";
    public final static String PARAM_LAST_NUM = "PARAM_LAST_NUM";;
    public final static String PARAM_CURRENT_WORD = "PARAM_CURRENT_WORD";
    public final static String PARAM_CURRENT_NUM = "PARAM_CURRENT_NUM";
    public final static String PARAM_RELOAD_WORDS = "PARAM_RELOAD_WORDS";
    public final static String BROADCAST_ACTION = "service_MainService";
    public static final String PREFS_NAME = "WordsPrefsFile";


    final String LOG_TAG = "wordLogs";

    TextView _textView;
    EditText _firstWord;
    EditText _lastWord;
    EditText _currentNum;
    CheckBox _ReloadWords;
    String startNum;
    String lastNum;
    String currentNum;
    Boolean reloadWords;
    Button button;

    BroadcastReceiver br;    // это для обратной связи от сервиса MainService


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Полноэкранный режим
        requestWindowFeature(Window.FEATURE_NO_TITLE);

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
        _currentNum = (EditText) findViewById(R.id.currentNum);
        _ReloadWords = (CheckBox) findViewById(R.id.cb_ReloadWords);

        // Текущий номер должен быть недоступен для ручного ввода
        _currentNum.setFocusable(false);


        // Установить шрифт  (app/assets)
        _textView.setTextSize(18);
        Typeface face2 = Typeface.createFromAsset(getAssets(), "MotionPicture_PersonalUseOnly.ttf");
        button.setTypeface(face2);
        button.setTextSize(40);


        // Восстановить сохраненное состояние формы (восстанавливаем значения переменных)
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        startNum = settings.getString("startNum", "");
        lastNum = settings.getString("lastNum", "");
        currentNum = settings.getString("currentNum", "");
        _firstWord.setText(startNum);
        _lastWord.setText(lastNum);
        _currentNum.setText(currentNum);



        // создаем BroadcastReceiver.  Будет подбирать сообщения от сервиса MainService
        br = new BroadcastReceiver() {

            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                String currentNum = intent.getStringExtra(PARAM_CURRENT_NUM);    // извлекаем значение параметра PARAM_CURRENT_NUM из intent, который пришел от MainService
                String currentWord = intent.getStringExtra(PARAM_CURRENT_WORD);
                _textView.setText(currentWord);
                _currentNum.setText(currentNum);
            }
        };

        // создаем фильтр для BroadcastReceiver. Он нужен для того, чтобы отлавливать сообщения от сервиса MainService
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);

        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, intFilt);
    }




    public void buttonOnClick(View v) throws InterruptedException {
//        button = (Button) v;

        try {
            startNum = (Integer.valueOf(_firstWord.getText().toString())).toString();
            lastNum = (Integer.valueOf(_lastWord.getText().toString())).toString();
            reloadWords = _ReloadWords.isChecked();

            if (TextUtils.isEmpty(_currentNum.getText())) {
                Log.d(LOG_TAG, "currentNum  is empty");
                currentNum = startNum;
            } else {
                Log.d(LOG_TAG, "currentNum  is NOT empty");
                currentNum = (Integer.valueOf(_currentNum.getText().toString())).toString();
            }
            if (Integer.valueOf(currentNum) > Integer.valueOf(lastNum)) {
                currentNum = startNum;
            }
            if (Integer.valueOf(currentNum) < Integer.valueOf(startNum)) {
                currentNum = startNum;
            }


        } catch (Exception e) {

            Log.d(LOG_TAG, "WRONG NUMBERS !!!");
            startNum = "100000";
            lastNum = "100000";
            currentNum = "100000";
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
            WordIntent.putExtra(PARAM_CURRENT_NUM, currentNum);
            WordIntent.putExtra(PARAM_RELOAD_WORDS, reloadWords);

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

        // Сохранить состояние формы (сохраняем значения переменных)
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("startNum", startNum);
        editor.putString("lastNum", lastNum);
        editor.putString("currentNum", currentNum);
        editor.commit();

        // дерегистрируем (выключаем) BroadcastReceiver
        unregisterReceiver(br);
    }

}