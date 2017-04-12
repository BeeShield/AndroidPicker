package cn.finalteam.rxgalleryfinal.ui.adapter;


import java.util.List;

import cn.finalteam.rxgalleryfinal.bean.BaseFile;
import cn.finalteam.rxgalleryfinal.bean.Directory;

/**
 * Created by Vincent Woo
 * Date: 2016/10/11
 * Time: 11:39
 */

public interface FilterResultCallback<T extends BaseFile> {
    void onResult(List<Directory<T>> directories);
}
