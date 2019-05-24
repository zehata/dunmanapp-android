package sg.dhs.dhypetest4;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import static android.view.Menu.NONE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    static WebView homeWebView;
    static WebView appWebView;
    static NavigationView navigationView;
    static StartGSO startGSO;
    static signOut signOut;
    static DrawerLayout mDrawerLayout;
    static boolean inter;
    static int topinset;

    static ArrayList<String> drawerList = new ArrayList<>();

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        100);
            }
        }

        topinset = getStatusBarHeight();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        homeWebView = (WebView) findViewById(R.id.homewebview);
        appWebView = (WebView) findViewById(R.id.appwebview);

        WebSettings homeWebSettings = homeWebView.getSettings();

        homeWebSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            homeWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            homeWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        homeWebSettings.setJavaScriptEnabled(true);
        homeWebSettings.setDomStorageEnabled(true);
        homeWebSettings.setAllowContentAccess(true);
        homeWebSettings.setMediaPlaybackRequiresUserGesture(false);

        WebSettings appWebSettings = appWebView.getSettings();

        appWebSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            appWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            appWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        appWebSettings.setJavaScriptEnabled(true);
        appWebSettings.setDomStorageEnabled(true);
        appWebSettings.setMediaPlaybackRequiresUserGesture(false);

        appWebView.setWebViewClient(new interceptedWebViewClient());
        appWebView.setWebChromeClient(new WebChromeClient() {
            // Grant permissions for cam
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }


        });

        if (savedInstanceState == null) {
            homeWebView.setWebViewClient(new WebViewClient(){});
            homeWebView.setWebChromeClient(new WebChromeClient() {
                // Grant permissions for cam
                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void run() {
                            request.grant(request.getResources());
                        }
                    });
                }


            });
            homeWebView.addJavascriptInterface(new JSObject(this, this), "Android");
            homeWebView.loadUrl("file:///android_asset/splash.html");
            mDrawerLayout = findViewById(R.id.drawer_layout);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        inter = isNetworkAvailable();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onStart(){
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account == null){
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            startGSO = new StartGSO();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        appWebView = null;
        homeWebView = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState ){
        super.onSaveInstanceState(savedInstanceState);
        Bundle appWebViewBundle = new Bundle();
        Bundle homeWebViewBundle = new Bundle();
        appWebView.saveState(appWebViewBundle);
        homeWebView.saveState(homeWebViewBundle);
        savedInstanceState.putBundle("app",appWebViewBundle);
        savedInstanceState.putBundle("home",homeWebViewBundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Bundle appWebViewBundle = savedInstanceState.getBundle("app");
        Bundle homeWebViewBundle = savedInstanceState.getBundle("home");
        appWebView.restoreState(appWebViewBundle);
        homeWebView.restoreState(homeWebViewBundle);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            homeWebView.evaluateJavascript("refreshapplist();", null);
        } else {
            homeWebView.loadUrl("javascript:refreshapplist();");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        if(item.getTitle().equals("Home")){
            gohome();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                homeWebView.evaluateJavascript("goto("+item.getItemId()+");", null);
            } else {
                homeWebView.loadUrl("javascript:goto("+item.getItemId()+");");
            }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        int athome = homeWebView.getVisibility();
        if ((keyCode == KeyEvent.KEYCODE_BACK) && appWebView.canGoBack()) {
            appWebView.goBack();
            return true;
        } else if((keyCode == KeyEvent.KEYCODE_BACK) && athome != View.VISIBLE) {
            gohome();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 0) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String email = account.getEmail();
            String displayname = account.getDisplayName();
            // Signed in successfully, show authenticated UI.
            if(email.endsWith("@dhs.sg")){
                signInSuccess(email,displayname);
            } else {
                signInFailed();
            }

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            System.out.println(e);
        }
    }

    public void gohome(){
        homeWebView.setVisibility(View.VISIBLE);
        appWebView.loadUrl("blank.html");
        appWebView.clearCache(true);
        appWebView.clearHistory();
    }

    private class interceptedWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                homeWebView.evaluateJavascript("gotopage(\""+url+"\");", null);
            } else {
                homeWebView.loadUrl("javascript:gotopage(\""+url+"\");");
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            return true;
        }
    }

    public class StartGSO{
        public void signIn() {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 0);
        }
    }

    public void signInSuccess(String email, String displayName){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            homeWebView.evaluateJavascript("success(\""+email+"\",\""+displayName+"\");", null);
        } else {
            homeWebView.loadUrl("javascript:success(\""+email+"\",\""+displayName+"\");");
        }
    }

    public void signInFailed(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            homeWebView.evaluateJavascript("failed();", null);
        } else {
            homeWebView.loadUrl("javascript:failed();");
        }
        GoogleSignOut();
    }

    public void GoogleSignOut(){
        mGoogleSignInClient.signOut();
    }

    public class signOut {
        public void signOut() {
            mGoogleSignInClient.signOut();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}