package net.aquadc.miha.octabraintest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by miha on 21.11.15.
 *
 */
class DBHelper extends SQLiteOpenHelper {

    static class Tables {
        static final String
                QUERIES = "queries",
                RESULTS = "results",
                ID      = "_id",
                ID_EQ   = "_id = ?";
        static class Queries {
            static final String
                    QUERY           = "query",
                    QUERY_EQ        = "query = ?",
                    QUERY_STARTS    = "query LIKE ?";
        }
        static class Results {
            static final String
                    QUERY_ID    = "query_id",
                    DESCRIPTION = "description",
                    THUMB_URL   = "thumb_url",

                    QUERY_ID_EQ     = "query_id = ?",
                    ADAPTER_FROM[]  = {DESCRIPTION, THUMB_URL};
        }
    }

    public DBHelper(Context context) {
        super(context, "searchImages", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // создаем таблицу с полями
        db.execSQL("create table queries ("
                + "_id integer primary key autoincrement,"
                + "query text"
                + ");" );
        db.execSQL("create table results ("
                + "_id integer primary key autoincrement,"
                + "query_id integer,"
                + "description text,"
                + "thumb_url text"
                + ");" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}