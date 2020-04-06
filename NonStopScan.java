package com.example.safezone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;


public class NonStopScan extends Service {

    public void showToast(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    // assigning the default bluetooth adapter to BluetoothAdapter object "bt"
    BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();

    public void startBluetooth() {
        if (bt != null) {
            if (bt.isEnabled()) {
                bt.startDiscovery();
                showToast("Scanning initiated");
            } else {
                bt.enable();
                bt.startDiscovery();
                showToast("Scanning initiated");
            }

            /**
             * sends intent "ACTION_FOUND" to the broadcastReciever
             * to perform a specific task as soon as a new device is found.
             */
            IntentFilter f1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(myReciever, f1);

            /**
             * sends intent "ACTION_DISCOVERY_FINISHED" to the broadcastReciever
             * to preform a specific task as soon as discovery is finished
             */
            IntentFilter f2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(myReciever, f2);

        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
    }
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        // do your jobs here

        startForeground();

       return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build());
                startBluetooth();
    }


    BroadcastReceiver myReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context p1, Intent p2) {
            String action = p2.getAction();
            // tasks to perform when a new BT tag is found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = p2.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String address = device.getAddress();
                showToast("device found");

                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showToast("scanning restarted");
                bt.startDiscovery();


            }
        }
    };
                @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("RestartService");
        broadcastIntent.setClass(this, RestartService.class);
        this.sendBroadcast(broadcastIntent);
    }

}

