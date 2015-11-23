package net.aquadc.miha.octabraintest;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    static final String ACTION_RESULT = "net.aquadc.miha.octabraintest.RESULT";

    // Data
    private DBHelper dbHelp;
    private SQLiteDatabase db;
    private Cursor searchResultsCur;
    private String lastQuery;

    // UI
    private ListView searchResults;
    private ProgressBar progressBar;
    private ActionMode actionMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchResults = (ListView) findViewById(R.id.searchResults);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        dbHelp = new DBHelper(this);
        db = dbHelp.getReadableDatabase();

        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode == null && parent.getCount() > 0) {
                    actionMode = startSupportActionMode(actionModeCallback);
                }
            }
        });
        fab.setOnClickListener(this);

        // Connection with service
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RESULT);
        bManager.registerReceiver(bReceiver, intentFilter);

        if (savedInstanceState != null) {
            lastQuery = savedInstanceState.getString("query");
            if (lastQuery != null) {
                search();
                searchResults.requestLayout();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchResults.getCheckedItemCount() > 0)
            actionMode = startSupportActionMode(actionModeCallback);
    }

    @Override
    protected void onDestroy() {
        if (searchResultsCur != null)
            searchResultsCur.close();
        db.close();
        dbHelp.close();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (lastQuery != null)
            outState.putString("query", lastQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        // autocomplete
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Cursor cursor = db.query(DBHelper.Tables.QUERIES, null,
                        DBHelper.Tables.Queries.QUERY_STARTS, new String[]{newText + "%"},
                        null, null, null, "10");
                CursorAdapter adapter = searchView.getSuggestionsAdapter();
                if (adapter == null) {
                    adapter = new android.support.v4.widget.SimpleCursorAdapter(
                            SearchActivity.this, android.R.layout.simple_list_item_1, cursor,
                            new String[]{DBHelper.Tables.Queries.QUERY},
                            new int[]{android.R.id.text1}, 0);
                    searchView.setSuggestionsAdapter(adapter);
                } else {
                    adapter.changeCursor(cursor);
                }
                return true;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override public boolean onSuggestionSelect(int position) { return false; }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cur = searchView.getSuggestionsAdapter().getCursor();
                cur.moveToPosition(position);
                searchView.setQuery(
                        cur.getString(cur.getColumnIndex(DBHelper.Tables.Queries.QUERY)), true);
                return true;
            }
        });

        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //int id = item.getItemId();

        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            lastQuery = intent.getStringExtra(SearchManager.QUERY);
            if (lastQuery == null) {
                lastQuery = intent.getStringExtra(SearchManager.USER_QUERY);    // from autocomplete
            }

            search();
        }
    }

    private void search() {
        Cursor queries = db.query(
                DBHelper.Tables.QUERIES, null,
                DBHelper.Tables.Queries.QUERY_EQ, new String[]{lastQuery}, null, null, null, "1");
        if (queries.getCount() == 1) {
            // in cache
            queries.moveToFirst();
            String id = queries.getString(queries.getColumnIndex(DBHelper.Tables.ID));
            searchResultsCur = db.query(
                    DBHelper.Tables.RESULTS, null,
                    DBHelper.Tables.Results.QUERY_ID_EQ, new String[]{id},
                    null, null, null);

            SimpleCursorAdapter adapter = (SimpleCursorAdapter) searchResults.getAdapter();
            if (adapter == null) {
                adapter = new OctabrainTestAdapter(this,
                        R.layout.item_image_even, R.layout.item_image_odd,
                        searchResultsCur, DBHelper.Tables.Results.ADAPTER_FROM,
                        new int[]{android.R.id.text1, android.R.id.icon});
                searchResults.setAdapter(adapter);
                searchResults.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            } else {
                if (actionMode != null)
                    actionMode.finish();
                adapter.changeCursor(searchResultsCur);
            }
        } else {
            // needs to be downloaded
            progressBar.setVisibility(View.VISIBLE);
            Intent intent = new Intent(this, DownloaderService.class);
            intent.putExtra(DownloaderService.EXTRA_QUERY, lastQuery);
            startService(intent);
        }
        queries.close();
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_RESULT)) {
                String status = intent.getStringExtra(DownloaderService.EXTRA_STATUS);
                if (DownloaderService.STATUS_OK.equals(status)) {
                    search();
                } else if (DownloaderService.STATUS_FAIL.equals(status)) {
                    Snackbar.make(searchResults,
                            R.string.err_download_failed, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(searchResults,
                            R.string.err_internal, Snackbar.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }
    };

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_search_actionmode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override @SuppressWarnings("deprecation")
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                SparseBooleanArray victims = searchResults.getCheckedItemPositions();
                int idIndex = -1;
                for (int i = 0, cnt = victims.size(); i < cnt; i++) {
                    if (victims.get(victims.keyAt(i))) {
                        Cursor victim = (Cursor) searchResults.getItemAtPosition(victims.keyAt(i));
                        if (idIndex == -1) {
                            idIndex = victim.getColumnIndex(DBHelper.Tables.ID);
                        }
                        if (!deleteSearchResult(victim.getInt(idIndex))) {
                            return false;
                        }
                    }
                }
                actionMode.finish();
                ((SimpleCursorAdapter) searchResults.getAdapter()).getCursor().requery();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            searchResults.clearChoices();
            actionMode = null;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean deleteSearchResult(int id) {
            try {
                SQLiteDatabase writable = dbHelp.getWritableDatabase();
                writable.delete(DBHelper.Tables.RESULTS,
                        DBHelper.Tables.ID_EQ, new String[]{String.valueOf(id)});
//                writable.close();
                // if close, main, readable database becomes closed!
                return true;
            } catch (SQLiteCantOpenDatabaseException e) {
                Snackbar.make(searchResults,
                        R.string.err_delete_failed, Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:

                Snackbar snack = Snackbar.make(v, R.string.info, Snackbar.LENGTH_LONG)
                        .setAction(R.string.sources, this);
                TextView tv = (TextView) snack.getView()
                        .findViewById(android.support.design.R.id.snackbar_text);
                tv.setMaxLines(100500); //Don't truncate my text!
                Log.d("lol", tv.getText().toString());
                snack.show();

                break;

            case R.id.snackbar_action:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("https://github.com/Miha-x64/Octabrain-Test"), "text/html");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
        }
    }

    /*private static String stringify(SparseBooleanArray a) {
        if (a == null)
            return "null";
        StringBuilder sb = new StringBuilder(3*a.size());
        sb.append('[');
        for (int i = 0, last = a.size()-1; i <= last; i++) {
            sb.append(a.keyAt(i)).append(':').append(a.get(a.keyAt(i)) ? '1' : '0');
            if (i != last)
                sb.append(", ");
        }
        sb.append(']');
        return sb.toString();
    }*/

    /*private static String stringify(Object o) {
        if (o instanceof int[]) {
            return Arrays.toString((int[]) o);
        }
        return o.toString().replace('\n', '/');
    }

    private static void listBundle(Context context, Bundle bundle, String comment) {
        String tag = context.getClass().getSimpleName();

        Log.d(tag, " === " + comment + " bundle start ===");
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            String text = key + ":\t";
            text += value == null ? "null" :
                    "(" + value.getClass().getSimpleName() + ")\t" + stringify(value);
            Log.d(tag, text);
        }
        Log.d(tag, " === " + comment + " bundle end ===");
    }*/
}
