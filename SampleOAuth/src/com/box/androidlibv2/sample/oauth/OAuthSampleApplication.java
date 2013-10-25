package com.box.androidlibv2.sample.oauth;

import android.app.Application;

import com.box.boxandroidlibv2.BoxAndroidClient;

public class OAuthSampleApplication extends Application {

    // TODO: use your own client settings
    public static final String CLIENT_ID = "";
    public static final String CLIENT_SECRET = "";

    private final BoxAndroidClient client = new BoxAndroidClient(CLIENT_ID, CLIENT_SECRET, null, null);

    public BoxAndroidClient getClient() {
        return client;
    }

}
