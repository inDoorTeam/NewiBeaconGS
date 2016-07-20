package gs.ibeacon.fcu.slideswipe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import gs.ibeacon.fcu.slideswipe.BlueTooth.BluetoothService;
import gs.ibeacon.fcu.slideswipe.Fragment.CartFragment;
import gs.ibeacon.fcu.slideswipe.Fragment.FriendFragment;
import gs.ibeacon.fcu.slideswipe.Fragment.ItemFragment;
import gs.ibeacon.fcu.slideswipe.JSON.*;
import gs.ibeacon.fcu.slideswipe.Log.*;

/**
 * Created by bing on 2016/6/10.
 */
public class ServerHandler {
    public Socket clientSocket = new Socket();
    public static final String TAG = "ServerHandler";
    private DataInputStream sendFromServer;
    private DataOutputStream sendToServer;
    private String username = null;
    private Handler mHandler = new Handler();
    private static boolean isLogin = false;
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
            String serverIP = MainActivity.mainActivity.getSharedPreferences(Config.TEMP_DATA_FILE_NAME, MainActivity.mainActivity.MODE_PRIVATE).getString(Config.TEMP_DATA_SERVER_IP, null);
            serverIP = serverIP == null ? Config.serverIP : serverIP;
            try {
                clientSocket = new Socket(InetAddress.getByName(serverIP), Config.PORT);
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
                        DLog.d(TAG, receiveMessage + "\n" );
                        if (receiveMessage != null) {
                            receiveObject = new JSONObject(receiveMessage);
                            int state = receiveObject.getInt(JSON.KEY_STATE);

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
                                                JSONArray friendLocationJSONArray = receiveObject.getJSONArray(JSON.KEY_FRIEND_LIST);
                                                JSONArray otherUserJSONArray = receiveObject.getJSONArray(JSON.KEY_OTHERUSER_LIST);

                                                for(int index = 0 ; index < friendLocationJSONArray.length() ; index ++){
                                                    String username = friendLocationJSONArray.getJSONObject(index).getString(JSON.KEY_USER_NAME);
                                                    FriendFragment.friendListAdapter.add(Config.FRIEND_FACE_ICON + username);
                                                    FriendFragment.friendNameList.add(username);
                                                    String location = friendLocationJSONArray.getJSONObject(index).getString(JSON.KEY_LOCATION);
                                                    FriendFragment.friendLocList.add(location);
                                                }
                                                for(int index = 0 ; index < otherUserJSONArray.length() ; index ++){
                                                    String username = otherUserJSONArray.getJSONObject(index).getString(JSON.KEY_USER_NAME);
                                                    FriendFragment.friendListAdapter.add(username);
                                                    FriendFragment.friendNameList.add(username);
                                                    FriendFragment.friendLocList.add(JSON.MESSAGE_NOLOATION);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    break;
                                case JSON.STATE_FIND_ITEM_LIST:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                ArrayList<String> itemNameList = new ArrayList<>();
                                                ArrayList<String> itemLocationList = new ArrayList<>();
                                                JSONArray itemListJSONArray = receiveObject.getJSONArray(JSON.KEY_ITEM_LIST);
                                                for(int index = 0 ; index < itemListJSONArray.length() ; index ++){
                                                    String itemName = itemListJSONArray.getJSONObject(index).getString(JSON.KEY_ITEM_NAME);
                                                    String itemLocation = itemListJSONArray.getJSONObject(index).getString(JSON.KEY_LOCATION);
                                                    itemNameList.add(itemName);
                                                    itemLocationList.add(itemLocation);
                                                }
                                                ItemFragment.getInstance().modifyItemList(itemNameList, itemLocationList);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    break;
                                case JSON.STATE_LOGOUT:
                                    isLogin = !receiveObject.getBoolean(JSON.KEY_RESULT);
                                    break;
                                case JSON.STATE_USER_MOVE:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            BluetoothService bluetoothService = BluetoothService.getInstance();
                                            if(bluetoothService != null && bluetoothService.isConnected()){
                                                bluetoothService.writeData("r");
                                            }
                                            String userLocation = null;
                                            try {
                                                userLocation = receiveObject.getString(JSON.KEY_TARGET_LOCATION);
                                                CartFragment.targetLocation = userLocation;
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            DLog.d(TAG, userLocation);
                                        }
                                    });
                                    break;
                                case JSON.STATE_FIND_TARGET_LOCATION:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            DLog.d(TAG, "FIND_TARGET_LOCATION");
                                            String userLocation = null;
                                            try {
                                                userLocation = receiveObject.getString(JSON.KEY_TARGET_LOCATION);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            DLog.d(TAG, "USER_LOCATION : " + userLocation);
                                            MainActivity.mainActivity.guideToTarget(userLocation, 4);
                                            CartFragment.setLocationText(userLocation);
                                        }
                                    });
                                    break;
                                case JSON.STATE_ASK_LOCATION_PERMISSION:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                String otherusername = receiveObject.getString(JSON.KEY_USER_NAME);
                                                final AlertDialog.Builder askLocationDialog = new AlertDialog.Builder(MainActivity.mainActivity);
                                                final JSONObject sendJSONObject = new JSONObject();
                                                sendJSONObject.put(JSON.KEY_STATE, JSON.STATE_RETURN_ASK_LOCATION_PERMISSION);
                                                askLocationDialog.setPositiveButton("允許", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        try {
                                                            sendJSONObject.put(JSON.KEY_OTHER_USER_PERMISION, true);
                                                            ServerHandler.getInstance().sendToServer(sendJSONObject);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).setPositiveButton("不要", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        try {
                                                            sendJSONObject.put(JSON.KEY_OTHER_USER_PERMISION, true);
                                                            ServerHandler.getInstance().sendToServer(sendJSONObject);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        dialog.dismiss();
                                                    }
                                                }).setMessage(otherusername + "想知道你的位置").setTitle("位置請求");

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
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
                sendToServer.flush();
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
