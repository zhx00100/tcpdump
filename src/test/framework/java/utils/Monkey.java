package test.framework.java.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Monkey {

    private Socket mSocketClient;
    private BufferedWriter monkeyWriter;
    private BufferedReader monkeyReader;
    
    private int monkeyPort = 12345;
    
    public void start() {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                boolean success = false;
                
                killAll();
                startMonkeyServer();
                
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    mSocketClient = new Socket(InetAddress.getLocalHost(), monkeyPort);
                    
                    monkeyWriter = new BufferedWriter(new OutputStreamWriter(mSocketClient.getOutputStream()));
                    monkeyReader = new BufferedReader(new InputStreamReader(mSocketClient.getInputStream()));
                    
                    success = true;
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                
                if (!success) {
                    close();
                    //log
                }
            }
        }).start();
        
    }
    
    private void startMonkeyServer() {
        try {
            Runtime.getRuntime().exec("su -c monkey --port 12345 &");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void killAll() {
        RootCmd.execRootCmd("busybox killall com.android.commands.monkey");
        RootCmd.execRootCmd("busybox killall com.android.commands.monkey");
    }
    
    private String sendMonkeyEventAndGetResponse(String command) throws IOException {
        command = command.trim();
//        LOG.info("Monkey Command: " + command + ".");

        if (monkeyWriter == null || monkeyReader == null) {
            //log
            return null;
        }
        // send a single command and get the response
        monkeyWriter.write(command + "\n");
        monkeyWriter.flush();
        return monkeyReader.readLine();
    }
    
    public void close() {
        try {
            if (mSocketClient != null) {
                mSocketClient.close();
                mSocketClient = null;
            }
        } catch (IOException e) {
//            LOG.log(Level.SEVERE, "Unable to close monkeySocket", e);
        }
        try {
            if (monkeyReader != null) {
                monkeyReader.close();
                monkeyReader = null;
            }
        } catch (IOException e) {
//            LOG.log(Level.SEVERE, "Unable to close monkeyReader", e);
        }
        try {
            if (monkeyWriter != null) {
                monkeyWriter.close();
                monkeyWriter = null;
            }
        } catch (IOException e) {
//            LOG.log(Level.SEVERE, "Unable to close monkeyWriter", e);
        }
    }
    
    public boolean press(int keyCode) throws IOException {
        return sendMonkeyEvent("press " + keyCode);
    }
    
    private boolean sendMonkeyEvent(String command) throws IOException {
        synchronized (this) {
            String monkeyResponse = sendMonkeyEventAndGetResponse(command);
            return parseResponseForSuccess(monkeyResponse);
        }
    }
    
    private boolean parseResponseForSuccess(String monkeyResponse) {
        if (monkeyResponse == null) {
            return false;
        }
        // return on ok
        if(monkeyResponse.startsWith("OK")) {
            return true;
        }

        return false;
    }
}
