package net.aquadc.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import net.aquadc.miha.octabraintest.R;
import net.aquadc.utils.AquaApplication;
import net.aquadc.utils.ImageCacher;

public class WebImageView extends ImageView {

    private final Context mContext;

    // rounded
    private float mCornerRadius;

    // frame
    private Paint mFramePaint;
    private float mFrameSize;

    /** Construct */
    public WebImageView(Context context) {
        super(context);
        mContext = context;
    }

    public WebImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setup(attrs, 0, 0);
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setup(attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        setup(attrs, defStyleAttr, defStyleRes);
    }

    private void setup(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = mContext.getTheme().obtainStyledAttributes(
                attrs, R.styleable.WebImageView, defStyleAttr, defStyleRes);

        try {   //why try?
            mCornerRadius = a.getFloat(R.styleable.WebImageView_cornerRadius, 0);

            mFrameSize = a.getFloat(R.styleable.WebImageView_frameSize, 0);
            int col = a.getColor(R.styleable.WebImageView_frameColor, 0);
            if (mFrameSize > 0) {
                mFramePaint = new Paint();
                mFramePaint.setColor(col);
            }
        } finally {
            a.recycle();
        }

    }

    /** Set */
    public void setImageURI(String uri) {
        if (uri.startsWith("//"))
            uri = "https:" + uri;
        setImageURI(Uri.parse(uri));
    }

    public void setImageURI(Uri uri) {
        if (AquaApplication.cacher.inCache(uri)) {
            setImageBitmap(
                    BitmapFactory.decodeFile(AquaApplication.cacher.getPath(uri)));
            invalidate();
        } else {
            AquaApplication.cacher.download(uri, new ImageCacher.OnDownloadListener() {
                @Override
                public void onDownload(Bitmap bitmap) {
                    setImageBitmap(bitmap);
                }
            });
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (mFrameSize > 0)
            bm = drawTransparentFrame(bm);

        if (mCornerRadius > 0)
            bm = roundCorners(bm);

        super.setImageBitmap(bm);
    }

    private Bitmap roundCorners(Bitmap bm) {
        BitmapShader shader = new BitmapShader(bm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        RectF rect = new RectF(0.0f, 0.0f, bm.getWidth(), bm.getHeight());

        // rect contains the bounds of the shape
        // radius is the radius in pixels of the rounded corners
        // paint contains the shader that will texture the shape
        Bitmap bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRoundRect(rect, mCornerRadius, mCornerRadius, paint);
        return bitmap;
    }

    private Bitmap drawTransparentFrame(Bitmap bm) {
        Bitmap bitmap = bm.copy(bm.getConfig(), true);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, bitmap.getWidth(), mFrameSize, mFramePaint);
        canvas.drawRect(bitmap.getWidth()-mFrameSize, mFrameSize, bitmap.getWidth(), bitmap.getHeight(), mFramePaint);
        canvas.drawRect(0, bitmap.getHeight()-mFrameSize, bitmap.getWidth()-mFrameSize, bitmap.getHeight(), mFramePaint);
        canvas.drawRect(0, mFrameSize, mFrameSize, bitmap.getHeight()-mFrameSize, mFramePaint);
        return bitmap;
    }
}
