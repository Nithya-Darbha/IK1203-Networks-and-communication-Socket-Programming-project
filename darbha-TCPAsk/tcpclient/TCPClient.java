package tcpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;


public class TCPClient {
    
    public TCPClient() {
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        Socket sock = new Socket(hostname,port);
        ByteArrayOutputStream outputarray = new ByteArrayOutputStream();
        OutputStream serveroutput = sock.getOutputStream();
        InputStream userinput = sock.getInputStream();
         

        if(toServerBytes !=null){
            serveroutput.write(toServerBytes);
            serveroutput.write('\n');
            serveroutput.flush();
        }

        int store ;
        while((store = userinput.read()) != -1){
            outputarray.write(store);
        }

        byte[] data = outputarray.toByteArray();
        outputarray.close();
        userinput.close();
        serveroutput.close();
        sock.close();

        return data;
    }

    public byte[] askServer(String hostname, int port) throws IOException {
        Socket sock = new Socket(hostname,port);
        ByteArrayOutputStream outputarray = new ByteArrayOutputStream();
        InputStream userinput = sock.getInputStream();
        

        int read ;
        while((read= userinput.read()) != -1){
            outputarray.write(read);
        }
        

        byte[] data = outputarray.toByteArray();
        outputarray.close();
        userinput.close();
        sock.close();

        return data;
    }
}
