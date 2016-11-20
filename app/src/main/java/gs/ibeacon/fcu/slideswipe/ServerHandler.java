package gs.ibeacon.fcu.slideswipe;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Key;
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
    private BluetoothService bluetoothService = BluetoothService.getInstance();

    private static boolean isMyItem = false;

    private static boolean lostItemDialog = true;

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
                        final String receiveMessage = sendFromServer.readUTF();
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

                                                for (int index = 0; index < friendLocationJSONArray.length(); index++) { // 好友
                                                    String username = friendLocationJSONArray.getJSONObject(index).getString(JSON.KEY_USER_NAME);
                                                    FriendFragment.friendListAdapter.add(Config.FRIEND_FACE_ICON + username);
                                                    FriendFragment.friendNameList.add(username);
                                                    String location = friendLocationJSONArray.getJSONObject(index).getString(JSON.KEY_LOCATION);
                                                    FriendFragment.friendLocList.add(location);
                                                }
                                                for (int index = 0; index < otherUserJSONArray.length(); index++) { // 非好友
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
                                case JSON.STATE_GET_ITEM_LOCATION:
                                    final String itemLocation = receiveObject.getString(JSON.KEY_ITEM_LOCATION);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            DLog.d(TAG, "ITEM_LOCATION : " + itemLocation);
                                            MainActivity.mainActivity.guideToTarget(itemLocation, 3);
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
                                                sendJSONObject.put(JSON.KEY_USER_NAME, receiveObject.getString(JSON.KEY_USER_NAME));
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
                                                }).setNegativeButton("不要", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        try {
                                                            sendJSONObject.put(JSON.KEY_OTHER_USER_PERMISION, false);
                                                            ServerHandler.getInstance().sendToServer(sendJSONObject);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        dialog.dismiss();
                                                    }
                                                }).setMessage(otherusername + "想知道你的位置").setTitle("位置請求").show();

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    break;
                                case JSON.STATE_RETURN_ASK_LOCATION_PERMISSION:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            boolean isPermission = false;
                                            try {
                                                isPermission = receiveObject.getBoolean(JSON.KEY_OTHER_USER_PERMISION);
                                                if(isPermission){
                                                    String targetLocation = null;
                                                    targetLocation = receiveObject.getString(JSON.KEY_TARGET_LOCATION);
                                                    MainActivity.mainActivity.guideToTarget(targetLocation, 2);
                                                }
                                                else{

                                                }
                                            } catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    break;
                                case JSON.STATE_RETURN_IS_OR_NOT_MY_ITEM:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            isMyItem = false;
                                            try {
                                                isMyItem = receiveObject.getBoolean(JSON.KEY_IS_MY_ITEM_OR_NOT);
                                            } catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    break;
                                case JSON.STATE_SEND_LOST_ITEM_LOCATION:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                final String lostItemLocation = receiveObject.getString(JSON.KEY_LOCATION);
                                                if(lostItemDialog) {
                                                    lostItemDialog = false;
                                                    AlertDialog.Builder askLocationDialog = new AlertDialog.Builder(MainActivity.mainActivity);
                                                    askLocationDialog.setPositiveButton("導航", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            try {
                                                                MainActivity.mainActivity.guideToTarget(lostItemLocation, 3);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            lostItemDialog = true;
                                                            dialog.dismiss();
                                                        }
                                                    }).setNegativeButton("已經找到遺失物", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            try {
                                                                final JSONObject sendJSONObject = new JSONObject();
                                                                sendJSONObject.put(JSON.KEY_STATE, JSON.STATE_FOUND_LOST_ITEM);
                                                                sendJSONObject.put(JSON.KEY_MINOR, receiveObject.getInt(JSON.KEY_MINOR));
                                                                ServerHandler.getInstance().sendToServer(sendJSONObject);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            lostItemDialog = true;
                                                            dialog.dismiss();
                                                        }
                                                    }).setMessage(lostItemLocation).setTitle("遺失位置").show();
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    break;
                                case JSON.STATE_ITEM_RSSI_TOO_FAR:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                NotificationCompat.Builder builder =
                                                        new NotificationCompat.Builder(MainActivity.mainActivity);

                                                Intent resultIntent = new Intent(MainActivity.mainActivity, PostLostItemActivity.class);
                                                int flags = PendingIntent.FLAG_CANCEL_CURRENT;
                                                // ONE_SHOT：      PendingIntent只使用一次；
                                                // CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的
                                                // NO_CREATE：     沿用先前的PendingIntent，不建立新的PendingIntent；
                                                // UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
                                                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.mainActivity.getApplicationContext(), 0, resultIntent, flags);

                                                builder.setSmallIcon(R.mipmap.ic_launcher)
                                                        .setWhen(System.currentTimeMillis())
                                                        .setContentTitle("物品可能遺失")
                                                        .setContentText("您的物品可能已遺失，請留意")
                                                        .setVibrate(new long[]{300, 100, 300, 10})
                                                        .setSound(soundUri)
                                                        .setAutoCancel(true)
                                                        .setNumber(1)
                                                        .setContentIntent(pendingIntent);
                                                NotificationManager mNotificationManager =
                                                        (NotificationManager) MainActivity.mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                                                mNotificationManager.notify(1000, builder.build());
                                            } catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    break;
                                case JSON.STATE_MOVE_TO_TARGET_PATH:
                                    String movePath = receiveObject.getString(JSON.KEY_MOVE_TO_TARGET_PATH);
                                    DLog.d(TAG, movePath);
                                    bluetoothService.writeData(movePath);

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
    public boolean isMyItem() {
        return isMyItem;
    }
}
