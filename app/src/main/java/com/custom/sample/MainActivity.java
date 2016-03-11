package com.custom.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.anitha.custom.viewgroup.CircularListViewGroup;
import com.custom.R;

/**
 * Created by anitham on 01-02-2016.
 */
public class MainActivity extends Activity {

    private CircularListViewGroup circularViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_layout);

        circularViewGroup = (CircularListViewGroup) findViewById(R.id.viewGroup);
        SampleAdapter sampleAdapter = new SampleAdapter(this);
        circularViewGroup.setAdapter(sampleAdapter);

        circularViewGroup.setOnItemInteractionListener(new CircularListViewGroup.OnItemInteractionListener() {

            @Override
            public void onItemClick(int position, View view) {
                Toast.makeText(MainActivity.this, position + " is clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
