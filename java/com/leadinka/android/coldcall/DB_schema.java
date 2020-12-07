package com.leadinka.android.coldcall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public final class DB_schema {
    public static final class PhonesTable {
        public static final String PHONE = "phones"; // все телефоны смотри ТЗ

        public static final class Cols {
            public static final String PHONE = "phone";
            public static final String STATUS = "status";
            public static final String CREATED = "created";
            public static final String DURATION = "duration";
            public static final String TXT = "text";
        }

        public static final class Status {
            public static final int NEW = 0; //Новый. Время поступления
            public static final int INBOUND = 1; // входящий. Время звонка
            public static final int CALLED = 2; // входящий. Время звонка
            public static final int QUEUE = 3; // Номер на обработке в телефоне
            public static final int MISSED = 4; // упущенный входящий вызов
            public static final int UNAVAILABLE = 5; // Недоступен - время звонка
            public static final int FROMSMS = 6; // Недоступен - время звонка
        }

    }

    public static final class PinTable {
        public static final String PIN = "pin";// текущий оператор - одна строка

        public static final class Cols {
            public static final String PIN = "pin";
            public static final String ICC = "icc";
            public static final String IMEI = "imei";
            public static final String TOKEN = "token";
        }
    }

    public static final class DraftTable {
        public static final String DRAFT = "draft";// черновик - временное хранение неотправленных заявок

        public static final class Cols {
            public static final String PIN = "pin";
            public static final String COMMENT = "comment";
            public static final String CREATED = "created";
            public static final String CALL_AT = "call_at";
            public static final String PHONE = "phone";
            public static final String CITY = "city";
            public static final String FIO = "fio";
        }
    }

    public static final class Report {
        public static final String REPORT = "report";

        public static final class Cols {
            public static final String DATE = "date";
            public static final String NAME = "name";
            public static final String FABULE = "fabule";
            public static final String COMMENT = "comment";
            public static final String SUMMA = "summa";
        }
    }

    public static final class Errors {
        public static final String ERRORS = "errors";

        public static final class Cols {
            public static final String DATE = "date";
            public static final String MODULE = "module";
            public static final String TEXT = "text";
        }
    }

    public static final class Sms {
        public static final String SMS = "sms";

        public static final class Cols {
            public static final String DATE = "date";
            public static final String PHONE = "phone";
            public static final String TEXT = "text";
        }
    }

    public static final class Params {
        public static final String PARAMS = "params";

        public static final class Cols {
            public static final String NAME = "name";
            public static final String VALUE = "value";
            public static final String TYPE = "type";
        }

        public static final class Types {
            public static final int COOKIES = 0; // Всегда отправляются на сервер
            public static final int REPORT_DATA = 1; // Никогда не отправляются на сервер
            public static final int AUTHORIZATION = 2; //Отправляются на сервер при авторизации
            public static final int SPECHES = 3; //Спичи
            public static final int SMS_LEAD = 4;
            public static final int DELETE = 9;
        }
    }

    public static final class LastPhoneTable {
        public static final String LASTPHONE = "last_phone";// временное хранилище звонков

        public static final class Cols {
            public static final String CREATED = "created";
            public static final String PHONE = "phone";
            public static final String TYPE = "type";
            public static final String FINISHED = "finished";
            public static final String IDNEWPHONE = "idnewphone";
        }

        public static final class Types {
            public static final int INBOUND = 0;
            public static final int OUTBOUND = 1;
            public static final int SMS = 2;
        }

    }


    /**
     * @param phone    - string
     * @param status   - int
     * @param created  - long
     * @param duration - int
     * @return ContentValues
     */
    public static ContentValues getContentValuesPhones(
            String phone,
            String text,
            int status,
            long created,
            int duration) {

        ContentValues values = new ContentValues();
        values.put(PhonesTable.Cols.PHONE, phone);
        values.put(PhonesTable.Cols.STATUS, status);
        values.put(PhonesTable.Cols.CREATED, created);
        values.put(PhonesTable.Cols.TXT, text);
        values.put(PhonesTable.Cols.DURATION, duration);
        return values;
    }

    /**
     * @param icc   - string
     * @param imei  - string
     * @param pin   - string
     * @param token -string
     * @return ContentValues
     */
    public static ContentValues getContentValuesPin(
            String icc,
            String imei,
            String pin,
            String token) {

        ContentValues values = new ContentValues();
        values.put(PinTable.Cols.ICC, icc);
        values.put(PinTable.Cols.IMEI, imei);
        values.put(PinTable.Cols.PIN, pin);
        values.put(PinTable.Cols.TOKEN, token);
        return values;
    }

    /**
     * @param created    - long
     * @param type       - int
     * @param phone      - string
     * @param idNewPhone - int Идентификатор записи звонка из НАШЕЙ базы, иначе 0
     * @return
     */
    public static ContentValues getContentValuesLastPhones(
            long created,
            int type,
            String phone,
            int idNewPhone) {

        ContentValues values = new ContentValues();
        values.put(LastPhoneTable.Cols.CREATED, created);
        values.put(LastPhoneTable.Cols.PHONE, phone);
        values.put(LastPhoneTable.Cols.TYPE, type);
        values.put(LastPhoneTable.Cols.FINISHED, 0);
        values.put(LastPhoneTable.Cols.IDNEWPHONE, idNewPhone);
        return values;
    }

    /**
     * @param call_at - string
     * @param comment - string
     * @param pin     - string
     * @param created - long
     * @param fio     - string
     * @param Phone   -string
     * @param city    - string
     * @return ContentValues
     */
    public static ContentValues getContentValuesDraft(
            String call_at,
            String comment,
            String pin,
            long created,
            String fio,
            String Phone,
            String city) {

        ContentValues values = new ContentValues();
        values.put(DraftTable.Cols.CREATED, created);
        values.put(DraftTable.Cols.COMMENT, comment);
        values.put(DraftTable.Cols.PIN, pin);
        values.put(DraftTable.Cols.FIO, fio);
        values.put(DraftTable.Cols.CITY, city);
        values.put(DraftTable.Cols.PHONE, Phone);
        values.put(DraftTable.Cols.CALL_AT, call_at);
        return values;
    }

    /**
     * table Report
     *
     * @param date    - string
     * @param name    - string
     * @param fabule  - string
     * @param comment - string
     * @param summa   - string
     * @return ContenValues
     */
    public static ContentValues getContentValuesReport(
            String date,
            String name,
            String fabule,
            String comment,
            int summa
    ) {
        ContentValues values = new ContentValues();
        values.put(Report.Cols.DATE, date);
        values.put(Report.Cols.COMMENT, comment);
        values.put(Report.Cols.NAME, name);
        values.put(Report.Cols.FABULE, fabule);
        values.put(Report.Cols.SUMMA, summa);

        return values;
    }

    /**
     * @param module - string
     * @param text   - string
     * @return ContentValues
     */
    public static ContentValues getContentValuesErrors(
            String module,
            String text
    ) {
        ContentValues values = new ContentValues();
        Date df = new java.util.Date(System.currentTimeMillis());
        // String vv = new SimpleDateFormat("MM dd, yyyy hh:mma").format(df);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(df);

        values.put(Errors.Cols.DATE, date);
        values.put(Errors.Cols.TEXT, text);
        values.put(Errors.Cols.MODULE, module);

        return values;
    }

    public static ContentValues getContentValuesSMS(
            String phone,
            String text
    ) {
        ContentValues values = new ContentValues();
        Date df = new java.util.Date(System.currentTimeMillis());
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mma").format(df);

        values.put(Sms.Cols.DATE, date);
        values.put(Sms.Cols.TEXT, text);
        values.put(Sms.Cols.PHONE, phone);

        return values;
    }

    /**
     * @param name  - String
     * @param value - String
     * @return
     */
    public static ContentValues getContentValuesParam(
            String name,
            String value,
            int type
    ) {
        ContentValues values = new ContentValues();

        values.put(Params.Cols.NAME, name);
        values.put(Params.Cols.VALUE, value);
        values.put(Params.Cols.TYPE, type);

        return values;
    }

    //==============================================================================================


    public static void setActivePin(SQLiteDatabase db, String icc, String imei, String pin, String token) {
        // Очистим системную таблицу
        db.execSQL("DELETE FROM " + DB_schema.PinTable.PIN);

        ContentValues values = DB_schema.getContentValuesPin(icc, imei, pin, token);
        db.insert(DB_schema.PinTable.PIN, null, values);
    }

    /**
     * @param db           - база данных
     * @param call_at      - String время заявки
     * @param comment      - текст заявки
     * @param phone        - String телефон
     * @param fio          - String ФИО
     * @param city         - String город
     * @param mPinOperator - String Пин оператора
     */
    public static void addDraft(
            SQLiteDatabase db,
            String call_at,
            String comment,
            String phone,
            String fio,
            String city,
            String mPinOperator
    ) {
        long created = System.currentTimeMillis() / 1000L;

        ContentValues values = DB_schema.getContentValuesDraft(call_at, comment, mPinOperator, created, fio, phone, city);
        db.insert(DraftTable.DRAFT, null, values);
    }

    /**
     * @param db     - база данных
     * @param phone  - String телефон
     * @param status - int статус
     */

    public static void addPhone(
            SQLiteDatabase db,
            String phone,
            String text,
            int status) {
        long lCreated = System.currentTimeMillis() / 1000L;

        ContentValues values = DB_schema.getContentValuesPhones(phone, text, status, lCreated, 0);
        db.insert(PhonesTable.PHONE, null, values);
    }

    /**
     * @param db    - База данных
     * @param type  - тип звонка: входящий/исходящий
     * @param phone - сам номер
     * @param idNp  - 0 или ID NextPhone если мы звоним по своей базе
     */
    public static void addLastPhone(
            SQLiteDatabase db,
            int type,
            String phone,
            int idNp
    ) {
        long lCreated = System.currentTimeMillis() / 1000L;
        ContentValues values = getContentValuesLastPhones(lCreated, type, phone, idNp);
        db.insert(LastPhoneTable.LASTPHONE, null, values);
    }

    /**
     * @param db
     * @param date
     * @param name
     * @param fabule
     * @param comment
     * @param summa
     */
    public static void addReport(
            SQLiteDatabase db,
            String date,
            String name,
            String fabule,
            String comment,
            int summa
    ) {
        ContentValues values = getContentValuesReport(date, name, fabule, comment, summa);
        db.insert(Report.REPORT, null, values);
    }

    /**
     * @param db     - база данных
     * @param module - TAG
     * @param text   - текстовка
     */
    public static void addError(
            SQLiteDatabase db,
            String module,
            String text

    ) {
        ContentValues values = getContentValuesErrors(module, text);
        db.insert(Errors.ERRORS, null, values);
    }

    public static String getParamByName(
            SQLiteDatabase db,
            String name) {

        String answer = "";
        try {
            Cursor cursor = db.query(Params.PARAMS, null,
                    "" + Params.Cols.NAME + " = '" + name + "'",
                    null, null, null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                answer = cursor.getString(cursor.getColumnIndex(Params.Cols.VALUE));
            }
            cursor.close();
        } catch (android.database.CursorIndexOutOfBoundsException mm) {
            CC_controller.SYS_LogError("getParamByName", "ошибка чтения БД " + mm);
        }
        return answer;
    }

    /**
     * @param db
     * @param phone - телефон, по которому будем звонить
     * @param text  - текст SMS-ки
     */
    public static void addSMS(
            SQLiteDatabase db,
            String phone,
            String text

    ) {
        ContentValues values = getContentValuesSMS(phone, text);
        long idSms = db.insert(Sms.SMS, null, values);
        addLastPhone(db, LastPhoneTable.Types.SMS, phone, (int) idSms);
    }

    /**
     * @param db - База данных
     * @return JSONArray
     * @throws android.database.CursorIndexOutOfBoundsException
     * @throws JSONException
     */
    public static JSONArray getAllDraft(
            SQLiteDatabase db
    ) throws android.database.CursorIndexOutOfBoundsException, JSONException {

        Cursor cursorDraft = db.query(DB_schema.DraftTable.DRAFT, null, null, null, null, null, null);
        cursorDraft.moveToFirst();
        JSONArray JSArray = new JSONArray();
        while (!cursorDraft.isAfterLast()) {
            JSONObject ob = new JSONObject();
            ob.put("id", "" + cursorDraft.getString(cursorDraft.getColumnIndex("_id")));
            ob.put("call_at", "" + cursorDraft.getString(cursorDraft.getColumnIndex(DB_schema.DraftTable.Cols.CALL_AT)));
            ob.put("comment", "" + cursorDraft.getString(cursorDraft.getColumnIndex(DB_schema.DraftTable.Cols.COMMENT)));
            ob.put("pin", "" + cursorDraft.getString(cursorDraft.getColumnIndex(DB_schema.DraftTable.Cols.PIN)));
            ob.put("fio", "" + cursorDraft.getString(cursorDraft.getColumnIndex(DB_schema.DraftTable.Cols.FIO)));
            ob.put("city", "" + cursorDraft.getString(cursorDraft.getColumnIndex(DB_schema.DraftTable.Cols.CITY)));
            ob.put("phone", "" + cursorDraft.getString(cursorDraft.getColumnIndex(DB_schema.DraftTable.Cols.PHONE)));
            JSArray.put(ob);
            cursorDraft.moveToNext();
        }
        cursorDraft.close();
        return JSArray;
    }

    /**
     * выбрать из БД для сервера список всех ошибок
     *
     * @param db - база данных
     * @return
     * @throws android.database.CursorIndexOutOfBoundsException
     * @throws JSONException
     */
    public static JSONArray getAllErrors(
            SQLiteDatabase db
    ) throws android.database.CursorIndexOutOfBoundsException, JSONException {

        Cursor cursor = db.query(Errors.ERRORS, null, null, null, null, null, null);
        cursor.moveToFirst();
        JSONArray JSArray = new JSONArray();
        while (!cursor.isAfterLast()) {
            JSONObject ob = new JSONObject();
            ob.put("id", "" + cursor.getString(cursor.getColumnIndex("_id")));
            ob.put("date", "" + cursor.getString(cursor.getColumnIndex(Errors.Cols.DATE)));
            ob.put("module", "" + cursor.getString(cursor.getColumnIndex(Errors.Cols.MODULE)));
            ob.put("text", "" + cursor.getString(cursor.getColumnIndex(Errors.Cols.TEXT)));
            JSArray.put(ob);
            cursor.moveToNext();
        }
        cursor.close();
        return JSArray;
    }


    /**
     * Выбрать для сервера списк всех параметров телефона
     *
     * @param db
     * @return
     * @throws android.database.CursorIndexOutOfBoundsException
     * @throws JSONException
     */
    public static JSONArray getAllParams(
            SQLiteDatabase db,
            int type
    ) throws android.database.CursorIndexOutOfBoundsException, JSONException {

        Cursor cursor = db.query(Params.PARAMS, null, Params.Cols.TYPE + " = " + type, null, null, null, null);
        cursor.moveToFirst();
        JSONArray JSArray = new JSONArray();
        while (!cursor.isAfterLast()) {
            JSONObject ob = new JSONObject();
            ob.put("id", "" + cursor.getString(cursor.getColumnIndex("_id")));
            ob.put("name", "" + cursor.getString(cursor.getColumnIndex(Params.Cols.NAME)));
            ob.put("value", "" + cursor.getString(cursor.getColumnIndex(Params.Cols.VALUE)));
            JSArray.put(ob);
            cursor.moveToNext();
        }
        cursor.close();
        return JSArray;
    }

    /**
     * получить JSON объект записи телефона для сервера
     *
     * @param cursor
     * @return
     * @throws JSONException
     */
    public static JSONObject getJSONObject4Phone(Cursor cursor) throws JSONException {
        JSONObject ob = new JSONObject();
        ob.put("id", "" + cursor.getString(cursor.getColumnIndex("_id")));
        ob.put("status", "" + cursor.getString(cursor.getColumnIndex(PhonesTable.Cols.STATUS)));
        ob.put("phone", "" + cursor.getString(cursor.getColumnIndex(PhonesTable.Cols.PHONE)));
        ob.put("created", "" + cursor.getString(cursor.getColumnIndex(PhonesTable.Cols.CREATED)));
        ob.put("duration", "" + cursor.getString(cursor.getColumnIndex(PhonesTable.Cols.DURATION)));
        return ob;
    }

    public static void setParams(
            SQLiteDatabase db,
            JSONArray ids
    ) throws JSONException, SQLException {
        if (ids.length() == 0) {
            return;
        }

        for (int i = 0; i < ids.length(); i++) {
            JSONObject ob = ids.getJSONObject(i);
            String name = ob.getString("name");
            String value = ob.getString("value");
            int type = ob.getInt("type");
            if (type == Params.Types.DELETE) {
                db.execSQL("DELETE FROM " + Params.PARAMS +
                        " WHERE " + Params.Cols.NAME + " = '" + name + "'");
                continue;
            }

            if (Helpers.exists(db, Params.PARAMS,
                    "" + Params.Cols.NAME + " = '" + name + "' " +
                            " AND " +
                            Params.Cols.VALUE + " = '" + value + "' " +
                            " AND " +
                            Params.Cols.TYPE + " = " + type
            )) {
                continue;
            }

            if (Helpers.exists(db, Params.PARAMS,
                    "" + Params.Cols.NAME + " = '" + name + "' " +
                            " AND " +
                            Params.Cols.TYPE + " = " + type
            )) {
                db.execSQL("UPDATE " + Params.PARAMS +
                        " SET " + Params.Cols.VALUE + " = '" + value + "' " +
                        " WHERE " + Params.Cols.NAME + " = '" + name + "'" +
                        " AND " +
                        Params.Cols.TYPE + " = " + type);
                continue;
            }

            ContentValues values = getContentValuesParam(name, value, type);
            db.insert(Params.PARAMS, null, values);

        }

    }

}

