package com.example.bangbangmail;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bangbangmail.Util.BaseAcctivity;
import com.example.bangbangmail.Util.FishMailApplication;
import com.example.bangbangmail.Util.SendMail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.mail.MessagingException;

public class WriteActivity extends BaseAcctivity implements NavigationView.OnClickListener {
    private EditText toInput;
    private EditText subjectText;
    private EditText contentText;
    public static final int SUCCESS = 1;
    public static final int FAILED = 0;
    private static final String TAG = "WriteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_mail);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("写邮件");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(this);
        toInput = (EditText) findViewById(R.id.write_receiver);
        subjectText = (EditText) findViewById(R.id.write_subject);
        contentText = (EditText) findViewById(R.id.write_content);
    }

    @Override
    public void onClick(View v) {
        WriteActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_menu,menu);
        return true;
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    Toast.makeText(WriteActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                    WriteActivity.this.finish();
                    break;
                case FAILED:
                    Toast.makeText(WriteActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@")&&email.contains(".");
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send:
                boolean cancel = false;
                View focusView = null;
                final String to =  toInput.getText().toString();
                if (TextUtils.isEmpty(to)) {
                    toInput.setError("请输入收件人");
                    focusView = toInput;
                    cancel = true;
                } else if (!isEmailValid(to)){
                    toInput.setError("请输入正确的收件人邮箱");
                    focusView = toInput;
                    cancel = true;
                }
                else{
                    String tmpsubject = subjectText.getText().toString();
                    String tmpcontent = contentText.getText().toString();
                    //如果主题内容为空，赋值"无主题"或“无内容”
                    if (TextUtils.isEmpty(tmpsubject)){
                        tmpsubject = "无主题";
                    }
                    if (TextUtils.isEmpty(tmpcontent)){
                        tmpcontent = "无内容";
                    }
                    final String subject = tmpsubject;
                    final String content = tmpcontent;
                    final ProgressDialog sendingProDialog = new ProgressDialog(WriteActivity.this);
                    sendingProDialog.setMessage("正在发送给" + to );
                    sendingProDialog.setCancelable(true);
                    sendingProDialog.show();
                    new Thread(new Runnable() {
                        boolean res = false;
                        Message message = new Message();
                        @Override
                        public void run() {
                            try {
                                res = SendMail.send(to,subject,content);
                                String mailid = SendMail.mailId;
                                String date = SendMail.date;
                                Log.d(TAG, "run: MailID" + mailid);
                                if (res) {
                                    Log.d(TAG, "发送结果: " + res);
                                    message.what = SUCCESS;
                                    boolean stres = storeMail(mailid, FishMailApplication.getMail(),to,date,subject,content);
                                    Log.d(TAG, "run: storeresult:" + stres);
                                }else {
                                    message.what = FAILED;
                                }
                            }catch (MessagingException mex) {
                                mex.printStackTrace();
                                message.what = FAILED;
                            }
                            sendingProDialog.dismiss(); //关闭提示框
                            handler.sendMessage(message);
                        }
                    }).start();
                }
                break;
        }
        return true;
    }


    /*
   *   发送http请求将邮件存在数据库
   *   @return boolean
   */
    private boolean storeMail(String mailId, String from, String to, String date,
                              String subject,String content){
        boolean signal = false;
        try{
            Log.d("storeMail", "存储文件中...");
            Log.d(TAG, "mailMatchTel: " + mailId + "-" + from + to + "-" + date + subject + "-" + content);
            URL url = new URL("http://120.77.168.57:9090/FishMail/UAddMailServlet");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            //传递数据
            String data = "mailId=" + URLEncoder.encode(mailId,"UTF-8")
                    + "&from=" + URLEncoder.encode(from,"UTF-8")
                    + "&to=" + URLEncoder.encode(to,"UTF-8")
                    + "&date=" + URLEncoder.encode(date,"UTF-8")
                    + "&subject=" + URLEncoder.encode(subject,"UTF-8")
                    + "&text=" + URLEncoder.encode(content,"UTF-8")
                    + "&folder=" + URLEncoder.encode("0","UTF-8");
            Log.d(TAG, "RegisterByPost: date:" + data);
            urlConnection.setRequestProperty("Connection","keep-alive");
            urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Length",String.valueOf(data.getBytes().length));
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            //获取输出流
            OutputStream os =urlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            //接收报文
            if(urlConnection.getResponseCode()==200){
                InputStream is = urlConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len = 0;
                byte buffer[] = new byte[1024];
                while((len=is.read(buffer)) != -1){
                    baos.write(buffer,0,len);
                }
                is.close();
                baos.close();
                final String res = new String(baos.toByteArray());
                if(res.equals("true")){
                    signal = true;
                }
                else {
                    signal = false;
                }
            } else {
                Log.d(TAG, "storeMail: 状态码：" + urlConnection.getResponseCode());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "storeMail: signal:" + signal);
        return signal;
    }
}
