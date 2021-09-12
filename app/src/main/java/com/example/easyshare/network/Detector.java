package com.example.easyshare.network;

import android.content.Context;

import com.example.easyshare.R;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.InterruptedIOException;
import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Detector {

    Sender sender;
    boolean config_loaded = false;
    private static Detector _instance = null;
    private static final Object synLock = new Object();

    public static synchronized Detector getInstance(){
        if (_instance == null){
            _instance = new Detector();
        }
        return _instance;
    }

    public boolean ByteCompare(byte[] a, int lena, byte[] b, int lenb) {
        if (lena != lenb) return false;
        for (int i=0; i<lena; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    private final ArrayList<Server> servers = new ArrayList<Server>();

    private Detector() {
        sender = Sender.getInstance();
    }

    public void loadFile(Context context){
        if (config_loaded) return;
        config_loaded = true;

        File fin;
        InputStream in;
        Scanner scanner;
        try{
            fin = new File(context.getFilesDir(),
                    String.valueOf(R.string.config_file));
            in = new FileInputStream(fin);
            scanner = new Scanner(in);
        } catch(Exception e){
            System.err.println("[error](config read): "
                    + "can't open config file");
            e.printStackTrace();
            return;
        }
        String ip;
        try{ while( true ){
            ip = scanner.nextLine();
            if (ip == null) break;
            this.addServer(ip,54124);
        } } catch (Exception e){
            e.printStackTrace();
        }

        try{
            scanner.close();
            in.close();
        }catch(Exception ignored){}
    }

    public void saveFile(Context context){
        File fout;
        OutputStream out;
        boolean failure = false;
        try {
            fout = new File(context.getFilesDir(), String.valueOf(R.string.config_file));
            if (fout.exists()){fout.delete();}
            fout.createNewFile();
            out = new FileOutputStream(fout);
        } catch(Exception e){
            System.err.println("[error](config save): "
                    + "can't open config file");
            e.printStackTrace();
            return;
        }
        ArrayList<Server> items = this.getServers();
        for (Server item :items){
            System.out.println(item.IP);
            try {
                out.write((item.IP + "\n").getBytes());//one line for each
            } catch (IOException e){
                System.err.println("[error](config save): "
                        +"write config file failed (system): "
                        +e.getMessage());
                failure = true;
            }
        }
        try {
            out.close();
        }catch (Exception ignored){}
        if (failure){
            System.out.println("[msg](config save) due to failure, giving up, deleting file");
            try{
                fout.delete();
            } catch (Exception ignored){}
        }
    }

    /**
     * add item
     * @param IP IP
     * @param port port
     */
    public void addServer(String IP, int port) {
        try {
            servers.add(new Server(IP, port));
        } catch (UnknownHostException e) {
            System.err.println("[info](add server): bad"
                    +" address "+IP+", giving up");
            //remove the last one
            servers.remove(servers.size()-1);
            //e.printStackTrace();
        }
    }

    public ArrayList<Server> getServers(){ return servers;}

    /**
     * detect available server via udp
     * directly communicate with sender
     */
    protected synchronized void detect() {
        final int TIMEOUT = 1000; //milliseconds
        final int MAXTRIES = 5;

        CountDownLatch latch;

        class Sub extends Thread implements Runnable {
            final Server server;
            final String dString = "Is this address stands a server?";
            final String rString = "Yes, I am!";
            final int MAXLINE = 1024;
            boolean succeed = false;
            final CountDownLatch mainLatch;
            final List<Sub> threads;


            public Sub(Server server, CountDownLatch mainlatch, List<Sub> threads) {
                this.server = server;
                this.mainLatch = mainlatch;
                this.threads = threads;
            }

            @Override
            public void run() {
                try {
                    DatagramSocket udp_detect = new DatagramSocket();
                    udp_detect.setSoTimeout(TIMEOUT);
                    DatagramPacket dPacket = new DatagramPacket
                            (dString.getBytes(), dString.length(),
                                        server._IP, server.port);
                    DatagramPacket rPacket = new DatagramPacket
                            (new byte[MAXLINE], MAXLINE);

                    int tries = 0;
                    boolean receivedResponse = false;
                    do {
                        try {
                            udp_detect.send(dPacket);
                        } catch (IOException e) {
                            System.err.println("[info](detect response): "
                                    + "udp.send error" + e.getMessage());
                            tries += 1;
                            continue;
                        }
                        try {
                            udp_detect.receive(rPacket);
                            if (!rPacket.getAddress().equals(this.server._IP)) {
                                System.out.println("[info](detect response): "
                                        + "package address not match");
                                tries += 1;
                            } else if (!ByteCompare(rPacket.getData(), rPacket.getLength(), rString.getBytes(), rString.length())) {
                                System.out.println("[info](detect response): "
                                        + "package content not match");
                                tries += 1;
                            } else receivedResponse = true;
                            rPacket.setLength(MAXLINE);
                        } catch (InterruptedIOException e) {
                            tries += 1;
                        } catch (IOException e) {
                            tries += 1;
                            System.err.println("[error](detect response): "
                                    + "udp.receive error" + e.getMessage());
                        }

                    } while (!receivedResponse && tries < MAXTRIES && !Thread.currentThread().isInterrupted());
                    udp_detect.close();
                    System.out.print("[info](detector sub):");
                    synchronized (synLock) { //the field under is synchronized
                        if (receivedResponse && !Thread.currentThread().isInterrupted()) {
                            System.out.println("succeed");
                            this.succeed = true;
                            this.stopOther();
                        }
                    }
                    if (!receivedResponse){
                        if (Thread.currentThread().isInterrupted()) {
                            System.out.print("sub process interrupted.");
                        }
                        System.out.println("sub process failed after " + tries + " tries");
                    }
                } catch (SocketException se) {
                    System.err.println("[error](upd_detect) SocketException :"
                            + se.getMessage());
                }
                mainLatch.countDown();
            }

            private void stopOther() {
                for (Runnable t : this.threads) {
                    if (t == Thread.currentThread()) continue;
                    ((Thread) t).interrupt();
                }
            }
            public boolean isSucceed(){
                return this.succeed;
            }
        }

        List<Sub> threads = new ArrayList<>();
        latch = new CountDownLatch(servers.size());
        for (Server s : servers) {
            threads.add(new Sub(s, latch, threads));
        }

        for (Sub t : threads) {
            t.start();
        }
        try {
            System.out.println("[info](detector): start waiting for"
                    + " sub process");
            latch.await();
        } catch (InterruptedException ignored) {}
        System.out.print("[info](detector): detect finished");

        int index = -1;
        for (int i = 0; i < threads.size(); i++) {
            if (threads.get(i).isSucceed()) {
                index = i;
                break;
            }
        }

        if (index == -1) sender.setServer(null);
        else sender.setServer(servers.get(index));
    }
}

