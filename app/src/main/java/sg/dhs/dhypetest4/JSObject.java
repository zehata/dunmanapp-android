package sg.dhs.dhypetest4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.View;
import android.webkit.JavascriptInterface;

import static android.view.Menu.NONE;
import static sg.dhs.dhypetest4.MainActivity.drawerList;
import static sg.dhs.dhypetest4.MainActivity.homeWebView;

public class JSObject {
    Context context;
    Activity activity;

    JSObject(Context c, Activity a) {
        context = c;
        activity = a;
    }

    @JavascriptInterface
    public void addtodrawerlist(final String name) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawerList.add(name);
            }
        });
    }

    @JavascriptInterface
    public void clearlist() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawerList.clear();
                drawerList.add("Home");
            }
        });
    }

    @JavascriptInterface
    public void consolelog(String str) {
        System.out.println(str);
    }

    @JavascriptInterface
    public void populatedrawer() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Menu menu = MainActivity.navigationView.getMenu();
                menu.clear();
                for (int i = 0; i < drawerList.size(); i++) {
                    menu.add(NONE, i, i + 1, drawerList.get(i));
                }
            }
        });
    }

    @JavascriptInterface
    public void goTo(final String html) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.appWebView.clearCache(true);
                MainActivity.appWebView.addJavascriptInterface(new appJS(context, activity), "Android");
                MainActivity.appWebView.loadDataWithBaseURL("file:///android_asset/blank.html", html, "text/html", "UTF-8", null);
                homeWebView.setVisibility(View.GONE);
            }
        });
    }

    @JavascriptInterface
    public void doURLChange(final String html){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.appWebView.clearCache(true);
                MainActivity.appWebView.getSettings().setDomStorageEnabled(true);
                MainActivity.appWebView.loadDataWithBaseURL("file:///android_asset/blank.html", html, "text/html", "UTF-8", "app.html");
            }
        });
    }

    @JavascriptInterface
    public void gso(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.startGSO.signIn();
            }
        });
    }

    @JavascriptInterface
    public void signinsuccess(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                homeWebView.loadUrl("file:///android_asset/index.html");
            }
        });
    }

    @JavascriptInterface
    public void signout(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.signOut.signOut();
            }
        });
    }

    @JavascriptInterface
    public void enabledrawer(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });
    }

    @JavascriptInterface
    public boolean checkint(){
        return MainActivity.inter;
    }

    @JavascriptInterface
    public void openDrawer(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }
}

