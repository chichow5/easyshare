package com.example.easyshare.plugin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.easyshare.R;
import com.example.easyshare.network.Sender;

public class plugin_text extends AppCompatActivity {

    private Sender sender;
    TextView tv_input;
    Button btn_send;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_text);
        bindInstance();
    }
    void bindInstance(){
        sender = Sender.getInstance();
        tv_input = findViewById(R.id.tv_input);
        btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(v -> {
            sender.setText(tv_input.getText().toString(),"simple_text");
            new Thread((Runnable) () -> {
                sender.send();
                //Toast.makeText(plugin_text.this,"Send finished",Toast.LENGTH_SHORT).show();
            }).start();
        });
    }
}