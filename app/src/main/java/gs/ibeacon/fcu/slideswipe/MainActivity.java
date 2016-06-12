package gs.ibeacon.fcu.slideswipe;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import gs.ibeacon.fcu.slideswipe.Fragment.*;
import gs.ibeacon.fcu.slideswipe.Log.*;
import gs.ibeacon.fcu.slideswipe.JSON.*;
import gs.ibeacon.fcu.slideswipe.BlueTooth.*;
import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_CONNECT_DEVICE = 1;
    public static final String TAG = "MainActivity";
    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ServerHandler serverHandler;
    private BluetoothService bluetoothService;

    private NavigationView navigationView;
    private MenuItem imgItem = null;
    private MenuItem logItem = null;
    private Switch btSwitch;
    public static MainActivity m;
    final MaterialDialog loginDialog = new MaterialDialog(this);
    final MaterialDialog logoutDialog = new MaterialDialog(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DLog.d(TAG, "nCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.mipmap.ic_launcher);
        getWindow().setStatusBarColor(getResources().getColor(R.color.toolbarU));
        actionBar.setTitle(Html.fromHtml("<font color='#00FFCC'>智慧導引</font>"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "I have no idea what to do.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DLog.d(TAG, "onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DLog.d(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.item_switch).setActionView(R.layout.switch_of_bluetooth);
        imgItem = menu.findItem(R.id.item_bticon);
        imgItem.setIcon(R.drawable.ic_bt);

        logItem = menu.findItem(R.id.item_login);

        btSwitch = (Switch) menu.findItem(R.id.item_switch).getActionView().findViewById(R.id.switchofbt);
        if(mBluetoothAdapter.isEnabled()) {
            btSwitch.setChecked(true);
            imgItem.setIcon(R.drawable.ic_bt2);
        }
        btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mBluetoothAdapter.enable();
                    snackMsg("藍芽已開啟");
                    imgItem.setIcon(R.drawable.ic_bt2);
                }
                else {
                    if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                        snackMsg("藍芽已關閉");
                    }
                    imgItem.setIcon(R.drawable.ic_bt);
                }
            }
        });


        final View viewLogin = LayoutInflater.from(this).inflate(R.layout.login, null);

        loginDialog.setPositiveButton("登入", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DLog.d(TAG, "登入中...");
                snackMsg("登入中...");
                JSONObject loginJSONObject = new JSONObject();
                EditText userEditText = (EditText) viewLogin.findViewById(R.id.usr_input);
                EditText pwdEditText = (EditText) viewLogin.findViewById(R.id.pwd_input);

                try {
                    loginJSONObject.put(JSON.KEY_USER_NAME, userEditText.getText());
                    loginJSONObject.put(JSON.KEY_USER_PWD, pwdEditText.getText());
                    serverHandler.sendToServer(loginJSONObject);
                    Thread.sleep(400);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (serverHandler == null || !serverHandler.isLogin()) {
                    DLog.d(TAG, "登入失敗");
                    snackMsg("登入失敗");
                } else {
                    DLog.d(TAG, "登入成功");
                    snackMsg("登入成功");
                    View vv = navigationView.getHeaderView(0);
                    TextView userID = (TextView) vv.findViewById(R.id.userID);
                    userID.setText(serverHandler.getUsername());
                    TextView helloText = (TextView) findViewById(R.id.welcome);
                    helloText.setText("Hello, " + serverHandler.getUsername() + "!");
                    logItem.setTitle("登出");
                }
                loginDialog.dismiss();
            }
        }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.dismiss();
            }
        }).setContentView(viewLogin).setCanceledOnTouchOutside(true).setTitle("用戶登入");

        logoutDialog.setPositiveButton("登出", new View.OnClickListener(){
            JSONObject logoutJSONObject = new JSONObject();
            @Override
            public void onClick(View v) {
                DLog.d(TAG, "登出中...");
                snackMsg("登出中...");
                try {
                    logoutJSONObject.put(JSON.KEY_STATE, JSON.STATE_LOGOUT);
                    serverHandler.sendToServer(logoutJSONObject);
                    Thread.sleep(400);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(!serverHandler.isLogin()){
                    DLog.d(TAG, "登出成功");
                    snackMsg("登出成功");
                    logItem.setTitle("登入");
                }else{
                    DLog.d(TAG, "登出失敗");
                    snackMsg("登出失敗");
                }
                logoutDialog.dismiss();

            }
        }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDialog.dismiss();
            }
        }).setCanceledOnTouchOutside(true).setTitle("確定登出？");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DLog.d(TAG, "ActivityOnOptionsItemSelected");

        int id = item.getItemId();

        switch(id){
            case R.id.item_conntoserver:
                DLog.d(TAG, "連線至Server中...");
                snackMsg("連線中...");
                serverHandler = new ServerHandler();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                snackMsg("連線" + (serverHandler.clientSocket.isConnected() ? "成功" : "失敗"));
                break;
            case R.id.item_login:

                if(serverHandler == null)
                    snackMsg("請先連線");
                else if(!serverHandler.isLogin()) {
                    snackMsg("登入以使用會員功能");
                    loginDialog.show();
                }
                else {
                    snackMsg("登出會員");
                    logoutDialog.show();
                }
                break;
            case R.id.item_bluetooth:
                snackMsg("選擇藍芽裝置");
                startActivityForResult(new Intent(this, DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DLog.d(TAG, "onNavigationItemSelected");
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        ((TextView) findViewById(R.id.welcome)).setVisibility(View.INVISIBLE);
        int id = item.getItemId();
        switch (id) {
            case R.id.guide:
                fragment = new GuideFragment();
                break;
            case R.id.friend:
                fragment = new FriendFragment();
                break;
            case R.id.item:
                fragment = new ItemFragment();
                break;
            case R.id.cart:
                fragment = new CartFragment();
                break;
            case R.id.nav_send:
                break;
            case R.id.nav_share:
                break;
        }
        if(fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.main, fragment)
                    .commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DLog.d(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String deviceName = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_NAME);
                    snackMsg("連線到..." + deviceName);
                    bluetoothService = new BluetoothService();
                    bluetoothService.connectDevice(data);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    snackMsg("連線" + (bluetoothService.isConnected()? "成功" : "失敗"));
                }
                break;
        }
    }
    public void snackMsg(String msg){
        Snackbar.make(findViewById(R.id.toolbar), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
