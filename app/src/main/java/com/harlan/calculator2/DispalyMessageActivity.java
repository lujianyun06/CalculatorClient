package com.harlan.calculator2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by SongChcng on 14-3-19.
 */
public class DispalyMessageActivity extends Activity {
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("New Activity");
        setContentView(textView);

    }
}