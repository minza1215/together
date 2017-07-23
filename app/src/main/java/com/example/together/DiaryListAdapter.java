package com.example.together;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 김민제 on 2017-07-23.
 */

public class DiaryListAdapter extends BaseAdapter {

    private Context context;
    private List<Notice> diaryList;

    public DiaryListAdapter(Context context, List<Notice> diaryList) {
        this.context = context;
        this.diaryList = diaryList;
    }

    @Override
    public int getCount() {
        return diaryList.size();
    }

    @Override
    public Object getItem(int i) {
        return diaryList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = View.inflate(context, R.layout.notice, null);
        TextView noticeText = (TextView)v.findViewById(R.id.noticeText);
        TextView nameText = (TextView)v.findViewById(R.id.nameText);
        TextView dateText = (TextView)v.findViewById(R.id.dateText);

        noticeText.setText(diaryList.get(i).getNotice());
        nameText.setText(diaryList.get(i).getName());
        dateText.setText(diaryList.get(i).getDate());

        v.setTag(diaryList.get(i).getNotice());
        return v;
    }
}
