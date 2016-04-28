package sk.henrichg.phoneprofiles;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationViewHolder {

    public ImageView imageViewIcon;
    public TextView textViewAppName;
    public CheckBox checkBox;
    public TextView textViewAppType;

    public ApplicationViewHolder() {
    }

    public ApplicationViewHolder(ImageView imageViewIcon, TextView textViewAppName,
                                 TextView textViewAppType, CheckBox checkBox)
    {
        this.imageViewIcon = imageViewIcon;
        this.textViewAppName = textViewAppName;
        this.checkBox = checkBox;
        this.textViewAppType = textViewAppType;
    }

}
