import java.io.*;
import java.net.*;
import tcpclient.TCPClient;

public class HTTPAsk {
    public static void main( String[] args) {
        // Your code here
        if (args.length!=1) {
            System.err.println("enter port no.");
            System.exit(1);
        }
        int portnum = Integer.parseInt(args[0]);
    
        try(ServerSocket httpserver = new ServerSocket(portnum);) {
            System.out.println("server active on port "+ portnum);

            while(true) { 
                try(Socket sock =httpserver.accept()){
                    OutputStream serveroutput=sock.getOutputStream();
                    InputStream userinput=sock.getInputStream();

                    byte [] buff= new byte[1024];
                    int read = userinput.read(buff);
                    if(read==-1){
                        continue;
                    }

                    String reqline=new String(buff,0,read);

                    if(!reqline.startsWith("GET")){
                        String rsp = "HTTP/1.1 400 Bad Request\r\n"+
                        "Content-Type: text/plain\r\n\r\n"+
                        "400 Bad Request";
                        serveroutput.write(rsp.getBytes());
                        continue;
                    }

                    String[] requestblk=reqline.split(" ");
                    
                    if (requestblk.length < 2) {
                        String rsp ="HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Type: text/plain\r\n\r\n" +
                        "400 Bad Request";
                        serveroutput.write(rsp.getBytes());
                        continue;
                    }

                    String urlpart=requestblk[1];
                    if (!urlpart.startsWith("/ask?")) {
                        String response = "HTTP/1.1 404 Not Found\r\n"+
                        "Content-Type: text/plain\r\n\r\n"+
                        "404 Not Found";
                        serveroutput.write(response.getBytes());
                        continue;
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
                        continue;
                    }

                    int remoteport;
                    try {
                        remoteport = Integer.parseInt(port);  
                    } catch (NumberFormatException e) {
                        String response = "HTTP/1.1 400 Bad Request\r\n"+
                        "Content-Type: text/plain\r\n\r\n"+
                        "400 Bad Request";
                        serveroutput.write(response.getBytes());
                        continue;
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
                            continue;
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
                        continue;
                    }
                    }
                    
                    TCPClient tcpClient=new TCPClient(sf,tmtf,lf);

                    byte[] toserverbytes = msg.getBytes(); 
                    byte[] serverresponse = tcpClient.askServer(hostname,remoteport,toserverbytes);
                    String response = "HTTP/1.1 200 OK\r\n"+
                    "Content-Type: text/plain\r\n\r\n";
                    serveroutput.write(response.getBytes());
                    serveroutput.write(serverresponse);
                    
                } catch (IOException e){}
                   
            }
        } 
        catch (IOException e){}
    
    }
}

