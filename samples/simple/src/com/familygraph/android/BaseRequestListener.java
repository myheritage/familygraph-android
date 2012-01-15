/*
 * Copyright 2012 MyHeritage, Ltd.
 *
 * The MyHeritage Family Graph Android SDK Example is based on the Facebook
 * Android simple example SDK:
 * Copyright 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.familygraph.android;

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
