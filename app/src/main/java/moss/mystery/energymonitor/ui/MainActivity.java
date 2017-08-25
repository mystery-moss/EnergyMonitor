package moss.mystery.energymonitor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import moss.mystery.energymonitor.ApplicationGlobals;
import moss.mystery.energymonitor.FileParsing;
import moss.mystery.energymonitor.MainService;
import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.classifier.Classifier;
import moss.mystery.energymonitor.processes.ProcessHandler;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG = "MainActivity";
    private static final int NUM_THRESHOLD = 4;
    private ApplicationGlobals globals;

    @Override
    //Perform checks and setup
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        globals = ApplicationGlobals.get(getApplicationContext());

        //Check permissions
        if(ProcessHandler.noReadPermission()){
            //TODO: Make this work properly in final version
            DialogFragment dialog = new ErrorDialog();
            dialog.show(getSupportFragmentManager(), "permission_error");
        }

        //Start service, update status text
        if(globals.serviceEnabled) {
            startService(new Intent(this, MainService.class));
        }
        updateMonitorText();


        TextView info = (TextView) findViewById(R.id.infoText);
        info.setVisibility(View.GONE);
//        info.setText(String.valueOf(globals.intervalHandler.numIntervals()));
//        info.setText(R.string.keep_running);
    }

    @Override
    protected void onStart() {
        super.onStart();
        populateAppList();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);

        //Set 'toggle monitor' text
        MenuItem toggle = menu.findItem(R.id.action_togglemonitor);
        if(globals.serviceEnabled) {
            toggle.setTitle(R.string.monitor_off);
        } else {
            toggle.setTitle(R.string.monitor_on);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                updateMonitorText();
                populateAppList();
                //Maybe do something to the page so it's clear it has actually refreshed - e.g. pop up a spinner for a second
                return true;
            case R.id.action_togglemonitor:
                //Set 'toggle monitor' menu and status text
                if(globals.serviceEnabled){
                    item.setTitle(R.string.monitor_on);
                    globals.serviceEnabled = false;
                    stopService(new Intent(this, MainService.class));
                } else {
                    item.setTitle(R.string.monitor_off);
                    globals.serviceEnabled = true;
                    startService(new Intent(this, MainService.class));
                }
                updateMonitorText();
                return true;
            case R.id.action_readfile:
                if(FileParsing.readFile(getApplicationContext(), globals.intervalHandler)){
                    Toast.makeText(getApplicationContext(), "File read", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error reading file", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_writefile:
                if(FileParsing.writeFile(getApplicationContext(), globals.intervalHandler)){
                    Toast.makeText(getApplicationContext(), "File written", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error writing file", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return true;
        }
    }

    //Call app classifier in a separate thread to avoid potential slowdowns
    private void populateAppList(){
        final Classifier classifier = new Classifier(globals.intervalHandler.getIntervals(), globals.intervalHandler.numIntervals());
        final Context context = this;
        final ProgressBar loading = (ProgressBar) findViewById(R.id.progressBar);

        new AsyncTask<Classifier, Void, Integer>() {
            @Override
            protected void onPreExecute(){
                //Show loading icon
                loading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Integer doInBackground(Classifier... param){
                //Classify apps
                return param[0].classify();
            }

            @Override
            protected void onPostExecute(Integer numClassified){
                ListView listView = (ListView) findViewById(R.id.app_list);
                TextView appText = (TextView) findViewById(R.id.appListText);
                TextView info = (TextView) findViewById(R.id.infoText);
                TextView heading = (TextView) findViewById(R.id.heading);

                //Populate list view
                if(numClassified > 0){
                    //Populate view with classified apps
                    appText.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    heading.setVisibility(View.VISIBLE);

                    AppArrayAdapter adapter = new AppArrayAdapter(context, classifier.getClassifiedApps(), false);
                    listView.setAdapter(adapter);

                    //If number of classified apps below threshold, show info text, else hide
                    if(numClassified <= NUM_THRESHOLD){
                        info.setVisibility(View.VISIBLE);
                    } else {
                        info.setVisibility(View.GONE);
                    }
                } else {
                    //Display message about lack of intervals
                    listView.setVisibility(View.GONE);
                    info.setVisibility(View.GONE);
                    heading.setVisibility(View.GONE);
                    appText.setVisibility(View.VISIBLE);
                }

                //Remove spinny thing
                loading.setVisibility(View.GONE);
            }
        }.execute(classifier);
    }

    private void updateMonitorText(){
        TextView enabled = (TextView) findViewById(R.id.monitorStatus);
        if(globals.serviceEnabled) {
            enabled.setText(R.string.monitor_running);
            enabled.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            enabled.setText(R.string.monitor_not_running);
            enabled.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }
}