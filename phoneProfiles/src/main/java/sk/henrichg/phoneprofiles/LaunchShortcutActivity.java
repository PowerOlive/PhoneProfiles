package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class LaunchShortcutActivity extends Activity {

    public static final String EXTRA_PACKAGE_NAME = "packageName";
    public static final String EXTRA_ACTIVITY_NAME = "activityName";

    String packageName;
    String activityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        activityName = getIntent().getStringExtra(EXTRA_ACTIVITY_NAME);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        ComponentName componentName = new ComponentName(packageName, activityName);
        if (componentName != null) {
            //intent = new Intent(Intent.ACTION_MAIN);
            Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setComponent(componentName);
            try {
                startActivityForResult(intent, 100);
            } catch (Exception e) {
                System.out.println(e);
                finish();
            }
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 100) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    System.out.println(e);
                }

            }
        }

        finish();
    }

}
