package com.tampilator.editor;

import android.content.Context;
import java.io.*;
import java.net.*;

public class LocalHttpServer {
    private final Context context; private final int port; private volatile boolean running; private ServerSocket serverSocket; private Thread thread;
    public LocalHttpServer(Context context, int port) { this.context=context; this.port=port; }
    public void start() {
        if (running) return; running=true;
        thread=new Thread(() -> {
            try {
                serverSocket=new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),port));
                while(running) { try { final Socket s=serverSocket.accept(); new Thread(() -> handle(s)).start(); } catch(IOException ignored) {} }
            } catch(IOException ignored) {} finally { closeServer(); }
        }); thread.start();
    }
    private void handle(Socket socket) {
        try(Socket s=socket) {
            BufferedReader r=new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line=r.readLine(); if(line==null)return;
            String[] p=line.split(" "); String path=p.length>1?p[1]:"/";
            while((line=r.readLine())!=null && !line.isEmpty()){}
            if(path.equals("/") || path.startsWith("/editor.html")) {
                byte[] body=readAsset();
                write(s,200,"text/html; charset=utf-8",body);
            } else write(s,404,"text/plain; charset=utf-8","Not Found".getBytes("UTF-8"));
        } catch(Exception ignored) {}
    }
    private byte[] readAsset() throws IOException { try(InputStream in=context.getAssets().open("editor.html"); ByteArrayOutputStream out=new ByteArrayOutputStream()){ byte[] b=new byte[8192]; int n; while((n=in.read(b))!=-1)out.write(b,0,n); return out.toByteArray(); } }
    private void write(Socket s,int code,String type,byte[] body)throws IOException { OutputStream o=s.getOutputStream(); String h="HTTP/1.1 "+code+(code==200?" OK":" Not Found")+"\r\nContent-Type: "+type+"\r\nContent-Length: "+body.length+"\r\nConnection: close\r\nCache-Control: no-store\r\n\r\n"; o.write(h.getBytes("UTF-8")); o.write(body); o.flush(); }
    public void stop(){running=false; closeServer();}
    private void closeServer(){try{if(serverSocket!=null)serverSocket.close();}catch(IOException ignored){}}
}
