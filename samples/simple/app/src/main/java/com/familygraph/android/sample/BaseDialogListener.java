package com.familygraph.android.sample;


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