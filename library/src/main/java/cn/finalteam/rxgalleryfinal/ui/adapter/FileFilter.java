package cn.finalteam.rxgalleryfinal.ui.adapter;

import android.support.v4.app.FragmentActivity;

import cn.finalteam.rxgalleryfinal.bean.NormalFile;

import static cn.finalteam.rxgalleryfinal.ui.adapter.FileLoaderCallbacks.TYPE_FILE;

/**
 * Created by Vincent Woo
 * Date: 2016/10/11
 * Time: 10:19
 */

public class FileFilter {
    public static void getFiles(FragmentActivity activity,
                                FilterResultCallback<NormalFile> callback, String[] suffix) {
        activity.getSupportLoaderManager().initLoader(3, null,
                new FileLoaderCallbacks(activity, callback, TYPE_FILE, suffix));
    }
}
