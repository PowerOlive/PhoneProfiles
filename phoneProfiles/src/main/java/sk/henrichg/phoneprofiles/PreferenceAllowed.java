package sk.henrichg.phoneprofiles;

import android.content.Context;

class PreferenceAllowed {
    int allowed;
    int notAllowedReason;
    String notAllowedReasonDetail;

    static final int PREFERENCE_NOT_ALLOWED = 0;
    static final int PREFERENCE_ALLOWED = 1;
    static final int PREFERENCE_NOT_ALLOWED_NO_HARDWARE = 1;
    static final int PREFERENCE_NOT_ALLOWED_NOT_ROOTED = 2;
    static final int PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND = 3;
    static final int PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND = 4;
    static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM = 5;
    private static final int PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS = 6;
    static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION = 7;
    private static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION = 8;
    static final int PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION = 9;

    String getNotAllowedPreferenceReasonString(Context context) {
        switch (notAllowedReason) {
            case PREFERENCE_NOT_ALLOWED_NO_HARDWARE: return context.getString(R.string.preference_not_allowed_reason_no_hardware);
            case PREFERENCE_NOT_ALLOWED_NOT_ROOTED: return context.getString(R.string.preference_not_allowed_reason_not_rooted);
            case PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND: return context.getString(R.string.preference_not_allowed_reason_settings_not_found);
            case PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND: return context.getString(R.string.preference_not_allowed_reason_service_not_found);
            case PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS: return context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM:
                return context.getString(R.string.preference_not_allowed_reason_not_supported) + " (" + notAllowedReasonDetail + ")";
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION:
                return context.getString(R.string.preference_not_allowed_reason_not_supported_by_application) + " (" + notAllowedReasonDetail + ")";
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION:
                return context.getString(R.string.preference_not_allowed_reason_not_supported_android_version) + " (" + notAllowedReasonDetail + ")";
            case PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION: return context.getString(R.string.preference_not_allowed_reason_not_granted_g1_permission);
            default: return context.getString(R.string.empty_string);
        }
    }

}
