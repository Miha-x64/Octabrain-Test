package net.aquadc.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * Created by miha on 21.11.15.
 *
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private Checkable checkable;
    /**
     * :|
     */
    public CheckableRelativeLayout(Context context) {
        super(context);                                     }
    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);                              }
    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                }
    @TargetApi(21)
    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);   }

    @Override
    public boolean isChecked() {
        checkCheckable();
        return checkable.isChecked();
    }
    @Override
    public void toggle() {
        checkCheckable();
        checkable.toggle();
    }
    @Override
    public void setChecked(boolean checked) {
        checkCheckable();
        checkable.setChecked(checked);
    }

    private void checkCheckable() {
        if (checkable == null) {
            if (!descent(this)) {
                throw new IllegalStateException("Layout must have at least one Checkable!");
            }
        }
    }

    private boolean descent(ViewGroup v) {
        for (int i = 0, cnt = v.getChildCount(); i < cnt; i++) {
            View child = getChildAt(i);
            if (child instanceof Checkable) {
                checkable = (Checkable) child;
                return true;
            } else if (child instanceof ViewGroup) {
                return descent((ViewGroup) child);
            }
        }
        return false;
    }
}
