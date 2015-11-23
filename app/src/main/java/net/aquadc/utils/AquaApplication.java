package net.aquadc.utils;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by miha on 11.11.15.
 * I use class "AquaApplication" in all my projects
 */
public class AquaApplication extends Application {

    static final ExecutorService executor = Executors.newFixedThreadPool(2);
    public static ImageCacher cacher;

    @Override
    public void onCreate() {
        super.onCreate();
        cacher = new ImageCacher(this, executor);
    }
}