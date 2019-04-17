package uk.ac.bris.cs.spe.navigationaltool;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Drawable for single characters onto {@link android.support.design.widget.FloatingActionButton}
 * objects. This can in theory be used for other drawable usages however is optimised for placement
 * onto {@code SIZE_MINI} size FABs. Perhaps could be refactored as a child of a less specific
 * implementation.
 */
public class FabTextDrawable extends Drawable {
    private Paint p = new Paint();
    private String text;

    FabTextDrawable(String s, int textColour) {
        text = s;
        p.setColor(textColour);
        p.setAntiAlias(true);
        p.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        p.setTextSize(canvas.getClipBounds().height() / 2f);
        canvas.drawText(text, canvas.getClipBounds().centerX(), canvas.getClipBounds().bottom * 3/5f, p);
    }

    @Override
    public void setAlpha(int i) {
        p.setAlpha(i);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        p.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
