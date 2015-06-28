package com.bin.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

    public static String sdcardPath = null;
    public static String extSdcardPath = null;
    Button internalStorageBt;
    Button externalStorageBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        internalStorageBt = (Button) findViewById(R.id.internalStorage);
        externalStorageBt = (Button) findViewById(R.id.externalStorage);
        internalStorageBt.setOnClickListener(this);
        externalStorageBt.setOnClickListener(this);
        extSdcardPath = System.getenv("SECONDARY_STORAGE");
        if (TextUtils.isEmpty(extSdcardPath)) {
            externalStorageBt.setVisibility(View.GONE);
        } else {
            externalStorageBt.setText(extSdcardPath);
        }
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        internalStorageBt.setText(sdcardPath);

    }


    public void internalStorageBt() {

        Intent intent = new Intent(this, FileListActivity.class);
        intent.putExtra("sdcardPath", sdcardPath);
        startActivity(intent);
    }

    public void externalStorageBt() {
        Intent intent = new Intent(this, FileListActivity.class);
        intent.putExtra("extSdcardPath", extSdcardPath);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.internalStorage:
                externalStorageBt();
                break;
            case R.id.externalStorage:
                externalStorageBt();
                break;
        }
    }
}
