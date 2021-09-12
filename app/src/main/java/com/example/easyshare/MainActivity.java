package com.example.easyshare;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.easyshare.module.FragmentServers;
import com.example.easyshare.module.GetPermission;
import com.example.easyshare.network.Detector;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public FragmentServers fragmentServers;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GetPermission.getPermission(this);
    }

    @Override
    protected void onDestroy(){
        Detector.getInstance().saveFile(this);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(1000, 100, 1, R.string.refresh);
        menu.add(1000, 101, 2, R.string.add_server);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case 100:
                System.out.println("[FINE](MainActivity) options menu refresh");
                fragmentServers.refresh();
                break;
            case 101:
                System.out.println("[FINE](MainActivity) options menu add server");
                fragmentServers.addServer();
        }
        return super.onOptionsItemSelected(item);
    }
}
