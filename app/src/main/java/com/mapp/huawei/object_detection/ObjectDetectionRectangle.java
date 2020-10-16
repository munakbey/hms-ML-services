package com.mapp.huawei.object_detection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.huawei.hms.mlsdk.objects.MLObject;

public class ObjectDetectionRectangle  extends GraphicOverlay.Graphic  {

    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 5.0f;
    private final MLObject object;
    private final Paint boxPaint;
    private final Paint textPaint;

    public ObjectDetectionRectangle(GraphicOverlay overlay, MLObject object) {
        super(overlay);

        this.object = object;

        this.boxPaint = new Paint();
        this.boxPaint.setColor(Color.GREEN);
        this.boxPaint.setStyle(Paint.Style.STROKE);
        this.boxPaint.setStrokeWidth(ObjectDetectionRectangle.STROKE_WIDTH);

        this.textPaint = new Paint();
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setTextSize(ObjectDetectionRectangle.TEXT_SIZE);
    }

    @Override
    public void draw(Canvas canvas) {
        // draw the object border.
        RectF rect = new RectF(this.object.getBorder());
        rect.left = this.translateX(rect.left);
        rect.top = this.translateY(rect.top);
        rect.right = this.translateX(rect.right);
        rect.bottom = this.translateY(rect.bottom);
        canvas.drawRect(rect, this.boxPaint);

        // draw other object info.
        canvas.drawText(ObjectDetectionRectangle.getCategoryName(this.object.getTypeIdentity()), rect.left, rect.bottom, this.textPaint);
        canvas.drawText("trackingId: " + this.object.getTracingIdentity(), rect.left, rect.top, this.textPaint);
        if (this.object.getTypePossibility() != null) {
            canvas.drawText("confidence: " + this.object.getTypePossibility(), rect.right, rect.bottom, this.textPaint);
        }
    }

    private static String getCategoryName(int category) {
        switch (category) {
            case MLObject.TYPE_OTHER:
                return "Unknown";
            case MLObject.TYPE_FURNITURE:
                return "FURNITURE";
            case MLObject.TYPE_GOODS:
                return "FASHION GOOD";
            case MLObject.TYPE_PLACE:
                return "PLACE";
            case MLObject.TYPE_PLANT:
                return "PLANT";
            case MLObject.TYPE_FOOD:
                return "FOOD";
            case MLObject.TYPE_FACE:
                return "FACE";
            default:
        }
        return "";
    }
}