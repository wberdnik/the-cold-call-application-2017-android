package com.leadinka.android.coldcall;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by VLF on 18.04.2017.
 */
public class ColdBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 15;
    private static final String DATABASE_NAME = "ColdCallBase.db";

    public ColdBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DB_schema.PhonesTable.PHONE + "(" +
                " _id integer primary key autoincrement, " +
                DB_schema.PhonesTable.Cols.PHONE + ", " +
                DB_schema.PhonesTable.Cols.STATUS + ", " +
                DB_schema.PhonesTable.Cols.DURATION + ", " +
                DB_schema.PhonesTable.Cols.TXT + ", " +
                DB_schema.PhonesTable.Cols.CREATED + ")");

        db.execSQL("create table " + DB_schema.PinTable.PIN + "(" +
                " _id integer primary key autoincrement, " +
                DB_schema.PinTable.Cols.ICC + ", " +
                DB_schema.PinTable.Cols.IMEI + ", " +
                DB_schema.PinTable.Cols.TOKEN + ", " +
                DB_schema.PinTable.Cols.PIN + ")");

        db.execSQL("create table " + DB_schema.DraftTable.DRAFT + "(" +
                " _id integer primary key autoincrement, " +
                DB_schema.DraftTable.Cols.CALL_AT + ", " +
                DB_schema.DraftTable.Cols.PIN + ", " +
                DB_schema.DraftTable.Cols.PHONE + ", " +
                DB_schema.DraftTable.Cols.CREATED + ", " +
                DB_schema.DraftTable.Cols.FIO + ", " +
                DB_schema.DraftTable.Cols.CITY + ", " +
                DB_schema.DraftTable.Cols.COMMENT + ")");

        db.execSQL("create table " + DB_schema.LastPhoneTable.LASTPHONE + "(" +
                " _id integer primary key autoincrement, " +
                DB_schema.LastPhoneTable.Cols.CREATED + ", " +
                DB_schema.LastPhoneTable.Cols.PHONE + ", " +
                DB_schema.LastPhoneTable.Cols.FINISHED + ", " +
                DB_schema.LastPhoneTable.Cols.IDNEWPHONE + ", " +
                DB_schema.LastPhoneTable.Cols.TYPE + ")");

        db.execSQL("create table " + DB_schema.Report.REPORT + "(" +
                " _id integer primary key autoincrement, " +
                DB_schema.Report.Cols.COMMENT + ", " +
                DB_schema.Report.Cols.DATE + ", " +
                DB_schema.Report.Cols.FABULE + ", " +
                DB_schema.Report.Cols.SUMMA + ", " +
                DB_schema.Report.Cols.NAME + ")");

        db.execSQL("create table " + DB_schema.Params.PARAMS + "(" +
                " _id integer primary key autoincrement, " +
                DB_schema.Params.Cols.VALUE + ", " +
                DB_schema.Params.Cols.TYPE + ", " +
                DB_schema.Params.Cols.NAME + ")");

        db.execSQL("create table " + DB_schema.Errors.ERRORS + "(" +
                " _id integer primary key autoincrement, " +
                DB_schema.Errors.Cols.DATE + ", " +
                DB_schema.Errors.Cols.TEXT + ", " +
                DB_schema.Errors.Cols.MODULE + ")");

        db.execSQL("create table " + DB_schema.Sms.SMS + "(" +
                " _id integer primary key autoincrement, " +
                DB_schema.Sms.Cols.DATE + ", " +
                DB_schema.Sms.Cols.TEXT + ", " +
                DB_schema.Sms.Cols.PHONE + ")");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(oldVersion == 1 ) {
           db.execSQL("create table IF NOT EXISTS " + DB_schema.DraftTable.DRAFT + "(" +
                    " _id integer primary key autoincrement, " +
                    DB_schema.DraftTable.Cols.CALL_AT + ", " +
                    DB_schema.DraftTable.Cols.PIN + ", " +
                    DB_schema.DraftTable.Cols.PHONE + ", " +
                    DB_schema.DraftTable.Cols.CREATED + ", " +
                    DB_schema.DraftTable.Cols.FIO + ", " +
                    DB_schema.DraftTable.Cols.CITY + ", " +
                    DB_schema.DraftTable.Cols.COMMENT + ")");

            db.execSQL("create table IF NOT EXISTS " + DB_schema.LastPhoneTable.LASTPHONE + "(" +
                    " _id integer primary key autoincrement, " +
                    DB_schema.LastPhoneTable.Cols.CREATED + ", " +
                    DB_schema.LastPhoneTable.Cols.PHONE + ", " +
                    DB_schema.LastPhoneTable.Cols.FINISHED + ", " +
                    DB_schema.LastPhoneTable.Cols.IDNEWPHONE + ", " +
                    DB_schema.LastPhoneTable.Cols.TYPE + ")");
        }
        if(oldVersion == 2) {
            db.execSQL("alter table " + DB_schema.DraftTable.DRAFT + " ADD COLUMN " + DB_schema.DraftTable.Cols.CITY);
        }

        if(oldVersion <= 6) {
            db.execSQL("alter table " + DB_schema.LastPhoneTable.LASTPHONE +
                    " ADD COLUMN " + DB_schema.LastPhoneTable.Cols.IDNEWPHONE);


            db.execSQL("create table IF NOT EXISTS " + DB_schema.Report.REPORT + "(" +
                    " _id integer primary key autoincrement, " +
                    DB_schema.Report.Cols.COMMENT + ", " +
                    DB_schema.Report.Cols.DATE + ", " +
                    DB_schema.Report.Cols.FABULE + ", " +
                    DB_schema.Report.Cols.SUMMA + ", " +
                    DB_schema.Report.Cols.NAME + ")");

            db.execSQL("create table IF NOT EXISTS " + DB_schema.Params.PARAMS + "(" +
                    " _id integer primary key autoincrement, " +
                    DB_schema.Params.Cols.VALUE + ", " +
               //     DB_schema.Params.Cols.TYPE + ", " +
                    DB_schema.Params.Cols.NAME + ")");

            db.execSQL("create table IF NOT EXISTS " + DB_schema.Errors.ERRORS + "(" +
                    " _id integer primary key autoincrement, " +
                    DB_schema.Errors.Cols.DATE + ", " +
                    DB_schema.Errors.Cols.TEXT + ", " +
                    DB_schema.Errors.Cols.MODULE + ")");
        }
        if(oldVersion <=14){
            db.execSQL("create table IF NOT EXISTS " + DB_schema.Sms.SMS + "(" +
                    " _id integer primary key autoincrement, " +
                    DB_schema.Sms.Cols.DATE + ", " +
                    DB_schema.Sms.Cols.TEXT + ", " +
                    DB_schema.Sms.Cols.PHONE + ")");
            db.execSQL("alter table " + DB_schema.Params.PARAMS +
                    " ADD COLUMN " + DB_schema.Params.Cols.TYPE);
        }
        if(oldVersion <=15){
           db.execSQL("alter table " + DB_schema.PhonesTable.PHONE +
                    " ADD COLUMN " + DB_schema.PhonesTable.Cols.TXT);
        }

    }

}
