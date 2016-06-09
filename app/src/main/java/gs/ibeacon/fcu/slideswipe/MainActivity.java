package gs.ibeacon.fcu.slideswipe;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import gs.ibeacon.fcu.slideswipe.Fragment.*;
import gs.ibeacon.fcu.slideswipe.Log.*;
import gs.ibeacon.fcu.slideswipe.JSON.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ServerHandler serverHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DLog.d("ActivityOnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DLog.d("ActivityOnBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DLog.d("ActivityOnCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DLog.d("ActivityOnOptionsItemSelected");

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.connect:
                snackMsg("連線中...");
                serverHandler = new ServerHandler();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                snackMsg("連線" + (serverHandler.clientSocket.isConnected() ? "成功" : "失敗"));
                break;
            case R.id.login:
                LayoutInflater factory = LayoutInflater.from(this);
                final View view = factory.inflate(R.layout.login, null);
                AlertDialog.Builder loginDialog = new AlertDialog.Builder(this);
                //loginDialog.setTitle("Login");
                loginDialog.setView(view);
                loginDialog.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        JSONObject loginJSONObject = new JSONObject();
                        EditText userEditText = (EditText) view.findViewById(R.id.usr_input);
                        EditText pwdEditText = (EditText) view.findViewById(R.id.pwd_input);

                        try {
                            loginJSONObject.put(JSON.KEY_USER_NAME, userEditText.getText());
                            loginJSONObject.put(JSON.KEY_USER_PWD, pwdEditText.getText());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        serverHandler.sendtoServer(loginJSONObject);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        snackMsg("登入" + (serverHandler.isLogin ? "成功" : "失敗"));
                    }
                });
                loginDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                loginDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DLog.d("ActivityOnNavigationItemSelected");
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
    public void snackMsg(String msg){
        Snackbar.make(findViewById(R.id.toolbar), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
