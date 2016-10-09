package com.homeautomation.ajithvalappil.homeautomation;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ajithvalappil2 on 8/26/16.
 */
public class StatusIntentService extends IntentService {
    final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
    String userName = null;
    String passWord = null;


    public StatusIntentService() {
        this(StatusIntentService.class.getName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public StatusIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean execute = true;


        while(execute){
            try {
                SimpleDateFormat frmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                int batteryLevel = (int)getBatteryLevel();
                int missedSms = unreadSmsCount();
                String myPhoneNumber = myPhoneNumber();
                String myName = myName();
                List<String> missedCalls = missedCalls();

                JSONObject mailBody = new JSONObject();
                mailBody.put("battery", batteryLevel);
                mailBody.put("sms_unread_count", missedSms);
                mailBody.put("me_number", myPhoneNumber);
                mailBody.put("date", frmt.format(new Date()));
                mailBody.put("me_name", myName);
                mailBody.put("missedcalls", missedCalls);

                System.out.println("JSONObject: " + mailBody.toString());
                makeServiceCall(mailBody.toString());


            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                Thread.sleep(1800000);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void makeServiceCall(final String data) {
        System.out.println(data);
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    if (data!=null){

                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost("http://10.0.1.22:8000/mobiledata");
                        // Add your data
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                        nameValuePairs.add(new BasicNameValuePair("stringdata", data));

                        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = client.execute(post);


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();

    }

    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }

    public int unreadSmsCount(){
        int unreadMessagesCount = 0;

        Cursor c = getContentResolver().query(SMS_INBOX, null, "read = 0", null, null);
        try{
            unreadMessagesCount = c.getCount();
        }catch(Exception e){

        }finally {
            try {
                if( c != null && !c.isClosed() )
                    c.close();
            } catch(Exception ex) {}
        }
        return unreadMessagesCount;
    }

    public List<String> missedCalls(){
        List<String> missedCalls = new ArrayList<String>();
        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        try {
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.HOUR, -5);
            Date oneHourBack = cal.getTime();
            int type = c.getColumnIndex(CallLog.Calls.TYPE);
            int phoneNum = c.getColumnIndex(CallLog.Calls.NUMBER);
            int date = c.getColumnIndex(CallLog.Calls.DATE);
            int nm= c.getColumnIndex(CallLog.Calls.CACHED_NAME);
            while(c.moveToNext()){
                String callTypeCode = c.getString(type);
                String phoneNumber = c.getString(phoneNum);
                String name= c.getString(nm);
                String strcallDate = c.getString(date);
                Date callDate = new Date(Long.valueOf(strcallDate));
                System.out.println("name: " + name + "|" + callTypeCode + "|" + callDate);
                if (callDate.after(oneHourBack)){

                    if (callTypeCode!=null){
                        int callcode = Integer.parseInt(callTypeCode);
                        if (callcode == CallLog.Calls.MISSED_TYPE){
                            missedCalls.add(callDate.toString() + "|" + name + "|" + phoneNumber);
                        }
                    }
                }
            }
        }catch(Exception e){

        }finally {
            try {
                if( c != null && !c.isClosed() )
                    c.close();
            } catch(Exception ex) {}
        }

        return missedCalls;
    }

    public String myPhoneNumber(){
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String number = tm.getLine1Number();
        return number;
    }

    public String myName(){
        String myName  = "";
        Cursor c = getApplication().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        try {
            c.moveToFirst();
            myName = c.getString(c.getColumnIndex("display_name"));
        }catch (Exception ee){
            ee.printStackTrace();
        }finally {
            try {
                if( c != null && !c.isClosed() )
                    c.close();
            } catch(Exception ex) {}
        }
        return myName;
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}


