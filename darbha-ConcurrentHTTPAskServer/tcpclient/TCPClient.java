package tcpclient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class TCPClient {
    private Boolean shutdown;
    private Integer timeout;
    private Integer limit;
    
    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown=shutdown;
        this.timeout=timeout;
        this.limit=limit;
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        Socket sock = new Socket(hostname,port);
        ByteArrayOutputStream outputarray = new ByteArrayOutputStream();
        OutputStream serveroutput = sock.getOutputStream();
        InputStream userinput = sock.getInputStream();

        //set the timeout(cmds from socket class){if(flag set-set it to the value)else(throw exception,return data)}
        if(timeout!=null){
            sock.setSoTimeout(timeout);
        }
        
        /*
          if(toServerBytes !=null){
            serveroutput.write(toServerBytes);
            serveroutput.write('\n');
            serveroutput.flush();
        }
         */
        
        if (toServerBytes != null && toServerBytes.length > 0) {
            serveroutput.write(toServerBytes);
            serveroutput.write('\n');  
            serveroutput.flush();  
        }
        
        
        //shutdown mech here(cmds from socket class){if(set-true)(then shutdown output)}shutdown after data is sent
        if(shutdown){
            sock.shutdownOutput();
        }

        int rcvd=0;
        int store ;
        try {
            while((store = userinput.read()) != -1){
                outputarray.write(store);
                rcvd++;
            //handle data limit here simple if-break statement:-declare another variable(counter for bytes received)
                if(limit!=null && rcvd>=limit){
                    break;
                }
            }
            
        } catch (SocketTimeoutException returndata) {
        }
        
        byte[] data = outputarray.toByteArray();
        outputarray.close();
        userinput.close();
        serveroutput.close();
        sock.close();

        return data;
    }
}
