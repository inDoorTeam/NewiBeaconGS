package gs.ibeacon.fcu.slideswipe.BlueTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import gs.ibeacon.fcu.slideswipe.Log.DLog;
import gs.ibeacon.fcu.slideswipe.MainActivity;

/**
 * Created by bing on 2016/6/10.
 */
public class BluetoothService {
    private OutputStream outStream = null;
    private BluetoothSocket btSocket = null;
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "BluetoothService";
    private BluetoothAdapter mBluetoothAdapter = null;
    public BluetoothService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    private static BluetoothService bluetoothService = null;
    public static BluetoothService getInstance(){
        if(bluetoothService == null)
            bluetoothService = new BluetoothService();
        return bluetoothService;
    }
    public void connectDevice(Intent data) {
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            btSocket.connect();
            DLog.d(TAG, "藍芽已連線");
        }
        catch (IOException e) {
            DLog.d(TAG, "藍芽socket建立失敗");
        }

    }
    public void writeData(String data) {
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            DLog.d(TAG, "OutStream 取得失敗");
        }

        byte[] msgBuffer = data.getBytes();

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            DLog.d(TAG, "藍芽訊息傳送失敗");
        }
    }
    public boolean isConnected(){
        try {
            return btSocket.isConnected();
        }
        catch (Exception e){
            return false;
        }
    }

}
