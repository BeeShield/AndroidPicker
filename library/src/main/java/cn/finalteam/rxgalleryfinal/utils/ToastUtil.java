package cn.finalteam.rxgalleryfinal.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperToast;

import java.lang.ref.WeakReference;

/**
 * Created by Vincent Woo
 * Date: 2016/1/22
 * Time: 17:27
 */
public class ToastUtil {
    private static WeakReference<Context> mContext;
    private static ToastUtil mInstance;
    private Toast mToast;

    public static ToastUtil getInstance(Context ctx) {
        if (mInstance == null || mContext.get() == null) {
            mInstance = new ToastUtil(ctx);
        }

        return mInstance;
    }

    private ToastUtil(Context ctx) {
        mContext = new WeakReference<>(ctx);
    }

    public void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext.get(), text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public void showToast(int resID) {
        showToast(mContext.get().getResources().getString(resID));
    }

    public void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    /**
     * pad统一上方吐司提示信息
     *
     * @param context
     * @param info
     */
    public static void showTipsToast(Context context, String info) {
        if (context == null) {
            Logger.e("context is null");
            return;
        }

        SuperToast superToast = new SuperToast(context);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setText(info);
        superToast.setGravity(Gravity.TOP, 0, 100);
        superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
        superToast.show();
    }
}
