package com.leadinka.android.coldcall;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * Created by VLF on 18.05.2017.
 */

public class ActiveRecordNextPhone {
    private static final String TAG = "ActiveRecordNextPhone";
    private int mId;
    private long start;
    private String mPhone;
    private String mText;
    private int mCountsOfNew;
    private SQLiteDatabase mDB;


    public ActiveRecordNextPhone(SQLiteDatabase Database) {
        mId = 0;
        mPhone = "";
        mText = "";
        mDB = Database;
        start = 0;
        mCountsOfNew = 0;
    }

    public String getText() {
        return mText;
    }

    public String getPhone() {
        if (mId == 0) {
            GetNextPhone();
        }
        return mPhone;
    }

    public int getCountsOfNew() {
        return mCountsOfNew;
    }

    public int getId() {
        return mId;
    }

    public void StartCallingTime(){
        start = System.currentTimeMillis() / 1000L;
    }

    public boolean isStarted(){
        if(start ==0){
            return false;
        }
        return true;
    }

    public void StopCallingAndQueue() {
        long duration = (System.currentTimeMillis() / 1000L) - start;

        if(start == 0 ){
            CC_controller.SYS_LogError(this.toString(),"Завершение неначатого звонка");
            duration = 777;
        }

        try {

            mDB.execSQL("UPDATE " + DB_schema.PhonesTable.PHONE +
                    " SET " + DB_schema.PhonesTable.Cols.DURATION + "= " + duration + "," +
                    DB_schema.PhonesTable.Cols.CREATED + " = '" +
                    (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(start*1000))+ "' " +
                     "  WHERE _id = " + mId);
        } catch (SQLException i) {
           CC_controller.SYS_LogError(TAG,"SQL ошибка " + i);
        }
        mId = 0;
        mPhone = "";
        start = 0;
    }



    //========================================================================

    private void GetNextPhone() {

        mId = 0;
        mPhone = "";
      //  start = 0;
        mCountsOfNew = 0;

        try {
            Cursor cursor = mDB.query(DB_schema.PhonesTable.PHONE, null,
                    "" + DB_schema.PhonesTable.Cols.STATUS + " = " + DB_schema.PhonesTable.Status.NEW,
                    null, null, null, null);
            mCountsOfNew = cursor.getCount();
            if (mCountsOfNew > 0) {
                cursor.moveToFirst();

                if (!cursor.isAfterLast()) {
                    mPhone = cursor.getString(cursor.getColumnIndex(DB_schema.PhonesTable.Cols.PHONE));
                    mText = cursor.getString(cursor.getColumnIndex(DB_schema.PhonesTable.Cols.TXT));
                    mId = cursor.getInt(cursor.getColumnIndex("_id"));
                }
            }
            cursor.close();
        } catch (android.database.CursorIndexOutOfBoundsException mm) {
            CC_controller.SYS_LogError(TAG,"Ошибка след номера " + mm);
        }

    }
    protected void finalize(){
        if(start !=0) {
            CC_controller.SYS_LogError(TAG, "Финализация начатого исходящего " + mPhone + " " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(start*1000)));
        }
    }
}
