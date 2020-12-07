package com.leadinka.android.coldcall;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;



public class ColdCallActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // id - шки активити фиксации заявки
    private static final int REQUEST_CODE_DRAFT = 0;
    private static final int REQUEST_CODE_REPORT = 1;

    // Вьюхи
    private TextView statusView;
    private ImageButton callBtn;
    private Button mainBtn;
    private ImageButton cronetabBtn;
    private ImageButton moneyBtn;
    private Button leadBtn;
    private Button breakBtn;
    //private ProgressBar mProgressBar;

    // возобновляемые значения переменных
    private CC_model2guiInterface mModel; // Он же CC_model2guiInterface
    private WifiManager wManager;


    /**
     * Сеттер с перерисовкой интерфейса статуса приложения
     * Одновременно является слушателем сообщений из Вечного фрагмента
     *
     * @param pModel
     */
    public void setCurrentStatus(CC_model2guiInterface pModel) {
        int mCurrentStatus = CC_controller.AppStatuses.INIT;
        mModel = pModel;
        if (mModel != null) {
            mCurrentStatus = mModel.GUI_getCurrentStatus();
        }


        // если нет во временном хранилище номеров, то перескакиваем с thinking на READY2CALL
        if (mCurrentStatus == CC_controller.AppStatuses.THINKING) { // После входящего или исходящего звонка
            mCurrentStatus = mModel.GUI_ThinkingOrReady();
        }

        switch (mCurrentStatus) {
            case CC_controller.AppStatuses.INIT: // Инициализация

                if(pModel != null) {
                    Toast toast = Toast.makeText(ColdCallActivity.this,
                            R.string.start_info,

                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                callBtn.setVisibility(View.INVISIBLE);

                mainBtn.setVisibility(View.VISIBLE);
                mainBtn.setText(R.string.master_init_button);

                moneyBtn.setVisibility(View.INVISIBLE);

                cronetabBtn.setVisibility(View.VISIBLE);
                leadBtn.setVisibility(View.INVISIBLE);
                breakBtn.setVisibility(View.INVISIBLE);

                statusView.setText("Регистрация у менеджера");


                break;
            case CC_controller.AppStatuses.INCOMING: // Входящий звонок
                callBtn.setVisibility(View.INVISIBLE);
                mainBtn.setVisibility(View.INVISIBLE);
                cronetabBtn.setVisibility(View.INVISIBLE);
                leadBtn.setVisibility(View.INVISIBLE);
                moneyBtn.setVisibility(View.INVISIBLE);

                breakBtn.setVisibility(View.VISIBLE);
                breakBtn.setText("Прервать");

                statusView.setText("ВХОДЯЩИЙ....");
                break;

            case CC_controller.AppStatuses.READY2CALL: // Начало - готов к работе, след. номер
                callBtn.setVisibility(View.VISIBLE);
                mainBtn.setVisibility(View.INVISIBLE);
                cronetabBtn.setVisibility(View.VISIBLE);
                // cronetabBtn.setText("Завершить");
                leadBtn.setVisibility(View.INVISIBLE);
                //leadBtn.setText(R.string.action_report);
                moneyBtn.setVisibility(View.VISIBLE);
                breakBtn.setVisibility(View.INVISIBLE);
                statusView.setText(mModel.GUI_getStatusStringReady2Call());
                break;

            case CC_controller.AppStatuses.RINGING: // Исходящий звонок
                callBtn.setVisibility(View.INVISIBLE);
                mainBtn.setVisibility(View.INVISIBLE);
                cronetabBtn.setVisibility(View.INVISIBLE);
                leadBtn.setVisibility(View.INVISIBLE);
                moneyBtn.setVisibility(View.INVISIBLE);

                breakBtn.setVisibility(View.VISIBLE);
                breakBtn.setText("Прервать");
                statusView.setText("ИСХОДЯЩИЙ ...");
                break;

            case CC_controller.AppStatuses.THINKING: // После входящего или исходящего звонка

                callBtn.setVisibility(View.INVISIBLE);   // пусть отчитается - тогда дадим след. номер

                mainBtn.setVisibility(View.VISIBLE);
                mainBtn.setText("Клиент отказался");

                cronetabBtn.setVisibility(View.INVISIBLE);
                moneyBtn.setVisibility(View.INVISIBLE);

                leadBtn.setVisibility(View.VISIBLE);
                leadBtn.setText(R.string.action_draft);

                breakBtn.setVisibility(View.VISIBLE);
                breakBtn.setText("Телефон недоступен");

                statusView.setText(mModel.GUI_getStatusStringThinking());
                break;
            case CC_controller.AppStatuses.SMS:

                callBtn.setVisibility(View.VISIBLE);
                mainBtn.setVisibility(View.INVISIBLE);

                cronetabBtn.setVisibility(View.INVISIBLE);
                moneyBtn.setVisibility(View.INVISIBLE);

                leadBtn.setVisibility(View.VISIBLE);
                leadBtn.setText(R.string.action_draft);

                breakBtn.setVisibility(View.VISIBLE);
                breakBtn.setText("НЕ звоним");

                statusView.setText(mModel.GUI_getStatusStringSMS());

                break;
            case CC_controller.AppStatuses.NEEDUPGRADE:
                callBtn.setVisibility(View.INVISIBLE);
                mainBtn.setVisibility(View.VISIBLE);
                mainBtn.setText("Скачать и обновить");
                cronetabBtn.setVisibility(View.VISIBLE);
                leadBtn.setVisibility(View.INVISIBLE);
                breakBtn.setVisibility(View.INVISIBLE);

                statusView.setText("ОБНОВИТЕ ПРОГРАММУ");
                break;
        }
    }

    /**
     * Start main activity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        InfinityFragment myFragment;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cold_call);

        /*

         */
        mModel = null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null){
            toolbar.setTitle(R.string.app_name);
            setSupportActionBar(toolbar);
        }

        //View-хи
        callBtn = (ImageButton) findViewById(R.id.call);
        mainBtn = (Button) findViewById(R.id.main);
        cronetabBtn = (ImageButton) findViewById(R.id.cronetab);
        leadBtn = (Button) findViewById(R.id.lead);
        breakBtn = (Button) findViewById(R.id.breakB);
        moneyBtn = (ImageButton) findViewById(R.id.money);
      //  mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        callBtn.setOnClickListener(onClickListenerCall);
        cronetabBtn.setOnClickListener(onClickListenerCrone);
        leadBtn.setOnClickListener(onClickListenerLead);
        moneyBtn.setOnClickListener(onClickListenerLead);
        breakBtn.setOnClickListener(onClickListenerBreak);
        mainBtn.setOnClickListener(onClickListenerMain);


        statusView = (TextView) findViewById(R.id.status);

        Toast toast = Toast.makeText(ColdCallActivity.this,
                R.string.toast_wifi,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);

        //wifi блок - просто матюгальник, без какой-либо логики
        try {
            wManager = (WifiManager) (getApplicationContext().getSystemService(Context.WIFI_SERVICE));

            if (!wManager.isWifiEnabled()) {
                toast.show();
            }

        } catch (RuntimeException e) {
            toast.show();
        }


// Цепляем вечный фрагмент для хранения модели
        // нельзя при каждом повороте запрашивать статус устройства по сети - будет http шторм и сервер ляжет.
        // Нехорошо при каждом повороте переподключаться к БД.
        // нельзя рвать http соединение при каждом повороте, да и данные HTTP ответа надо куда-то складировать

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.master_fragment);
        if (fragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();

            // добавляем фрагмент
            myFragment = new InfinityFragment();
            fragmentTransaction.add(R.id.master_fragment, (Fragment) myFragment);
            fragmentTransaction.commit();
            setCurrentStatus(null); // обновим экран
            Intent i = new Intent(this, AuthorizationActivity.class);
            this.startActivity(i);

            // InfinityFragment сам передаст нам модельку, когда будет создан

        } else { // Это поворот экрана - восстановим модель и обновимся
            myFragment = (InfinityFragment) fragment;
            mModel = myFragment.getModel();
            setCurrentStatus(mModel);
        }
    }

    public final void setModel(CC_controller model) {
        mModel = model;
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast toast = Toast.makeText(ColdCallActivity.this,
                R.string.toast_wifi,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);

        //wifi блок - просто матюгальник, без какой-либо логики
        try {
            if (!wManager.isWifiEnabled()) {
                toast.show();
            }
        } catch (RuntimeException e) {
            toast.show();
        }
    }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_cold_call, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_menu_SMS) {
                return true;
            }
            if(id == R.id.action_menu_call){

            }

            return super.onOptionsItemSelected(item);
        }

    private final View.OnClickListener onClickListenerCall = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //  Взять следующий номер
            String mNextPhone = mModel.GUI_getPhone2CallAndStartCalling();
            if (mNextPhone == null) {
                setCurrentStatus(mModel);
                statusView.setText("Набор номера прерван входящим звонком");
                return;
            }
            Uri uri = Uri.parse("tel:" + mNextPhone);
            Intent intent = new Intent(Intent.ACTION_CALL, uri);
            statusView.setText("Подготовка вызова " + mNextPhone);

            try {
                startActivity(intent);
            } catch (SecurityException ex) {
                CC_controller.SYS_LogError(TAG, "101 SecurityException - не совершен звонок");
            }
        }
    };

    private final View.OnClickListener onClickListenerMain = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (mModel.GUI_getCurrentStatus()) {
                case CC_controller.AppStatuses.INIT: // Инициализация - обращаемся к HTTP
                    mModel.GUI_startAuthorization();
                    break;
                case CC_controller.AppStatuses.INCOMING: // Входящий звонок - не рабочий
                case CC_controller.AppStatuses.READY2CALL: // Начало - готов к работе, след. номер
                case CC_controller.AppStatuses.RINGING: // Исходящий звонок
                case CC_controller.AppStatuses.SMS:
                    break;

                case CC_controller.AppStatuses.THINKING: // После входящего или исходящего звонка
                    mModel.GUI_RefuseClient();
                    break;

                case CC_controller.AppStatuses.NEEDUPGRADE:
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://crm.leadinka.com/ccc_intellect.apk"));
                        startActivity(intent);
                    } catch (SecurityException ex) {
                        CC_controller.SYS_LogError(TAG, "108 SecurityException - не могу обновиться");
                    }

                    break;
            }


        }
    };
    private final View.OnClickListener onClickListenerCrone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (mModel.GUI_getCurrentStatus()) {
                case CC_controller.AppStatuses.INIT:
                case CC_controller.AppStatuses.READY2CALL:
                case CC_controller.AppStatuses.NEEDUPGRADE:

                    FragmentManager fragmentManager = getFragmentManager();
                    Fragment fragment = fragmentManager.findFragmentById(R.id.master_fragment);
                    if (fragment != null) {
                        FragmentTransaction fragmentTransaction = fragmentManager
                                .beginTransaction();
                        fragmentTransaction.remove(fragment);
                        fragmentTransaction.commit();
                    }

                    ColdCallActivity.this.finish();
                default:
                    break;

            }
        }
    };

    private final View.OnClickListener onClickListenerLead = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (mModel.GUI_getCurrentStatus()) {
                case CC_controller.AppStatuses.INIT: // Инициализация - обращаемся к HTTP
                    break;
                case CC_controller.AppStatuses.READY2CALL: // Начало - готов к работе, след. номер
                    mModel.GUI_OpenActivityReport(ColdCallActivity.this, REQUEST_CODE_REPORT);
                    break;
                case CC_controller.AppStatuses.INCOMING: // Входящий звонок - не рабочий
                case CC_controller.AppStatuses.RINGING: // Исходящий звонок
                    break;
                case CC_controller.AppStatuses.THINKING: // После входящего или исходящего звонка
                case CC_controller.AppStatuses.SMS:
                    mModel.GUI_OpenActivityNewLead(ColdCallActivity.this, REQUEST_CODE_DRAFT);
                    break;
            }


        }
    };

    private final View.OnClickListener onClickListenerBreak = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (mModel.GUI_getCurrentStatus()) {
                case CC_controller.AppStatuses.INIT: // Инициализация - обращаемся к HTTP
                case CC_controller.AppStatuses.READY2CALL: // Начало - готов к работе, след. номер
                    break;
                case CC_controller.AppStatuses.INCOMING: // Входящий звонок - не рабочий
                case CC_controller.AppStatuses.RINGING: // Исходящий звонок

                    mModel.GUI_StopCalling();
                    break;
                case CC_controller.AppStatuses.THINKING: // После входящего или исходящего звонка
                    mModel.GUI_UnavailableNumber();
                    break;
                case CC_controller.AppStatuses.SMS:
                    mModel.GUI_LFSkipSMS();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mModel == null) {
            String tmp = "Сбой в приложении - нет модели при возврате интента - сообщите разработчику";
            Toast.makeText(ColdCallActivity.this, tmp,
                    Toast.LENGTH_LONG).show();
            CC_controller.SYS_LogError(TAG, tmp);
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_DRAFT) {
            if (data == null) {
                return;
            }
            mModel.GUI_FixDraft(data);
        }
    }

}
