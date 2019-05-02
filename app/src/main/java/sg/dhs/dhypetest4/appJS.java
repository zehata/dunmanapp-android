package sg.dhs.dhypetest4;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.webkit.JavascriptInterface;

import static android.view.Menu.NONE;
import static sg.dhs.dhypetest4.MainActivity.drawerList;
import static sg.dhs.dhypetest4.MainActivity.homeWebView;

public class appJS {
    Context context;
    Activity activity;

    appJS(Context c, Activity a) {
        context = c;
        activity = a;
    }

    @JavascriptInterface
    public void requestURLChange(final String htmlname){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    homeWebView.evaluateJavascript("gotopage(\""+htmlname+"\");", null);
                } else {
                    homeWebView.loadUrl("javascript:gotopage(\""+htmlname+"\");");
                }
            }
        });
    }
}
