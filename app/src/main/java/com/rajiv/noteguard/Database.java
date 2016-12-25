package com.rajiv.noteguard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rajiv on 24-12-2016.
 */

public class Database extends SQLiteOpenHelper {

    private final static String TABLE_POINTS = "POINTS";
    private final static String ID_COL = "ID";
    private final static String X_COL = "X";
    private final static String Y_COL = "Y";

    public Database(Context context) {
        super(context, "noteGuardDatabase.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY,%s INTEGER NOT NULL,%s INTEGER NOT NULL)",
                TABLE_POINTS, ID_COL, X_COL, Y_COL);

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void storePoints(List<Point> pointList) {

        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_POINTS, null, null);

        int i = 0;
        for (Point point : pointList) {
            ContentValues values = new ContentValues();
            values.put(ID_COL, i);
            values.put(X_COL, point.x);
            values.put(Y_COL, point.y);

            db.insert(TABLE_POINTS, null, values);
            i++;
        }
        db.close();
    }

    public List<Point> getPoint() {

        List<Point> pointList = new ArrayList<Point>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = String.format("SELECT %s,%s FROM %s ORDER BY %s", X_COL, Y_COL, TABLE_POINTS, ID_COL);

        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            int x = cursor.getInt(0);
            int y = cursor.getInt(1);

            pointList.add(new Point(x, y));
        }

        db.close();

        return pointList;

    }

}
