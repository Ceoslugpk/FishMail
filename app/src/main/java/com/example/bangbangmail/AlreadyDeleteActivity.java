package com.example.bangbangmail;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bangbangmail.Model.Mail;
import com.example.bangbangmail.Util.ActivityController;
import com.example.bangbangmail.Util.BaseAcctivity;
import com.example.bangbangmail.Util.FishMailApplication;
import com.example.bangbangmail.Util.MailAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlreadyDeleteActivity extends BaseAcctivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private LinearLayout mianLayout;

    private SwipeRefreshLayout swipeRefresh;

    private RecyclerView recyclerView ;

    private MailAdapter adapter;

    private ArrayList<Mail> mailList= new ArrayList<>();

    private static final String TAG = "AlreadySendActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("已删除");
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //主要布局
        mianLayout = (LinearLayout) findViewById(R.id.main_layout);
        //发送快捷按钮
        FloatingActionButton sendMailBtn = (FloatingActionButton) findViewById(R.id.send_mail);
        sendMailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlreadyDeleteActivity.this, WriteActivity.class);
                startActivity(intent);
            }
        });
        //加载用户信息
        View headerview = navigationView.inflateHeaderView(R.layout.nav_header);
        TextView usernameText = (TextView) headerview.findViewById(R.id.header_username);
        TextView usermailText = (TextView) headerview.findViewById(R.id.header_mail);
        String mail = FishMailApplication.getMail();
        String username = mail.substring(0,mail.indexOf("@"));
        Log.d(TAG, "onCreate: mail" + mail);
        usernameText.setText(username);
        usermailText.setText(mail);
        //获取已发送的邮件
        Log.d(TAG, "onCreate: maillist 长度" + mailList.size());
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.lightblue);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    //主线程沉睡，等待子线程完成
                    Thread pullThread = getAlreadyDelMailList();
                    pullThread.start();
                    pullThread.join();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                Toast.makeText(AlreadyDeleteActivity.this, "刷新完成", Toast.LENGTH_SHORT).show();
            }
        });
        if (!mailList.isEmpty()) {
            Log.d(TAG, "onCreate时候，mailList长度：" + mailList.size());
            showMail(mailList);
        } else {
            Log.d(TAG, "onCreate时候，mailList长度：" + mailList.size());
            try {
                Thread pullThread = getAlreadyDelMailList();
                pullThread.start();
                pullThread.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }

    /**
     * 处理并显示收取的mail
     */
    private void showMail(ArrayList<Mail> mailList){
        mianLayout.removeAllViews();
        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(
                R.layout.activity_receive, null).findViewById(R.id.recv_layout);
        recyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view_recv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MailAdapter(mailList);
        recyclerView.setAdapter(adapter);
        mianLayout.addView(layout);
    }

    /**
     * 从数据库得到已发送的邮件
     */
    public Thread getAlreadyDelMailList() {
        Thread pullThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mailList = getDeleteMaillist("2",FishMailApplication.getMail());
//                    getSendMaillist("0",FishMailApplication.getMail());
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    Log.d(TAG, "mailList长度: " + mailList.size());
                    Log.d(TAG, "run: " + mailList.size());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mailList.isEmpty()) {
                            Toast.makeText(AlreadyDeleteActivity.this, "已删除为空", Toast.LENGTH_SHORT).show();
                        } else {
                            showMail(mailList);
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        return pullThread;
    }


    /**
     * 使用OKhttp发送请求，获取mailJson对象
     * @param folder
     * @param mail
     */
    public ArrayList<Mail> getDeleteMaillist(String folder, final String mail) {
        final ArrayList<Mail> mailList = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String url = "http://120.77.168.57:9090/FishMail/UMailServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("floder", folder)
                .add("username", mail).build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            Log.d("Json数据:", responseData);
            try {
                JSONArray jsonArray = new JSONArray(responseData);
                mailList.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String mailid = jsonObject.getString("MailID");
                    String from = jsonObject.getString("From");
                    String to = jsonObject.getString("To");
                    String date = jsonObject.getString("date");
                    String subject = jsonObject.getString("subject");
                    String content = jsonObject.getString("content");
                    Log.d(TAG, "jsonArray: " + mailid + content);
                    mailList.add(new Mail(mailid, from, to, date, subject, content));
                    Log.d(TAG, "mailList长度: " + mailList.size());
                }
                Log.d(TAG, "mailList长度: " + mailList.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mailList;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id){
            //收件箱
            case R.id.nav_rec_mail:
                intent=new Intent(AlreadyDeleteActivity.this,ReceiveActivity.class);
                startActivity(intent);
                break;
            //写邮件
            case R.id.nav_write_mail:
                intent= new Intent(AlreadyDeleteActivity.this,WriteActivity.class);
                startActivity(intent);
                break;
//            //通讯录
//            case R.id.nav_contacts:
//                intent = new Intent(AlreadyDeleteActivity.this,AddressActivity.class);
//                startActivity(intent);
//                break;
            //已发送
            case R.id.nav_send:
                intent = new Intent(AlreadyDeleteActivity.this,AlreadySendActivity.class);
                startActivity(intent);
                break;
            //已删除
            case R.id.nav_delete_mail:
                intent = new Intent(AlreadyDeleteActivity.this,AlreadyDeleteActivity.class);
                startActivity(intent);
                break;
            //切换账号
            case R.id.switch_acount:
                FishMailApplication.setMail(null);
                FishMailApplication.setPwd(null);
                //结束所有的活动
                ActivityController.finishAll();
                intent = new Intent(AlreadyDeleteActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
//                ActivityController.finishAllButLogin();
                break;
            //退出应用
            case R.id.exit_app:
                //结束所有的活动
                ActivityController.finishAll();
                //杀死当前进程
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
