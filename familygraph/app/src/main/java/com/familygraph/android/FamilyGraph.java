/*
 * Copyright 2012 MyHeritage, Ltd.
 *
 * The MyHeritage FamilyGraph Android SDK is based on the Facebook Android SDK:
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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieSyncManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Main FamilyGraph object for interacting with the Family Graph API. Provides
 * methods to log in and log out a user, make requests using the REST and Graph
 * APIs, and start user interface interactions with the API (such as pop-ups
 * promoting for credentials, permissions, stream posts, etc.)
 * 
 */
public class FamilyGraph {

    // Strings used in the authorization flow
    public static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    public static final String CANCEL_URI = "urn:ietf:wg:oauth:2.0:oob";
    public static final String TOKEN = "access_token";
    public static final String BEARER_TOKEN = "bearer_token";
    public static final String EXPIRES = "expires_in";

    private static final String LOGIN = "authorize";

    // Used as default activityCode by authorize(). See authorize() below.
    private static final int DEFAULT_AUTH_ACTIVITY_CODE = 32665;

    // FamilyGraph server endpoints: may be modified in a subclass for testing
    protected static String DIALOG_BASE_URL = "https://accounts.myheritage.com/oauth2/";
    protected static String GRAPH_BASE_URL = "https://familygraph.myheritage.com/";

    private String mAccessToken = null;
    private long mAccessExpires = 0;
    private String mClientId;

    private int mAuthActivityCode;
    private DialogListener mAuthDialogListener;

    /**
     * Constructor for FamilyGraph object.
     * 
     * @param clientId
     *            Your FamilyGraph client ID.
     */
    public FamilyGraph(String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException(
                    "You must specify your client ID when instantiating "
                            + "a FamilyGraph object. See README for details.");
        }
        mClientId = clientId;
    }

    /**
     * Default authorize method. Grants only basic permissions.
     * 
     * See authorize() below for @params.
     */
    public void authorize(Activity activity, final DialogListener listener) {
        authorize(activity, new String[] {}, DEFAULT_AUTH_ACTIVITY_CODE,
                listener);
    }

    /**
     * Authorize method that grants custom permissions.
     * 
     * See authorize() below for @params.
     */
    public void authorize(Activity activity, String[] permissions,
            final DialogListener listener) {
        authorize(activity, permissions, DEFAULT_AUTH_ACTIVITY_CODE, listener);
    }

    /**
     * Full authorize method.
     * 
     * Starts either an Activity or a dialog which prompts the user to log in to
     * MyHeritage and grant the requested permissions to the given application.
     * 
     * This method will use the OAuth 2.0 User-Agent flow to obtain an access
     * token. In this flow, the user credentials are handled by MyHeritage in an
     * embedded WebView, not by the client application. As such, the dialog
     * makes a network request and renders HTML content rather than a native UI.
     * The access token is retrieved from a redirect to a special URL that the
     * WebView handles.
     * 
     * See http://www.familygraph.com/documentation/authentication and
     * http://wiki.oauth.net/OAuth-2 for more details.
     * 
     * Note that this method is asynchronous and the callback will be invoked in
     * the original calling thread (not in a background thread).
     * 
     * Also note that requests may be made to the API without calling authorize
     * first, in which case only public information is returned.
     * 
     * @param activity
     *            The Android activity in which we want to display the
     *            authorization dialog.
     * @param permissions
     *            A list of permissions required for this application: e.g.
     *            "basic", "offline_access", etc. see
     *            http://www.familygraph.com/documentation/authentication This
     *            parameter should not be null -- if you do not require any
     *            permissions, then pass in an empty String array.
     * @param activityCode
     *            Single sign-on requires an activity result to be called back
     *            to the client application -- if you are waiting on other
     *            activities to return data, pass a custom activity code here to
     *            avoid collisions. See http://developer.android.com/reference/
     *            android/app/Activity.html for more information.
     * @param listener
     *            Callback interface for notifying the calling application when
     *            the authentication dialog has completed, failed, or been
     *            canceled.
     */
    public void authorize(Activity activity, String[] permissions,
            int activityCode, final DialogListener listener) {
        mAuthDialogListener = listener;
        startDialogAuth(activity, permissions);
    }

    /**
     * Internal method to handle dialog-based authentication backend for
     * authorize().
     * 
     * @param activity
     *            The Android Activity that will parent the auth dialog.
     * @param permissions
     *            A list of permissions required for this application. If you do
     *            not require any permissions, pass an empty String array.
     */
    private void startDialogAuth(Activity activity, String[] permissions) {
        Bundle params = new Bundle();
        if (permissions.length > 0) {
            params.putString("scope", TextUtils.join(",", permissions));
        }
        CookieSyncManager.createInstance(activity);
        dialog(activity, LOGIN, params, new DialogListener() {

            public void onComplete(Bundle values) {
                // ensure any cookies set by the dialog are saved
                Log.d("onComplete",
                        "ensure any cookies set by the dialog are saved");
                CookieSyncManager.getInstance().sync();
                setAccessToken(values.getString(TOKEN));
                setAccessExpiresIn(values.getString(EXPIRES));
                if (isSessionValid()) {
                    Log.d("FamilyGraph-authorize",
                            "Login Success! access_token=" + getAccessToken()
                                    + " expires=" + getAccessExpires());
                    mAuthDialogListener.onComplete(values);
                } else {
                    mAuthDialogListener
                            .onFamilyGraphError(new FamilyGraphError(
                                    "Failed to receive access token."));
                }
            }

            public void onError(DialogError error) {
                Log.d("FamilyGraph-authorize", "Login failed: " + error);
                mAuthDialogListener.onError(error);
            }

            public void onFamilyGraphError(FamilyGraphError error) {
                Log.d("FamilyGraph-authorize", "Login failed: " + error);
                mAuthDialogListener.onFamilyGraphError(error);
            }

            public void onCancel() {
                Log.d("FamilyGraph-authorize", "Login canceled");
                mAuthDialogListener.onCancel();
            }
        });
    }

    /**
     * IMPORTANT: This method must be invoked at the top of the calling
     * activity's onActivityResult() function or FamilyGraph authentication will
     * not function properly!
     * 
     * If your calling activity does not currently implement onActivityResult(),
     * you must implement it and include a call to this method if you intend to
     * use the authorize() method in this SDK.
     * 
     * For more information, see
     * http://developer.android.com/reference/android/app/
     * Activity.html#onActivityResult(int, int, android.content.Intent)
     */
    public void authorizeCallback(int requestCode, int resultCode, Intent data) {
        if (requestCode == mAuthActivityCode) {

            // Successfully redirected.
            if (resultCode == Activity.RESULT_OK) {

                // Check OAuth 2.0 error code.
                String error = data.getStringExtra("error_description");
                if (error == null) {
                    error = data.getStringExtra("error");
                }

                // A FamilyGraph error occurred.
                if (error != null) {
                    if (error.equals("access_denied")
                            || error.equals("OAuthAccessDeniedException")) {
                        Log.d("FamilyGraph-authorize",
                                "Login canceled by user.");
                        mAuthDialogListener.onCancel();
                    } else {
                        String description = data
                                .getStringExtra("error_description");
                        if (description != null) {
                            error = error + ":" + description;
                        }
                        Log.d("FamilyGraph-authorize", "Login failed: " + error);
                        mAuthDialogListener
                                .onFamilyGraphError(new FamilyGraphError(error));
                    }

                    // No errors.
                } else {
                    setAccessToken(data.getStringExtra(TOKEN));
                    setAccessExpiresIn(data.getStringExtra(EXPIRES));
                    if (isSessionValid()) {
                        Log.d("FamilyGraph-authorize",
                                "Login Success! access_token="
                                        + getAccessToken() + " expires="
                                        + getAccessExpires());
                        mAuthDialogListener.onComplete(data.getExtras());
                    } else {
                        mAuthDialogListener
                                .onFamilyGraphError(new FamilyGraphError(
                                        "Failed to receive access token."));
                    }
                }

                // An error occurred before we could be redirected.
            } else if (resultCode == Activity.RESULT_CANCELED) {

                // An Android error occurred.
                if (data != null) {
                    Log.d("FamilyGraph-authorize",
                            "Login failed: "
                                    + data.getStringExtra("error_description"));
                    mAuthDialogListener.onError(new DialogError(data
                            .getStringExtra("error_description"), data
                            .getIntExtra("error", -1), data
                            .getStringExtra("failing_url")));

                    // User pressed the 'back' button.
                } else {
                    Log.d("FamilyGraph-authorize", "Login canceled by user.");
                    mAuthDialogListener.onCancel();
                }
            }
        }
    }

    /**
     * Invalidate the current user session by removing the access token in
     * memory, clearing the browser cookie, and calling auth.expireSession
     * through the API.
     * 
     * Note that this method blocks waiting for a network response, so do not
     * call it in a UI thread.
     * 
     * @param context
     *            The Android context in which the logout should be called: it
     *            should be the same context in which the login occurred in
     *            order to clear any stored cookies
     * @throws IOException
     * @throws MalformedURLException
     * @return JSON string representation of the response: 'true' on success,
     *         'false' otherwise
     */
    public String logout(Context context) throws MalformedURLException,
            IOException {

        Util.clearCookies(context);
        setAccessToken(null);
        setAccessExpires(0);
        return "true";
    }

    /**
     * Make a request to the MyHeritage FamilyGraph API without any parameters.
     * 
     * See http://www.familygraph.com/documentation
     * 
     * Note that this method blocks waiting for a network response, so do not
     * call it in a UI thread.
     * 
     * @param graphPath
     *            Path to resource in the FamilyGraph, e.g., to fetch data about
     *            the currently logged authenticated user, provide "me", which
     *            will fetch https://familygraph.myheritage.com/me
     * @throws IOException
     * @throws MalformedURLException
     * @return JSON string representation of the response
     */
    public String request(String graphPath) throws MalformedURLException,
            IOException {

        return request(graphPath, new Bundle(), "GET");
    }

    /**
     * Make a request to the MyHeritage FamilyGraph API with the given string
     * parameters using an HTTP GET (default method).
     * 
     * See http://www.familygraph.com/documentation
     * 
     * Note that this method blocks waiting for a network response, so do not
     * call it in a UI thread.
     * 
     * @param graphPath
     *            Path to resource in the FamilyGraph, e.g., to fetch data about
     *            the currently logged authenticated user, provide "me", which
     *            will fetch https://familygraph.myheritage.com/me
     * @param parameters
     *            key-value string parameters, e.g. the path "me" with
     *            parameters "fields" : "nickname,gender" would produce a query
     *            for the following graph resource:
     *            https://familygraph.myheritage.com/me?fields=nickname,gender
     * @throws IOException
     * @throws MalformedURLException
     * @return JSON string representation of the response
     */
    public String request(String graphPath, Bundle parameters)
            throws MalformedURLException, IOException {

        return request(graphPath, parameters, "GET");
    }

    /**
     * Synchronously make a request to the MyHeritage FamilyGraph API with the
     * given HTTP method and string parameters. Note that binary data parameters
     * (e.g. pictures) are not yet supported by this helper function.
     * 
     * See http://www.familygraph.com/documentation
     * 
     * Note that this method blocks waiting for a network response, so do not
     * call it in a UI thread.
     * 
     * @param graphPath
     *            Path to resource in the FamilyGraph, e.g., to fetch data about
     *            the currently logged authenticated user, provide "me", which
     *            will fetch https://familygraph.myheritage.com/me
     * @param params
     *            key-value string parameters, e.g. the path "me" with
     *            parameters "fields" : "nickname,gender" would produce a query
     *            for the following graph resource:
     *            https://familygraph.myheritage.com/me?fields=nickname,gender
     * @param httpMethod
     *            http verb, e.g. "GET", "POST"
     * @throws IOException
     * @throws MalformedURLException
     * @return JSON string representation of the response
     */
    public String request(String graphPath, Bundle params, String httpMethod)
            throws FileNotFoundException, MalformedURLException, IOException {

        if (isSessionValid()) {
            params.putString(BEARER_TOKEN, getAccessToken());
        }
        String url = GRAPH_BASE_URL + graphPath;
        return Util.openUrl(url, httpMethod, params);
    }

    /**
     * Generate a UI dialog for the request action in the given Android context.
     * 
     * Note that this method is asynchronous and the callback will be invoked in
     * the original calling thread (not in a background thread).
     * 
     * @param context
     *            The Android context in which we will generate this dialog.
     * @param action
     *            String representation of the desired method: e.g. "login",
     *            "stream.publish", ...
     * @param listener
     *            Callback interface to notify the application when the dialog
     *            has completed.
     */
    public void dialog(Context context, String action, DialogListener listener) {
        dialog(context, action, new Bundle(), listener);
    }

    /**
     * Generate a UI dialog for the request action in the given Android context
     * with the provided parameters.
     * 
     * Note that this method is asynchronous and the callback will be invoked in
     * the original calling thread (not in a background thread).
     * 
     * @param context
     *            The Android context in which we will generate this dialog.
     * @param action
     *            String representation of the desired method: e.g. "feed" ...
     * @param parameters
     *            String key-value pairs to be passed as URL parameters.
     * @param listener
     *            Callback interface to notify the application when the dialog
     *            has completed.
     */
    public void dialog(Context context, String action, Bundle parameters,
            final DialogListener listener) {

        String endpoint = DIALOG_BASE_URL + action;
        parameters.putString("display", "touch");
        parameters.putString("redirect_uri", REDIRECT_URI);
        parameters.putString("response_type", "token");
        parameters.putString("client_id", mClientId);

        if (isSessionValid()) {
            parameters.putString(BEARER_TOKEN, getAccessToken());
        }
        String url = endpoint + "?" + Util.encodeUrl(parameters);
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Util.showAlert(context, "Error",
                    "Application requires permission to access the Internet");
        } else {
            new FgDialog(context, url, listener).show();
        }
    }

    /**
     * @return boolean - whether this object has an non-expired session token
     */
    public boolean isSessionValid() {
        return (getAccessToken() != null)
                && ((getAccessExpires() == 0) || (System.currentTimeMillis() < getAccessExpires()));
    }

    /**
     * Retrieve the OAuth 2.0 access token for API access: treat with care.
     * Returns null if no session exists.
     * 
     * @return String - access token
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * Retrieve the current session's expiration time (in milliseconds since
     * Unix epoch), or 0 if the session doesn't expire or doesn't exist.
     * 
     * @return long - session expiration time
     */
    public long getAccessExpires() {
        return mAccessExpires;
    }

    /**
     * Set the OAuth 2.0 access token for API access.
     * 
     * @param token
     *            - access token
     */
    public void setAccessToken(String token) {
        mAccessToken = token;
    }

    /**
     * Set the current session's expiration time (in milliseconds since Unix
     * epoch), or 0 if the session doesn't expire.
     * 
     * @param time
     *            - time stamp in milliseconds
     */
    public void setAccessExpires(long time) {
        mAccessExpires = time;
    }

    /**
     * Set the current session's duration (in seconds since Unix epoch), or "0"
     * if session doesn't expire.
     * 
     * @param expiresIn
     *            - duration in seconds (or 0 if the session doesn't expire)
     */
    public void setAccessExpiresIn(String expiresIn) {
        if (expiresIn != null) {
            long expires = expiresIn.equals("0") ? 0 : System
                    .currentTimeMillis() + Long.parseLong(expiresIn) * 1000L;
            setAccessExpires(expires);
        }
    }

    public String getClientId() {
        return mClientId;
    }

    public void setClientId(String clientId) {
        mClientId = clientId;
    }

    /**
     * Callback interface for dialog requests.
     * 
     */
    public static interface DialogListener {

        /**
         * Called when a dialog completes.
         * 
         * Executed by the thread that initiated the dialog.
         * 
         * @param values
         *            Key-value string pairs extracted from the response.
         */
        public void onComplete(Bundle values);

        /**
         * Called when a FamilyGraph responds to a dialog with an error.
         * 
         * Executed by the thread that initiated the dialog.
         * 
         */
        public void onFamilyGraphError(FamilyGraphError e);

        /**
         * Called when a dialog has an error.
         * 
         * Executed by the thread that initiated the dialog.
         * 
         */
        public void onError(DialogError e);

        /**
         * Called when a dialog is canceled by the user.
         * 
         * Executed by the thread that initiated the dialog.
         * 
         */
        public void onCancel();
    }
}
