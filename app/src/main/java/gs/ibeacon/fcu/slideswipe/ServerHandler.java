package gs.ibeacon.fcu.slideswipe;

import android.os.Handler;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

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
    private String address = "192.168.43.122";
    private Handler mHandler = new Handler();
    private boolean isLogin = false;
    private int port = 8766;

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
                            (new Thread(serverHandler)).start();
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public Runnable serverHandler = new Runnable(){
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
                            switch(state){
                                case JSON.STATE_WHOAMI:
                                    String name = receiveObject.getString(JSON.KEY_USER_NAME);
                                    System.out.println("You are :" + name);
                                    break;
                                case JSON.STATE_FIND_FRIEND:
                                    break;
                                case JSON.STATE_LOGOUT:
                                    isLogin = !receiveObject.getBoolean(JSON.KEY_RESULT);
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
}
