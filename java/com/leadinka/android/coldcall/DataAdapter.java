package com.leadinka.android.coldcall;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by VLF on 23.05.2017.
 */

public class DataAdapter extends BaseAdapter {
    private static final String TAG = "DataAdapter";
    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<Rows> objects;
    SQLiteDatabase mDB;

    public DataAdapter(Context c, ArrayList<Rows> records, SQLiteDatabase db) {
        mContext = c;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        objects = records;
        mDB = db;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View grid = convertView;

        if (convertView == null) {
            grid = inflater.inflate(R.layout.cellgrid, parent, false);
        }

        Rows row = (Rows) getItem(position);
        ((TextView) grid.findViewById(R.id.date)).setText(row.getDate());
        ((TextView) grid.findViewById(R.id.name)).setText(row.getName());
        ((TextView) grid.findViewById(R.id.fabule)).setText(row.getFabule());
        ((TextView) grid.findViewById(R.id.comment)).setText(row.getComment());
        ((TextView) grid.findViewById(R.id.summa)).setText(row.getSumma());

        return grid;
    }


}