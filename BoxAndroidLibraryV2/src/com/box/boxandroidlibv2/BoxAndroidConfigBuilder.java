package com.box.boxandroidlibv2;

import com.box.boxjavalibv2.BoxConfigBuilder;

public class BoxAndroidConfigBuilder extends BoxConfigBuilder {

    /** sdk version number */
    private final static String VERSION_NUMBER = "v3.0.3";

    /** Default User-Agent String. */
    private static final String USER_AGENT = "BoxAndroidLibraryV2";

    public BoxAndroidConfigBuilder() {
        super();
        this.setUserAgent(USER_AGENT);
        this.setVersion(VERSION_NUMBER);
    }
}
