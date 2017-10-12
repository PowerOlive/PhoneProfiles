package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

class HandlerWithContext extends Handler {

    // A weak reference to the enclosing context
    private final WeakReference<Context> mContext;

    HandlerWithContext (Looper looper, Context context) {
        super(looper);
        mContext = new WeakReference<>(context);
    }

    public Context getContext() {
        return mContext.get();
    }
}
