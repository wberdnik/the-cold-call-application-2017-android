package com.leadinka.android.coldcall;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.inputmethodservice.Keyboard;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;


// Утащил из http://developer.alexanderklimov.ru/android/views/gridview.php
public class ReportActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    private static final String TAG = "Report_activity";

    private TextView mSelectText;
    private DataAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mSelectText = (TextView) findViewById(R.id.info);

        final GridView g = (GridView) findViewById(R.id.ReportGridView);

        ArrayList<Rows> cacheDB = new ArrayList<Rows>();
        SQLiteDatabase mDB;
        try{
            mDB = CC_controller.getDB();
        } catch (java.lang.NullPointerException mm) {
            CC_controller.SYS_LogError(TAG, "ошибка получения дескриптора БД Report " + mm);
            throw new java.lang.NullPointerException();
        }

        try {
            Cursor cursor = mDB.query(DB_schema.Report.REPORT, null, null, null, null, null,
                    DB_schema.Report.Cols.DATE + " DESC");
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                cacheDB.add(new Rows(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        } catch (android.database.CursorIndexOutOfBoundsException mm) {
            CC_controller.SYS_LogError(TAG, "ошибка чтения БД Report " + mm);
        }

        mAdapter = new DataAdapter(getApplicationContext(), cacheDB, mDB);
        mSelectText.setText(DB_schema.getParamByName(mDB,"summa"));

        g.setAdapter(mAdapter);
       // g.setOnItemSelectedListener(this);
        g.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                mSelectText.setText(
                        ((Rows) mAdapter.getItem(position))
                                .getComment()
                );
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position,
                               long id) {
        mSelectText.setText(
                ((Rows) mAdapter.getItem(position))
                        .getSumma());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mSelectText.setText("---");
    }
}