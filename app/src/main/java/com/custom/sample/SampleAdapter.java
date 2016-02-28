package com.custom.sample;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.custom.viewgroup.CircularListViewGroup;

import static com.custom.viewgroup.CircularListViewGroup.*;

/**
 * Created by anitham on 29/1/16.
 */
public class SampleAdapter extends BaseAdapter {
    private int maxData = 26;
    private Context context;

    public SampleAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return maxData;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView = new TextView(context);
        int size = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, context.getResources().getDisplayMetrics());
        textView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        textView.setBackgroundColor(Color.BLUE);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setText(Character.toString((char)(i+65)));
        return textView;
    }



}
