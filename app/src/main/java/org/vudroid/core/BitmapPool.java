package org.vudroid.core;

import android.graphics.Bitmap;
import android.support.v4.util.Pools;

/**
 * Created by archko on 16/12/24.
 */

public class BitmapPool {

    private static BitmapPool sInstance = new BitmapPool();
    private Pools.SimplePool<Bitmap> simplePool;

    private BitmapPool() {
        simplePool = new Pools.SimplePool<>(32);
    }

    public static BitmapPool getInstance() {
        return sInstance;
    }

    public Bitmap acquire(int width, int height) {
        Bitmap b = simplePool.acquire();
        if (null == b) {
            b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } else {
            b.eraseColor(0);
        }
        return b;
    }

    public void release(Bitmap bitmap) {
        simplePool.release(bitmap);
    }

    public void clear() {
        Bitmap bitmap;
        while ((bitmap = simplePool.acquire()) != null) {
            bitmap.recycle();
        }
        simplePool = null;
    }
}
