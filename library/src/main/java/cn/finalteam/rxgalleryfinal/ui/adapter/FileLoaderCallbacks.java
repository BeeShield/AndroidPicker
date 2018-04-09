package cn.finalteam.rxgalleryfinal.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.finalteam.rxgalleryfinal.bean.Directory;
import cn.finalteam.rxgalleryfinal.bean.NormalFile;
import cn.finalteam.rxgalleryfinal.ui.activity.NormalFilePickActivity;
import cn.finalteam.rxgalleryfinal.utils.Util;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Files.FileColumns.MIME_TYPE;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

/**
 * Created by Vincent Woo
 * Date: 2016/10/11
 * Time: 11:04
 */

public class FileLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "FileLoaderCallbacks";
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_AUDIO = 2;
    public static final int TYPE_FILE = 3;

    private WeakReference<Context> context;
    private FilterResultCallback resultCallback;

    private int mType = TYPE_IMAGE;
    private String[] mSuffixArgs;
    private CursorLoader mLoader;
    private String mSuffixRegex;
    private NormalFilePickActivity activity;
    private NormalFilePickAdapter adapter;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 2) {
                NormalFile file = (NormalFile) msg.getData().getSerializable("file");
                adapter.add(file);
            }
        }
    };

    public FileLoaderCallbacks(NormalFilePickActivity context, FilterResultCallback resultCallback, int type) {
        this(context, resultCallback, type, null, null);
    }

    public FileLoaderCallbacks(NormalFilePickActivity context, FilterResultCallback resultCallback,
                               int type, String[] suffixArgs, NormalFilePickAdapter adapter) {
        this.context = new WeakReference<>(context);
        this.activity = context;
        this.resultCallback = resultCallback;
        this.mType = type;
        this.adapter = adapter;
        this.mSuffixArgs = suffixArgs;
        if (suffixArgs != null && suffixArgs.length > 0) {
            mSuffixRegex = obtainSuffixRegex(suffixArgs);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (mType) {
//            case TYPE_IMAGE:
//                mLoader = new ImageLoader(context.get());
//                break;
//            case TYPE_VIDEO:
//                mLoader = new VideoLoader(context.get());
//                break;
//            case TYPE_AUDIO:
//                mLoader = new AudioLoader(context.get());
//                break;
            case TYPE_FILE:
                mLoader = new FileLoader(context.get());
                break;
        }

        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) return;
        switch (mType) {
//            case TYPE_IMAGE:
//                onImageResult(data);
//                break;
//            case TYPE_VIDEO:
//                onVideoResult(data);
//                break;
//            case TYPE_AUDIO:
//                onAudioResult(data);
//                break;
            case TYPE_FILE:
                onFileResult(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @SuppressWarnings("unchecked")
    private void onFileResult(Cursor data) {
        List<Directory<NormalFile>> directories = new ArrayList<>();
        if (data.getPosition() != -1) {
            data.moveToPosition(-1);
        }
        findHideVoice(adapter);
        //增加一个线程
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (data.moveToNext()) {
                    if (activity.isFinishing()) {
                        break;
                    }
                    String path = data.getString(data.getColumnIndexOrThrow(DATA));

                    if (path != null && contains(path)) {

                        //Create a File instance
                        NormalFile file = new NormalFile();
                        file.setId(data.getLong(data.getColumnIndexOrThrow(_ID)));
                        file.setName(data.getString(data.getColumnIndexOrThrow(TITLE)));
                        file.setPath(data.getString(data.getColumnIndexOrThrow(DATA)));
                        file.setSize(data.getLong(data.getColumnIndexOrThrow(SIZE)));
                        file.setDate(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));

                        file.setMimeType(data.getString(data.getColumnIndexOrThrow(MIME_TYPE)));
                        Message message = new Message();
                        message.what = 2;
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("file", file);
                        message.setData(bundle);
                        handler.sendMessage(message);
//                if (resultCallback != null) {
//                    resultCallback.onResult(directories);
//                }
//        }
//                Create a Directory
//                Directory<NormalFile> directory = new Directory<>();
//                directory.setName(Util.extractFileNameWithSuffix(Util.extractPathWithoutSeparator(file.getPath())));
//                directory.setPath(Util.extractPathWithoutSeparator(file.getPath()));

//                if (!directories.contains(directory)) {
//                    directory.addFile(file);
//                    directories.add(directory);
//                } else {
//                    directories.get(directories.indexOf(directory)).addFile(file);
//                }
                    }
                }
            }
        }).start();

//        if (resultCallback != null) {
//            resultCallback.onResult(directories);
//        }
    }


    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    private String saveBitmap(Bitmap bitmap, String pathName) {
        if (bitmap == null) {
            return "";
        }

        String path = "";
//        String pathName = context.get().getExternalCacheDir().getAbsolutePath() + "/" + String.valueOf(System.currentTimeMillis()) + ".png";
        File f = new File(pathName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            path = pathName;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return path;
    }

    private boolean contains(String path) {
        String name = Util.extractFileNameWithSuffix(path);
        Pattern pattern = Pattern.compile(mSuffixRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    private String obtainSuffixRegex(String[] suffixes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < suffixes.length; i++) {
            if (i == 0) {
                builder.append(suffixes[i].replace(".", ""));
            } else {
                builder.append("|\\.");
                builder.append(suffixes[i].replace(".", ""));
            }
        }
        return ".+(\\." + builder.toString() + ")$";
    }

    /**
     * 手动添加被隐藏的文件
     *
     * @param adapter
     */
    private void findHideVoice(NormalFilePickAdapter adapter) {
        List<String> pathNameList = new ArrayList<>();
        //QQ
        pathNameList.add("/storage/emulated/0/tencent/QQfile_recv");
        //钉钉
        pathNameList.add("/storage/emulated/0/DingTalk");
        //微信
        pathNameList.add("/storage/emulated/0/tencent/MicroMsg/Download");
        for (String path : pathNameList) {
            File[] files = new File(path).listFiles();
            if (files == null || files.length == 0) continue;//未下载钉钉微信或者QQ无法扫描出文件,跳出本次扫描循环
            for (File file : files) {
                String fileName = file.getName();
                if (TextUtils.isEmpty(fileName) || !fileName.contains(".")) continue;
                String suffix = "." + fileName.split("\\.")[1];
                if (!Arrays.asList(mSuffixArgs).contains(suffix)) continue;
                NormalFile normalFile = new NormalFile();
                normalFile.setName(fileName);
                normalFile.setDate(file.lastModified());
                normalFile.setPath(file.getPath());
                adapter.add(normalFile);
                Log.e(TAG, "文件名===" + file.getName() + "\n文件路径=====" + file.getPath());
            }
        }
    }

    /**
     * 排序
     *
     * @param fileList
     */
    private void sortList(List<NormalFile> fileList) {
        //1.去重
        HashSet<NormalFile> h = new HashSet<>(fileList);
        fileList.clear();
        fileList.addAll(h);
        //2.排序
        Collections.sort(fileList, new FileComparator());

    }

    private class FileComparator implements Comparator<NormalFile> {

        @Override
        public int compare(NormalFile o1, NormalFile o2) {
            if (o1.getDate() == o2.getDate()) {
                return 0;
            }
            return (o1.getDate() < o2.getDate()) ? 1 : -1;
        }
    }
}
