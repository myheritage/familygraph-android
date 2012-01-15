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

import com.familygraph.android.DialogError;
import com.familygraph.android.FamilyGraphError;
import com.familygraph.android.FamilyGraph.DialogListener;

/**
 * Skeleton base class for RequestListeners, providing default error handling.
 * Applications should handle these error conditions.
 * 
 */
public abstract class BaseDialogListener implements DialogListener {

    public void onFamilyGraphError(FamilyGraphError e) {
        e.printStackTrace();
    }

    public void onError(DialogError e) {
        e.printStackTrace();
    }

    public void onCancel() {

    }
}
