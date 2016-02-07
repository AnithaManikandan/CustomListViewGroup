package com.custom.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.custom.viewgroup.OnItemInteractionListener;
import com.custom.R;
import com.custom.viewgroup.CircularListViewGroup;

/**
 * Created by anitham on 01-02-2016.
 */
public class MainActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_layout);

        CircularListViewGroup circularViewGroup= (CircularListViewGroup) findViewById(R.id.viewGroup);
        circularViewGroup.setBackgroundColor(Color.RED);
        circularViewGroup.setOrientation(CircularListViewGroup.HORIZONTAL_ORIENTATION);
        SampleAdapter sampleAdapter = new SampleAdapter(this);
        circularViewGroup.setAdapter(sampleAdapter);
        circularViewGroup.setOnItemInteractionListener(new OnItemInteractionListener() {

            @Override
            public void onItemClick(int position, View view) {
                Toast.makeText(MainActivity.this, position+" is clicked",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongPress(int position, View view) {
                Toast.makeText(MainActivity.this, position+" is Long Pressed",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
