import java.io.*;
import java.net.*;
import tcpclient.TCPClient;

public class ConcHTTPAsk {
    public static void main( String[] args) {
        // Your code here
        if(args.length!=1){
            System.out.println("enter valid port number");
            return;
        }
        int port=Integer.parseInt(args[0]);
        try(ServerSocket httpserver=new ServerSocket(port)){
            System.out.println("server started on port "+port);
            while(true){ 
                Socket client=httpserver.accept();
                MyRunnable test=new MyRunnable(client);
                Thread clientthread=new Thread(test);
                clientthread.start();
            }  
        } catch (IOException e){}
    }
}

 class MyRunnable implements Runnable {
        private Socket sock;
    public MyRunnable(Socket client) {
        // store parameter for later user
        this.sock=client;
    }
 
    public void run() {
        try(OutputStream serveroutput=sock.getOutputStream();
          InputStream userinput=sock.getInputStream()) {
            byte [] buff= new byte[1024];
                    int read = userinput.read(buff);
                    if(read==-1){
                        return;
                    }

                    String reqline=new String(buff,0,read);

                    if(!reqline.startsWith("GET")){
                        String rsp = "HTTP/1.1 400 Bad Request\r\n"+
                        "Content-Type: text/plain\r\n\r\n"+
                        "400 Bad Request";
                        serveroutput.write(rsp.getBytes());
                        return;
                    }

                    String[] requestblk=reqline.split(" ");
                    
                    if (requestblk.length < 2) {
                        String rsp ="HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Type: text/plain\r\n\r\n" +
                        "400 Bad Request";
                        serveroutput.write(rsp.getBytes());
                        return;
                    }

                    String urlpart=requestblk[1];
                    if (!urlpart.startsWith("/ask?")) {
                        String response = "HTTP/1.1 404 Not Found\r\n"+
                        "Content-Type: text/plain\r\n\r\n"+
                        "404 Not Found";
                        serveroutput.write(response.getBytes());
                        return;
                    }

                    String hostname=null,port=null,timeout=null,limit=null, msg="";
                    String query=urlpart.substring(5);
                    
                    String[] components = query.split("&");
                    for(String subcomp:components){
                        String[] spltpt= subcomp.split("=");
                        if(spltpt.length==2){
                        String a=spltpt[0];
                        String b=spltpt[1];
                        if(a.equals("hostname")) {
                            hostname=b;
                        } else if (a.equals("port")) {
                            port=b;
                        } else if (a.equals("timeout")) {
                            timeout=b;
                        } else if (a.equals("limit")) {
                            limit=b;
                        }
                        else if (a.equals("string")){
                            msg=b;
                        }
                     }
                    }

                    if (hostname==null || port==null) {
                        String response = "HTTP/1.1 400 Bad Request\r\n"+
                        "Content-Type: text/plain\r\n\r\n"+
                        "400 Bad Request";
                        serveroutput.write(response.getBytes());
                        return;
                    }

                    int remoteport;
                    try {
                        remoteport = Integer.parseInt(port);  
                    } catch (NumberFormatException e) {
                        String response = "HTTP/1.1 400 Bad Request\r\n"+
                        "Content-Type: text/plain\r\n\r\n"+
                        "400 Bad Request";
                        serveroutput.write(response.getBytes());
                        return;
                    }

                    boolean sf;
                    if(timeout!=null && timeout.equals(true)){
                        sf=true;
                    }
                    else{
                        sf=false;
                    }

                    Integer tmtf=null;
                    if(timeout!=null){
                        try {
                            tmtf=Integer.parseInt(timeout);
                        } catch (NumberFormatException e) {
                            String response = "HTTP/1.1 400 Bad Request\r\n"+
                            "Content-Type: text/plain\r\n\r\n"+
                            "400 Bad Request";
                            serveroutput.write(response.getBytes());
                            return;
                        }

                    }
                    
                    Integer lf=null;
                    if(limit!=null){
                        try {
                        lf=Integer.parseInt(limit);
                    } catch (NumberFormatException e) {
                        String response = "HTTP/1.1 400 Bad Request\r\n"+
                        "Content-Type: text/plain\r\n\r\n"+
                        "400 Bad Request";
                        serveroutput.write(response.getBytes());
                        return;
                    }
                    }
                    
                    TCPClient tcpClient=new TCPClient(sf,tmtf,lf);
                    // /n extrabyte sent -possible soltuion
                    byte[] toserverbytes;
                    if(msg.isEmpty()){
                        toserverbytes=new byte[0];
                    }
                    else{
                        toserverbytes=msg.getBytes();
                    }
                    
                    byte[] serverresponse = tcpClient.askServer(hostname,remoteport,toserverbytes);
                    String response = "HTTP/1.1 200 OK\r\n"+
                    "Content-Type: text/plain\r\n\r\n";
                    serveroutput.write(response.getBytes());
                    serveroutput.write(serverresponse);

        } catch (IOException e){}
    }
 }