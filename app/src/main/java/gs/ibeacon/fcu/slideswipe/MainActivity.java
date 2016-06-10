package gs.ibeacon.fcu.slideswipe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
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

import org.json.JSONObject;

import gs.ibeacon.fcu.slideswipe.Fragment.*;
import gs.ibeacon.fcu.slideswipe.Log.*;
import gs.ibeacon.fcu.slideswipe.JSON.*;
import gs.ibeacon.fcu.slideswipe.BlueTooth.*;
import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ServerHandler serverHandler;
    private NavigationView navigationView;
    private MenuItem imgitem = null;
    private Switch BtSwitch;
    private BluetoothService bluetoothService;
    public static MainActivity m;
    public static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DLog.d(TAG, "ActivityOnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.mipmap.ic_launcher);
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
        DLog.d(TAG, "ActivityOnBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DLog.d(TAG, "ActivityOnCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.myswitch);
        imgitem = menu.findItem(R.id.bticon);
        item.setActionView(R.layout.switch_of_bluetooth);
        imgitem.setIcon(R.drawable.ic_bt);
        BtSwitch = (Switch) menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchofbt);
        if(mBluetoothAdapter.isEnabled()) {
            BtSwitch.setChecked(true);
            imgitem.setIcon(R.drawable.ic_bt2);
        }

        BtSwitch.setOnCheckedChangeListener(new MyOnClickListener());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DLog.d(TAG, "ActivityOnOptionsItemSelected");

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.connect:
                snackMsg("連線中...");
                serverHandler = new ServerHandler();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                snackMsg("連線" + (serverHandler.clientSocket.isConnected() ? "成功" : "失敗"));
                break;
            case R.id.login:
                snackMsg("登入以使用會員功能");
                final View viewLogin = LayoutInflater.from(this).inflate(R.layout.login, null);
                final MaterialDialog loginDialog = new MaterialDialog(this);
                loginDialog.setTitle("用戶登入");

                loginDialog.setContentView(viewLogin).setCanceledOnTouchOutside(true);
                loginDialog.setPositiveButton("Login", new View.OnClickListener() {
                        int i = 0;
                        @Override
                        public void onClick(View v) {
                            snackMsg("登入中...");
                            DLog.d(TAG, v.getId()+"");
                            JSONObject loginJSONObject = new JSONObject();
                            EditText userEditText = (EditText) viewLogin.findViewById(R.id.usr_input);
                            EditText pwdEditText = (EditText) viewLogin.findViewById(R.id.pwd_input);

                            try {
                                loginJSONObject.put(JSON.KEY_USER_NAME, userEditText.getText());
                                loginJSONObject.put(JSON.KEY_USER_PWD, pwdEditText.getText());
                                serverHandler.sendtoServer(loginJSONObject);
                                Thread.sleep(400);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (serverHandler == null || !serverHandler.getLoginState()) {
                                snackMsg("登入失敗");
                            }
                            else {
                                View vv = navigationView.getHeaderView(0);
                                TextView userID = (TextView) vv.findViewById(R.id.userID);
                                userID.setText("Hello, " + serverHandler.getUsername() + "!");
                                TextView helloText = (TextView) findViewById(R.id.welcome);
                                helloText.setText("Hello, " + serverHandler.getUsername() + "!");
                                snackMsg("登入成功");
                            }
                            loginDialog.dismiss();
                        }
                    })
                    .setNegativeButton("CANCEL", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loginDialog.dismiss();
                        }
                    }).show();
                break;
            case R.id.item_bluetooth:
                snackMsg("選擇藍芽裝置");
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DLog.d(TAG, "ActivityOnNavigationItemSelected");
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


    public class MyOnClickListener implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                mBluetoothAdapter.enable();
                snackMsg("藍芽已開啟");
                imgitem.setIcon(R.drawable.ic_bt2);
            }
            else {
                if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    snackMsg("藍芽已關閉");
                }
                imgitem.setIcon(R.drawable.ic_bt);
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
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
                    snackMsg("連線" + (bluetoothService.btSocket.isConnected()? "成功" : "失敗"));
                }
                break;
        }
    }
    public void snackMsg(String msg){
        Snackbar.make(findViewById(R.id.toolbar), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
