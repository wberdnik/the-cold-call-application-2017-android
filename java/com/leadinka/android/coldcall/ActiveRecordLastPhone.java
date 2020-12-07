package com.leadinka.android.coldcall;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by VLF on 11.05.2017.
 */

public class ActiveRecordLastPhone {
    private static final String TAG = "ActiveRecordLastPhone";

    private int mId;
    private String mPhone;
    private long mStarted;
    private long mDuration;
    private long mType;
    private int idNewPhone;
    private int CountPhones;
    private SQLiteDatabase mDB;

    public ActiveRecordLastPhone(SQLiteDatabase pDatabase) {

        mDB = pDatabase;
        mId = 0;
        idNewPhone = 0;

        try {
            Cursor cursor = mDB.query(DB_schema.LastPhoneTable.LASTPHONE, null,
                    "" + DB_schema.LastPhoneTable.Cols.FINISHED + " > 0 ",
                    null, null, null, DB_schema.LastPhoneTable.Cols.FINISHED + " DESC");
            CountPhones = cursor.getCount();
            if (CountPhones > 0) {
                cursor.moveToFirst();

                if (!cursor.isAfterLast()) {
                    mPhone = cursor.getString(cursor.getColumnIndex(DB_schema.LastPhoneTable.Cols.PHONE));
                    mId = cursor.getInt(cursor.getColumnIndex("_id"));
                    mStarted = cursor.getInt(cursor.getColumnIndex(DB_schema.LastPhoneTable.Cols.CREATED));
                    mDuration = cursor.getInt(cursor.getColumnIndex(DB_schema.LastPhoneTable.Cols.FINISHED)) - mStarted;
                    mType = cursor.getInt(cursor.getColumnIndex(DB_schema.LastPhoneTable.Cols.TYPE));
                    idNewPhone = cursor.getInt(cursor.getColumnIndex(DB_schema.LastPhoneTable.Cols.IDNEWPHONE));
                }
            }
            cursor.close();
        } catch (android.database.CursorIndexOutOfBoundsException mm) {
            CC_controller.SYS_LogError(TAG, "ошибка чтения БД " + mm);
        }
    }

    public int getId() {
        return mId;
    }

    public String getPhone() {
        return mPhone;
    }

    public int getCountPhones() {
        return CountPhones;
    }

    public String getSMS() {
        if (mType == DB_schema.LastPhoneTable.Types.SMS) {
            String answer = "";
            try {
                Cursor cursor = mDB.query(DB_schema.Sms.SMS, null,
                        " _id = " + idNewPhone,
                        null, null, null, null);
                CountPhones = cursor.getCount();
                if (CountPhones > 0) {
                    cursor.moveToFirst();
                    if (!cursor.isAfterLast()) {
                        answer = cursor.getString(cursor.getColumnIndex(DB_schema.Sms.Cols.TEXT));
                    }
                }
                cursor.close();
            } catch (android.database.CursorIndexOutOfBoundsException mm) {
                CC_controller.SYS_LogError(TAG, "ошибка чтения БД SMS " + mm);
            }
            return answer;
        }
        CC_controller.SYS_LogError(TAG, "Попытка получения SMS вне статуса");
        return "";

    }

    public int getType() {
        return (int) mType;
    }

    public String getType_() {
        if (mType == DB_schema.LastPhoneTable.Types.OUTBOUND) {
            return "ИСХОДЯЩИЙ";
        }
        if (mType == DB_schema.LastPhoneTable.Types.SMS) {
            return "НОВАЯ SMS";
        }
        return "ВХОДЯЩИЙ";
    }

    public long getDuration() {
        return mDuration;
    }

    public String getStarted_() {
        Date df = new java.util.Date(mStarted * 1000);
        String vv = new SimpleDateFormat("hh:mma dd-MM-yyyy ").format(df);
        return vv;
    }

    //============================================================================

    public void Decide(Boolean success) {
        //TODO для статуса SMS

        if (mId <= 0) {
            CC_controller.SYS_LogError(TAG, "Попытка фиксации Decide без mID");
            return;
        }

        try {

            mDB.execSQL("DELETE FROM " + DB_schema.LastPhoneTable.LASTPHONE +
                    " WHERE _id = " + mId);

            if (mType == DB_schema.LastPhoneTable.Types.SMS) {
                mDB.execSQL("DELETE FROM " + DB_schema.Sms.SMS +
                        " WHERE _id = " + idNewPhone);
                if(success){
                    DB_schema.addPhone(mDB,mPhone,"from sms",DB_schema.PhonesTable.Status.FROMSMS);
                    // Запись для статистики, номер будет болтаться как чужой
                  //  CC_controller.SYS_LogError(TAG, "Фиксация номера из SMS - удалять на сервере!!!");
                }
                return;
            }

            int newstatus = 0;


            if (mType == DB_schema.LastPhoneTable.Types.INBOUND) {
                newstatus = DB_schema.PhonesTable.Status.MISSED;
                if (mDuration >= 3 && success) {
                    newstatus = DB_schema.PhonesTable.Status.INBOUND;
                }

                // фиксируем INCOMING звонки

                ContentValues values = new ContentValues();
                values.put(DB_schema.PhonesTable.Cols.PHONE, mPhone);
                values.put(DB_schema.PhonesTable.Cols.STATUS, newstatus);
                values.put(DB_schema.PhonesTable.Cols.CREATED, mStarted);
                values.put(DB_schema.PhonesTable.Cols.DURATION, mDuration);
                mDB.insert(DB_schema.PhonesTable.PHONE, null, values);
                return;
            }


            if (mType == DB_schema.LastPhoneTable.Types.OUTBOUND) {

                newstatus = DB_schema.PhonesTable.Status.UNAVAILABLE;
                if (mDuration >= 3 && success) {
                    newstatus = DB_schema.PhonesTable.Status.CALLED;
                }

                if (idNewPhone <= 0) {// это свободный исходящий звонок
                    ContentValues values = new ContentValues();
                    values.put(DB_schema.PhonesTable.Cols.PHONE, mPhone);
                    values.put(DB_schema.PhonesTable.Cols.STATUS, newstatus);
                    values.put(DB_schema.PhonesTable.Cols.CREATED, mStarted);
                    values.put(DB_schema.PhonesTable.Cols.DURATION, mDuration);
                    mDB.insert(DB_schema.PhonesTable.PHONE, null, values);

                } else {
                    mDB.execSQL("UPDATE " + DB_schema.PhonesTable.PHONE +
                            " SET " + DB_schema.PhonesTable.Cols.STATUS + " = " + newstatus
                            + " WHERE _id = " + idNewPhone
                    );

                }

            }
        } catch (SQLException i) {
            CC_controller.SYS_LogError(TAG, "Общая SQL ошибка " + i);
        }


    }


}

