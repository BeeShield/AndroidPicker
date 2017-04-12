package cn.finalteam.rxgalleryfinal.rxbus.event;

import java.util.List;

import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.bean.NormalFile;

/**
 * Desction:
 * Author:pengjianbo
 * Date:16/8/1 下午10:52
 */
public class FileMultipleResultEvent implements BaseResultEvent {
    private List<NormalFile> normalFileResultList;

    public FileMultipleResultEvent(List<NormalFile> list) {
        this.normalFileResultList = list;
    }

    public List<NormalFile> getResult() {
        return normalFileResultList;
    }
}
