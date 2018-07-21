package com.familygraph.android.sample;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.util.Log;

import com.familygraph.android.FamilyGraphError;
import com.familygraph.android.AsyncFamilyGraphRunner.RequestListener;

/**
 * Skeleton base class for RequestListeners, providing default error handling.
 * Applications should handle these error conditions.
 *
 */
public abstract class BaseRequestListener implements RequestListener {

    public void onFamilyGraphError(FamilyGraphError e, final Object state) {
        Log.e("FamilyGraph", e.getMessage());
        e.printStackTrace();
    }

    public void onFileNotFoundException(FileNotFoundException e,
                                        final Object state) {
        Log.e("FamilyGraph", e.getMessage());
        e.printStackTrace();
    }

    public void onIOException(IOException e, final Object state) {
        Log.e("FamilyGraph", e.getMessage());
        e.printStackTrace();
    }

    public void onMalformedURLException(MalformedURLException e,
                                        final Object state) {
        Log.e("FamilyGraph", e.getMessage());
        e.printStackTrace();
    }
}