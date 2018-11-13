package com.harlan.calculator2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PreDinamic extends Activity implements OnClickListener{
	private Button loadV;
	private Button loadA;
	private Button loadP;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pre_dynamic);
		loadV=(Button) findViewById(R.id.loadVideo);
		loadA=(Button) findViewById(R.id.loadAudio);
		loadP=(Button) findViewById(R.id.loadPicture);
		loadV.setOnClickListener(this);
		loadA.setOnClickListener(this);
		loadP.setOnClickListener(this);
		
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent=new Intent();
		if(v==loadV){
			intent.setClass(PreDinamic.this, DynamicLoadVideo.class);
			startActivity(intent);
		}else if(v==loadA){
			intent.setClass(PreDinamic.this,DynamicLoadAudio.class);
			startActivity(intent);
		}else if(v==loadP){
			intent.setClass(PreDinamic.this, DynamicLoadPicture.class);
			startActivity(intent);
		}
	}

}
