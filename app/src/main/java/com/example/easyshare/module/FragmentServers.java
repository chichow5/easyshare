package com.example.easyshare.module;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.easyshare.ActionSend;
import com.example.easyshare.MainActivity;
import com.example.easyshare.Plugin;
import com.example.easyshare.R;
import com.example.easyshare.network.AvailableHost;
import com.example.easyshare.network.Detector;
import com.example.easyshare.network.Sender;

public class FragmentServers extends Fragment {

    Detector detector;
    TextView tv_ip;
    AvailableHost availableHost;
    ListView lv_ip;
    IpList ipList;

    DiagEditor diagEditor;
    Button btn_add;
    Button btn_refresh;

    public FragmentServers() { }

    public static FragmentServers newInstance() { return new FragmentServers(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View re = inflater.inflate(R.layout.fragment_servers, container, false);

        tv_ip = re.findViewById(R.id.fs_available_server);
        availableHost = new AvailableHost(tv_ip);
        if (getActivity() instanceof MainActivity){
            ((MainActivity) getActivity()).fragmentServers = this;
            ((ConstraintLayout) re.findViewById(R.id.fs_constraintLayout)).setVisibility(View.GONE);
            tv_ip.setOnClickListener((v)->{
                if(Sender.getInstance().IsIPSet()){
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), Plugin.class);
                    startActivity(intent);
                }
            });
        }

        lv_ip = re.findViewById(R.id.fs_stored_ip);
        detector = Detector.getInstance();
        detector.loadFile(getContext());
        ipList = new IpList(detector, getContext(), lv_ip);

        availableHost.refresh(getActivity(), tv_ip);

        btn_add = re.findViewById(R.id.fs_btn_add);
        diagEditor = new DiagEditor(getContext());
        btn_add.setOnClickListener((v)->{
            System.out.println("[INFO](FragmentServer) add button pressed");
            addServer();
        });

        btn_refresh = re.findViewById(R.id.fs_btn_refresh);
        btn_refresh.setOnClickListener((v)->{
            refresh();
        });

        return re;
    }

    public void refresh(){
        availableHost.refresh(getActivity(), tv_ip);
    }

    public void addServer(){
        diagEditor.Edit(null, ipList.getAdapter());
    }
}