package cn.finalteam.rxgalleryfinal.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.Directory;
import cn.finalteam.rxgalleryfinal.bean.NormalFile;
import cn.finalteam.rxgalleryfinal.rxbus.RxBus;
import cn.finalteam.rxgalleryfinal.rxbus.event.BaseResultEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.FileMultipleResultEvent;
import cn.finalteam.rxgalleryfinal.ui.adapter.DividerListItemDecoration;
import cn.finalteam.rxgalleryfinal.ui.adapter.FileFilter;
import cn.finalteam.rxgalleryfinal.ui.adapter.FilterResultCallback;
import cn.finalteam.rxgalleryfinal.ui.adapter.NormalFilePickAdapter;
import cn.finalteam.rxgalleryfinal.ui.adapter.OnSelectStateListener;
import cn.finalteam.rxgalleryfinal.utils.Constant;

/**
 * Created by Vincent Woo
 * Date: 2016/10/26
 * Time: 10:14
 */

public class NormalFilePickActivity extends BaseFileActivity {
    private static final String TAG = "NormalFilePickActivity";
    public static final int DEFAULT_MAX_NUMBER = 9;
    public static final String SUFFIX = "Suffix";
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private Toolbar mTbImagePickToolbar;
    private SuperRecyclerView mRecyclerView;
    private TextView titleTV;
    private NormalFilePickAdapter mAdapter;
    private ArrayList<NormalFile> mSelectedList = new ArrayList<>();
    private ProgressBar mProgressBar;
    private Button ensureButton;
    private List<NormalFile> normalFileList;
    private String[] mSuffix;
    private boolean isDayModel;

    @Override
    void permissionGranted() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 1000);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_file_pick);

        mMaxNumber = getIntent().getIntExtra(Constant.MAX_NUMBER, DEFAULT_MAX_NUMBER);
        mSuffix = getIntent().getStringArrayExtra(SUFFIX);

        isDayModel = getIntent().getBooleanExtra(Constant.IS_DAY_MODEL, false);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        titleTV = (TextView) findViewById(R.id.tv_title);
        ensureButton = (Button) findViewById(R.id.bt_ensure);
        titleTV.setText("请选择录音文件");
        mTbImagePickToolbar = (Toolbar) findViewById(R.id.tb_file_pick);
        setSupportActionBar(mTbImagePickToolbar);
        if (mCurrentNumber != 0) {
            ensureButton.setText(String.format("完成(%d/%d)", mCurrentNumber, mMaxNumber));
        }
        mTbImagePickToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ensureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //            Intent intent = new Intent();
//            intent.putParcelableArrayListExtra(Constant.RESULT_PICK_FILE, mSelectedList);
//            setResult(RESULT_OK, intent);
                BaseResultEvent event = new FileMultipleResultEvent(mSelectedList);
                for (NormalFile file : mSelectedList) {
                    Log.e(TAG, file.getName());
                }
                RxBus.getDefault().post(event);
                RxBus.getDefault().clear();
                finish();
            }
        });

        mRecyclerView = (SuperRecyclerView) findViewById(R.id.rv_file_pick);
        int bgColorId = isDayModel ? Color.parseColor("#F8F8F8") : Color.parseColor("#2F323B");
        mRecyclerView.setBackgroundColor(bgColorId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        int dividerDrawable = isDayModel ? R.drawable.divider_rv_file : R.drawable.divider_rv_file_night;
        initAdaptor();
        mRecyclerView.addItemDecoration(new DividerListItemDecoration(this,
                LinearLayoutManager.VERTICAL, dividerDrawable));

        mProgressBar = (ProgressBar) findViewById(R.id.pb_file_pick);
        mRecyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                NormalFilePickActivity.this.loadData();
            }
        });
    }

    private void initAdaptor() {
        //初始化选择的界面
        mCurrentNumber = 0;
        mSelectedList.clear();
        ensureButton.setText("完成");
        mAdapter = new NormalFilePickAdapter(this, mMaxNumber, isDayModel);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<NormalFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, NormalFile file) {
                if (state) {
                    mSelectedList.add(file);
                    Log.e(TAG, "add file:" + file.getName());
                    mCurrentNumber++;
                } else {
                    mSelectedList.remove(file);
                    Log.e(TAG, "remove file:" + file.getName());
                    mCurrentNumber--;
                }
                if (mCurrentNumber != 0) {
                    ensureButton.setText(String.format(Locale.CHINA, "完成(%d/%d)", mCurrentNumber, mMaxNumber));
                } else {
                    ensureButton.setText("完成");
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void loadData() {
        initAdaptor();
        FileFilter.getFiles(this, new FilterResultCallback<NormalFile>() {
            @Override
            public void onResult(List<Directory<NormalFile>> directories) {
                mProgressBar.setVisibility(View.GONE);
                normalFileList = new ArrayList<>();
                findHideVoice(normalFileList);
                for (Directory<NormalFile> directory : directories) {
                    normalFileList.addAll(directory.getFiles());
                }
                for (NormalFile file : mSelectedList) {
                    int index = normalFileList.indexOf(file);
                    if (index != -1) {
                        normalFileList.get(index).setSelected(true);
                    }
                }
                //对集合进行排序和去重工作
                sortList(normalFileList);
                mAdapter.refresh(normalFileList);
            }
        }, mSuffix);
    }

    /**
     * 手动添加被隐藏的文件
     *
     * @param fileList
     */
    private void findHideVoice(List<NormalFile> fileList) {
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
                if (!Arrays.asList(mSuffix).contains(suffix)) continue;
                NormalFile normalFile = new NormalFile();
                normalFile.setName(fileName);
                normalFile.setDate(file.lastModified());
                normalFile.setPath(file.getPath());
                fileList.add(normalFile);
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
