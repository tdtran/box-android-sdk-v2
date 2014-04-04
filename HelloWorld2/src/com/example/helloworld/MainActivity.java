package com.example.helloworld;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils.Null;
import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.box.boxandroidlibv2.BoxAndroidClient;
import com.box.boxandroidlibv2.activities.FilePickerActivity;
import com.box.boxandroidlibv2.activities.FolderPickerActivity;
import com.box.boxandroidlibv2.activities.OAuthActivity;
import com.box.boxandroidlibv2.dao.BoxAndroidFile;
import com.box.boxandroidlibv2.dao.BoxAndroidFolder;
import com.box.boxandroidlibv2.dao.BoxAndroidOAuthData;
import com.box.boxjavalibv2.authorization.OAuthRefreshListener;
import com.box.boxjavalibv2.dao.IAuthData;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.jsonparsing.IBoxJSONParser;
import com.box.restclientv2.requestsbase.BoxFileUploadRequestObject;

public class MainActivity extends Activity {

    private static final String SHARED_PREF_NAME = MainActivity.class.getName();
    private static final String AUTH_KEY = "authdatastring";

    private final static int AUTH_REQUEST = 1;
    private final static int UPLOAD_REQUEST = 2;
    private final static int DOWNLOAD_REQUEST = 3;

    private Button btnUpload;
    private Button btnAuth;
    private Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {
        initUploadButton();
        initDownloadButton();
        initAuthButton();
    }

    private void initAuthButton() {
        btnAuth = (Button) findViewById(R.id.authenticate);
        btnAuth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Try load from saved auth first. If no saved auth or loading failed, start over from OAuth UI flow.
                if (!authenticateFromSavedAuth()) {
                    startAuthenticationFromUI();
                }
            }
        });

        if (authenticated()) {
            btnDownload.setVisibility(View.VISIBLE);
            btnUpload.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTH_REQUEST) {
            onAuthenticated(resultCode, data);
        }
        else if (requestCode == UPLOAD_REQUEST) {
            onFolderSelected(resultCode, data);
        }
        else if (requestCode == DOWNLOAD_REQUEST) {
            onFileSelected(resultCode, data);
        }
    }

    private void initDownloadButton() {
        btnDownload = (Button) findViewById(R.id.downloadfile);
        btnDownload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                doDownload();
            }
        });
        btnDownload.setVisibility(View.GONE);
    }

    private void initUploadButton() {
        btnUpload = (Button) findViewById(R.id.uploadfile);
        btnUpload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                doUpload();
            }
        });
        btnUpload.setVisibility(View.GONE);
    }

    private void onFileSelected(int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
            Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
        }
        else {
            final BoxAndroidFile file = data.getParcelableExtra(FilePickerActivity.EXTRA_BOX_ANDROID_FILE);
            AsyncTask<Null, Integer, Null> task = new AsyncTask<Null, Integer, Null>() {

                @Override
                protected void onPostExecute(Null result) {
                    Toast.makeText(MainActivity.this, "done downloading", Toast.LENGTH_LONG).show();
                    super.onPostExecute(result);
                }

                @Override
                protected void onPreExecute() {
                    Toast.makeText(MainActivity.this, "start downloading", Toast.LENGTH_LONG).show();
                    super.onPreExecute();
                }

                @Override
                protected Null doInBackground(Null... params) {
                    BoxAndroidClient client = ((HelloWorldApplication) getApplication()).getClient();
                    try {
                        File f = new File(Environment.getExternalStorageDirectory(), file.getName());
                        System.out.println(f.getAbsolutePath());
                        client.getFilesManager().downloadFile(file.getId(), f, null, null);
                    }
                    catch (Exception e) {
                    }
                    return null;
                }
            };
            task.execute();

        }
    }

    private void onFolderSelected(int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
            Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
        }
        else {
            final BoxAndroidFolder folder = data.getParcelableExtra(FolderPickerActivity.EXTRA_BOX_ANDROID_FOLDER);
            AsyncTask<Null, Integer, Null> task = new AsyncTask<Null, Integer, Null>() {

                @Override
                protected void onPostExecute(Null result) {
                    Toast.makeText(MainActivity.this, "done uploading", Toast.LENGTH_LONG).show();
                    super.onPostExecute(result);
                }

                @Override
                protected void onPreExecute() {
                    Toast.makeText(MainActivity.this, "start uploading", Toast.LENGTH_LONG).show();
                    super.onPreExecute();
                }

                @Override
                protected Null doInBackground(Null... params) {
                    BoxAndroidClient client = ((HelloWorldApplication) getApplication()).getClient();
                    try {
                        File mockFile = createMockFile();
                        client.getFilesManager().uploadFile(BoxFileUploadRequestObject.uploadFileRequestObject(folder.getId(), mockFile.getName(), mockFile));
                    }
                    catch (Exception e) {
                    }
                    return null;
                }
            };
            task.execute();
        }
    }

    private void doUpload() {
        try {
            BoxAndroidClient client = ((HelloWorldApplication) getApplication()).getClient();
            Intent intent = FolderPickerActivity.getLaunchIntent(this, "0", (BoxAndroidOAuthData) client.getAuthData(), HelloWorldApplication.CLIENT_ID,
                HelloWorldApplication.CLIENT_SECRET);
            startActivityForResult(intent, UPLOAD_REQUEST);
        }
        catch (AuthFatalFailureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void doDownload() {
        try {
            BoxAndroidClient client = ((HelloWorldApplication) getApplication()).getClient();
            Intent intent = FilePickerActivity.getLaunchIntent(this, "0", (BoxAndroidOAuthData) client.getAuthData(), HelloWorldApplication.CLIENT_ID,
                HelloWorldApplication.CLIENT_SECRET);
            startActivityForResult(intent, DOWNLOAD_REQUEST);
        }
        catch (AuthFatalFailureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private File createMockFile() {
        try {
            File file = File.createTempFile("tmp", ".txt");
            FileUtils.writeStringToFile(file, "string");
            return file;
        }
        catch (Exception e) {
            return null;
        }
    }

    private boolean authenticateFromSavedAuth() {
        BoxAndroidOAuthData auth = loadSavedAuth();
        if (auth != null) {
            authenticate(auth);
            return true;
        }
        else {
            return false;
        }
    }

    private void startAuthenticationFromUI() {
        Intent intent = OAuthActivity.createOAuthActivityIntent(this, HelloWorldApplication.CLIENT_ID, HelloWorldApplication.CLIENT_SECRET, false,
            HelloWorldApplication.REDIRECT_URL);
        this.startActivityForResult(intent, AUTH_REQUEST);
    }

    private void onAuthenticated(int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
            Toast.makeText(this, "fail:" + data.getStringExtra(OAuthActivity.ERROR_MESSAGE), Toast.LENGTH_LONG).show();
        }
        else {
            BoxAndroidOAuthData oauth = data.getParcelableExtra(OAuthActivity.BOX_CLIENT_OAUTH);
            authenticate(oauth);
        }
    }

    private void authenticate(BoxAndroidOAuthData auth) {
        BoxAndroidClient client = new BoxAndroidClient(HelloWorldApplication.CLIENT_ID, HelloWorldApplication.CLIENT_SECRET, null, null, null);
        client.authenticate(auth);
        ((HelloWorldApplication) getApplication()).setClient(client);
        saveAuth(auth);
        client.addOAuthRefreshListener(new OAuthRefreshListener() {

            @Override
            public void onRefresh(IAuthData newAuthData) {
                saveAuth((BoxAndroidOAuthData) newAuthData);
            }

        });
        btnDownload.setVisibility(View.VISIBLE);
        btnUpload.setVisibility(View.VISIBLE);
        Toast.makeText(this, "authenticated", Toast.LENGTH_LONG).show();
    }

    private void saveAuth(BoxAndroidOAuthData auth) {
        try {
            IBoxJSONParser parser = ((HelloWorldApplication) getApplication()).getJSONParser();
            String authString = parser.convertBoxObjectToJSONString(auth);
            getSharedPreferences(SHARED_PREF_NAME, 0).edit().putString(AUTH_KEY, authString).commit();
        }
        catch (Exception e) {
        }
    }

    private BoxAndroidOAuthData loadSavedAuth() {
        String authString = getSharedPreferences(SHARED_PREF_NAME, 0).getString(AUTH_KEY, "");
        if (StringUtils.isNotEmpty(authString)) {
            try {
                IBoxJSONParser parser = ((HelloWorldApplication) getApplication()).getJSONParser();
                BoxAndroidOAuthData auth = parser.parseIntoBoxObject(authString, BoxAndroidOAuthData.class);
                return auth;
            }
            catch (Exception e) {
                // failed, null will be returned. You can also add more logging, error handling here.
            }
        }
        return null;
    }

    private boolean authenticated() {
        BoxAndroidClient client = ((HelloWorldApplication) getApplication()).getClient();
        return client != null && client.isAuthenticated();
    }

}
