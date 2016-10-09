package com.homeautomation.ajithvalappil.homeautomation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class MainActivity extends ActionBarActivity {

    Button authn = null;
    String userName = null;
    String passWord = null;
    protected static final int RESULT_SPEECH = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Intent i = new Intent(this, StatusIntentService.class);
        startService(i);

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


    public void light(View view) {
            switch (view.getId()) {
                case R.id.l1ON:
                    makeServiceCall("on1", null);
                    break;
                case R.id.l1OFF:
                    makeServiceCall("off1", null);
                    break;
                case R.id.l2ON:
                    makeServiceCall("on2", null);
                    break;
                case R.id.l2OFF:
                    makeServiceCall("off2", null);
                    break;
                case R.id.l3ON:
                    makeServiceCall("on3", null);
                    break;
                case R.id.l3OFF:
                    makeServiceCall("off3", null);
                    break;
            }
    }


    public void makeServiceCall(final String data, String text) {
            System.out.println(data);
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        if (data!=null){

                            HttpClient httpclient = new DefaultHttpClient();
                            HttpGet httpget = new HttpGet("http://10.0.1.22:8000/turn/" + data);
                            HttpResponse response1 = httpclient.execute(httpget);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();

    }


    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

}
