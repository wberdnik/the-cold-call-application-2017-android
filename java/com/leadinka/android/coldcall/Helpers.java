package com.leadinka.android.coldcall;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by VLF on 22.05.2017.
 */

public class Helpers {
    /**
     *
     * @param db - база данных
     * @param table - имя таблицы
     * @param where - условие Where
     * @return - Есть ли записи
     */
    public static Boolean exists(SQLiteDatabase db, String table, String where){
        try {
            Cursor cursor = db.query(
                    table,
                    null,
                    where,
                    null,
                    null,
                    null,
                    null);

            if (cursor.getCount() > 0) {
                cursor.close();
                return Boolean.TRUE;
            }

            cursor.close();
        } catch (android.database.CursorIndexOutOfBoundsException mm) {
            CC_controller.SYS_LogError("HELPER", "Исключение в функции exists");
        }
        return Boolean.FALSE;
    }

    /**
     *
     * @param sec - задержка в секундах
     */
    public static void sleep(int sec){
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException ioe) {

        }
    }

    public static void DeleteAll(
            SQLiteDatabase db,
            String table,
            JSONArray ids
    )throws JSONException, SQLException {

        if (ids.length() > 0) {
            String myin = "";
            for (int i = 0; i < ids.length(); i++) {
                int code = ids.getInt(i);
                if (i == 0) {
                    myin = "" + code;
                } else {
                    myin = myin + ", " + code;
                }
            }
            db.execSQL("DELETE FROM " + table + " WHERE _id IN (" + myin + ")");
        }
    }
    public static void LoadReport(
            SQLiteDatabase db,
            String table,
            JSONArray ids
    )throws JSONException, SQLException {

        if (ids.length() > 0) {
            db.execSQL("DELETE FROM " + table);
            for (int i = 0; i < ids.length(); i++) {
                JSONObject ob = ids.getJSONObject(i);
                DB_schema.addReport(
                        db,
                        ob.getString("date"),
                        ob.getString("name"),
                        ob.getString("fabule"),
                        ob.getString("comment"),
                        ob.getInt("summa")
                );
            }

        }
    }
}
