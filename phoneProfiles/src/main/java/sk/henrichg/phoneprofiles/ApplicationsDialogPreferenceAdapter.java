package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;

class ApplicationsDialogPreferenceAdapter extends RecyclerView.Adapter<ApplicationsDialogPreferenceViewHolder>
                                            implements ItemTouchHelperAdapter
{
    private Context context;

    private ApplicationsDialogPreference preference;

    private final OnStartDragItemListener mDragStartListener;

    ApplicationsDialogPreferenceAdapter(Context context, ApplicationsDialogPreference preference,
                                        OnStartDragItemListener dragStartListener)
    {
        this.context = context;
        this.preference = preference;
        this.mDragStartListener = dragStartListener;
    }

    @Override
    public ApplicationsDialogPreferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.applications_preference_list_item, parent, false);
        return new ApplicationsDialogPreferenceViewHolder(view, context, preference);
    }

    @Override
    public void onBindViewHolder(final ApplicationsDialogPreferenceViewHolder holder, int position) {
        Application application = preference.applicationsList.get(position);
        holder.bindApplication(application);

        holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return preference.applicationsList.size();
    }

    @Override
    public void onItemDismiss(int position) {
        preference.applicationsList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (preference.applicationsList == null)
            return false;

        Log.d("----- ApplicationsDialogPreferenceAdapter.onItemMove", "fromPosition="+fromPosition);
        Log.d("----- ApplicationsDialogPreferenceAdapter.onItemMove", "toPosition="+toPosition);

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(preference.applicationsList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(preference.applicationsList, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

}