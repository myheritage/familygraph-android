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

import java.io.InputStream;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.familygraph.android.AsyncFamilyGraphRunner;
import com.familygraph.android.FamilyGraph;
import com.familygraph.android.FamilyGraphError;
import com.familygraph.android.sample.R;
import com.familygraph.android.Util;
import com.familygraph.android.sample.SessionEvents.AuthListener;
import com.familygraph.android.sample.SessionEvents.LogoutListener;

public class Example extends Activity {

    // Your FamilyGraph client ID must be set before running this example
    // See http://www.familygraph.com/getAccess
    public static final String CLIENT_ID = "0863d587a2310161a910022cc41db154";

    private LoginButton mLoginButton;
    private TextView mText;
    private ImageView mPersonalPhoto;
    private Button mRequestButton;

    private FamilyGraph mFamilyGraph;
    private AsyncFamilyGraphRunner mAsyncRunner;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (CLIENT_ID == null) {
            Util.showAlert(
                    this,
                    "Warning",
                    "FamilyGraph Applicaton ID must be "
                            + "specified before running this example: see Example.java");
        }

        setContentView(R.layout.layout);
        mLoginButton = findViewById(R.id.loginButton);
        mText = Example.this.findViewById(R.id.txt);
        mPersonalPhoto = Example.this
                .findViewById(R.id.personalPhoto);
        mRequestButton = findViewById(R.id.requestButton);

        mFamilyGraph = new FamilyGraph(CLIENT_ID);
        mAsyncRunner = new AsyncFamilyGraphRunner(mFamilyGraph);

        SessionStore.restore(mFamilyGraph, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());

        String[] permissions = { "basic", "offline_access" };
        mLoginButton.init(this, mFamilyGraph, permissions);

        mRequestButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mFamilyGraph.isSessionValid()) {
                    Bundle params = new Bundle();
                    params.putString("fields", "name,picture");
                    mAsyncRunner.request("me", params,
                            new SampleRequestListener());
                }
            }
        });
        int visibility = mFamilyGraph.isSessionValid() ? View.VISIBLE
                : View.INVISIBLE;
        mRequestButton.setVisibility(visibility);
        mPersonalPhoto.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFamilyGraph.authorizeCallback(requestCode, resultCode, data);
    }

    public class SampleAuthListener implements AuthListener {

        public void onAuthSucceed() {
            mText.setText("You have logged in!");
            mRequestButton.setVisibility(View.VISIBLE);
        }

        public void onAuthFail(String error) {
            mText.setText("Login Failed: " + error);
        }
    }

    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
            mText.setText("Logging out...");
        }

        public void onLogoutFinish() {
            mText.setText("You have logged out!");
            mRequestButton.setVisibility(View.INVISIBLE);
            mPersonalPhoto.setVisibility(View.INVISIBLE);
        }
    }

    public class SampleRequestListener extends BaseRequestListener {
        /**
         * Extracts personal photo url from the json object
         *
         * @param json
         * @return
         */
        private String extractPersonalPhotoUrl(JSONObject json) {
            String personalPhotoUrl = null;

            try {
                JSONObject picture = json.getJSONObject("picture");
                if (picture != null) {
                    JSONObject data = picture.getJSONObject("data");
                    JSONArray thumbnails = data.getJSONArray("thumbnails");
                    if (thumbnails != null) {
                        for (int i = 0; i < thumbnails.length(); ++i) {
                            JSONObject thumbnail = thumbnails.getJSONObject(i);
                            int width = thumbnail.getInt("width");
                            int height = thumbnail.getInt("height");

                            if (width < 300 && height < 300) {
                                personalPhotoUrl = thumbnail.getString("url");
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // ignore error
            }

            return personalPhotoUrl;
        }

        public void onComplete(final String response, final Object state) {
            try {
                // process the response here: executed in background thread
                Log.d("FamilyGraph-Example", "Response: " + response.toString());
                JSONObject json = Util.parseJson(response);
                final String name = json.getString("name");
                final String personalPhotoUrl = extractPersonalPhotoUrl(json);

                // then post the processed result back to the UI thread
                // if we do not do this, an runtime exception will be generated
                // e.g. "CalledFromWrongThreadException: Only the original
                // thread that created a view hierarchy can touch its views."
                Example.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mText.setText("Hello there, " + name + "!");

                        try {
                            if (personalPhotoUrl != null) {
                                Bitmap bitmap = BitmapFactory
                                        .decodeStream((InputStream) new URL(
                                                personalPhotoUrl).getContent());
                                mPersonalPhoto.setImageBitmap(bitmap);
                                mPersonalPhoto.setVisibility(View.INVISIBLE);
                            }
                        } catch (Exception e) {
                            // ignore error
                        }
                    }
                });
            } catch (JSONException e) {
                Log.w("FamilyGraph-Example", "JSON Error in response");
            } catch (FamilyGraphError e) {
                Log.w("FamilyGraph-Example",
                        "FamilyGraph Error: " + e.getMessage());
            }
        }
    }
}