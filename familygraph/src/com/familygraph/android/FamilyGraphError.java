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

package com.familygraph.android;

/**
 * Encapsulation of a Family Graph Error: a FamilyGraph request that could not
 * be fulfilled.
 * 
 */
public class FamilyGraphError extends Throwable {

    private static final long serialVersionUID = 1L;

    private int mErrorCode = 0;
    private String mErrorType;

    public FamilyGraphError(String message) {
        super(message);
    }

    public FamilyGraphError(String message, String type, int code) {
        super(message);
        mErrorType = type;
        mErrorCode = code;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getErrorType() {
        return mErrorType;
    }
}
