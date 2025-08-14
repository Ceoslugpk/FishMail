package com.example.bangbangmail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bangbangmail.Util.BaseAcctivity;
import com.example.bangbangmail.Util.DeleteFromDB;
import com.example.bangbangmail.Util.FishMailApplication;
import com.example.bangbangmail.Util.PullMail;

public class MailDetail extends BaseAcctivity implements NavigationView.OnClickListener {
    private static final String TAG = "MailDetail";
    private String mailid = "";
    private String sender = "";
    private String receiver ="";
    private int whichmail;
    private boolean delres = false;
    private String context;
    private ProgressDialog deleProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("返回");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(this);
        Intent intent = getIntent();
        whichmail = intent.getIntExtra("POSITION", 0);
        mailid = intent.getStringExtra("MAILID");
        Log.d(TAG, "onCreate: whichmail: " + whichmail);
        Log.d(TAG, "onCreate: mailid: " + mailid);
        sender = intent.getStringExtra("FROM");
        receiver = intent.getStringExtra("TO");
        String subject = intent.getStringExtra("SUBJECT");
        String time = intent.getStringExtra("DATE");
        String content = intent.getStringExtra("TEXT");
        Log.d(TAG, "onCreate: " + sender);
        TextView senderText = (TextView) findViewById(R.id.sender);
        TextView receiverText = (TextView) findViewById(R.id.receiver);
        TextView subjectText = (TextView) findViewById(R.id.subject);
        TextView timeText = (TextView) findViewById(R.id.rcv_time);
        TextView contentText = (TextView) findViewById(R.id.content);
        senderText.setText(sender);
        receiverText.setText(receiver);
        subjectText.setText(subject);
        timeText.setText(time);
        contentText.setText(content);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_mail:
                deleProgress = new ProgressDialog(MailDetail.this);
                deleProgress.setMessage("删除中...");
                deleProgress.setCancelable(true);
                deleProgress.show();
                context = getIntent().getStringExtra("CONTEXT");
                context = context.substring(0,context.indexOf("@"));
                Log.d(TAG, "onOptionsItemSelected: " + context);
                try {
                    //主线程沉睡，等待子线程完成
                    Thread pullThread = deleteMail();
                    pullThread.start();
                    pullThread.join();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }


    /**
     * 拉取邮件
     */
    public Thread deleteMail() {
        Thread pullThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (context.equals("com.example.bangbangmail.ReceiveActivity")){
                        delres = DeleteFromDB.deleteMail(mailid,"0","0");
                        PullMail.DeleteMail(whichmail);
                    } else if (context.equals("com.example.bangbangmail.AlreadySendActivity")) {
                        delres = DeleteFromDB.deleteMail(mailid,"1","0");
                    } else if (context.equals("com.example.bangbangmail.AlreadyDeleteActivity")) {
                        if (sender.equals(FishMailApplication.getMail()))
                            delres = DeleteFromDB.deleteMail(mailid,"1","1");
                        if (receiver.equals(FishMailApplication.getMail()))
                            delres = DeleteFromDB.deleteMail(mailid,"0","1");
                    }
                    Log.d(TAG, "run: " + delres);
                }catch (Exception e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deleProgress.dismiss();
                        if (delres) {
                            Toast.makeText(MailDetail.this, "删除成功", Toast.LENGTH_SHORT).show();
                            MailDetail.this.finish();
                        } else {
                            Toast.makeText(MailDetail.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return pullThread;
    }
    @Override
    public void onClick(View v) {
        MailDetail.this.finish();
    }

}
