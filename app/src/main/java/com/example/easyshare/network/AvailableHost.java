package com.example.easyshare.network;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easyshare.R;

public class AvailableHost{
    private final Detector detector;
    private final Sender sender;
    private int status;

    private final String[] statuss={
            "instance init",
            "on network scanning",
            "on network scanning end",
            "nothing to do"
    };

    public AvailableHost(TextView textView){
        status = 0;
        detector = Detector.getInstance();
        sender = Sender.getInstance();
        textView.setText(R.string.wait_scan);
    }

    public void refresh(Activity activity, TextView textView){

        if (status == 1 || status == 2) return;
        textView.setText(R.string.wait_scan);
        new Thread(()->{
            status = 1;
            detector.detect();
            status = 2;
            activity.runOnUiThread(()->{
                if (sender.IsIPSet()){
                    textView.setText(sender.getAvailable().toString());
                }else{
                    textView.setText(R.string.no_server);
                    Toast.makeText(activity, R.string.no_server, Toast.LENGTH_SHORT).show();
                }
            });
            status = 3;
        }).start();
    }

    public Server getAvailable(){
        return sender.getAvailable();
    }

    public boolean availalbe(){
        return sender.IsIPSet();
    }

    public String getStatus(){
        return statuss[status];
    }
}
