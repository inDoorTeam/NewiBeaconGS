package gs.ibeacon.fcu.slideswipe;

import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

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

import gs.ibeacon.fcu.slideswipe.JSON.*;
import gs.ibeacon.fcu.slideswipe.Log.*;

/**
 * Created by bing on 2016/6/10.
 */
public class ServerHandler {
    public static Socket clientSocket = new Socket();
    DataOutputStream outToServer;
    DataInputStream sendFromServer;
    protected static boolean isLogin = false;
    private String address = "192.168.43.122";
    private int port = 8766;
    private Handler mHandler;
    public ServerHandler(){
        DLog.d("ServerHandlerCreate");
        (new Thread(connecttoServer)).start();
    }
    public Runnable connecttoServer = new Runnable() {


        @Override
        public void run() {
            DLog.d("connecttoServerRun");

            try {
                clientSocket = new Socket(InetAddress.getByName(address), port);
                outToServer = new DataOutputStream( clientSocket.getOutputStream() );
                sendFromServer = new DataInputStream( clientSocket.getInputStream() );
                JSONObject receiveObject;
                if(!clientSocket.isInputShutdown()) {
                    String receiveMessage = sendFromServer.readUTF();
                    if(receiveMessage != null){
                        receiveObject = new JSONObject(receiveMessage);
                        isLogin = receiveObject.getBoolean(JSON.KEY_RESULT);
                        DLog.d("LOGIN ? " + isLogin);
                        if(isLogin){
                            (new Thread(serverhandler)).start();
                        }
                        else{
                        }
                    }

                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public Runnable serverhandler = new Runnable(){
        @Override
        public void run() {
            DLog.d("serverhandlerRun");
            try {
                while (true) {
                    final JSONObject receiveObject;
                    if (clientSocket.isConnected()) {
                        String receiveMessage = null;
                        receiveMessage = sendFromServer.readUTF();
                        if (receiveMessage != null) {
                            receiveObject = new JSONObject(receiveMessage);
                            int state = receiveObject.getInt(JSON.KEY_STATE);
                            switch(state){
                                case JSON.STATE_WHOAMI:
                                    String name = receiveObject.getString(JSON.KEY_USER_NAME);
                                    System.out.println("You are :" + name);
                                    break;
                                case JSON.STATE_FIND_FRIEND:

                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public void sendtoServer(JSONObject sendtoServer){
        if(clientSocket.isConnected()) {
            try {
                outToServer.writeUTF(sendtoServer.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
