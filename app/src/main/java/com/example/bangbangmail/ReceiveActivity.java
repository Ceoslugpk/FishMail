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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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
import com.example.bangbangmail.Util.PullMail;

import java.util.ArrayList;

public class ReceiveActivity extends BaseAcctivity implements NavigationView.OnNavigationItemSelectedListener {
    private LinearLayout mianLayout;
    private SwipeRefreshLayout swipeRefresh;



    private RecyclerView recyclerView ;

    private MailAdapter adapter;

    private ArrayList<Mail> mailList= new ArrayList<>();

    private static final String TAG = "ReceiveActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.inbox);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //主要布局
        mianLayout = (LinearLayout) findViewById(R.id.main_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //发送快捷按钮
        FloatingActionButton sendMailBtn = (FloatingActionButton) findViewById(R.id.send_mail);
        sendMailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceiveActivity.this, WriteActivity.class);
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

        Log.d(TAG, "onCreate: maillist 长度" + mailList.size());
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.lightblue);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    //主线程沉睡，等待子线程完成
                    Thread pullThread = getMailList();
                    pullThread.start();
                    pullThread.join();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                Toast.makeText(ReceiveActivity.this, "刷新完成", Toast.LENGTH_SHORT).show();
            }
        });
        if (!mailList.isEmpty()) {
            showMail(mailList);
        } else {
            try {
                Thread pullThread = getMailList();
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
     * 没收到邮件时候加载布局mail
     */
    private void showMail(){
        mianLayout.removeAllViews();
        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(
                R.layout.activity_receive, null).findViewById(R.id.recv_layout);
        mianLayout.addView(layout);
    }

    /**
     * 拉取邮件
     */
    public Thread getMailList() {
        Thread pullThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mailList.clear();
                    mailList = PullMail.pullMail();
                    Log.d(TAG, "run: " + mailList.size());
                }catch (Exception e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mailList.isEmpty()) {
                            Toast.makeText(ReceiveActivity.this, "你还没有收到任何邮件", Toast.LENGTH_SHORT).show();
                            showMail();
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


    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.theme_light:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            case R.id.theme_dark:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return true;
            case R.id.theme_system:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id){
            //收件箱
            case R.id.nav_rec_mail:
                intent=new Intent(ReceiveActivity.this,ReceiveActivity.class);
                startActivity(intent);
                break;
            //写邮件
            case R.id.nav_write_mail:
                intent= new Intent(ReceiveActivity.this,WriteActivity.class);
                startActivity(intent);
                break;
//            //通讯录
//            case R.id.nav_contacts:
//                intent = new Intent(ReceiveActivity.this,AddressActivity.class);
//                startActivity(intent);
//                break;
            //已发送
            case R.id.nav_send:
                intent = new Intent(ReceiveActivity.this,AlreadySendActivity.class);
                startActivity(intent);
                break;
            //已删除
            case R.id.nav_delete_mail:
                intent = new Intent(ReceiveActivity.this,AlreadyDeleteActivity.class);
                startActivity(intent);
                break;
            //切换账号
            case R.id.switch_acount:
                FishMailApplication.setMail(null);
                FishMailApplication.setPwd(null);
                //结束所有的活动
                ActivityController.finishAll();
                intent = new Intent(ReceiveActivity.this,LoginActivity.class);
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
