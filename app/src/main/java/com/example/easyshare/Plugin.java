package com.example.easyshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.easyshare.plugin.plugin_file;
import com.example.easyshare.plugin.plugin_text;

public class Plugin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin);
        TextView tvt = findViewById(R.id.tv_sendtext);
        tvt.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(Plugin.this, plugin_text.class);
            startActivity(intent);
        });
        TextView tvf = findViewById(R.id.tv_sendfile);
        tvf.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(Plugin.this, plugin_file.class);
            startActivity(intent);
        });
    }
}