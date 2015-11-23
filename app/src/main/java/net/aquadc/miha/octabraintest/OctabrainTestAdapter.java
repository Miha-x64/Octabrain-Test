package net.aquadc.miha.octabraintest;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import net.aquadc.widgets.WebImageView;

/**
 * Created by miha on 21.11.15.
 * adapter ;)
 */
class OctabrainTestAdapter extends SimpleCursorAdapter {

    /** This field should be made private, so it is hidden from the SDK. WAT!?? */
    private Cursor mCursor;
    private final Context mContext;
    private final LayoutInflater mInflater;

    private final int mEvenLayout, mOddLayout;

    private static final String EVEN = "even", ODD = "odd";

    private static final ViewBinder viewBinder = new ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view instanceof TextView) {
                ((TextView) view).setText(Html.fromHtml(cursor.getString(columnIndex)));
                return true;
            } else if (view instanceof WebImageView) {
                ((WebImageView) view).setImageURI(cursor.getString(columnIndex));
                return true;
            }
            return false;
        }
    };

    OctabrainTestAdapter(Context context, @LayoutRes int evenLayout, @LayoutRes int oddLayout,
                         Cursor cursor, String[] from, @IdRes int[] to) {
        super(context, 0, cursor, from, to, 0);
        mCursor = cursor;
        mContext = context;
        mInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mEvenLayout = evenLayout;
        mOddLayout = oddLayout;
        setViewBinder(viewBinder);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // almost copy of CursorAdapter#getView
        if (mCursor.isClosed()) {
            throw new IllegalStateException("Cursor is closed.");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        boolean even = (position%2 == 1);
        String tag = even ? EVEN : ODD;
        if (convertView == null || !tag.equals(convertView.getTag())) {
            v = mInflater.inflate(even ? mEvenLayout : mOddLayout, parent, false);
        } else {
            v = convertView;
        }
        bindView(v, mContext, mCursor);
        return v;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        throw new IllegalStateException("This adapter inflates views by other way.");
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        mCursor = cursor;
        // ok, Google
    }
}
