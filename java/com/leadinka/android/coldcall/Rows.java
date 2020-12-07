package com.leadinka.android.coldcall;

import android.database.Cursor;

/**
 * Created by VLF on 23.05.2017.
 */

public class Rows {
     private String mDate;
    private String mName;
    private String mFabule;
    private String mComment;
    private String mSumma;

    public Rows(Cursor cursor){
        mDate = cursor.getString(cursor.getColumnIndex(DB_schema.Report.Cols.DATE));
        mName = cursor.getString(cursor.getColumnIndex(DB_schema.Report.Cols.NAME));
        mFabule = cursor.getString(cursor.getColumnIndex(DB_schema.Report.Cols.FABULE));
        mComment = cursor.getString(cursor.getColumnIndex(DB_schema.Report.Cols.COMMENT));
        mSumma = cursor.getString(cursor.getColumnIndex(DB_schema.Report.Cols.SUMMA));
    }

    public String getDate() {
        return mDate;
    }

    public String getName() {
        return mName;
    }

    public String getFabule() {
        return mFabule;
    }

    public String getComment() {
        return mComment;
    }

    public String getSumma() {
        return mSumma;
    }
}
