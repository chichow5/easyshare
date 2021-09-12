package com.example.easyshare.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {

    public interface Callback {
        void callback(Server s);
    }

    public String IP;
    public InetAddress _IP;
    public int port;
    public Server(String IP, int port) throws UnknownHostException {
        this.IP = IP;
        this._IP = InetAddress.getByName(IP);
        this.port = port;
    }
    public void setIP(String IP) throws UnknownHostException {
        this.IP = IP;
        this._IP = InetAddress.getByName(IP);
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String toString(){
        return this.IP+":"+this.port;
    }
}
