package moss.mystery.energymonitor.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.classifier.ClassifiedApp;
import moss.mystery.energymonitor.classifier.Classifier;

public class AppArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final ClassifiedApp[] apps;
    private final PackageManager pm;

    public AppArrayAdapter(Context context, ClassifiedApp[] apps){
        super(context, -1, apps);
        this.context = context;
        this.apps = apps;
        this.pm = context.getPackageManager();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolderItem viewHolder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.app_list_view, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.appName = convertView.findViewById(R.id.appName);
            viewHolder.classification = convertView.findViewById(R.id.classification);
            viewHolder.confidence = convertView.findViewById(R.id.confidence);
            viewHolder.image = convertView.findViewById(R.id.icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        ClassifiedApp app = apps[position];

        //Get appInfo
        ApplicationInfo ai = null;
        if(!app.unknownPackage) {
            try {
                ai = pm.getApplicationInfo(app.name, 0);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        //Flags for setting colours
        boolean greyedOut = false;
        boolean red = false;

        //Get app name
        if(ai != null) {
            viewHolder.appName.setText(pm.getApplicationLabel(ai));
            viewHolder.image.setImageDrawable(pm.getApplicationIcon(ai));
        } else {
            viewHolder.appName.setText(app.name);
            viewHolder.appName.setTextColor(ContextCompat.getColor(context, R.color.grey));
        }

        //Set classification text
        StringBuilder text = new StringBuilder();
        switch(app.classification){
            case Classifier.HIGH:
                text.append(context.getString(R.string.high_drain));
                red = true;
                break;
            case Classifier.MEDIUM:
                text.append(context.getString(R.string.medium_drain));
                break;
            case Classifier.LOW:
                text.append(context.getString(R.string.low_drain));
        }
        if(app.network){
            text.append(context.getString(R.string.when_network));
        }
        viewHolder.classification.setText(text);

        //Set confidence text
        switch(app.confidence){
            case Classifier.HIGH:
                viewHolder.confidence.setText(context.getString(R.string.high_confidence));
                break;
            case Classifier.MEDIUM:
                viewHolder.confidence.setText(context.getString(R.string.medium_confidence));
                break;
            case Classifier.LOW:
                viewHolder.confidence.setText(context.getString(R.string.low_confidence));
                greyedOut = true;
        }

        //Set colours
        if(greyedOut){
            viewHolder.appName.setTextColor(ContextCompat.getColor(context, R.color.grey));
            if(red){
                viewHolder.classification.setTextColor(ContextCompat.getColor(context, R.color.paleRed));
            } else {
                viewHolder.classification.setTextColor(ContextCompat.getColor(context, R.color.grey));
            }
        } else {
            viewHolder.classification.setTextColor(ContextCompat.getColor(context, R.color.red));
        }

        return convertView;
    }

    private static class ViewHolderItem {
        TextView appName;
        TextView classification;
        TextView confidence;
        ImageView image;
    }
}
