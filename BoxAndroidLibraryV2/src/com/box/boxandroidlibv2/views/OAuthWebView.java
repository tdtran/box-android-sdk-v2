package com.box.boxandroidlibv2.views;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils.Null;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.box.boxandroidlibv2.BoxAndroidClient;
import com.box.boxandroidlibv2.R;
import com.box.boxandroidlibv2.dao.BoxAndroidOAuthData;
import com.box.boxandroidlibv2.exceptions.BoxAndroidLibException;
import com.box.boxandroidlibv2.exceptions.UserTerminationException;
import com.box.boxandroidlibv2.jsonparsing.AndroidBoxResourceHub;
import com.box.boxandroidlibv2.viewlisteners.OAuthWebViewListener;
import com.box.boxandroidlibv2.viewlisteners.StringMessage;
import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.authorization.OAuthDataMessage;
import com.box.boxjavalibv2.authorization.OAuthWebViewData;
import com.box.boxjavalibv2.events.OAuthEvent;
import com.box.boxjavalibv2.interfaces.IAuthEvent;
import com.box.boxjavalibv2.interfaces.IAuthFlowListener;
import com.box.boxjavalibv2.interfaces.IAuthFlowMessage;
import com.box.boxjavalibv2.interfaces.IAuthFlowUI;
import com.box.boxjavalibv2.jsonparsing.BoxJSONParser;
import com.box.boxjavalibv2.requests.requestobjects.BoxOAuthRequestObject;
import com.box.restclientv2.httpclientsupport.HttpClientURIBuilder;

/**
 * A WebView used for OAuth flow.
 */
public class OAuthWebView extends WebView implements IAuthFlowUI {

    private boolean allowShowingRedirectPage = true;

    private OAuthWebViewData mWebViewData;

    private OAuthWebViewClient mWebClient;

    private String deviceId;

    private String deviceName;

    /**
     * Constructor.
     * 
     * @param context
     *            context
     * @param attrs
     *            attrs
     */
    public OAuthWebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set the state, this is optional.
     * 
     * @param optionalState
     *            state
     */
    public void setOptionalState(final String optionalState) {
        mWebViewData.setOptionalState(optionalState);
    }

    @Override
    public void initializeAuthFlow(final Object activity, final String clientId, final String clientSecret) {
        initializeAuthFlow(activity, clientId, clientSecret, null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initializeAuthFlow(Object activity, String clientId, String clientSecret, String redirectUrl) {
        AndroidBoxResourceHub hub = new AndroidBoxResourceHub();
        BoxAndroidClient boxClient = new BoxAndroidClient(clientId, clientSecret, hub, new BoxJSONParser(hub));
        this.mWebViewData = new OAuthWebViewData(boxClient.getOAuthDataController());
        if (StringUtils.isNotEmpty(redirectUrl)) {
            mWebViewData.setRedirectUrl(redirectUrl);
        }
        mWebClient = createOAuthWebViewClient(mWebViewData, activity, boxClient);
        getSettings().setJavaScriptEnabled(true);
        setWebViewClient(mWebClient);
        setDevice(deviceId, deviceName);
    }

    @Override
    public void authenticate(IAuthFlowListener listener) {
        mWebClient.addListener(listener);

        try {
            loadUrl(mWebViewData.buildUrl().toString());
        }
        catch (URISyntaxException e) {
            if (listener != null) {
                listener.onAuthFlowException(e);
            }
        }
    }

    public void setDevice(final String id, final String name) {
        deviceId = id;
        deviceName = name;
        if (mWebClient != null) {
            mWebClient.setDevice(id, name);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.webkit.WebView#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        if (mWebClient != null) {
            mWebClient.destroy();
        }
    }

    /**
     * @return the allowShowingRedirectPage
     */
    public boolean allowShowRedirectPage() {
        return allowShowingRedirectPage;
    }

    /**
     * @param allowShowingRedirectPage
     *            the allowShowingRedirectPage to set
     */
    public void setAllowShowingRedirectPage(boolean allowShowingRedirectPage) {
        this.allowShowingRedirectPage = allowShowingRedirectPage;
    }

    protected OAuthWebViewClient createOAuthWebViewClient(OAuthWebViewData data, Object activity, BoxClient boxClient) {
        OAuthWebViewClient c = new OAuthWebViewClient(data, (Activity) activity, boxClient);
        c.setAllowShowRedirectPage(allowShowRedirectPage());
        return c;
    }

    /**
     * WebViewClient for the OAuth WebView.
     */
    public static class OAuthWebViewClient extends WebViewClient {

        private BoxClient mBoxClient;
        private final OAuthWebViewData mwebViewData;
        private boolean allowShowRedirectPage = true;
        private boolean startedCreateOAuth = false;

        private String deviceId;

        private String deviceName;

        private final List<IAuthFlowListener> mListeners = new ArrayList<IAuthFlowListener>();
        private Activity mActivity;

        /**
         * Constructor.
         * 
         * @param webViewData
         *            data
         * @param listener
         *            listener
         * @param activity
         *            activity hosting this webview
         */
        public OAuthWebViewClient(final OAuthWebViewData webViewData, final Activity activity, final BoxClient boxClient) {
            super();
            this.mwebViewData = webViewData;
            this.mActivity = activity;
            this.mBoxClient = boxClient;
        }

        public void setDevice(final String id, final String name) {
            deviceId = id;
            deviceName = name;
        }

        public void addListener(final IAuthFlowListener listener) {
            this.mListeners.add(listener);
        }

        void setStartedCreateOAuth(boolean started) {
            startedCreateOAuth = started;
        }

        @Override
        public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
            for (IAuthFlowListener listener : mListeners) {
                if (listener != null) {
                    listener.onAuthFlowEvent(OAuthEvent.PAGE_STARTED, new StringMessage(StringMessage.MESSAGE_URL, url));
                }
            }

            String code = null;
            try {
                code = getResponseValueFromUrl(url);
            }
            catch (URISyntaxException e) {
                fireExceptions(e);
            }
            if (StringUtils.isNotEmpty(code)) {
                for (IAuthFlowListener listener : mListeners) {
                    if (listener != null) {
                        listener.onAuthFlowMessage(new StringMessage(mwebViewData.getResponseType(), code));
                    }
                }
                setStartedCreateOAuth(true);
                startCreateOAuth(code);
                if (!allowShowRedirectPage()) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (startedCreateOAuth && !allowShowRedirectPage()) {
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedHttpAuthRequest(final WebView view, final HttpAuthHandler handler, final String host, final String realm) {
            for (IAuthFlowListener listener : mListeners) {
                listener.onAuthFlowEvent(OAuthEvent.AUTH_REQUEST_RECEIVED, new StringMessage(host, realm));
            }
            LayoutInflater factory = mActivity.getLayoutInflater();
            final View textEntryView = factory.inflate(R.layout.boxandroidlibv2_alert_dialog_text_entry, null);

            AlertDialog loginAlert = new AlertDialog.Builder(mActivity).setTitle(R.string.boxandroidlibv2_alert_dialog_text_entry).setView(textEntryView)
                .setPositiveButton(R.string.boxandroidlibv2_alert_dialog_ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        String userName = ((EditText) textEntryView.findViewById(R.id.username_edit)).getText().toString();
                        String password = ((EditText) textEntryView.findViewById(R.id.password_edit)).getText().toString();
                        handler.proceed(userName, password);
                    }
                }).setNegativeButton(R.string.boxandroidlibv2_alert_dialog_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        fireExceptions(new UserTerminationException());
                    }
                }).create();
            loginAlert.show();
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            fireEvents(OAuthEvent.PAGE_FINISHED, new StringMessage(StringMessage.MESSAGE_URL, url));
        }

        private static ProgressDialog dialog;

        /**
         * Start to create OAuth after getting the code.
         * 
         * @param config
         *            config
         * @param code
         *            code
         */
        private void startCreateOAuth(final String code) {
            dialog = ProgressDialog.show(mActivity, mActivity.getText(R.string.boxandroidlibv2_Authenticating),
                mActivity.getText(R.string.boxandroidlibv2_Please_wait));
            AsyncTask<Null, Null, BoxAndroidOAuthData> task = new AsyncTask<Null, Null, BoxAndroidOAuthData>() {

                @Override
                protected BoxAndroidOAuthData doInBackground(final Null... params) {
                    BoxAndroidOAuthData oauth = null;
                    try {
                        BoxOAuthRequestObject requestObj = BoxOAuthRequestObject.createOAuthRequestObject(code, mwebViewData.getClientId(),
                            mwebViewData.getClientSecret(), mwebViewData.getRedirectUrl());
                        if (StringUtils.isNotEmpty(deviceId) && StringUtils.isNotEmpty(deviceName)) {
                            requestObj.put("device_id", deviceId);
                            requestObj.put("device_name", deviceName);
                        }
                        oauth = (BoxAndroidOAuthData) mBoxClient.getOAuthManager().createOAuth(requestObj);
                    }
                    catch (Exception e) {
                        oauth = null;
                    }
                    return oauth;
                }

                @Override
                protected void onPostExecute(final BoxAndroidOAuthData result) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (result != null) {
                        try {
                            setStartedCreateOAuth(false);
                            fireEvents(OAuthEvent.OAUTH_CREATED, new OAuthDataMessage(result, mBoxClient.getJSONParser(), mBoxClient.getResourceHub()));
                        }
                        catch (Exception e) {
                            fireExceptions(new BoxAndroidLibException(e));
                        }
                    }
                    else {
                        fireExceptions(new BoxAndroidLibException());
                    }
                }
            };
            task.execute();
        }

        @Override
        public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl) {
            for (IAuthFlowListener listener : mListeners) {
                if (listener != null && listener instanceof OAuthWebViewListener) {
                    ((OAuthWebViewListener) listener).onError(errorCode, description, failingUrl);
                }
            }
        }

        @Override
        public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {
            for (IAuthFlowListener listener : mListeners) {
                if (listener != null && listener instanceof OAuthWebViewListener) {
                    ((OAuthWebViewListener) listener).onSslError(handler, error);
                }
            }
        }

        /**
         * Destroy.
         */
        public void destroy() {
            mListeners.clear();
            mBoxClient = null;
            mActivity = null;
        }

        /**
         * Get response value.
         * 
         * @param url
         *            url
         * @return response value
         * @throws URISyntaxException
         *             exception
         */
        private String getResponseValueFromUrl(final String url) throws URISyntaxException {
            HttpClientURIBuilder builder = new HttpClientURIBuilder(url);
            List<NameValuePair> query = builder.getQueryParams();
            for (NameValuePair pair : query) {
                if (pair.getName().equalsIgnoreCase(mwebViewData.getResponseType())) {
                    return pair.getValue();
                }
            }
            return null;
        }

        private void fireExceptions(Exception e) {
            for (IAuthFlowListener listener : mListeners) {
                if (listener != null) {
                    listener.onAuthFlowException(e);
                }
            }
        }

        private void fireEvents(IAuthEvent event, IAuthFlowMessage message) {
            for (IAuthFlowListener listener : mListeners) {
                if (listener != null) {
                    listener.onAuthFlowEvent(event, message);
                }
            }
        }

        /**
         * @return the allowShowRedirectPage
         */
        public boolean allowShowRedirectPage() {
            return allowShowRedirectPage;
        }

        /**
         * @param allowShowRedirectPage
         *            the allowShowRedirectPage to set
         */
        public void setAllowShowRedirectPage(boolean allowShowRedirectPage) {
            this.allowShowRedirectPage = allowShowRedirectPage;
        }
    }

}
