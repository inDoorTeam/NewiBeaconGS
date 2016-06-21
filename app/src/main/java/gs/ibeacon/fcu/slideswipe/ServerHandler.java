package gs.ibeacon.fcu.slideswipe;

import android.content.DialogInterface;
import android.os.Handler;

import com.sails.engine.LocationRegion;
import com.sails.engine.overlay.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import gs.ibeacon.fcu.slideswipe.BlueTooth.BluetoothService;
import gs.ibeacon.fcu.slideswipe.Fragment.FriendFragment;
import gs.ibeacon.fcu.slideswipe.JSON.*;
import gs.ibeacon.fcu.slideswipe.Log.*;

/**
 * Created by bing on 2016/6/10.
 */
public class ServerHandler {
    public static Socket clientSocket = new Socket();
    public static final String TAG = "ServerHandler";
    private DataInputStream sendFromServer;
    private DataOutputStream sendToServer;
    private String username = null;
    private String address = "140.134.226.181";
    private Handler mHandler = new Handler();
    private static boolean isLogin = false;
    private int port = 8766;
    private static ServerHandler serverHandler = null;
    public static ServerHandler getInstance() {
        if(serverHandler == null || !isLogin)
            serverHandler = (new ServerHandler());
        return serverHandler;
    }

    public ServerHandler(){
        DLog.d(TAG, "ServerHandler");
        (new Thread(connectToServer)).start();
    }
    public Runnable connectToServer = new Runnable() {
        @Override
        public void run() {
            DLog.d(TAG, "connectToServerRun");
            try {
                clientSocket = new Socket(InetAddress.getByName(address), port);
                sendToServer = new DataOutputStream( clientSocket.getOutputStream() );
                sendFromServer = new DataInputStream( clientSocket.getInputStream() );
                JSONObject receiveObject;
                if(!clientSocket.isInputShutdown()) {
                    String receiveMessage = sendFromServer.readUTF();
                    DLog.d(TAG, receiveMessage);
                    if(receiveMessage != null){
                        receiveObject = new JSONObject(receiveMessage);
                        isLogin = receiveObject.getBoolean(JSON.KEY_RESULT);
                        DLog.d(TAG, "LOGIN ? " + isLogin);
                        if(isLogin){
                            username = receiveObject.getString(JSON.KEY_USER_NAME);
                            (new Thread(serverHandlerRunnable)).start();
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public Runnable serverHandlerRunnable = new Runnable(){
        @Override
        public void run() {
            DLog.d(TAG, "serverHandlerRun");
            try {
                while (true) {
                    final JSONObject receiveObject;
                    if (clientSocket.isConnected()) {
                        String receiveMessage = sendFromServer.readUTF();
                        DLog.d(TAG, receiveMessage);
                        if (receiveMessage != null) {
                            receiveObject = new JSONObject(receiveMessage);
                            int state = receiveObject.getInt(JSON.KEY_STATE);
                            DLog.d(TAG, "state : " + state);
                            switch(state){
                                case JSON.STATE_WHOAMI:
                                    String name = receiveObject.getString(JSON.KEY_USER_NAME);
                                    System.out.println("You are :" + name);
                                    break;
                                case JSON.STATE_FIND_FRIEND:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            FriendFragment.friendListAdapter.clear();
                                            FriendFragment.friendNameList.clear();
                                            FriendFragment.friendLocList.clear();
                                            try {
                                                JSONArray friendLocationJSONArray = receiveObject.getJSONArray(JSON.KEY_USER_LIST);
                                                for(int index = 0 ; index < friendLocationJSONArray.length() ; index ++){
                                                    String username = friendLocationJSONArray.getJSONObject(index).getString(JSON.KEY_USER_NAME);
                                                    String location = friendLocationJSONArray.getJSONObject(index).getString(JSON.KEY_LOCATION);
                                                    FriendFragment.friendListAdapter.add(username);
                                                    FriendFragment.friendNameList.add(username);
                                                    FriendFragment.friendLocList.add(location);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    break;
                                case JSON.STATE_LOGOUT:
                                    isLogin = !receiveObject.getBoolean(JSON.KEY_RESULT);
                                    break;
                                case JSON.STATE_CAR_MOVE:
                                    BluetoothService bluetoothService = BluetoothService.getInstance();
                                    if(bluetoothService != null && bluetoothService.isConnected()){
                                        bluetoothService.writeData("r");
                                    }
                                    break;

                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public void sendToServer(JSONObject JSONToServer){
        if(clientSocket.isConnected()) {
            try {
                sendToServer.writeUTF(JSONToServer.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean isLogin(){
        return isLogin;
    }
    public String getUsername(){
        return username;
    }
    public boolean isConnected(){
        return clientSocket.isConnected();
    }
}
