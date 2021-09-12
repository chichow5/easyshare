package com.example.easyshare.network;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.example.easyshare.module.PathResolver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Example header
 * I want to send a packagefile;GEM.mp3;1896;(then come with binary content)
 * I want to send a packagetext;xxxxxxx;88;(then come with char sequence)
 * Notice: no sepetate between package & type
 */
public class Sender {

    private static Sender _instace = null;

    public static synchronized Sender getInstance(){
        if (_instace == null){
            _instace = new Sender();
        }
        return _instace;
    }


    public static final String HeadSeparator = ";";
    public static final String str_need = "I want to send a package";
    public static final String FILE = "file";
    public static final String TEXT = "text";
    public static final int MAXLINE = 4096;
    private InputStream payloadStream;

    private Server server;
    private String IP;
    private int port;

    private boolean ip_is_set, payload_is_set;
    private String type;
    private String path;
    private String description;
    /* description:
     * file: filename
     * text: website|clipboard|etc... remain using for future
     */
    private long length;

    private Sender(){
        ip_is_set = false;
        payload_is_set = false;
    }

    public synchronized void setPath(String path){
        if (payload_is_set) {
            if (payloadStream != null) {
                try {
                    payloadStream.close();
                } catch(IOException ignored) {}
            }
            payloadStream = null;
        }
        this.type = FILE;
        this.path = path;
        String[] tmp = path.split(File.separator);
        this.description = tmp[tmp.length-1];//get filename
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            System.err.println("[error](sender set file): file doesn't "
                    +"exist or is not a file or can't be read");
            return;
        }
        this.length = file.length();
        payload_is_set = true;
        try {
            payloadStream = new FileInputStream(file);
        }catch(FileNotFoundException e) {
            System.err.println("[error](sender create payloadStream) "
                    +e.getMessage());
            payload_is_set = false;
        }
    }

    public synchronized void setText(String text, String description) {
        if (payload_is_set) {
            if (payloadStream != null) {
                try {
                    payloadStream.close();
                } catch(IOException ignored) {}
            }
            payloadStream = null;
        }
        this.type = TEXT;
        this.description = description;
        this.length = text.length();
        this.payload_is_set = true;

        try {
            payloadStream = new ByteArrayInputStream(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            System.err.println("[error](sender set payloadStream): "
                    +e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public synchronized void setByIntent(Context context, Intent intent) {
        if (payload_is_set) {
            if (payloadStream != null) {
                try {
                    payloadStream.close();
                } catch(IOException ignored) {}
            }
            payloadStream = null;
        }
        if ("text/plain".equals(intent.getType())){
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null){
                this.setText(sharedText,intent.getType());
            }else{
                payload_is_set = false;
            }
        }else{
            this.type = FILE;
            Bundle bundle = intent.getExtras();
            Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
            this.path = PathResolver.getPath(context, uri);
            String[] tmp = path.split(File.separator);
            this.description = tmp[tmp.length-1];//get filename
             //pay = null;

            try { payloadStream = context.getContentResolver().openInputStream(intent.getParcelableExtra(Intent.EXTRA_STREAM));
            } catch (FileNotFoundException e){
                System.out.println("[error](init intent to send) can't cast intent to inputstream (sysetm)"+e.getMessage());
                //log(e.getMessage());
                //e.printStackTrace();
                payload_is_set = false;
                return ;
            }

            try { this.length = payloadStream.available();
            } catch(IOException e){
                System.out.println("[error](init intent to send) can't get inputstream length(system)"+e.getMessage());
                payload_is_set = false;
                return;
            }

            payload_is_set = true;
        }
    }

    protected synchronized void setServer(Server s){
        if(s == null){
            this.ip_is_set = false;
            return;
        }
        this.server = s;
        this.IP = s.IP;
        this.port = s.port;
        this.ip_is_set = true;
    }

    public int send(){
        return this.send(this.IP,this.port);
    }

    private synchronized int send(String IP, int port) {
        if (!ip_is_set || IP == null) {
            System.err.println("[error](sender send) ip not set yet "
                    +"before sending!");
            return -1;
        }
        if (!payload_is_set) {
            System.err.println("[error](sender send) payload not set yet "
                    +"before sending!");
            return -1;
        }
        
        /*int tcp_port = port-1;*/
        int tcp_port = port-1;
        if (port < 0 || port > 65535){
            System.err.println("[error](sender send) bad port "
                    + "before sending");
            return -1;
        }
        //payloadStream was prepared already, just do it!
        try {
            Socket socket = new Socket(IP, tcp_port);
            System.out.println("[info](sender send): bind to port "
                    +IP+":"+tcp_port);
            OutputStream out = socket.getOutputStream();
            //Header
            out.write(Sender.str_need.getBytes("US-ASCII"));
            //Detail
            out.write(this.generateHeader().getBytes("UTF-8"));
            int nget = 0;
            byte[] buf = new byte[MAXLINE+100];
            try {
                while(true) {
                    //assume this.length is not changed during
                    //set and send, send it directly
                    nget = payloadStream.read(buf, 0, MAXLINE);
                    if (nget <= 0) break;
                    out.write(buf, 0, nget);
                }
            } catch (IOException ignored) { }
            try{
                socket.close();
            } catch(IOException ignored) {}
        } catch (UnknownHostException e) {
            System.err.println("[error](sender send): "
                    +"can't bind to "+IP+";"+tcp_port+"(system): "
                    +e.getMessage());
            return -1;
        } catch (IOException e) {
            System.err.println("[error](sender sned): "
                    +"can't get output stream"+"(system): "
                    +e.getMessage());
            return -1;
        }
        return 0;
    }

    public String generateHeader() {
        /* header goes like
         * file;filename;length;
         * text;description;length;*/
        if (payload_is_set) {
//            try {
                return type+HeadSeparator
                        +description+HeadSeparator
                        +length+HeadSeparator;

        } else return "null";
    }

    public boolean ready(){ return (payload_is_set && ip_is_set); }

    public boolean IsIPSet(){ return ip_is_set; }

    protected Server getAvailable(){
        if (ip_is_set) return server;
        else return null;
    }
}
