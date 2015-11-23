package net.aquadc.miha.octabraintest;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import net.aquadc.utils.Http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miha on 21.11.15.
 * downloads search results
 */
public class DownloaderService extends IntentService {

    public static final String
            EXTRA_QUERY         = "query",

            EXTRA_STATUS        = "status",
            STATUS_OK           = "ok",
            STATUS_FAIL         = "fail";

    private static final String
            SEARCH_URL          = "https://yandex.ru/images/search",
            SEARCH_QUERY_KEY    = "text",

            TAG = "DownloaderService";

    private static final Pattern LINK_PATTERN = Pattern.compile(
            "<div[^>]+class=\"serp-item serp-item_type_search[^\"]+\"[^>]+><div[^>]+>" +
                    "<a class=\"serp-item__link\" href=\"[^\"]+\"[^>]+>" +
                    "<img[^>]+class=\"serp-item__thumb\"[^>]+src=\"([^\"]+)\"[^>]*>" +
                    "<div class=\"serp-item__plates\"><div class=\"serp-item__meta\">[^<]+" +
                    "</div></div></a></div><div[^>]+class=\"serp-item__snippet\">" +
                    "<div class=\"serp-item__title\">(.+?)</div>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public DownloaderService() {
        super("OctabrainTestApp DownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String query = intent.getStringExtra(EXTRA_QUERY);
        if (query == null || query.isEmpty()) {
            return;
        }

        Map<String, String> params = new HashMap<>(1);
        params.put(SEARCH_QUERY_KEY, query);
        try {
            String response = Http.get(SEARCH_URL, params);

            Matcher linksMatcher = LINK_PATTERN.matcher(response);

            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(DBHelper.Tables.Queries.QUERY, query);
            long queryId = db.insert(DBHelper.Tables.QUERIES, null, cv);

            while (linksMatcher.find()) {
                cv = new ContentValues();
                cv.put(DBHelper.Tables.Results.QUERY_ID, queryId);
                Spanned thumbUrl = Html.fromHtml(linksMatcher.group(1));    //unescape
                cv.put(DBHelper.Tables.Results.THUMB_URL, thumbUrl.toString());
                cv.put(DBHelper.Tables.Results.DESCRIPTION, linksMatcher.group(2));
                db.insert(DBHelper.Tables.RESULTS, null, cv);
            }
            db.close();
            dbHelper.close();
            sendResult(STATUS_OK);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e.getCause());
            sendResult(STATUS_FAIL);
        }
    }

    private void sendResult(String status) {
        Intent intent = new Intent(SearchActivity.ACTION_RESULT);
        intent.putExtra(EXTRA_STATUS, status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
