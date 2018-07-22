/*
 * Copyright 2012 MyHeritage, Ltd.
 *
 * The MyHeritage Family Graph Android SDK is based on the Facebook Android SDK:
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



package com.familygraph.android.sample;


import com.familygraph.android.AsyncFamilyGraphRunner;
import com.familygraph.android.DialogError;
import com.familygraph.android.FamilyGraph;
import com.familygraph.android.FamilyGraphError;
import com.familygraph.android.FamilyGraph.DialogListener;
import com.familygraph.android.sample.SessionEvents.AuthListener;
import com.familygraph.android.sample.SessionEvents.LogoutListener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class LoginButton extends android.support.v7.widget.AppCompatButton {

    private FamilyGraph mFamilyGraph;
    private Handler mHandler;
    private SessionListener mSessionListener = new SessionListener();
    private String[] mPermissions;
    private Activity mActivity;

    public LoginButton(Context context) {
        super(context);
    }

    public LoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(final Activity activity, final FamilyGraph familyGraph) {
        init(activity, familyGraph, new String[] {});
    }

    public void init(final Activity activity, final FamilyGraph familyGraph,
                     final String[] permissions) {
        mActivity = activity;
        mFamilyGraph = familyGraph;
        mPermissions = permissions;
        mHandler = new Handler();

        setText(familyGraph.isSessionValid() ? "Logout" : "Login");
        drawableStateChanged();

        SessionEvents.addAuthListener(mSessionListener);
        SessionEvents.addLogoutListener(mSessionListener);
        setOnClickListener(new ButtonOnClickListener());
    }

    private final class ButtonOnClickListener implements OnClickListener {
        public void onClick(View arg0) {
            if (mFamilyGraph.isSessionValid()) {
                SessionEvents.onLogoutBegin();
                AsyncFamilyGraphRunner asyncRunner = new AsyncFamilyGraphRunner(
                        mFamilyGraph);
                asyncRunner.logout(getContext(), new LogoutRequestListener());
            } else {
                mFamilyGraph.authorize(mActivity, mPermissions,
                        new LoginDialogListener());
            }
        }
    }

    private final class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            SessionEvents.onLoginSuccess();
        }

        public void onFamilyGraphError(FamilyGraphError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        public void onError(DialogError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        public void onCancel() {
            SessionEvents.onLoginError("Action Canceled");
        }
    }

    private class LogoutRequestListener extends BaseRequestListener {
        public void onComplete(String response, final Object state) {
            // callback should be run in the original thread,
            // not the background thread
            mHandler.post(new Runnable() {
                public void run() {
                    SessionEvents.onLogoutFinish();
                }
            });
        }
    }

    private class SessionListener implements AuthListener, LogoutListener {
        public void onAuthSucceed() {
            setText("Logout");
            SessionStore.save(mFamilyGraph, getContext());
        }

        public void onAuthFail(String error) {}

        public void onLogoutBegin() {}

        public void onLogoutFinish() {
            SessionStore.clear(getContext());
            setText("Login");
        }
    }
}