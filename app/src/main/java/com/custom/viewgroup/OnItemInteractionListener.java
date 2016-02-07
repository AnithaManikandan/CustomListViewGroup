package com.custom.viewgroup;

import android.view.View;

/**
 * Created by anitham on 29/1/16.
 */
public interface OnItemInteractionListener {
    public void onItemClick(int position, View view);
    public void onItemLongPress(int position, View view);
}
