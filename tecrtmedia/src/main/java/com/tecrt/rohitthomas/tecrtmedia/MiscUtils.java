package com.tecrt.rohitthomas.tecrtmedia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some handy utilities.
 */
public class MiscUtils {
    private static final String TAG = "MiscUtils";

    private MiscUtils() {}

    /**
     * Obtains a list of files that live in the specified directory and match the glob pattern.
     */
    public static String[] getFiles(File dir, String glob) {
        String regex = globToRegex(glob);
        final Pattern pattern = Pattern.compile(regex);
        String[] result = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Matcher matcher = pattern.matcher(name);
                return matcher.matches();
            }
        });
        Arrays.sort(result);

        return result;
    }

    /**
     * Converts a filename globbing pattern to a regular expression.
     * <p>
     * The regex is suitable for use by Matcher.matches(), which matches the entire string, so
     * we don't specify leading '^' or trailing '$'.
     */
    private static String globToRegex(String glob) {
        // Quick, overly-simplistic implementation -- just want to handle something simple
        // like "*.mp4".
        //
        // See e.g. http://stackoverflow.com/questions/1247772/ for a more thorough treatment.
        StringBuilder regex = new StringBuilder(glob.length());
        //regex.append('^');
        for (char ch : glob.toCharArray()) {
            switch (ch) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append('.');
                    break;
                case '.':
                    regex.append("\\.");
                    break;
                default:
                    regex.append(ch);
                    break;
            }
        }
        //regex.append('$');
        return regex.toString();
    }

    /**
     * Obtains the approximate refresh time, in nanoseconds, of the default display associated
     * with the activity.
     * <p>
     * The actual refresh rate can vary slightly (e.g. 58-62fps on a 60fps device).
     */
    public static long getDisplayRefreshNsec(Activity activity) {
        Display display = ((WindowManager)
                activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        double displayFps = display.getRefreshRate();
        long refreshNs = Math.round(1000000000L / displayFps);
        Log.d(TAG, "refresh rate is " + displayFps + " fps --> " + refreshNs + " ns");
        return refreshNs;
    }

    /**
     * create video folder and file to save to.
     * <p>
     *  We create the folder in the gallery if it doesnt exist and create a empty mp4 file wrapper
     *  to hold the data
     *  We can also store the file in the app folder
     */
    public static File createVideoFolder(String FolderName) {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File mFolder = new File(movieFile, FolderName);
        if(!mFolder.exists()) {
            mFolder.mkdirs();
        }
        return mFolder;
    }
    public static File createVideoFileName(File mFolder, String FileName, String extension) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "fbo-gl-recording";//"VIDEO_" + timestamp + "_";

        //Create Temp file in Gallery
        //File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);

        //Creat file in Gallery
        File videoFile = new File(mFolder.getAbsolutePath(), FileName+extension);

        //Create file in app folder
        // File videoFile = new File(getFilesDir(), "fbo-gl-recording.mp4");

        return videoFile;//videoFile;
    }

    public static void BroadcastGallery(File out, Context view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(out); //out is your file you saved/deleted/moved/copied
            mediaScanIntent.setData(contentUri);
            view.sendBroadcast(mediaScanIntent);
        } else {
            view.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }
    }


}
