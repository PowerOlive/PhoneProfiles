package sk.henrichg.phoneprofiles;

import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;

class RootToolsSmall {

    private static final HashMap<String, Boolean> BIN_MAP = new HashMap<>();

    static boolean hasBinary(String binaryName) {
        Boolean exists = BIN_MAP.get(binaryName);
        if (exists != null) {
            return exists;
        }
        exists = false;
        try {
            String path = System.getenv("PATH");
            if (!TextUtils.isEmpty(path)) {
                for (String p : path.split(":")) {
                    if (new File(p + binaryName).exists()) {
                        exists = true;
                        break;
                    }
                }
            }
        } catch (Throwable e) {
            //Debug.log(e);
        }
        BIN_MAP.put(binaryName, exists);
        return exists;
    }

    public static boolean isRooted() {
        return hasBinary("/su");
    }

    static boolean hasSettingBin() {
        return hasBinary("/settings");
    }

    static boolean hasServiceBin() {
        return hasBinary("/service");
    }

    /*private static boolean runSuCommand(String command) {
        if (!isRooted()) {
            return false;
        }

        Process proc = null;
        OutputStreamWriter osw = null;

        try { // Run Script
            proc = new ProcessBuilder("su").redirectErrorStream(true).start();
            osw = new OutputStreamWriter(proc.getOutputStream(), "UTF-8");
            osw.write(command);
            osw.flush();
            osw.close();
        } catch (IOException ignored) {
            //Debug.log(ex);
            //ex.printStackTrace();
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException ignored) {
                    //Debug.log(e);
                    //e.printStackTrace();
                }
            }
        }
        try {
            if (proc != null) {
                proc.waitFor();
                proc.destroy();
            } else {
                return false;
            }
        } catch (InterruptedException ignored) {
            //Debug.log(e);
            //e.printStackTrace();
            return false;
        }
        return proc.exitValue() == 0;
    }*/

    /*public static boolean runJavaCommand(Class<?> mainClass, String name, Context context, Object cmdParam) {
    try {
      String cmd = "export CLASSPATH=" +
              context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.sourceDir +
              "\nexec app_process $base/bin " + mainClass.getName() + " " + cmdParam + " \"$@\"\n";
      
      FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE);
      fos.write(cmd.getBytes());
      fos.close();

      File file = context.getFileStreamPath(name);
      file.setExecutable(true);
      return RootToolsSmall.runSuCommand(file.getAbsolutePath());
    } catch (Exception ignored) {
      //Debug.log(e);
      //  e.printStackTrace();
    }
      return false;
    }*/
}
