package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import java.util.Date;

public abstract class PhoneCallReceiver extends BroadcastReceiver {

    private static TelephonyManager telephony;
    //The receiver will be recreated whenever android feels like it.
    //We need a static variable to remember data between instantiations
    private static PhoneCallStartEndDetector listener;
    //String outgoingSavedNumber;
    Context savedContext;


    @Override
    public void onReceive(Context context, Intent intent) {
        savedContext = context.getApplicationContext();

        if (onStartReceive())
        {
            if (telephony == null)
                telephony = (TelephonyManager)savedContext.getSystemService(Context.TELEPHONY_SERVICE);

            if(listener == null){
                listener = new PhoneCallStartEndDetector();
            }

            //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
            if ((intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                if (intent.getExtras() != null)
                    listener.setOutgoingNumber(intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER));
                else
                    listener.setOutgoingNumber("");
                return;
            }

            listener.onCallStateChanged(intent);

        }
        onEndReceive();
    }


    //Derived classes should override these to respond to specific events of interest
    protected abstract boolean onStartReceive();
    @SuppressWarnings("unused")
    protected abstract void onIncomingCallStarted(String number, Date start);
    @SuppressWarnings("unused")
    protected abstract void onOutgoingCallStarted(String number, Date start);
    @SuppressWarnings("unused")
    protected abstract void onOutgoingCallAnswered(String number, Date start);
    @SuppressWarnings("unused")
    protected abstract void onIncomingCallAnswered(String number, Date start);
    @SuppressWarnings("unused")
    protected abstract void onIncomingCallEnded(String number, Date start, Date end);
    @SuppressWarnings("unused")
    protected abstract void onOutgoingCallEnded(String number, Date start, Date end);
    @SuppressWarnings("unused")
    protected abstract void onMissedCall(String number, Date start);
    @SuppressWarnings("EmptyMethod")
    protected abstract void onEndReceive();

    //Deals with actual events
    private class PhoneCallStartEndDetector {
        int lastState = TelephonyManager.CALL_STATE_IDLE;
        Date callStartTime;
        boolean inCall;
        boolean isIncoming;
        String savedNumber;  //because the passed incoming is only valid in ringing

        PhoneCallStartEndDetector() {}

        //The outgoing number is only sent via a separate intent, so we need to store it out of band
        void setOutgoingNumber(String number){
            inCall = false;
            isIncoming = false;
            savedNumber = number;
            callStartTime = new Date();
            onOutgoingCallStarted(savedNumber, callStartTime);
        }

        //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
        void onCallStateChanged(Intent intent) {
            int state = telephony.getCallState();
            if(lastState == state){
                //No change, debounce extras
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    inCall = false;
                    isIncoming = true;
                    callStartTime = new Date();
                    String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    savedNumber = incomingNumber;
                    onIncomingCallStarted(incomingNumber, callStartTime);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->offhook are pickups of incoming calls.  Nothing down on them
                    if(lastState != TelephonyManager.CALL_STATE_RINGING){
                        inCall = true;
                        isIncoming = false;
                        callStartTime = new Date();
                        onOutgoingCallAnswered(savedNumber, callStartTime);
                    }
                    else
                    {
                        inCall = true;
                        isIncoming = true;
                        callStartTime = new Date();
                        onIncomingCallAnswered(savedNumber, callStartTime);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if(!inCall){
                        //Ring but no pickup-  a miss
                        onMissedCall(savedNumber, callStartTime);
                    }
                    else
                    {
                        if(isIncoming){
                            onIncomingCallEnded(savedNumber, callStartTime, new Date());
                        }
                        else{
                            onOutgoingCallEnded(savedNumber, callStartTime, new Date());
                        }
                        inCall = false;
                    }
                    break;
            }
            lastState = state;
        }

    }

}
