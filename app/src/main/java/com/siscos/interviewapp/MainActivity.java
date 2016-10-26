package com.siscos.interviewapp;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.LinearLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    final String APP_NAME = "Interview App";
    final String POST_FEATRUE_FIELDS = "message,id,link,is_published,created_time,picture";
    final int REQ_CODE_SELECT_IMAGE = 10001;

    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private ProfileTracker profileTracker;

    private ScrollView scrollView;
    private ProfilePictureView profilePictureView;
    private LoginButton loginButton;
    private TextView displayName;
    private EditText editText;
    private List<PageInfo> pageInfoList;
    private ListView pageNameListView;
    ArrayAdapter<String> pageNameAdapter;
    private Spinner typeSelSpinner;
    private Button postBtn;
    private Button galleryBtn;
    private LinearLayout postLayout;
    private CheckBox publishedCheckBox;
    private TextView postTextView;

    private int selectedPageIdx;
    private int selectedType;
    String[] typeListStr = {"message", "link", "picture"};

    private List<RelativeLayout> postingView;
    private List<PostInfo> postInfoList;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        selectedPageIdx = -1;
        selectedType = -1;

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
//                        handlePendingAction();
                        Set<String> permissions = AccessToken.getCurrentAccessToken().getPermissions();
                        Log.d(APP_NAME, "permission = " + permissions.toString());
                        accessToken = AccessToken.getCurrentAccessToken();

                        setUserProfile();
                        getPageInfo();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {

                    }


                });

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        loginButton = (LoginButton) findViewById(R.id.loginButton);
        loginButton.setPublishPermissions(Arrays.asList("manage_pages", "publish_pages"));
        profilePictureView = (ProfilePictureView)findViewById(R.id.profilePicture);
        displayName = (TextView) findViewById(R.id.displayName);

        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                postBtn.setEnabled(selectedPageIdx != -1 && s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                setUserProfile();
                if (pageInfoList.isEmpty())
                    getPageInfo();
            }
        };

        pageInfoList = new ArrayList<PageInfo>();
        postInfoList = new ArrayList<PostInfo>();

        typeSelSpinner = (Spinner) findViewById(R.id.typeSelSpinner);
        makeTypeSelSpinner();

        pageNameListView = (ListView)findViewById(R.id.pageNameListView);

        postBtn = (Button) findViewById(R.id.postButton);
        postBtn.setEnabled(selectedPageIdx != -1 && editText.getText().length() > 0);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPageIdx != -1) {
                    if (publishedCheckBox.isChecked())
                        postInPage(editText.getText().toString(), publishedCheckBox.isChecked(), selectedPageIdx, null);
                    else {
                        Calendar canlendar = new GregorianCalendar();
                        canlendar.add(Calendar.MINUTE, 11);
                        DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                if (!view.isShown())
                                    return;
                                Log.d(APP_NAME, "DatePickerDialog selected");
                                ShowDatePickernMakeTime(year, monthOfYear, dayOfMonth);
                            }
                        }, canlendar.get(Calendar.YEAR), canlendar.get(Calendar.MONTH), canlendar.get(Calendar.DAY_OF_MONTH));
                        dialog.show();
                    }
                }
            }
        });

        galleryBtn = (Button) findViewById(R.id.GalleryBtn);
//        galleryBtn.setEnabled(true);
        galleryBtn.setVisibility(View.INVISIBLE);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_CODE_SELECT_IMAGE);
            }
        });

        publishedCheckBox = (CheckBox) findViewById(R.id.published);
        publishedCheckBox.setChecked(true);

        postLayout = (LinearLayout) findViewById(R.id.postLayout);

        postTextView = (TextView) findViewById(R.id.postTextView);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserProfile();

        if (pageInfoList.isEmpty())
            getPageInfo();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE_SELECT_IMAGE && resultCode == RESULT_OK && null != data) {
            try {
                Log.d(APP_NAME, "onActivityResult. REQ_CODE_SELECT_IMAGE = " + data.getData().toString());
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
//                ImageView image = (ImageView)findViewById(R.id.imageView1);
//                  image.setImageBitmap(image_bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setUserProfile() {
        Log.d(APP_NAME, "setUserProfile! profile is null? " + (Profile.getCurrentProfile() == null));

        Profile profile = Profile.getCurrentProfile();

        if (AccessToken.getCurrentAccessToken() != null && profile != null) {
            Log.d(APP_NAME, "profile = " + profile.toString());
            Log.d(APP_NAME, "profile name = " + profile.getName());
            Log.d(APP_NAME, "accessToken = " + AccessToken.getCurrentAccessToken());
            profilePictureView.setProfileId(profile.getId());
            displayName.setText(profile.getName());
        } else {
            profilePictureView.setProfileId(null);
            displayName.setText(null);
            clearAction();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.siscos.interviewapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.siscos.interviewapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private void showAlert(Context cnt, String title, String message, String button) {
        new AlertDialog.Builder(cnt)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, null)
                .show();
    }

    private void getPageInfo() {
        if (Profile.getCurrentProfile() == null) {
            Log.d(APP_NAME, "getPageInfo profile is null!!!");
            return;
        }

        final ProgressDialog progressDialog = makeProgressDialog();
        progressDialog.show();

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s/accounts", Profile.getCurrentProfile().getId()),
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() != null) {
                            Log.d(APP_NAME, "getPageInfo fail!!! " + response.getError().toString());
                            Toast.makeText(MainActivity.this, response.getError().toString(),  Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            return;
                        }
                        Log.d(APP_NAME, "success. response = " + response.toString());
                        pageInfoList.clear();

                        try {
                            JSONObject json = response.getJSONObject();
                            JSONArray data = (JSONArray) json.get("data");
                            for (int i = 0; i < data.length(); i++) {
                                PageInfo pageInfo = new PageInfo();
                                JSONObject dataJson = (JSONObject) data.get(i);
                                pageInfo.setCategory(dataJson.getString("category"));
                                pageInfo.setName(dataJson.getString("name"));
                                pageInfo.setId(dataJson.getString("id"));

                                pageInfo.setPageAccessToken(dataJson.getString("access_token"));

                                JSONArray jsonArray = (JSONArray) dataJson.get("perms");
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    pageInfo.addPerm((String) jsonArray.get(j));
                                }
                                pageInfoList.add(pageInfo);
                            }
                        } catch (JSONException e) {
                            pageInfoList.clear();
                            Log.d(APP_NAME, "getPageInfo json parse fail!!");
                            showAlert(MainActivity.this, getString(R.string.error), getString(R.string.error), getString(R.string.ok));
                            progressDialog.dismiss();
                            return;
                        }
                        Log.d(APP_NAME, "pageInfoList = " + pageInfoList.toString());
                        progressDialog.dismiss();
                        makePageSelectingList();
                    }
                }
        ).executeAsync();
    }

    private void makePageSelectingList() {
        if (pageInfoList.isEmpty())
            return;

        ArrayList<String> pageNames = new ArrayList<String>();
        for (PageInfo pageInfo : pageInfoList)
            pageNames.add(pageInfo.getName());

        pageNameAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_textview, pageNames);
        pageNameListView.setAdapter(pageNameAdapter);

        ViewGroup.LayoutParams params = pageNameListView.getLayoutParams();
        //TextView sizeView = (TextView)findViewById(R.id.text_1);
        params.height = 60 * pageInfoList.size();
        pageNameListView.setLayoutParams(params);
        pageNameListView.requestLayout();

        pageNameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                scrollView.requestDisallowInterceptTouchEvent(true);
                if (selectedPageIdx == position)
                    return;
                selectedPageIdx = position;
                postLayout.removeAllViews();
                readPagePublishedPost(position);
            }
        });

    }

    public void postInPage(String postStr, final boolean published, final int pageIdx, final Timestamp timestamp) {
        final ProgressDialog progressDialog = makeProgressDialog();
        progressDialog.show();

        Bundle params = new Bundle();
        params.putString(typeListStr[selectedType], postStr);
        if (selectedType == 2)
            params.putString("link", postStr);

        params.putBoolean("published", published);

        if (!published && timestamp != null) {
            Log.d(APP_NAME, "postInPage. timestamp = " + timestamp.getTime());
            params.putInt("scheduled_publish_time", (int) (timestamp.getTime() / 1000));
        }

        new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
                new AccessToken(pageInfoList.get(pageIdx).getPageAccessToken(), getString(R.string.facebook_app_id), AccessToken.getCurrentAccessToken().getUserId(),
                        AccessToken.getCurrentAccessToken().getPermissions(), AccessToken.getCurrentAccessToken().getDeclinedPermissions(), null, null, null),
                String.format("/%s/feed", pageInfoList.get(pageIdx).getId()),
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        if (response.getError() != null){
                            Log.d(APP_NAME, "postInPage fail!!! " + response.getError().toString());
                            postResponseAction(response.getError().toString(), progressDialog);
                            return;
                        }
                        Log.d(APP_NAME, "postInPage reponse = " + response.toString());
                        postResponseAction(getString(R.string.success), progressDialog);
                        readPagePublishedPost(pageIdx);
                    }
                }
        ).executeAsync();
    }

    private void postResponseAction(String toastString, ProgressDialog progressDialog){
        Toast.makeText(MainActivity.this,toastString , Toast.LENGTH_SHORT).show();
        editText.setText("");
        progressDialog.dismiss();
        publishedCheckBox.setChecked(true);
    }

    private void readPagePublishedPost(final int pageIdx) {
        final ProgressDialog progressDialog = makeProgressDialog();
        progressDialog.show();

        Log.d(APP_NAME, "readPagePublishedPost query = " + String.format("/%s/feed?fields=message,id,link,is_published,created_time", pageInfoList.get(pageIdx).getId()));

        Bundle params = new Bundle();
        params.putString("fields", POST_FEATRUE_FIELDS);

        new GraphRequest(
                new AccessToken(pageInfoList.get(pageIdx).getPageAccessToken(), getString(R.string.facebook_app_id), AccessToken.getCurrentAccessToken().getUserId(),
                        AccessToken.getCurrentAccessToken().getPermissions(), AccessToken.getCurrentAccessToken().getDeclinedPermissions(), null, null, null),
                String.format("/%s/feed", pageInfoList.get(pageIdx).getId()),
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        if (response.getError() != null) {
                            Log.d(APP_NAME, "readPagePublishedPost fail!!! " + response.getError().toString());
                            readPostResponseErrorAction();
                            progressDialog.dismiss();
                            return;
                        }

                        postInfoList.clear();
                        postLayout.removeAllViews();

                        Log.d(APP_NAME, "readPagePublishedPost response = " + response.toString());

                        if (handlePostInfoResponse(response)) {
                            readPageUnpublishedPost(pageIdx);
                        } else {
                            Log.d(APP_NAME, "readPagePublishedPost json parse fail!!");
                            readPostResponseErrorAction();
                            progressDialog.dismiss();
                        }
                    }
                }
        ).executeAsync();
    }

    private void readPageUnpublishedPost(final int pageIdx) {
        final ProgressDialog progressDialog = makeProgressDialog();

        Bundle params = new Bundle();
        params.putString("fields", POST_FEATRUE_FIELDS);
        params.putString("is_published", "false");

        new GraphRequest(
                new AccessToken(pageInfoList.get(pageIdx).getPageAccessToken(), getString(R.string.facebook_app_id), AccessToken.getCurrentAccessToken().getUserId(),
                        AccessToken.getCurrentAccessToken().getPermissions(), AccessToken.getCurrentAccessToken().getDeclinedPermissions(), null, null, null),
                String.format("/%s/promotable_posts", pageInfoList.get(pageIdx).getId()),
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        if (response.getError() != null){
                            Log.d(APP_NAME, "readPageUnpublishedPost fail!!! " + response.getError().toString());
                            readPostResponseErrorAction();
                            progressDialog.dismiss();
                            return;
                        }

                        Log.d(APP_NAME, "readPageUnpublishedPost response = " + response.toString());

                        if (handlePostInfoResponse(response)) {
                            makePostUI();
                            postTextView.setText("Posts of " + pageInfoList.get(pageIdx).getName());
                        } else {
                            Log.d(APP_NAME, "readPagePublishedPost json parse fail!!");
                            readPostResponseErrorAction();
                        }
                        if (editText.getText().length() > 0)
                            postBtn.setEnabled(true);
                        progressDialog.dismiss();
                    }
                }

        ).executeAsync();
    }

    private void readPostResponseErrorAction(){
        pageInfoList.clear();
        postLayout.removeAllViews();
        showAlert(MainActivity.this, getString(R.string.error), getString(R.string.error), getString(R.string.ok));
    }

    private void makePostUI(){
        Collections.sort(postInfoList, new PostInfoDescCompare());
        for(PostInfo postInfo : postInfoList){
            LinearLayout parent = new LinearLayout(this);
            parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            parent.setOrientation(LinearLayout.VERTICAL);

            if (postInfo.getMessage() != null && !postInfo.getMessage().isEmpty()) {
                TextView textView = new TextView(this);
                textView.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
                textView.setText(postInfo.getMessage());
//                textView.setPadding(20, 20, 20, 20);
                textView.setTextColor(0xFF000000);
                if (!postInfo.isPublished())
                    textView.setBackgroundColor(0xFFABABAB);
                parent.addView(textView);
            }

            if (postInfo.getPicture() != null && !postInfo.getPicture().isEmpty()){
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                new ImageDownLoader().execute(imageView, postInfo.getPicture());
                parent.addView(imageView);
            }

            if (postInfo.getLink() != null && !postInfo.getLink().isEmpty()){
                WebView webView = new WebView(this);
                webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl(postInfo.getLink());
                parent.addView(webView);
            }

            parent.setBackgroundResource(R.drawable.image_border);
            postLayout.addView(parent);
        }
    }

    static class PostInfoDescCompare implements Comparator<PostInfo> {
        @Override
        public int compare(PostInfo arg0, PostInfo arg1) {
            // TODO Auto-generated method stub
            return arg1.getCreateTime().compareTo(arg0.getCreateTime());
        }

    }

    private boolean handlePostInfoResponse(GraphResponse response){
        try {
            JSONObject json = response.getJSONObject();
            JSONArray data = (JSONArray) json.get("data");
            for(int i = 0; i < data.length(); i++){
                PostInfo postinfo = new PostInfo();
                JSONObject dataJson = (JSONObject) data.get(i);
                if (!(dataJson.has("message") || dataJson.has("link")))
                    continue;

                if (dataJson.has("message"))
                    postinfo.setMessage(dataJson.getString("message"));

                if (dataJson.has("link"))
                    postinfo.setLink(dataJson.getString("link"));

                if (dataJson.has("picture"))
                    postinfo.setPicture(dataJson.getString("picture"));

                postinfo.setCreateTime(dataJson.getString("created_time"));
                postinfo.setId(dataJson.getString("id"));
                postinfo.setIsPublished(dataJson.getBoolean("is_published"));
                postInfoList.add(postinfo);
            }
        }
        catch (JSONException e){
            return false;
        }
        return true;
    }

    private void getPageAccessToken(int pageIdx) {
        final ProgressDialog progressDialog = makeProgressDialog();
        progressDialog.show();

        Bundle params = new Bundle();
        params.putString("fields", "access_token");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                pageInfoList.get(pageIdx).getId(),
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        Log.d(APP_NAME, "getPageAccessToken response = " + response.toString());
                        progressDialog.dismiss();
                    }
                }
        ).executeAsync();
    }

    private ProgressDialog makeProgressDialog() {
        return ProgressDialogSingleton.getInstance().getProgressDialog(this);
    }

    private void ShowDatePickernMakeTime(final int year, final int monthOfYear, final int dayOfMonth){
        Calendar canlendar = new GregorianCalendar();
        canlendar.add(Calendar.MINUTE, 11);
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (!view.isShown())
                    return;

                String selectedTimeStr = String.format("%d-%02d-%02d %02d:%02d:00.000000000", year, monthOfYear + 1, dayOfMonth, hourOfDay, minute);
                Log.d(APP_NAME, "selected time = " + selectedTimeStr);

                Timestamp timestamp = Timestamp.valueOf(selectedTimeStr);

                postInPage(editText.getText().toString(), false, selectedPageIdx, timestamp);
            }
        }, canlendar.get(Calendar.HOUR_OF_DAY), canlendar.get(Calendar.MINUTE), false);
        dialog.show();
    }

    private void makeTypeSelSpinner(){
        List typeNames = Arrays.asList(typeListStr);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, typeNames);

        typeSelSpinner.setAdapter(adapter);
        typeSelSpinner.setOnItemSelectedListener(this);
        typeSelSpinner.setSelected(false);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int prevselectedType = selectedType;
        selectedType = position;
        Log.d(APP_NAME, "onItemSelected position = " + position);
        if (selectedType == 0)
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        else {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
//            if (selectedType == 2) {
//                final RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(editText.getWidth(),editText.getHeight() / 2);
//                editText.setLayoutParams(param);
//            }
        }

//        if (prevselectedType == 2) {
//            final RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(editText.getWidth(),editText.getHeight() * 2);
//            editText.setLayoutParams(param);
//        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void clearAction(){
        pageInfoList.clear();
        postInfoList.clear();
        postLayout.removeAllViews();
        selectedPageIdx = -1;
        postBtn.setEnabled(false);
        postTextView.setText("");
        if (pageNameAdapter != null)
            pageNameAdapter.clear();
    }
}


