package com.box.boxandroidlibv2;

import com.box.boxandroidlibv2.dao.BoxAndroidOAuthData;
import com.box.boxandroidlibv2.jsonparsing.AndroidBoxResourceHub;
import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.dao.BoxOAuthToken;
import com.box.boxjavalibv2.interfaces.IAuthFlowMessage;
import com.box.boxjavalibv2.interfaces.IBoxJSONParser;
import com.box.boxjavalibv2.interfaces.IBoxResourceHub;
import com.box.restclientv2.interfaces.IBoxConfig;

/**
 * This is the main entrance of the sdk. The client contains all resource managers and also handles authentication. Make sure you call authenticate method
 * before making any api calls. you can use the resource managers to execute requests <b>synchronously</b> against the Box REST API(V2). Full details about the
 * Box API can be found at {@see <a href="http://developers.box.com/docs">http://developers.box.com/docs</a>} . You must have an OpenBox application with a
 * valid API key to use the Box API. All methods in this class are executed in the invoking thread, and therefore are NOT safe to execute in the UI thread of
 * your application. You should only use this class if you already have worker threads or AsyncTasks that you want to incorporate the Box API into.
 */
public class BoxAndroidClient extends BoxClient {

    /**
     * @param clientId
     *            client id
     * @param clientSecret
     *            client secret
     * @param resourcehub
     *            resource hub, use null for default resource hub.
     * @param parser
     *            json parser, use null for default parser.
     */
    public BoxAndroidClient(final String clientId, final String clientSecret, final IBoxResourceHub resourcehub, final IBoxJSONParser parser) {
        super(clientId, clientSecret, resourcehub, parser);
    }

    @Deprecated
    public BoxAndroidClient(String clientId, String clientSecret) {
        super(clientId, clientSecret);
    }

    @Override
    protected IBoxResourceHub createResourceHub() {
        return new AndroidBoxResourceHub();
    }

    @Override
    public IBoxConfig getConfig() {
        return BoxAndroidConfig.getInstance();
    }

    @Override
    protected BoxOAuthToken getOAuthTokenFromMessage(IAuthFlowMessage message) {
        return new BoxAndroidOAuthData(super.getOAuthTokenFromMessage(message));
    }
}
