package sk.henrichg.phoneprofiles;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

class ApplicationViewHolder {

    final ImageView imageViewIcon;
    final TextView textViewAppName;
    final CheckBox checkBox;
    final TextView textViewAppType;

    ApplicationViewHolder(ImageView imageViewIcon, TextView textViewAppName,
                                 TextView textViewAppType, CheckBox checkBox)
    {
        this.imageViewIcon = imageViewIcon;
        this.textViewAppName = textViewAppName;
        this.checkBox = checkBox;
        this.textViewAppType = textViewAppType;
    }

}
