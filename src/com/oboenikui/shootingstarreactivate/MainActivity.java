package com.oboenikui.shootingstarreactivate;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final String CALLBACK_URL = "oboenikui://shootingstar";
    private final String CONSUMER_KEY = "ShootingStarのコンシューマキー";
    private final String CONSUMER_SECRET = "ShootingStarのコンシューマシークレット";
    private Handler mHandler = new Handler();
    private AsyncTwitter twitter = new AsyncTwitterFactory().getInstance();
    private RequestToken mRequestToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            Modify.modHttp();
        }
        setContentView(R.layout.activity_main);
        Button agree = (Button)findViewById(R.id.agree);
        agree.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    public void run() {
                        twitter = new AsyncTwitterFactory().getInstance();
                        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
                        Intent intent;
                        try {
                            mRequestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
                            intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(mRequestToken.getAuthorizationURL()));
                            startActivity(intent);
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onNewIntent(Intent intent) {
        final Uri uri = intent.getData();
        if(uri == null) {
            return ;
        }
        final String verifier = uri.getQueryParameter("oauth_verifier");
        
        
        new Thread(new Runnable() {
            public void run() {
                
                new Thread(new Runnable() {
                    public void run() {
                        AccessToken mAccessToken;
                        try {
                            mAccessToken = twitter.getOAuthAccessToken(mRequestToken, verifier);
                            String accessToken = mAccessToken.getToken();
                            String accessTokenSecret = mAccessToken.getTokenSecret();
                            final Twitter t = new TwitterFactory().getInstance();
                            t.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
                            t.setOAuthAccessToken(mAccessToken);
                            String screenName = t.getScreenName();
                            long id = t.getId();
                            User user = t.showUser(id);
                            String iconURL = user.getProfileImageURL().toString();
                            Log.d("id",id+"");
                            Log.d("Screen Name",screenName);
                            Log.d("Token",accessToken);
                            Log.d("Token Secret",accessTokenSecret);
                            Log.d("Icon URL",iconURL);
                            Process process;
                            try {
                                process = Runtime.getRuntime().exec("su");
                                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                                String[] cmds={"/system/bin/sh >/sdcard/log.txt","-c",
                                               "echo "+ 
                                               "\"insert into account values(" + id +
                                               ",'" + screenName +
                                               "','" + "1" +
                                               "','" + accessToken +
                                               "','" + accessTokenSecret +
                                               "','" + iconURL +
                                               "');\"" +
                                               "| sqlite3 /data/data/com.shootingstar067/databases/data"};
                                for (String tmpCmd : cmds) {
                                    os.writeBytes(tmpCmd+"\n");
                                }
                                os.writeBytes("exit\n");
                                os.flush();
                                os.close();
                                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(process.getInputStream()), 1024);
                                String line;
                                StringBuilder log = new StringBuilder();
                                while ((line = bufferedReader.readLine()) != null) {
                                    log.append(line);
                                    log.append("\n");
                                }
                                Log.i("LogSample", log.toString());
                                process.waitFor();

                            } catch (IOException e1) {
                                e1.printStackTrace();
                            } catch (InterruptedException e1){
                                e1.printStackTrace();
                            }
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }).start();
    }
    class SubOpenHelper extends SQLiteOpenHelper{
        public SubOpenHelper(Context c,String dbname,int version){
            super(c,dbname,null,version);
        }
        @Override
        public void onCreate(SQLiteDatabase db){
        }
        @Override
        public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        }
    }
}
