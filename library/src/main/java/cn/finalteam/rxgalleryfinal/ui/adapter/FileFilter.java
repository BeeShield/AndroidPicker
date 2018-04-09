package cn.finalteam.rxgalleryfinal.ui.adapter;

import android.support.v4.app.FragmentActivity;
import android.widget.ProgressBar;

import cn.finalteam.rxgalleryfinal.bean.NormalFile;
import cn.finalteam.rxgalleryfinal.ui.activity.NormalFilePickActivity;

import static cn.finalteam.rxgalleryfinal.ui.adapter.FileLoaderCallbacks.TYPE_FILE;

/**
 * Created by Vincent Woo
 * Date: 2016/10/11
 * Time: 10:19
 */

public class FileFilter {
    public static void getFiles(NormalFilePickActivity activity, FilterResultCallback callback, NormalFilePickAdapter adapter, String[] suffix) {
        activity.getSupportLoaderManager().initLoader(3, null, new FileLoaderCallbacks(activity, callback, TYPE_FILE, suffix, adapter));
    }
}
