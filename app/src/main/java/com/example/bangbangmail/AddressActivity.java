package com.example.bangbangmail;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.bangbangmail.Util.ActivityController;
import com.example.bangbangmail.Util.BaseAcctivity;
import com.example.bangbangmail.Util.FishMailApplication;

public class AddressActivity extends BaseAcctivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "AddressActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("通讯录");
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //发送快捷按钮
        FloatingActionButton sendMailBtn = (FloatingActionButton) findViewById(R.id.send_mail);
        sendMailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddressActivity.this, WriteActivity.class);
                startActivity(intent);
            }
        });
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //加载用户信息
        View headerview = navigationView.inflateHeaderView(R.layout.nav_header);
        TextView usernameText = (TextView) headerview.findViewById(R.id.header_username);
        TextView usermailText = (TextView) headerview.findViewById(R.id.header_mail);
        String mail = FishMailApplication.getMail();
        String username = mail.substring(0,mail.indexOf("@"));
        Log.d(TAG, "onCreate: mail" + mail);
        usernameText.setText(username);
        usermailText.setText(mail);
        navigationView.setNavigationItemSelectedListener(this);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id){
            //收件箱
            case R.id.nav_rec_mail:
                intent=new Intent(AddressActivity.this,ReceiveActivity.class);
                startActivity(intent);
                break;
            //写邮件
            case R.id.nav_write_mail:
                intent= new Intent(AddressActivity.this,WriteActivity.class);
                startActivity(intent);
                break;
//            //通讯录
//            case R.id.nav_contacts:
//                intent = new Intent(AddressActivity.this,AddressActivity.class);
//                startActivity(intent);
//                break;
            //已发送
            case R.id.nav_send:
                intent = new Intent(AddressActivity.this,AlreadySendActivity.class);
                startActivity(intent);
                break;
            //已删除
            case R.id.nav_delete_mail:
                intent = new Intent(AddressActivity.this,AlreadyDeleteActivity.class);
                startActivity(intent);
                break;
            //切换账号
            case R.id.switch_acount:
                FishMailApplication.setMail(null);
                FishMailApplication.setPwd(null);
                //结束所有的活动
                ActivityController.finishAll();
                intent = new Intent(AddressActivity.this,LoginActivity.class);
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
