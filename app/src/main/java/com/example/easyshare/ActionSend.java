package com.example.easyshare;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easyshare.module.FragmentServers;
import com.example.easyshare.network.Sender;

import java.util.ArrayList;

public class ActionSend extends AppCompatActivity {

    Sender sender;
    TextView tv_detail;
    Button cfm_send;
    boolean sent = false;

    String msg = "no server available";
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_send);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (type == null || !Intent.ACTION_SEND.equals(action)){
            msg = "bad type or Intent ACTION!";
            Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
        }else{
            sender = Sender.getInstance();
            sender.setByIntent(this, getIntent());

            tv_detail = findViewById(R.id.show_type);
            tv_detail.setText(sender.generateHeader());
            cfm_send = findViewById(R.id.btn_confirm_send);
            cfm_send.setOnClickListener(this::SendButtonOnClick);
        }
    }

    private void SendButtonOnClick(View v){
        if (sender.ready() && !sent){
            new Thread(()->{
                int re = sender.send();
                runOnUiThread(()->{
                    if (re == -1){
                        msg = "error sending";
                        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this,"finished",Toast.LENGTH_SHORT).show();
                        sent = true;
                        msg = "already sent";
                    }
                });
            }).start();
        }else{
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

}