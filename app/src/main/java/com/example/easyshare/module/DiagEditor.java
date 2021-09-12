package com.example.easyshare.module;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.easyshare.R;
import com.example.easyshare.network.Server;

import java.net.UnknownHostException;

public class DiagEditor  {
    private EditText et_ip;
    private EditText et_port;
    private AlertDialog.Builder builder = null;
    private View diagView = null;
    private Context mContext;
    private Server server,  t;
    private Server.Callback callback;
    private Object synLock = new Object();

    public DiagEditor(Context context) {
        this.mContext = context;
        builder = new AlertDialog.Builder(context);
        diagView = LayoutInflater.from(context).inflate(R.layout.ip_port_edit_diag, null);
        builder.setView(diagView);
        builder.setTitle("Edit IP and port");
        et_ip = diagView.findViewById(R.id.ev_diag_ip);
        et_port = diagView.findViewById(R.id.ev_diag_port);
        builder.setPositiveButton("OK", this::onClickOK);
        builder.setNegativeButton("Cancel", this::onClickCancel);
    }

    /**
     *
     * @param s null if it's creating, else modifying
     * @param cl
     */
    public void Edit(Server s, Server.Callback cl) {
        this.callback = cl;
        this.server = s;

        builder = new AlertDialog.Builder(mContext);
        diagView = LayoutInflater.from(mContext).inflate(R.layout.ip_port_edit_diag, null);
        builder.setView(diagView);
        builder.setTitle("Edit IP and port");
        et_ip = diagView.findViewById(R.id.ev_diag_ip);
        et_port = diagView.findViewById(R.id.ev_diag_port);
        builder.setPositiveButton("OK", this::onClickOK);
        builder.setNegativeButton("Cancel", this::onClickCancel);
        if (s == null) {
            et_ip.setText("");
            et_port.setText("");
        } else {
            et_ip.setText(s.IP);
            et_port.setText(Integer.toString(s.port));
        }
        builder.show();
    }

    public void onClickCancel(DialogInterface dialog, int which){
        Toast.makeText(mContext,"Action canceled",Toast.LENGTH_SHORT).show();
    }
    public void onClickOK(DialogInterface dialog, int which) {
        synchronized (synLock) {
            boolean flag = false;
            if (et_ip.getText() == null || et_port.getText() == null) { flag = true; }
            else if (!IPCheck(et_ip.getText().toString(),et_port.getText().toString())) { flag = true; }

            if (!flag) try {
                this.t = new Server(et_ip.getText().toString(), Integer.parseInt(et_port.getText().toString()));
            } catch (UnknownHostException e) {
                System.err.println("[error]:(diag get ip&port):UnknownHostException :" + e.getMessage());
                flag = true;
            } catch(Exception e){
                //Incorrect ip will cause an Exception which UnknownHostException can't catch
                flag = true;
            }
            if (flag){
                Toast.makeText(mContext, "bad IP or port", Toast.LENGTH_SHORT).show();
                return;
            }

            if (server == null){
                callback.callback(t);
                //null object server means the caller wants to add, deliever to it
                return;
            }
            try {
                server.setIP(t.IP);//t.IP already checked before
                server.setPort(t.port);
            } catch (Exception ignored) { }
            //thNumberFormatExceptione non-null object server has been modified
            //callback only for NotifyDataChange()
            callback.callback(null);
        }
    }
    boolean IPCheck(String IP, String port){
        if (IP == null || port == null) return false;
        int t;
        try{
            t = Integer.parseInt(port);
        }catch(NumberFormatException e){
            return false;
        }
        if (t<0 || t>65535) return false;

        String[] nums = IP.split("\\.");
        if (nums.length!=4) return false;
        for (String num:nums){
            try{
                t = Integer.parseInt(num);
            }catch(NumberFormatException e){
                return false;
            }
            if (t<0 || t>255) return false;
        }
        return true;
    }
}