package com.flashlightpowered;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import kankan.wheel.widget.adapters.AbstractWheelAdapter;

/**
 * File:         SlotMachineAdapter.java
 * Created:      6/21/2018
 * Last Changed: 6/21/2018 2:37 PM
 * Author:       <A HREF="mailto:rakesh@plowns.com">Rakesh Kumar</A>
 * <p>
 * This code is copyright © 2016 Plowns
 **/
public class SlotMachineAdapter extends AbstractWheelAdapter {
    // Image size
    final int IMAGE_WIDTH = 69;
    final int IMAGE_HEIGHT = 170;
    // Layout params for image view
    final ViewGroup.LayoutParams params;
    // Slot machine symbols
    private final int[] items = new int[]{
            R.drawable.original_14,
            R.drawable.original_15,
            R.drawable.original_16,
            R.drawable.original_26,
            R.drawable.original_27,
            R.drawable.original_28,
            R.drawable.original_29,
            R.drawable.original_30,
            R.drawable.original_31,
            R.drawable.original_32,
            R.drawable.original_33,
            R.drawable.original_34,
            R.drawable.original_35,
            R.drawable.original_36,
            R.drawable.original_37,
            R.drawable.original_38,
            R.drawable.original_39,
            R.drawable.original_40,
            R.drawable.original_41,
            R.drawable.original_42,
            R.drawable.original_12,
            R.drawable.original_13
//            R.drawable.original_10,
//            R.drawable.original_11,
//            R.drawable.original_12,
//            R.drawable.original_13,
//            R.drawable.original_14,
//            R.drawable.original_15,
//            R.drawable.original_16,
//            R.drawable.original_17,
//            R.drawable.original_18,
//            R.drawable.original_19,
//            R.drawable.original_20,

    };
    int IMAGE_WIDTH_CAL = 69;
    int IMAGE_HEIGHT_CAL = 170;
    // Cached images
    private final List<SoftReference<Bitmap>> images;
    // Layout inflater
    private final Context context;

    /**
     * Constructor
     */
    public SlotMachineAdapter(Context context) {
        this.context = context;
        setUpHeightWidth();
        images = new ArrayList<SoftReference<Bitmap>>(items.length);
        for (int id : items) {
            images.add(new SoftReference<Bitmap>(loadImage(id)));
        }
        params = new ViewGroup.LayoutParams(IMAGE_WIDTH_CAL, IMAGE_HEIGHT_CAL);
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /**
     * Loads image from resources
     */
    private Bitmap loadImage(int id) {
        Log.d("Width", "" + IMAGE_WIDTH_CAL);
        Log.d("Height", "" + IMAGE_HEIGHT_CAL);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH_CAL, IMAGE_HEIGHT_CAL, true);
        bitmap.recycle();
        return scaled;
    }

    public int getItemsCount() {
        return items.length;
    }

    public View getItem(int index, View cachedView, ViewGroup parent) {
        ImageView img;
        if (cachedView != null) {
            img = (ImageView) cachedView;
        } else {
            img = new ImageView(context);
        }
        img.setLayoutParams(params);
        SoftReference<Bitmap> bitmapRef = images.get(index);
        Bitmap bitmap = bitmapRef.get();
        if (bitmap == null) {
            bitmap = loadImage(items[index]);
            images.set(index, new SoftReference<Bitmap>(bitmap));
        }
//            img.setBackgroundResource(R.drawable.background_up);
        img.setImageBitmap(bitmap);

        return img;
    }

    private void setUpHeightWidth() {
        int heightPx = getScreenHeight(context);
        IMAGE_HEIGHT_CAL = heightPx / 5;
        IMAGE_WIDTH_CAL = IMAGE_HEIGHT_CAL * IMAGE_WIDTH / IMAGE_HEIGHT;
    }
}