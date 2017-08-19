package moss.mystery.energymonitor.apps;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Keeps track of which applications have been active and when
 */

public class AppHandler {
    private HashMap<String, App> apps;
    private Context appContext;

    public AppHandler(Context appContext){
        this.apps = new HashMap<>();
        this.appContext = appContext;
    }

    public App getApp(String name){
        //Find ApplicationInfo associated with this name
        PackageManager pm = appContext.getPackageManager();
        ApplicationInfo ai = getApplicationInfo(name, pm);
        String label;

        //TODO: Special handling - want to record cmdline somewhere!
        if(ai == null){
            label = "Unknown";
        } else {
            label = (String) pm.getApplicationLabel(ai);
        }

        App app = apps.get(label);
        if(app == null){
            app = new App(label);
            apps.put(label, app);
        }
        return app;
    }

    //Return a list of apps which were active in the current sample, and reset all activities to 0
    public App[] startNewSample(){
        ArrayList<App> activeApps = new ArrayList<>();

        for(String key : apps.keySet()){
            App app = apps.get(key);
            if(app.ticks > 0){
                activeApps.add(app);
                app.ticks = 0;
            }
        }

        return activeApps.toArray(new App[0]);
    }


    //TODO: Maybe time as well??
    //Wait, is this needed? What for? Shouldn't be...
    public void addTicks(String name, Long ticks){
        App app = apps.get(name);

        if(app == null){
            //TODO: Can this happen? What do?
        } else {
            app.addTicks(ticks);
        }
    }

    //Query PackageManager to get the label associated with a given /proc/[pid]/cmdline
    private ApplicationInfo getApplicationInfo(String name, PackageManager pm) {
        ApplicationInfo ai;
        ArrayList<String> testStrings = new ArrayList<>();

        //If name extracted from /proc/[pid]/cmdline contains and '/'s, split on them
        if (name.contains("/")) {
            String[] slashSplit = name.split("[/]");
            //Assuming name is of the form 'x.y.[...z]', so look for '.'s
            for (String str : slashSplit) {
                if (str.contains(".")) {
                    testStrings.add(str);
                }
            }
            //If no substrings containing '.'s are found, just try the last substring
            if (testStrings.size() == 0) {
                testStrings.add(slashSplit[slashSplit.length - 1]);
            }
        } else {
            testStrings.add(name);
        }

        //Now have one (or potentially multiple) target strings to test. May be of forms:
        //"x", "x.y...", or "x.y:z", where ':' could be any special char
        //Want to try trimming anything following a special char from the end of the string, request
        //appInfo from result. Repeat until either an app is found or we run out of things to trim
        for (String str : testStrings) {
            //Try this string as the package name
            try {
                ai = pm.getApplicationInfo(str, 0);
                return ai;
            } catch (final PackageManager.NameNotFoundException ignored) {
            }

            int i = str.length() - 1;
            char c;
            while (i > 0) {
                //Find position of last non-char symbol in the string
                c = str.charAt(i);
                if (!Character.isLetter(c)) {
                    //Remove everything from this char onwards, use result as package name
                    try {
                        ai = pm.getApplicationInfo(str.substring(0, i), 0);
                        return ai;
                    } catch (final PackageManager.NameNotFoundException ignored) {
                    }
                }
                --i;
            }
        }
        return null;
    }
}