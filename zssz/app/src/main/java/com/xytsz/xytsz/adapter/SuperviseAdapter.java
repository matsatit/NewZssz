package com.xytsz.xytsz.adapter;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xytsz.xytsz.R;
import com.xytsz.xytsz.activity.FloodActivity;
import com.xytsz.xytsz.activity.LibActivity;
import com.xytsz.xytsz.activity.MakerProblemActivty;
import com.xytsz.xytsz.activity.MemberLocationActivity;
import com.xytsz.xytsz.util.IntentUtil;
import com.xytsz.xytsz.util.ToastUtil;

import java.util.List;

/**
 * Created by admin on 2017/6/29.
 * 监管adapter
 *
 */
public class SuperviseAdapter extends BaseQuickAdapter<String> {

    private int[] mIcons = {R.mipmap.more_person,R.mipmap.more_problem,R.mipmap.more_notice,R.mipmap.more_lib,R.mipmap.more_flood };
    private int role;

    public SuperviseAdapter(List<String> data , int role) {
        super(R.layout.item_more, data);
        this.role = role;


    }



    @Override
    protected void convert(BaseViewHolder viewHolder, String item) {
        viewHolder.setText(R.id.more_tv,item);
        int layoutPosition = viewHolder.getLayoutPosition();
        viewHolder.setImageResource(R.id.more_icon,mIcons[layoutPosition-1]);

    }
}
