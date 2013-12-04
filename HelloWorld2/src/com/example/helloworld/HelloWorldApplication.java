package com.example.helloworld;

import android.app.Application;

import com.box.boxandroidlibv2.BoxAndroidClient;

public class HelloWorldApplication extends Application {

    public static final String CLIENT_ID = "qfvqglm6w1to95y6tib4cqctj0uw7y1m";
    public static final String CLIENT_SECRET = "7mBSlDyfqrG5SkL7xI2ZKuQ7Hv9bQf8q";
    public static final String REDIRECT_URL = "https://app.box.com/services/ddpce_test2";

    private BoxAndroidClient mClient;

    public void setClient(BoxAndroidClient client) {
        this.mClient = client;
    }

    /**
     * Gets the BoxAndroidClient for this app.
     * 
     * @return a singleton instance of BoxAndroidClient.
     */
    public BoxAndroidClient getClient() {
        return mClient;
    }
}
