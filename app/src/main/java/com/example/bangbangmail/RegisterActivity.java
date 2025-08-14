package com.example.bangbangmail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.bangbangmail.Util.BaseAcctivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class RegisterActivity extends BaseAcctivity implements NavigationView.OnClickListener {
    private static final String TAG = "RegisterActivity";
    private RegisterTask mAuthTask = null;
    // UI 组件
    private EditText usernameText;
    private EditText phoneText;
    private EditText mPasswordView;
    private EditText rePasswordText;
    private View mRegisterForm;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("注册");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(this);
        //表单项
        mRegisterForm = findViewById(R.id.register_form);
        usernameText = (EditText) findViewById(R.id.username);
        phoneText = (EditText) findViewById(R.id.tel_no);
        mPasswordView = (EditText) findViewById(R.id.reg_pwd);
        rePasswordText = (EditText) findViewById(R.id.re_pwd);
        //注册按钮事件
        Button registerbtn = (Button) findViewById(R.id.register_button);
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
        mProgressView = findViewById(R.id.register_progress);
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        usernameText.setError(null);
        phoneText.setError(null);
        mPasswordView.setError(null);
        rePasswordText.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameText.getText().toString();
        String tel = phoneText.getText().toString();
        String password = mPasswordView.getText().toString();
        String rePassword = rePasswordText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(username)) {
            usernameText.setError("用户名不能为空");
            focusView = usernameText;
            cancel = true;
        }
        if (TextUtils.isEmpty(tel)) {
            phoneText.setError("手机号不能为空");
            focusView = phoneText;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("密码不能为空");
            focusView = mPasswordView;
            cancel = true;
        }
        if (!rePassword.equals(password)) {
            rePasswordText.setError("前后密码不一致");
            focusView = rePasswordText;
            cancel = true;
        }

        // Check for a valid
        if (!isTelLenthLegal(tel)) {
            phoneText.setError("输入正确的手机号");
            focusView = phoneText;
            cancel = true;
        }
        if (!TextUtils.isEmpty(username)&&!isUsernameValid(username)) {
            usernameText.setError("用户名只能由数字字母下划线组成");
            focusView = usernameText;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password)&&!isPwdValid(password)) {
            mPasswordView.setError("密码只能由数字字母下划线组成");
            focusView = mPasswordView;
            cancel = true;
        }

        if (!isPwdLenthLegal(password)) {
            mPasswordView.setError("密码长度应为 6-16 位");
            focusView = mPasswordView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
            mAuthTask = new RegisterTask(username, tel, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isTelLenthLegal(String tel) {
        //TODO: Replace this with your own logic
        return tel.length() == 11;
    }

    /**
     * 用户名只能包含数字，英文，下划线
     * @param username
     * @return
     */
    private boolean isUsernameValid(String username) {
        String reg = "\\w+([-+.]\\w+)*";
        // 创建 Pattern 对象
        Pattern p = Pattern.compile(reg);
        return p.matcher(username).matches();
    }

    //验证密码长度是否在 6 - 16位
    private boolean isPwdLenthLegal(String pwd) {
        //TODO: Replace this with your own logic
        return (pwd.length() > 5) && (pwd.length() < 17);
    }

    /**
     * 密码只能包含数字，英文，下划线
     * @param password
     * @return
     */
    private boolean isPwdValid(String password) {
        String reg = "\\w+([_]\\w+)*";
        // 创建 Pattern 对象
        Pattern p = Pattern.compile(reg);
        return p.matcher(password).matches();
    }



//    /**
//     * Shows the progress UI and hides the login form.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    private void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
//            mRegisterForm.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }




    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mTel;
        private final String mPassword;
        ProgressDialog registerProgress = new ProgressDialog(RegisterActivity.this);

        RegisterTask(String username, String tel, String password) {
            mEmail = username + "@fishmail.com";
            mTel = tel;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
//            registerProgress.setTitle("你好，" + mEmail);
            registerProgress.setMessage(mEmail + ",注册中...");
            registerProgress.setCancelable(true);
            registerProgress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            boolean registerResult = false;
            try {
                // Simulate network access.
                registerResult = RegisterByPost(mEmail, mTel, mPassword);
                Log.d(TAG, "doInBackground:registerResult: " + registerResult);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                registerResult = false;
            }

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }
            return registerResult;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
//            showProgress(false);
            registerProgress.dismiss();
            if (success) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                usernameText.setError("用户名已被占用！");
                usernameText.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
//            showProgress(false);
        }
    }

    /*
    *   发送http请求验证用户名、密码
    *   @return boolean
    */
    private boolean RegisterByPost(String email, String tel, String pwd){
        boolean signal = false;
        try{
            Log.d("RegisterByPost", "注册中...");
            Log.d(TAG, "RegisterByPost: " + email + "-" + tel);
            URL url = new URL("http://120.77.168.57:9090/FishMail/RegisterServlet");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            //传递数据
            String data = "mail=" + URLEncoder.encode(email,"UTF-8")
                    + "&password=" + URLEncoder.encode(pwd,"UTF-8")
                    + "&phone=" + URLEncoder.encode(tel,"UTF-8");
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
                Log.d(TAG, "RegisterByPost: 状态码：" + urlConnection.getResponseCode());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "loginByPost: signal:" + signal);
        return signal;
    }

    @Override
    public void onClick(View v) {
        RegisterActivity.this.finish();
    }
}
