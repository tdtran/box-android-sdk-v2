Box Android SDK
===============

Building
--------
The Android SDK depends on the [Box Java SDK][java-sdk], so you must first
import it into your workspace and make sure it builds. Import the Android SDK
second and make the Java SDK a build dependency.

* 1. In your workspace, clone java sdk: git clone git@github.com:box/box-java-sdk-v2.git
* 2. In your workspace, clone android sdk: git clone git@github.com:box/box-android-sdk-v2.git

### Eclipse
* 3. In eclipse, import the two projects. File->Import->General->Existing Projects into Workspace, then select these two projects.
* 4. Copy jar files in java sdk library to your android project.
   - If you don't have a "libs" folder under your android project root folder, create one.
   - In "libs" folder(BoxJavaLibraryV2/libs) of java sdk project, copy the non-testing jar files into your android libs folder. They are: commons*.jar, http*.jar, jackson*.jar.
* 5. Add box android sdk as an android library project: Open properties of your project, select "Android", on bottom "Library" section, click "Add...", add BoxAndroidLibraryV2.
* 6. Resolve possible conflicts. In case your project already referred to different version of a jar our sdk refers, there would be a conflict. Choose the best version and replace the jar in both projects. We recommend you always use the latest version, or the version that's more stable if you know.

### Ant

* 3. Follow the instructions in [Box Java SDK][java-sdk]'s
readme on how to build it. 
* 4. Copy the the built BoxJavaLibraryV2.jar to
BoxAndroidLibraryV2/libs. 
* 5. You can then use Ant to build the project like you
would with any other Android library. The simplest way to do this is by running
`ant debug`.
* 6. Resolve possible conflict, same as step 6 in eclipse build instruction.

### Gradle (Experimental)

There is also experimental support for Gradle, allowing you to use the SDK with
Android Studio. You must have [Gradle 1.6](http://www.gradle.org/downloads)
installed.

Before the Android SDK can be built, you must first install the [Box Java SDK
][java-sdk] to your local Maven repository. This can be done by following the
Gradle build instructions included in the Java SDK's readme.

The Android SDK also depends on the Android Support Library. Unfortunately,
telling Gradle to look for the android-support JAR directly will likely result
in dex merge conflicts if more than one project uses the support library. The
easiest way to get around this is by also installing android-support-v4.jar to
your local Maven repo. Run the following command, where $ANDROID_HOME points to
your Android SDK root (you must have Maven installed).

	mvn install:install-file \
	-Dfile=$ANDROID_HOME/extras/android/support/v4/android-support-v4.jar \
	-DgroupId=com.google.android \
	-DartifactId=support-v4 \
	-Dversion=r13 \
	-Dpackaging=jar

You can now run `gradle build` to build the SDK. However, building the library
by itself isn't very useful. To reference the SDK from another Android Gradle
project, add the following to your list of dependencies:

	dependencies {
		...
		compile project(':box-android-sdk-private:BoxAndroidLibraryV2')
	}

You can refer to the Android Gradle guide on multi project setups [here
][android-gradle].

Here is a more detailed [tutorial][tutorial-box-gradle] on setting up box sdk using gradle.

API Calls Quickstart
--------------------

For migration to V3 from earlier version, please see "Migrate to V3" section at the end.

Authentication
--------------
Make sure you've set up your client id, client secret and (optional) redirect url correctly. Please refer to [developer document]
(http://developers.box.com/oauth/) for more information.
You can find a full example of how to perform authentication in the sample app.

### Basic Authentication

The easiest way to authenticate is to use the OAuthActivity, which is included
in the SDK. Add it to your manifest to use it.

```java
// If you don't have a server redirect url, use this instead:
// Intent intent = createOAuthActivityIntent(context, clientId, clientSecret, false, "http://localhost"); 
Intent intent = OAuthActivity.createOAuthActivityIntent(this, clientId, 
	clientSecret);
startActivityForResult(intent);

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (resultCode == Activity.RESULT_CANCELED) {
		// Get the error message for why authentication failed.
		String failMessage = data.getStringExtra(OAuthActivity.ERROR_MESSAGE);
		// Implement your own logic to handle the error.
	   handleFail(failMessage);
	} else {
		// You will get an authenticated oath token object back upon success.
		BoxAndroidOAuthData oauth = data.getParcelableExtra(OAuthActivity.BOX_CLIENT_OAUTH);
                // If you don't want to supply a customized hub or parser, use null to fall back to defaults.
                BoxAndroidClient client = new BoxAndroidClient(clientId, clientSecret, null, null, null);
                client.authenticate(oauth);
		youOwnMethod(client);
	}
}
```
Our sdk auto refreshes OAuth access token when it expires. You will want to listen to the refresh events and update your stored token after refreshing.
```java
boxClient.addOAuthRefreshListener(OAuthRefreshListener listener) {
    new OAuthRefreshListener() {
        @Override
        public void onRefresh(IAuthData newAuthData) {
	    BoxOAuthToken oauthObject = boxClient.getAuthData();
            // TODO: save the auth data.
        }						       
    }
}
```

### Advanced Authentication

Alternatively, you can use your own custom login activity with a WebView for
authentication.

```java
oauthView = (OAuthWebView) findViewById(R.id.oauthview);
oauthView.initializeAuthFlow(this, clientId, clientSecret);
boxClient.authenticate(oauthView, autoRefreshOAuth, getOAuthFlowListener());

// Create a listener listening to OAuth flow. The most important part you need
// to implement is onAuthFlowEvent and catch the OAUTH_CREATED event. This event
// indicates that the OAuth flow is done, the BoxClient is authenticated and
// that you can start making API calls. 
private OAuthWebViewListener getOAuthFlowListener() {
	return new OAuthWebViewListener() {
		@Override
		public onAuthFlowEvent(final IAuthEvent event,
			final IAuthFlowMessage message) {
			// Authentication is done, you can start using your BoxClient
			// instance.
		}
	}
}
boxClient.addOAuthRefreshListener(OAuthRefreshListener listener) {
    new OAuthRefreshListener() {
        @Override
        public void onRefresh(IAuthData newAuthData) {
	    BoxOAuthToken oauthObject = boxClient.getAuthData();
            // TODO: save the auth data.
        }						       
    }
}
```


After you exit the app and return back, you can use the stored oauth data to authenticate:
```java
// Re-authenticate using the previously obtained OAuth object.
boxClient.authenticate(oauthObject);
``` 

### Get Default File Info

```java
BoxFile boxFile = boxClient.getFilesManager().getFile(fileId, null);
```

### Get Additional File Info

Get default file info plus its description and SHA1.

```java
BoxDefaultRequestObject requestObj = new BoxDefaultRequestObject();
requestObj.getRequestExtras().addField(BoxFile.FIELD_SHA1);
requestObj.getRequestExtras().addField(BoxFile.FIELD_DESCRIPTION);
BoxFile boxFile = boxClient.getFilesManager().getFile(fileId, requestObj);
```

### Get Folder Children

Get 30 child items, starting from the 20th one, requiring etag, description, and
name to be included.

```java
BoxPagingRequestObject requestObj = BoxPagingRequestObject.BpagingRequestObject(30, 20);
requestObj.getRequestExtras().addField(BoxFolder.FIELD_NAME);
requestObj.getRequestExtras().addField(BoxFolder.FIELD_DESCRIPTION);
requestObj.getRequestExtras().addField(BoxFolder.FIELD_ETAG);
BoxCollection collection = 
	boxClient.getFoldersManager().getFolderItems(folderId, requestObj);
```

### Upload a New File

```java
BoxFileUploadRequestObject requestObj = 
	BoxFileUploadRequestObject.uploadFileRequestObject(parent, "name", file);
BoxFile bFile = boxClient.getFilesManager().uploadFile(requestObj);
```

### Upload a File with a Progress Listener

```java
BoxFileUploadRequestObject requestObj = 
    BoxFileUploadRequestObject.uploadFileRequestObject(parent, "name", file)
    .setListener(listener);
BoxAndroidFile bFile = boxClient.getFilesManager().uploadFile(requestObj);
```

### Download a File

```java
boxClient.getFilesManager().downloadFile(fileId, null);
```

### Delete a File

Delete a file, but only if the etag matches.

```java
BoxDefaultRequestObject requestObj = new BoxDefaultRequestObject();
requestObject.getRequestExtras.setIfMatch(etag);
boxClient.getFilesManager().deleteFile(fileId, requestObj);
```

Migration to V3
------------

- Resource manager interfaces.
pre-v3, our boxClient.get***Manager() method returns concrete class of resource managers. For the purpose of a cleaner interface, in v3, they return resource manager interfaces.
```java
old code:
BoxFilesManager filesManager = boxClient.getFilesManager();
filesManager.doSomething(...);
new code:
IBoxFilesManager filesManager = boxClient.getFilesManager();
filesManager.doSomething(...);
```
- Made certain methods more convenient, e.g., OAuth api related methods:
```java
old code:
BoxOAuthRequestObject obj = BoxOAuthRequestObject.crateOAuthRequestObject(code, clientId, clientSecret, redirectUrl);
BoxOAuthData oauth = oauthManager.createOAuth(obj);
new Code:
BoxOAuthData oauth = oauthManager.createOAuth(code, clientId, clientSecret, redirectUrl);
```
- BoxFilesManager/BoxFoldersManager. Methods acting upon BoxItems are moved to BoxItemsManager to avoid confusion.
Example, get a BoxFile.
```java
Old code:
Two ways to get it:
1. boxClient.getFilesManager.getFile(fileId, null);
2. boxClient.getFilesManager.getItem(fileId, null, BoxResourceType.FILE);
New code:
Two ways to get it:
1. same as old code.
2. boxClient.getBoxItemsManager.getItem(fileId, null, BoxResourceType.FILE);
```
- Trash Manager: old code has methods for trashed files/folders in FilesManager/FoldersManager, new code moved them into a trash manager.
Example:
```java
old code:
boxClient.getFilesManager.getTrashFile(fileId, null);
new code:
boxClient.getTrashManager.getTrashFile(fileId, null);
```
- request objects: To avoid confusion, request objects now are more api specific. There are some type changes, however the way you used to write the code remain the same. One example:
```java
Old code of create a shared link.
BoxFileRequestObject  obj = BoxFileRequestObject. createSharedLinkRequestObject(......);
filesManager.createSharedLink(fileId, obj);
New code:
BoxSharedLinkRequestObject obj = 
   BoxSharedLinkRequestObject.
   createSharedLinkRequestObject(sharedLinkEntity);
filesManager.createSharedLink(fileId, obj);
```
Also in order to provide cleaner interface, we moved the setters for basic http requests in the request objects to a "requestExtra".
```java
Old code:
requestObject.addField("some field");
new code:
requestObject.getRequestExtras().addField("some field");
```
(Optional/Deprecated)
- utils methods in resource managers.
In case you were using the utils methods in resource managers to filter for specific items from collection, they are now deprecated and moved to util methods.
```java
old code:
List<BoxFile> filesInCollection = BoxFilesManager.getFiles(collection);
new code:
List<BoxFile> filesInCollection = Utils.getTypedObjects(collection, BoxFile.class);
```
- get thumbnail.
```java
old code: 
InputStream is = filesManager.downloadThumbnail(fileId, extension, null);
new code:
BoxThumbnail thumbnail = filesManager.downloadThumbnail(fileId, extension, null);
```


[java-sdk]: https://github.com/box/box-java-sdk-private
[android-gradle]: http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Multi-project-setup
[tutorial-box-gradle]:  http://rexstjohn.com/using-android-box-sdk-android-studio


## Copyright and License

Copyright 2014 Box, Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
