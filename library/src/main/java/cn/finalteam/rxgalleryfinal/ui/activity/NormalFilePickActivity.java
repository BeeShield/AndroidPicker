package cn.finalteam.rxgalleryfinal.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
import cn.finalteam.rxgalleryfinal.utils.Logger;
import cn.finalteam.rxgalleryfinal.utils.OsCompat;
import cn.finalteam.rxgalleryfinal.utils.ThemeUtils;

/**
 * Created by Vincent Woo
 * Date: 2016/10/26
 * Time: 10:14
 */

public class NormalFilePickActivity extends BaseFileActivity {
    public static final int DEFAULT_MAX_NUMBER = 9;
    public static final String SUFFIX = "Suffix";
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private Toolbar mTbImagePickToolbar;
    private RecyclerView mRecyclerView;
    private TextView titleTV;
    private NormalFilePickAdapter mAdapter;
    private ArrayList<NormalFile> mSelectedList = new ArrayList<>();
    private ProgressBar mProgressBar;
    private Button ensureButton;
    private String[] mSuffix;

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

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initView();
    }

    @SuppressLint("DefaultLocale")
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
        ensureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //            Intent intent = new Intent();
//            intent.putParcelableArrayListExtra(Constant.RESULT_PICK_FILE, mSelectedList);
//            setResult(RESULT_OK, intent);
                BaseResultEvent event = new FileMultipleResultEvent(mSelectedList);
                RxBus.getDefault().post(event);
                RxBus.getDefault().clear();
                finish();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_file_pick);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerListItemDecoration(this,
                LinearLayoutManager.VERTICAL, R.drawable.divider_rv_file));
        mAdapter = new NormalFilePickAdapter(this, mMaxNumber);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<NormalFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, NormalFile file) {
                if (state) {
                    mSelectedList.add(file);
                    mCurrentNumber++;
                } else {
                    mSelectedList.remove(file);
                    mCurrentNumber--;
                }
                if (mCurrentNumber != 0) {
                    ensureButton.setText(String.format("完成(%d/%d)", mCurrentNumber, mMaxNumber));
                } else {
                    ensureButton.setText("完成");
                }
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.pb_file_pick);
    }

    private void loadData() {
        FileFilter.getFiles(this, new FilterResultCallback<NormalFile>() {
            @Override
            public void onResult(List<Directory<NormalFile>> directories) {
                mProgressBar.setVisibility(View.GONE);
                List<NormalFile> list = new ArrayList<>();
                for (Directory<NormalFile> directory : directories) {
                    list.addAll(directory.getFiles());
                }

                for (NormalFile file : mSelectedList) {
                    int index = list.indexOf(file);
                    if (index != -1) {
                        list.get(index).setSelected(true);
                    }
                }

                mAdapter.refresh(list);
            }
        }, mSuffix);
    }

}
