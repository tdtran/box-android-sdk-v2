package com.box.androidlibv2.sample.oauth;

import android.app.Application;

import com.box.boxandroidlibv2.BoxAndroidClient;

public class OAuthSampleApplication extends Application {

    // TODO: use your own client settings
    public static final String CLIENT_ID = "vipzeyjh3g4s7phlzwjyvm79jfxkt4ga";
    public static final String CLIENT_SECRET = "szBTd8Q86KgEJNgJgeOPDEEHdWY8Nrkr";

    private final BoxAndroidClient client = new BoxAndroidClient(CLIENT_ID, CLIENT_SECRET, null, null);

    public BoxAndroidClient getClient() {
        return client;
    }

}
