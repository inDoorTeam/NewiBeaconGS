package gs.ibeacon.fcu.slideswipe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Vibrator;
//import android.support.design.widget.FloatingActionButton;

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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.sails.engine.LocationRegion;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.overlay.Marker;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import gs.ibeacon.fcu.slideswipe.Fragment.*;
import gs.ibeacon.fcu.slideswipe.Log.*;
import gs.ibeacon.fcu.slideswipe.JSON.*;
import gs.ibeacon.fcu.slideswipe.BlueTooth.*;
import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BeaconConsumer {
    private static final int REQUEST_CONNECT_DEVICE = 1;
    public static final String TAG = "MainActivity";
    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ServerHandler serverHandler;
    private BluetoothService bluetoothService;
    private NavigationView navigationView;
    private MenuItem imgItem = null;
    private MenuItem logItem = null;
    private Switch btSwitch;
    private TextView userID;
    private TextView helloText;
    private FrameLayout mapLayout;
    public static MainActivity mainActivity;
    private MaterialDialog loginDialog;
    private MaterialDialog logoutDialog;

    private static SAILS mSails;
    private static SAILSMapView mSailsMapView;
    private Vibrator mVibrator;
    private ProgressDialog msgLoading ;
    private MaterialDialog msgLoadSuccess ;

    private BeaconManager beaconManager;
    private String myLocation = null;
    private int Rssi, Major, Minor;
    public int PreviousRssi = -1000;
    public int PreviousMajor = 0,PreviousMinor = 0;
    private List<LocationRegion> locationRegions = null;
    private Handler mHandler;
    private String Uuid = null;
    private TextView rssiText = null;
    private TextView majorText = null;
    private TextView minorText = null;
    private boolean binding = false;
    private int pathColor = 0xFF46A3FF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DLog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.mipmap.ic_launcher);
        getWindow().setStatusBarColor(getResources().getColor(R.color.toolbarU));
        actionBar.setTitle(Html.fromHtml("<font color='#00FFCC'>智慧導引</font>"));


        FloatingActionButton actionLoadingMap = (FloatingActionButton) findViewById(R.id.loadingMapAction);
        assert actionLoadingMap != null;
        actionLoadingMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadingMap();
            }
        });

        FloatingActionButton actionFindLocation = (FloatingActionButton) findViewById(R.id.findLocationAction);
        assert actionFindLocation != null;
        actionFindLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (locationRegions != null && myLocation != null) {
                        locationRegions = mSails.findRegionByLabel(myLocation);
                        mSailsMapView.getMarkerManager().clear();
                        mSailsMapView.getRoutingManager().setStartRegion(locationRegions.get(0));
                        mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegions.get(0), Marker.boundCenter(getResources().getDrawable(R.drawable.ic_start_blue_point)));
                        mSailsMapView.getRoutingManager().setStartMakerDrawable(Marker.boundCenter(getResources().getDrawable(R.drawable.ic_start_blue_point)));
                    }
                    ((FloatingActionsMenu)findViewById(R.id.multiple_actions)).toggle();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        FloatingActionButton actionClearMark = (FloatingActionButton) findViewById(R.id.clearMarkAction);
        assert actionClearMark != null;
        actionClearMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mSailsMapView.getRoutingManager().disableHandler();
                    mSailsMapView.getMarkerManager().clear();
                    ((FloatingActionsMenu)findViewById(R.id.multiple_actions)).toggle();

                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        userID = (TextView) headerView.findViewById(R.id.userID);
        helloText = (TextView) findViewById(R.id.welcome);

        mSails = new SAILS(this);
        mSails.setMode(SAILS.BLE_GFP_IMU);
        mSailsMapView = new SAILSMapView(this);
//        ((FrameLayout) findViewById(R.id.SAILSMap)).addView(mSailsMapView);
        mapLayout = (FrameLayout) findViewById(R.id.SAILSMap);
        mapLayout.addView(mSailsMapView);
        //mapLayout.setVisibility(View.INVISIBLE);
        msgLoading = new ProgressDialog(this);
        msgLoadSuccess = new MaterialDialog(this);
        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);


        rssiText = (TextView) findViewById(R.id.textRssi);
        majorText = (TextView) findViewById(R.id.textMajor);
        minorText = (TextView) findViewById(R.id.textMinor);

        mHandler = new Handler();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        startScan();
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
        if(mBluetoothAdapter.isEnabled()) {
            btSwitch.setChecked(true);
            imgItem.setIcon(R.drawable.ic_bt2);
        }

        final View viewLogin = LayoutInflater.from(this).inflate(R.layout.login, null);
        loginDialog = new MaterialDialog(this);
        loginDialog.setPositiveButton("登入", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.dismiss();
                DLog.d(TAG, "登入中...");
                snackMsg("登入中...");
                if(!serverHandler.isConnected()) {
                    snackMsg("無法連線至Server");
                    return;
                }
                JSONObject loginJSONObject = new JSONObject();
                EditText userEditText = (EditText) viewLogin.findViewById(R.id.usr_input);
                EditText pwdEditText = (EditText) viewLogin.findViewById(R.id.pwd_input);
                try {
                    loginJSONObject.put(JSON.KEY_USER_NAME, userEditText.getText());
                    loginJSONObject.put(JSON.KEY_USER_PWD, pwdEditText.getText());
                    serverHandler.sendToServer(loginJSONObject);
                    msgLoading.setTitle("登入中");
                    msgLoading.setMessage("Waiting...");
                    msgLoading.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        if (!serverHandler.isLogin()) {
                            DLog.d(TAG, "登入失敗");
                            snackMsg("登入失敗");
                        } else {
                            DLog.d(TAG, "登入成功");
                            snackMsg("登入成功");
                            userID.setText(serverHandler.getUsername());
                            helloText.setText("Hello, " + serverHandler.getUsername() + "!");
                            logItem.setTitle("登出");
                        }

                        msgLoading.dismiss();
                    }
                }, 2000);
            }
        }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.dismiss();
            }
        }).setContentView(viewLogin).setCanceledOnTouchOutside(true).setTitle("用戶登入");

        logoutDialog = new MaterialDialog(this);
        logoutDialog.setPositiveButton("登出", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DLog.d(TAG, "登出中...");
                snackMsg("登出中...");
                logoutDialog.dismiss();
                JSONObject logoutJSONObject = new JSONObject();
                try {
                    logoutJSONObject.put(JSON.KEY_STATE, JSON.STATE_LOGOUT);
                    serverHandler.sendToServer(logoutJSONObject);
                    msgLoading.setTitle("登出中");
                    msgLoading.setMessage("Waiting...");
                    msgLoading.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        if(!serverHandler.isLogin()){
                            DLog.d(TAG, "登出成功");
                            snackMsg("登出成功");
                            userID.setText(R.string.not_login);
                            helloText.setText(R.string.welcome_text);
                            logItem.setTitle("登入");
                        }else{
                            DLog.d(TAG, "登出失敗");
                            snackMsg("登出失敗");
                        }
                        msgLoading.dismiss();
                    }
                }, 2000);
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
            case R.id.item_login:
                snackMsg("連線中...");
                serverHandler = ServerHandler.getInstance();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                snackMsg("連線" + (serverHandler.clientSocket.isConnected() ? "成功" : "失敗"));

                if(!serverHandler.isLogin()) {
                    snackMsg("登入以使用會員功能");
                    loginDialog.show();
                }
                else {
                    snackMsg("用戶登出");
                    logoutDialog.show();
                }
                break;
            case R.id.item_bluetooth:
                snackMsg("連線到藍芽裝置");
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
        findViewById(R.id.welcome).setVisibility(View.INVISIBLE);
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
                    bluetoothService = BluetoothService.getInstance();
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

    public Runnable loadMapRunnable = new Runnable() {
        @Override
        public void run() {
            msgLoading.show();
            mSails.loadCloudBuilding("ad8538700fd94717bbeda154b2a1c584", "5705e42055cce32e10002a2d", new SAILS.OnFinishCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            msgLoading.dismiss();
                            msgLoadSuccess.show();

                            mSailsMapView.setSAILSEngine(mSails);
                            mSailsMapView.setLocationMarker(R.drawable.circle, R.drawable.arrow, null, 35);
                            mSailsMapView.setLocatorMarkerVisible(true);
                            mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(0));
                            mSailsMapView.autoSetMapZoomAndView();


                            mSailsMapView.setOnRegionLongClickListener(new SAILSMapView.OnRegionLongClickListener() {
                                @Override
                                public void onLongClick(List<LocationRegion> locationRegions) {
                                    if (mSails.isLocationEngineStarted())
                                        return;
                                    mVibrator.vibrate(70);
                                    mSailsMapView.getMarkerManager().clear();
                                    mSailsMapView.getRoutingManager().setStartRegion(locationRegions.get(0));
                                    mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegions.get(0), Marker.boundCenter(getResources().getDrawable(R.drawable.ic_start_blue_point)));
                                    mSailsMapView.getRoutingManager().setStartMakerDrawable(Marker.boundCenter(getResources().getDrawable(R.drawable.ic_start_blue_point)));

                                    myLocation = locationRegions.get(0).label;
                                    MainActivity.this.locationRegions = mSails.findRegionByLabel(myLocation);
                                    rssiText.setText( "當前位置 : " + myLocation);
                                    JSONObject ibeaconJSONObject = new JSONObject();
                                    if(myLocation != null) {
                                        try {
                                            ibeaconJSONObject.put(JSON.KEY_STATE, JSON.STATE_SEND_IBEACON);
                                            ibeaconJSONObject.put(JSON.KEY_LOCATION, myLocation);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if(serverHandler != null && serverHandler.isLogin())
                                        serverHandler.sendToServer(ibeaconJSONObject);
                                }
                            });

                            mSailsMapView.setOnRegionClickListener(new SAILSMapView.OnRegionClickListener() {
                                @Override
                                public void onClick(List<LocationRegion> locationRegions) {
                                    if (mSailsMapView.getRoutingManager().getStartRegion() != null) {
                                        LocationRegion lr = locationRegions.get(0);
                                        mSailsMapView.getRoutingManager().setTargetMakerDrawable(Marker.boundCenterBottom(getDrawable(R.drawable.ic_dest)));
                                        mSailsMapView.getRoutingManager().getPathPaint().setColor(pathColor);
                                        mSailsMapView.getRoutingManager().setTargetRegion(lr);
                                        mSailsMapView.getRoutingManager().enableHandler();
                                    }
                                }
                            });
                        }
                    });

                }

                public void onFailed(String response) {
                    msgLoadSuccess.setMessage("Load Failed");
                    msgLoadSuccess.show();
                }
            });
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();

        beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect() {
        try {
            //beaconManager.startMonitoringBeaconsInRegion(new Region("all-beacons-region", null, null, null ));
            beaconManager.startRangingBeaconsInRegion(new Region("ibeaconscan", null, null, null ));
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        //beaconManager.setMonitorNotifier(this);
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    org.altbeacon.beacon.Beacon beacon = beacons.iterator().next();

                    Rssi = beacon.getRssi();
                    Uuid = beacon.getId1().toUuidString();
                    Major = beacon.getId2().toInt();
                    Minor = beacon.getId3().toInt();
                    mHandler.post(scanRunnable);
                }

            }
        });
    }

    public void startScan() {
        beaconManager.bind(this);
    }

    public Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            DLog.d(TAG, "scanRun");
            if( PreviousMajor != Major || PreviousMinor != Minor ) {
                JSONObject ibeaconJSONObject = new JSONObject();
                if (Major == 4369 && Minor == 8738) {
                    myLocation = "資電234 - 網際網路及軟體工程學程實驗室";
                } else if(Major == 43690 && Minor == 65505){
                    myLocation = "資電201 - 資訊系辦公室";
                } else if(Major == 257 && Minor == 65505){
                    myLocation = "資電222 - 第三國際會議廳";
                }

                if(myLocation != null) {
                    locationRegions = mSails.findRegionByLabel(myLocation);
                    try {
                        ibeaconJSONObject.put(JSON.KEY_STATE, JSON.STATE_SEND_IBEACON);
                        ibeaconJSONObject.put(JSON.KEY_LOCATION, myLocation);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(serverHandler != null && serverHandler.isLogin())
                    serverHandler.sendToServer(ibeaconJSONObject);

            }
            PreviousRssi = Rssi;
            PreviousMajor = Major;
            PreviousMinor = Minor;

//            UuidText.setText( "Uuid  : " + Uuid);
//            rssiText.setText( "Rssi  : " + Rssi);
//            majorText.setText("Major : " + Major);
//            minorText.setText("Minor : " + Minor);
            if(myLocation != null){
                rssiText.setText( "當前位置 : " + myLocation);
            }
        }
    };
    public void guideToTarget(String targetLocation, int imageView){
        DLog.d(TAG, "Guide to : " + targetLocation);
        try {
            locationRegions = mSails.findRegionByLabel(myLocation);
            mSailsMapView.getRoutingManager().setStartRegion(locationRegions.get(0));

            mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegions.get(0), Marker.boundCenter(getResources().getDrawable(R.drawable.ic_start_blue_point)));
            mSailsMapView.getRoutingManager().setStartMakerDrawable(Marker.boundCenter(getResources().getDrawable(R.drawable.ic_start_blue_point)));

            locationRegions = mSails.findRegionByLabel(targetLocation);
            LocationRegion lr = locationRegions.get(0);
            int image = 0;
            switch(imageView){
                case 1:
                    image = R.drawable.ic_dest;
                    break;
                case 2:
                    image = R.drawable.ic_search_user;
                    break;
                case 3:
                    image = R.drawable.ic_item;
                    break;
                case 4: // 人對車
                    image = R.drawable.ic_blue_cart;
                    break;
                case 5: // 車對人
                    image = R.drawable.ic_user;
                    mSailsMapView.getRoutingManager().setStartMakerDrawable(Marker.boundCenter(getResources().getDrawable(R.drawable.ic_blue_cart)));
                    break;
            }

            mSailsMapView.getRoutingManager().setTargetMakerDrawable(Marker.boundCenterBottom(getDrawable(image)));

            mSailsMapView.getRoutingManager().getPathPaint().setColor(pathColor);
            mSailsMapView.getRoutingManager().setTargetRegion(lr);
            mSailsMapView.getRoutingManager().enableHandler();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public SAILS getSails(){
        return mSails;
    }
    public void setBindingState(boolean binding){
        this.binding = binding;
    }
    public boolean getBindingState(){
        return binding;
    }
    public void reloadingMap(){
        helloText.setVisibility(View.INVISIBLE);
        ((FloatingActionsMenu)findViewById(R.id.multiple_actions)).toggle();
        msgLoading.setTitle("載入地圖中");
        msgLoading.setMessage("Waiting ...");
        msgLoadSuccess.setPositiveButton("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgLoadSuccess.dismiss();
            }
        }).setCanceledOnTouchOutside(true).setTitle("載入地圖").setMessage("載入成功！");
        mSailsMapView.post(loadMapRunnable);
        try{
            mSailsMapView.getRoutingManager().disableHandler();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        mSailsMapView.getMarkerManager().clear();
    }
}