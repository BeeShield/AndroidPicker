package cn.finalteam.rxgalleryfinal.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.NormalFile;
import cn.finalteam.rxgalleryfinal.utils.ToastUtil;
import cn.finalteam.rxgalleryfinal.utils.Util;

/**
 * Created by Vincent Woo
 * Date: 2016/10/26
 * Time: 10:23
 */

public class NormalFilePickAdapter extends BaseAdapter<NormalFile, NormalFilePickAdapter.NormalFilePickViewHolder> {
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private boolean isDayModel;

    public NormalFilePickAdapter(Context ctx, int max, boolean isDayModel) {
        this(ctx, new ArrayList<NormalFile>(), max, isDayModel);
    }

    public NormalFilePickAdapter(Context ctx, ArrayList<NormalFile> list, int max, boolean isDayModel) {
        super(ctx, list);
        mMaxNumber = max;
        this.isDayModel = isDayModel;
    }

    @Override
    public NormalFilePickViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_normal_file_pick, parent, false);
        return new NormalFilePickViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final NormalFilePickViewHolder holder, final int position) {
        final NormalFile file = mList.get(position);
        holder.mTvTitle.setText(Util.extractFileNameWithSuffix(file.getPath()));
        holder.fileSize.setText(FormetFileSize(file.getSize()));
        holder.mTvTitle.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        if (holder.mTvTitle.getMeasuredWidth() >
                Util.getScreenWidth(mContext) - Util.dip2px(mContext, 10 + 32 + 10 + 48 + 10 * 2)) {
            holder.mTvTitle.setLines(2);
        } else {
            holder.mTvTitle.setLines(1);
        }

        if (file.isSelected()) {
            holder.mCbx.setSelected(true);
        } else {
            holder.mCbx.setSelected(false);
        }

        if (file.getPath().endsWith("xls") || file.getPath().endsWith("xlsx")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_excel);
        } else if (file.getPath().endsWith("doc") || file.getPath().endsWith("docx")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_word);
        } else if (file.getPath().endsWith("ppt") || file.getPath().endsWith("pptx")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_ppt);
        } else if (file.getPath().endsWith("pdf")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_pdf);
        } else if (file.getPath().endsWith("txt")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_txt);
        } else if (file.getPath().endsWith("mp3") ||
                file.getPath().endsWith("m4a") ||
                file.getPath().endsWith("aac") ||
                file.getPath().endsWith("wav") ||
                file.getPath().endsWith("flac") ||
                file.getPath().endsWith("wma") ||
                file.getPath().endsWith("mar") ||
                file.getPath().endsWith(".ogg") ||
                file.getPath().endsWith(".3gpp") ||
                file.getPath().endsWith("amr")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_audio);
        } else {
            holder.mIvIcon.setImageResource(R.drawable.ic_file);
        }
        if (isDayModel) {
            holder.mTvTitle.setTextColor(Color.parseColor("#868686"));
        } else {
            holder.mTvTitle.setTextColor(Color.parseColor("#AEAEAE"));
        }
        holder.mCbx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected() && isUpToMax()) {
                    ToastUtil.showTipsToast(mContext, "已超过最大限制");
                    return;
                }

                if (v.isSelected()) {
                    holder.mCbx.setSelected(false);
                    mCurrentNumber--;
                } else {
                    holder.mCbx.setSelected(true);
                    mCurrentNumber++;
                }

                mList.get(holder.getAdapterPosition()).setSelected(holder.mCbx.isSelected());

                if (mListener != null) {
                    mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(holder.getAdapterPosition()));
                }
            }
        });

        holder.itemRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.mCbx.isSelected() && isUpToMax()) {
                    ToastUtil.showTipsToast(mContext, "已超过最大限制");
                    return;
                }

                if (holder.mCbx.isSelected()) {
                    holder.mCbx.setSelected(false);
                    mCurrentNumber--;
                } else {
                    holder.mCbx.setSelected(true);
                    mCurrentNumber++;
                }

                mList.get(holder.getAdapterPosition()).setSelected(holder.mCbx.isSelected());

                if (mListener != null) {
                    mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(holder.getAdapterPosition()));
                }
            }
        });

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = Uri.parse("file://" + file.getPath());
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(uri, "audio/mp3");
//                if (Util.detectIntent(mContext, intent)) {
//                    mContext.startActivity(intent);
//                } else {
//                    Toast.makeText(mContext, "No Application exists for audio!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class NormalFilePickViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvIcon;
        private TextView mTvTitle;
        private TextView fileSize;
        private ImageView mCbx;
        private RelativeLayout itemRL;

        public NormalFilePickViewHolder(View itemView) {
            super(itemView);
            mIvIcon = (ImageView) itemView.findViewById(R.id.ic_file);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_file_title);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
            itemRL = (RelativeLayout) itemView.findViewById(R.id.rl_file_item);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
        }
    }

    private boolean isUpToMax() {
        return mCurrentNumber >= mMaxNumber;
    }

    private String formatFileSize(long size) {
        float middle = 0;
        if (size < 1073741824) {
            middle = size % 1048576;
        }
        return String.format(Locale.getDefault(), "%.2f", middle);
    }

    public String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        if (fileSizeString.startsWith(".")) {
            fileSizeString = "0" + fileSizeString;
        }
        return fileSizeString;
    }

}
