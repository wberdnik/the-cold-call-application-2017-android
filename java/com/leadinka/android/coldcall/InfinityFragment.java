package com.leadinka.android.coldcall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by VLF on 21.04.2017.
 */

public class InfinityFragment extends Fragment {
    private CC_controller mModel;
    private BroadcastReceiver broadcastReceiver;

    private static final String TAG = "ColdCall BroadcastRec";
    private Context context;

    public CC_model2guiInterface getModel() {
        return mModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // помечаем фрагмент как вечный

        ColdCallActivity MainActivity = (ColdCallActivity) getActivity();

        // единственный запуск модели
        context = MainActivity.getApplicationContext();
        mModel = new CC_controller(context, this, MainActivity);
        // Создание происходит позже лайнчера - мы отдадим модельку

        MainActivity.setModel(mModel);


        // слушалка статуса телефона
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
                    String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        //Трубка не поднята, телефон звонит
                        String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        mModel.TMI_StartIncoming(phoneNumber);


                    } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        mModel.TMI_StartOutcoming();

                    } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        //Телефон находится в ждущем режиме - это событие наступает по окончанию разговора
                        //или в ситуации "отказался поднимать трубку и сбросил звонок".
                        mModel.TMI_StopCalling();
                    }
                }

                if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                    //получаем исходящий номер
                    String phoneNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
                    mModel.TMI_StartNewCall(phoneNumber);
                }
            }
        };
        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilter = new IntentFilter("android.intent.action.PHONE_STATE");
        // регистрируем (включаем) BroadcastReceiver
        context.registerReceiver(broadcastReceiver, intFilter);

        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilter2 = new IntentFilter("android.intent.action.NEW_OUTGOING_CALL");
        // регистрируем (включаем) BroadcastReceiver
        context.registerReceiver(broadcastReceiver, intFilter2);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment, container, false);// Не важно какая фигня
    }

    @Override
    public void onDestroy() {
        mModel.SYS_StopHttpLoop();
        context.unregisterReceiver(broadcastReceiver);
        mModel = null;
        super.onDestroy();
    }

}
