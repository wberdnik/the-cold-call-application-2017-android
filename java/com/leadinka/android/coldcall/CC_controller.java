package com.leadinka.android.coldcall;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;


public class CC_controller implements CC_model2guiInterface {

    private static final String TAG = "CC_controller";
    private static final int CCC_VERSION = 23; //2.3
    private boolean flagStop;

    // состояние приложения как автомата
    public static final class AppStatuses {
        public static final int INIT = 0; // инициализация
        public static final int READY2CALL = 1; // готов к работе
        public static final int RINGING = 2; // звонок исходящий
        public static final int INCOMING = 3; // звонок входящий
        public static final int THINKING = 4; // после входящего или исходящего звонка
        public static final int SMS = 7;
        public static final int NEEDUPGRADE = 8;
    }

    //==============================================================================================
    //  Системные элементы
    private Context mContext;
    private InfinityFragment mInfinityFragment;
    private InitTask httpTask;
    private SQLiteDatabase mDB;


    //==============================================================================================
    //  Локальные элементы

    public static CC_controller that;

    private ActiveRecordNextPhone mNF;
    private ActiveRecordLastPhone mLF;

    protected int mCurrentStatus;
    protected int mPinOperator;

    public static SQLiteDatabase getDB() {
        return that.mDB;
    }


    //==============================================================================================
    //  Реализация интерфейса для GUI

    @Override
    public String GUI_getStatusStringReady2Call() {
        if (mNF == null) {
            mNF = new ActiveRecordNextPhone(mDB);
        }
        return "Пин " + mPinOperator + "\n" + mNF.getPhone() + "\n"+mNF.getText(); //"\n Запас: " + mNF.getCountsOfNew();
    }

    @Override
    public String GUI_getStatusStringThinking() {
        if (mLF == null) {
            mLF = new ActiveRecordLastPhone(mDB);
        }

        return "" + mLF.getPhone() + "\n" + mLF.getType_() + "\n\n"
                + mLF.getStarted_() + "\nДлитель.: " + mLF.getDuration() + "сек.\n\nВсего: " + mLF.getCountPhones();

    }


    @Override
    public String GUI_getStatusStringSMS() {
        if (mLF == null) {
            mLF = new ActiveRecordLastPhone(mDB);
        }

        return "" + mLF.getPhone() + "\n" + mLF.getType_() + "\n\n"
                + mLF.getSMS() + "\n\nВсего: " + mLF.getCountPhones();
    }

    @Override
    public String GUI_getPhone2CallAndStartCalling() {
        // if (mCurrentStatus == AppStatuses.INCOMING) {
        //     return null;
        // }


        if (mCurrentStatus == AppStatuses.READY2CALL) {
            // Вызов телефонного модуля из нашего приложения.
            mNF = null;
            mNF = new ActiveRecordNextPhone(mDB);

            mNF.StartCallingTime(); // предварительно фиксируем время звонка, и тем самым, обозначаем статус начатого
            return mNF.getPhone();
        }
        if (mCurrentStatus == AppStatuses.SMS) {
            // Вызов телефонного модуля из нашего приложения.
            mLF = null;
            mLF = new ActiveRecordLastPhone(mDB);
            if (mLF.getType() == DB_schema.LastPhoneTable.Types.SMS) {

                String phone = mLF.getPhone();
                mLF.Decide(TRUE);
                mLF = null;
                mCurrentStatus = AppStatuses.THINKING;
                return phone;
            }
            SYS_LogError(TAG, "Попытка звонка по SMS, но запись - не SMS");
            return "0";
        }
        //SYS_LogError(TAG, "getPhone2CallAndStartCalling вне статуса, статус : " + mCurrentStatus);
        //GUI_MakeAndShowStatus(AppStatuses.NEEDUPGRADE);
        return null;
    }

    @Override
    public int GUI_getCurrentStatus() {
        return mCurrentStatus;
    }

    @Override
    public boolean GUI_startAuthorization() {


        mCurrentStatus = AppStatuses.INIT;
        mPinOperator = 0;

        // Очистим системную таблицу
        mDB.execSQL("DELETE FROM " + DB_schema.PinTable.PIN);
// Android M
       /* if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
        }*/
        // телефонный блок
        String imeiSIM1;
        String imeiSIM2;
        String iccidSIM1;
        String iccidSIM2;
        try {
            TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(mContext);
            imeiSIM1 = telephonyInfo.getImeiSIM1();
            imeiSIM2 = telephonyInfo.getImeiSIM2();
            iccidSIM1 = telephonyInfo.getIccidSIM1();
            iccidSIM2 = telephonyInfo.getIccidSIM2();
        } catch (java.lang.SecurityException ex) {

            ActivityCompat.requestPermissions(mInfinityFragment.getActivity(),
                    new String[]{
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_SMS,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PackageManager.PERMISSION_GRANTED);


            for (int i = 0; i < 4; i++) {
                ColdCallActivity MainActivity = (ColdCallActivity) (mInfinityFragment.getActivity());

                if (MainActivity == null) {
                    Helpers.sleep(1);
                    continue;
                }

                TextView statusView = (TextView) (MainActivity.findViewById(R.id.status));
                statusView.setText("если предоставлены права - повторите попытку");
                return false;
            }
            return false;

        }

        if (iccidSIM1 == null && iccidSIM2 == null) {
            for (int i = 0; i < 4; i++) {
                ColdCallActivity act = (ColdCallActivity) mInfinityFragment.getActivity();

                if (act == null) {
                    Helpers.sleep(1);
                    continue;
                }
                TextView statusView = (TextView) (act.findViewById(R.id.status));
                statusView.setText("НЕТ СИМ карт");
                return false;
            }
            return false;
        }

        httpTask = new InitTask();
        httpTask.execute(
                "iccidSIM1", iccidSIM1,
                "imeiSIM1", imeiSIM1,
                "iccidSIM2", iccidSIM2,
                "imeiSIM2", imeiSIM2
        );
        return true;

    }


    @Override
    public void GUI_UnavailableNumber() {
        if (mLF == null) {
            CC_controller.SYS_LogError(TAG, "Пометка НЕдоступным для несозданной mLF");
            return;
        }
        mLF.Decide(Boolean.FALSE);
        mLF = null;
        mCurrentStatus = AppStatuses.THINKING;
        GUI_Refresh();
    }

    @Override
    public void GUI_LFSkipSMS() {
        GUI_UnavailableNumber(); // так правильнее

    }


    @Override
    public void GUI_RefuseClient() {
        if (mLF == null) {
            CC_controller.SYS_LogError(TAG, "Пометка доступным для несозданной mLF");
            return;
        }
        mLF.Decide(TRUE);
        mLF = null;
        mCurrentStatus = AppStatuses.THINKING;
        GUI_Refresh();
    }

    @Override
    public void GUI_OpenActivityReport(ColdCallActivity activity, int CodeActivity) {
        Intent i = new Intent(activity, ReportActivity.class);
        //i.putExtra("pin", mPinOperator);
        activity.startActivity(i);
    }

    @Override
    public void GUI_OpenActivityNewLead(ColdCallActivity activity, int CodeActivity) {
        if (mLF == null) {
            CC_controller.SYS_LogError(TAG, "Черновик для несозданной mLF");
            return;
        }

        Intent i = new Intent(activity, DraftActivity.class);
        i.putExtra("pin", mPinOperator);
        i.putExtra("lastPhone", mLF.getPhone());
        i.putExtra("type", mLF.getType_());
        activity.startActivityForResult(i, CodeActivity);
    }

    @Override
    public void GUI_StopCalling() {
        // Зависание телефонного модуля
        // "аварийное" завершение звонка
        TMI_StopCalling();
    }

    @Override
    public void GUI_FixDraft(Intent data) {
        String fio = data.getStringExtra("fio");
        String Phone = data.getStringExtra("lastPhone");
        String fabule = data.getStringExtra("fabule");
        String datetime = data.getStringExtra("datetime");
        String city = data.getStringExtra("city");

        String type = data.getStringExtra("type"); //1 - заявка, 2 приход
        int pin;

        try {
            if (Integer.parseInt(type) == 2) {
                pin = mPinOperator + 1000;
            } else {
                pin = mPinOperator;
            }
        } catch (NumberFormatException ii) {
            pin = mPinOperator;
        }

        DB_schema.addDraft(mDB, datetime, fabule, Phone, fio, city, "" + pin);

        if (mLF == null) {
            CC_controller.SYS_LogError(TAG, "После второй активити - несозданной mLF");
            return;
        }

        mLF.Decide(TRUE);
        mLF = null;
        mCurrentStatus = AppStatuses.THINKING;
        GUI_Refresh();

    }

    @Override
    public int GUI_ThinkingOrReady() {
        if (mCurrentStatus != AppStatuses.THINKING) {
            return mCurrentStatus;
        }
        mLF = null;
        mLF = new ActiveRecordLastPhone(mDB);//Нас спрашивают - перечитаем базу

        if (mLF.getCountPhones() < 1) {
            mCurrentStatus = AppStatuses.READY2CALL;
        } else if (mLF.getType() == DB_schema.LastPhoneTable.Types.SMS) {
            mCurrentStatus = AppStatuses.SMS;
        }
        return mCurrentStatus;
    }

    protected void GUI_Refresh() {
        GUI_MakeAndShowStatus(mCurrentStatus);
    }

    protected void GUI_MakeAndShowStatus(int newStatus) {
        mCurrentStatus = newStatus;
        ColdCallActivity MainActivity = null;

        for (int i = 0; i < 4; i++) {
            MainActivity = (ColdCallActivity) (mInfinityFragment.getActivity());
            if (MainActivity == null) {
                Helpers.sleep(1);
                continue;
            }
            MainActivity.setCurrentStatus(CC_controller.this);
            break;
        }

    }
    //==============================================================================================


    public static void SYS_LogError(String module, String text) {
        if (that != null) {
            DB_schema.addError(that.mDB, module, text);
        } else {
            Log.e(module, text);
        }
    }

    public void SYS_StopHttpLoop() {
        mCurrentStatus = AppStatuses.INIT;
        if (httpTask != null) {
            httpTask.cancel(true);
        }
        flagStop = true;
        httpTask = null;
    }


//=================================================================================================
//  Телефонный интерфейс

    public void TMI_StartIncoming(String phone) {

        if (mCurrentStatus == AppStatuses.INIT) { // в фоновом режиме не мешаем работе телефона
            return;
        }

        NF_StopOutcoming(); // сначала NF, потом LF


        if (!Helpers.exists(mDB,
                DB_schema.LastPhoneTable.LASTPHONE,
                "" + DB_schema.LastPhoneTable.Cols.PHONE + " = '" + phone +
                        "' AND " +
                        DB_schema.LastPhoneTable.Cols.TYPE + " = " + DB_schema.LastPhoneTable.Types.INBOUND
        )) {

            LF_StopRingingAndFixDuration(); // закроем старые звонки

            DB_schema.addLastPhone(mDB, DB_schema.LastPhoneTable.Types.INBOUND, phone, 0); // откроем новый

            GUI_MakeAndShowStatus(AppStatuses.INCOMING); // освежим экран
        }
    }

    public void TMI_StartNewCall(String phone) {
        if (mCurrentStatus == AppStatuses.INIT) {
            return;
        }

        int idNewCall = 0;

        if (mNF != null && mNF.isStarted()) { // обновим время начала звонка из пула
            mNF.StartCallingTime();
            idNewCall = mNF.getId();
        }

        if (!Helpers.exists(mDB,
                DB_schema.LastPhoneTable.LASTPHONE,
                "" + DB_schema.LastPhoneTable.Cols.PHONE + " = '" + phone +
                        "' AND " +
                        DB_schema.LastPhoneTable.Cols.TYPE + " = " + DB_schema.LastPhoneTable.Types.OUTBOUND
        )) {

            LF_StopRingingAndFixDuration();  // закроем старые звонки

            DB_schema.addLastPhone( // откроем новый
                    mDB,
                    DB_schema.LastPhoneTable.Types.OUTBOUND,
                    phone,
                    idNewCall // наш/ не наш
            );

            GUI_MakeAndShowStatus(AppStatuses.RINGING); // обновим экран
        }
    }

    /**
     * Запасная заглушка для начала звонка
     */
    public void TMI_StartOutcoming() {

        if (mCurrentStatus == AppStatuses.INIT) {
            return;
        }
        if (mNF != null && mNF.isStarted()) {
            TMI_StartNewCall(mNF.getPhone());
        }
    }


    public void TMI_NewSMS(ArrayList<String> messages, ArrayList<String> numbers) {
        if (mCurrentStatus == AppStatuses.INIT) {
            return;
        }
        for (int i = 0; i < messages.size(); i++) {
            String smsMessageBody = messages.get(i);
            Pattern pattern = Pattern.compile("(\\d[\\d|\\(|\\/|\\-|\\s|\\)]{8,17}\\d)");
            Matcher matcher = pattern.matcher(smsMessageBody);
            String clearPhone = "";
            if (matcher.find()) {
                String substr = matcher.group();
                int ln = substr.length();

                for (int j = ln; j > 0; j--) {
                    char sm = substr.charAt(j - 1);
                    if (sm == '0' || sm == '1' || sm == '2' || sm == '3' || sm == '4' || sm == '5' || sm == '6'
                            || sm == '7' || sm == '8' || sm == '9') {
                        clearPhone = sm + clearPhone;
                    }
                }
            }
            long phone = 0;
            if (clearPhone.length() > 0) {
                phone = Long.parseLong(clearPhone);
            }
            if (phone < 3000000000L) {
                clearPhone = numbers.get(i);
            } else {
                phone = phone % 10000000000L;
                clearPhone = "8" + phone;
            }
            if (!Helpers.exists(mDB, DB_schema.Sms.SMS, "" + DB_schema.Sms.Cols.PHONE + " = '" + clearPhone + "'")) {
                DB_schema.addSMS(mDB, clearPhone, smsMessageBody);
            }
        }

    }

    public void TMI_StopCalling() {

        NF_StopOutcoming();// сначала NF

        LF_StopRingingAndFixDuration(); // потом LF
        GUI_MakeAndShowStatus(AppStatuses.THINKING); // обновим экран
    }

    //-------------------------------------------------------------------------------------------

    private void NF_StopOutcoming() {
        if (mNF != null && mNF.isStarted()) {
            mNF.StopCallingAndQueue();
        }
    }

    private void LF_StopRingingAndFixDuration() {

        long lCreated = System.currentTimeMillis() / 1000L;
        try {
            mDB.execSQL("UPDATE " + DB_schema.LastPhoneTable.LASTPHONE +
                    " SET " + DB_schema.LastPhoneTable.Cols.FINISHED + "= '" + lCreated + "' WHERE " +
                    DB_schema.LastPhoneTable.Cols.FINISHED + " = 0");
        } catch (SQLException i) {
            SYS_LogError("StopRinging", i.toString());
        }
    }
//==============================================================================================


    private class InitTask extends AsyncTask<String, String, String> {

        private static final String TAG = "AsyncTask_Main";

        @Override
        public String doInBackground(String... params) {// метод возвращает для onPostExecute
            String token;
            List<NameValuePair> pairs = new ArrayList<NameValuePair>(4);
            JSONObject dataJsonObj;
            String response = "";

            dataJsonObj = new JSONObject();

            pairs.add(new BasicNameValuePair(params[0], params[1]));
            pairs.add(new BasicNameValuePair(params[2], params[3]));
            pairs.add(new BasicNameValuePair(params[4], params[5]));
            pairs.add(new BasicNameValuePair(params[6], params[7]));

            pairs.add(new BasicNameValuePair("token", "hr843j743g2t33rh8"));


            try {
                try {

                    dataJsonObj.put("params", DB_schema.getAllParams(that.mDB, DB_schema.Params.Types.AUTHORIZATION));
                    pairs.add(new BasicNameValuePair("params", dataJsonObj.toString()));
                } catch (JSONException e) {
                    String test = "Личные параметры: Ошибка разбора JSON, код " + e + " => " + dataJsonObj.toString();
                    CC_controller.SYS_LogError(TAG, test);
                }
                response = MakeHttpRequest(pairs, "http://leadinka.com/fetch/init_cc");

                dataJsonObj = new JSONObject(response);
                int status = dataJsonObj.getInt("status");

                if (status != 200) {

                    String tmp = "Отказано сервером: " + status + " " + dataJsonObj.getString("comment");
                    if (status != 403) { //Отказано сервером: 403 iccidSIM1 Доступ закрыт - обратитесь к менеджеру
                        CC_controller.SYS_LogError(TAG, tmp);
                    } else {
                        tmp = "Пока не разрешено. ПОПРОСИТЕ менеджера, что бы включил.";
                    }
                    return tmp;
                }

                token = dataJsonObj.getString("token");
                that.mPinOperator = dataJsonObj.getInt("pin");

                if (CCC_VERSION < dataJsonObj.getInt("need_version")) {

                    while (true) {
                        publishProgress("needUpgrade", "Нужно обновить программу");
                        Helpers.sleep(10);
                    }
                }

                DB_schema.setActivePin(mDB,
                        dataJsonObj.getString("icc"),
                        dataJsonObj.getString("imei"),
                        dataJsonObj.getString("pin"),
                        token);

            } catch (HttpHostConnectException ex) {

                return "НЕТ ПОДКЛЮЧЕНИЯ К ИНТЕРНЕТУ";
            } catch (IOException ioe) {
                String test = ioe.toString();
                if (test.indexOf("No address associated with hostname") != -1 ||
                        test.indexOf("recvfrom failed: ETIMEDOUT (Connection timed out)") != -1) {
                    return "НЕТ ПОДКЛЮЧЕНИЯ К ИНТЕРНЕТУ";
                } else {
                    CC_controller.SYS_LogError(TAG, "Ошибка подключения инициализации URL: " + test);
                }
                return "Ошибка подключения, код " + test;
            } catch (JSONException e) {
                String test = "Auth: Ошибка разбора JSON, код " + e;
                CC_controller.SYS_LogError(TAG, test + " "+response);
                return test;
            }finally {
                publishProgress("progressbar","stop");
            }

            int CountofErrors =0;

            // к работе готовы - делем первый цикл
            while (!flagStop) {
                if(CountofErrors > 3){
                    return "Проблемы с интернетом. Заявки могут не отправляться";
                }
                int CountNewPhones = 0;
                dataJsonObj = new JSONObject();

                Cursor cursorPhone = mDB.query(DB_schema.PhonesTable.PHONE, null, null, null, null, null, null);
                cursorPhone.moveToFirst();
                try {

                    JSONArray JSArray = new JSONArray();
                    while (!cursorPhone.isAfterLast()) {
                        int status = cursorPhone.getInt(cursorPhone.getColumnIndex(DB_schema.PhonesTable.Cols.STATUS));
                        // нет смысла отправлять свежеполученные телефоны
                        if (status == DB_schema.PhonesTable.Status.NEW) {
                            CountNewPhones++;
                            cursorPhone.moveToNext();
                            continue;
                        }

                        JSArray.put(DB_schema.getJSONObject4Phone(cursorPhone));
                        cursorPhone.moveToNext();
                    }

                    dataJsonObj.put("phones", JSArray);

                    if (CountNewPhones > 0) {
                        publishProgress("ready", "Остались номера - работаем");
                    }

                    dataJsonObj.put("CountNewPhones", CountNewPhones);
                    dataJsonObj.put("draft", DB_schema.getAllDraft(that.mDB));
                    dataJsonObj.put("errors", DB_schema.getAllErrors(that.mDB));
                    dataJsonObj.put("params", DB_schema.getAllParams(that.mDB, DB_schema.Params.Types.COOKIES));


                } catch (android.database.CursorIndexOutOfBoundsException mm) {
                    publishProgress("show", "Ошибка 101 - сообщите разработчику");
                    CC_controller.SYS_LogError(TAG, "Ошибка выборки пакета для отправки на лидинку");
                    cursorPhone.close();
                    CountofErrors ++;
                    continue;
                } catch (JSONException e) {
                    CC_controller.SYS_LogError(TAG, "202 Ошибка ПОСТРОЕНИЯ JSON, код " + e);
                    cursorPhone.close();
                    return "Ошибка 202 - сообщите разработчику " + e;
                }

                cursorPhone.close();

                pairs = new ArrayList<NameValuePair>(1);
                pairs.add(new BasicNameValuePair("data", dataJsonObj.toString()));
                pairs.add(new BasicNameValuePair("token", token));

                try {

                    response = MakeHttpRequest(pairs, "http://leadinka.com/fetch/android");

                    dataJsonObj = new JSONObject(response);
                    int status = dataJsonObj.getInt("status");
                    if (status != 200) {
                        if (status == 500) {
                            return "Запрещена работа на сервере";
                        }
                        String tmp = "Отказано сервером в синхронизации: " + status + " " + dataJsonObj.getString("comment");
                        CC_controller.SYS_LogError(TAG, tmp);
                        publishProgress("show", tmp);

                        Helpers.sleep(20);
                        continue;
                    }

                    //    publishProgress("show", "Подтверждены заявки");
                    Helpers.DeleteAll(that.mDB, DB_schema.DraftTable.DRAFT, dataJsonObj.getJSONArray("iddraft"));
                    Helpers.DeleteAll(that.mDB, DB_schema.Errors.ERRORS, dataJsonObj.getJSONArray("iderrors"));
                    Helpers.DeleteAll(that.mDB, DB_schema.PhonesTable.PHONE, dataJsonObj.getJSONArray("idphones"));
                    Helpers.LoadReport(that.mDB, DB_schema.Report.REPORT, dataJsonObj.getJSONArray("report"));


                    JSONArray newphones = dataJsonObj.getJSONArray("newphones");
                    JSONArray newtexts = dataJsonObj.getJSONArray("newtexts");

                    if (newphones.length() > 0) {
                        for (int i = 0; i < newphones.length(); i++) {
                            DB_schema.addPhone(
                                    that.mDB,
                                    newphones.getString(i),
                                    newtexts.getString(i),
                                    DB_schema.PhonesTable.Status.NEW);
                        }
                        publishProgress("show", "Получено " + newphones.length() + " номеров");

                    }

                    DB_schema.setParams(that.mDB, dataJsonObj.getJSONArray("newparams"));

                } catch (HttpResponseException ex) {
                    CountofErrors ++;
                    CC_controller.SYS_LogError(TAG, "500-я ошибка сервера: " + ex);
                } catch (HttpHostConnectException ex)
                {
                    CountofErrors ++;
                    publishProgress("show", "НЕТ ПОДКЛЮЧЕНИЯ К ИНТЕРНЕТУ");
                } catch (IOException ioe) {
                    CountofErrors ++;
                    String test = ioe.toString();//
                    if (test.indexOf("No address associated with hostname") != -1
                            || test.indexOf("Connection to http://leadinka.com refused") != -1
                            ) {
                        publishProgress("show", "НЕТ ПОДКЛЮЧЕНИЯ К ИНТЕРНЕТУ");

                    } else {
                        CC_controller.SYS_LogError(TAG, "308 Общая сетевая ошибка: " + test);
                        publishProgress("show", "ОШИБКА СЕТИ");
                    }
                    Helpers.sleep(20);
                    continue;
                } catch (JSONException e) {
                    CC_controller.SYS_LogError(TAG, "303 Ошибка разбора JSON, код " + e + " Ответ "+response);
                    return "Ошибка 303 - сообщите разработчику " + e;
                }finally {
                    publishProgress("progressbar","stop");
                }
                CountofErrors =0;
                publishProgress("ready", "Успешная репликация");

                Helpers.sleep(60);
            }
            return "";
        }

        private String MakeHttpRequest(List<NameValuePair> pairs, String mUrl) throws IOException {

           publishProgress("progressbar","start");

            HttpClient client;
            HttpPost http;
            System.setProperty("http.keepAlive","false");

            HttpParams httpParameters = new BasicHttpParams();
            HttpProtocolParams.setContentCharset(httpParameters, HTTP.UTF_8);
            HttpProtocolParams.setHttpElementCharset(httpParameters, HTTP.UTF_8);

            client = new DefaultHttpClient();

            client.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
            //client.getParams().setParameter("http.socket.timeout", new Integer(2000));
            client.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
            httpParameters.setBooleanParameter("http.protocol.expect-continue", false);

            http = new HttpPost(mUrl);
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(pairs, HTTP.UTF_8);
            http.setEntity(formEntity);

            publishProgress("show", "Подключаемся к серверу,\n если висит - нет интернета");
            String response = client.execute(http, new BasicResponseHandler());
            publishProgress("show", "Сервер ответил");
            publishProgress("progressbar","stop");

            return response;
        }

        @Override
        protected void onPostExecute(String items) {
            super.onPostExecute(items);
            // Завершение фонового потока указывает на неработоспособность приложения

            mCurrentStatus = AppStatuses.INIT;
            mPinOperator = 0;
            this.cancel(true);

            for (int i = 0; i < 4; i++) {
                ColdCallActivity MainActivity = (ColdCallActivity) (mInfinityFragment.getActivity());

                if (MainActivity == null) {
                    Helpers.sleep(1);
                    continue;
                }

                TextView statusView = (TextView) (MainActivity.findViewById(R.id.status));
                MainActivity.setCurrentStatus(CC_controller.this);
                statusView.setText(items);
                return;
            }
        }

        @Override
        public void onProgressUpdate(String... params) {
            super.onProgressUpdate(params);
            //publishProgress(…)
            if (params[0] == "show") {
                for (int i = 0; i < 4; i++) {
                    ColdCallActivity MainActivity = (ColdCallActivity) (mInfinityFragment.getActivity());

                    if (MainActivity == null) {
                        Helpers.sleep(1);
                        continue;
                    }

                    TextView statusView = (TextView) (MainActivity.findViewById(R.id.status));
                    statusView.setText(params[1]);
                    return;
                }
            } else if (params[0] == "ready") {
                if (mCurrentStatus == AppStatuses.INIT) {
                    GUI_MakeAndShowStatus(AppStatuses.READY2CALL);

                } else {
                    GUI_Refresh();
                }

            } else if (params[0] == "needUpgrade") {
                mCurrentStatus = AppStatuses.NEEDUPGRADE;
                GUI_MakeAndShowStatus(mCurrentStatus);
            }else  if(params[0] == "progressbar"){
                for (int i = 0; i < 4; i++) {
                    ColdCallActivity MainActivity = (ColdCallActivity) (mInfinityFragment.getActivity());

                    if (MainActivity == null) {
                        Helpers.sleep(1);
                        continue;
                    }

                    ProgressBar pb = (ProgressBar) (MainActivity.findViewById(R.id.progressBar));
                    pb.setVisibility(params[1] == "start" ? ProgressBar.VISIBLE: ProgressBar.INVISIBLE);
                    return;
                }

            }

        }

    }


    public CC_controller(Context context, InfinityFragment pInfinityFragment, ColdCallActivity MainActivity) {
        mContext = context.getApplicationContext();
        mDB = new ColdBaseHelper(mContext)
                .getWritableDatabase();
        mCurrentStatus = AppStatuses.INIT;
        mPinOperator = 0;
        httpTask = null;
        that = null;

        mNF = null;
        flagStop = false;

        mInfinityFragment = pInfinityFragment; // Через вечный фрагмент будем находить Host Activity

        that = this; // Так как объект класса существует всегда, делаем его доступным по статической ссылке

    }


}
