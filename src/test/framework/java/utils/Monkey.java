package test.framework.java.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;

import test.framework.java.utils.LinearInterpolator.Point;
import android.util.Log;
import android.view.KeyEvent;

public class Monkey {

	private static String TAG = Monkey.class.getSimpleName();
	
    private Socket mSocketClient;
    private BufferedWriter monkeyWriter;
    private BufferedReader monkeyReader;
    
    private int monkeyPort = 12345;
    
    public Monkey() {}
    
    public interface OnMonkeyListener {
        public void OnStarted(boolean success);
    }
    
    public void start(final OnMonkeyListener onMonkeyListener) {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                boolean success = false;
                
//                killAll();
//                startMonkeyServer();
                
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
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (!success) {
                    close();
                }
                
                if (onMonkeyListener != null) {
                    onMonkeyListener.OnStarted(success);
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
    
    private void killAll() {
        RootCmd.execRootCmd("busybox killall com.android.commands.monkey");
        RootCmd.execRootCmd("busybox killall com.android.commands.monkey");
    }
    
    public void close() {
        try {
            if (mSocketClient != null) {
                mSocketClient.close();
                mSocketClient = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to close monkeySocket" + Log.getStackTraceString(e));
        }
        try {
            if (monkeyReader != null) {
                monkeyReader.close();
                monkeyReader = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to close monkeyReader" + Log.getStackTraceString(e));
        }
        try {
            if (monkeyWriter != null) {
                monkeyWriter.close();
                monkeyWriter = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to close monkeyWriter" + Log.getStackTraceString(e));
        }
        
        killAll();
    }

//    /**
//     * Create a new MonkeyMananger to talk to the specified device.
//     *
//     * @param monkeySocket the already connected socket on which to send protocol messages.
//     */
//    public MonkeyManager(Socket monkeySocket) {
//        try {
//            this.monkeySocket = monkeySocket;
//            monkeyWriter = new BufferedWriter(new OutputStreamWriter(monkeySocket.getOutputStream()));
//            monkeyReader = new BufferedReader(new InputStreamReader(monkeySocket.getInputStream()));
//        } catch(IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public Collection<String> getRootView() throws IOException {    	
    	synchronized (this) {
            String response = sendMonkeyEventAndGetResponse("getrootview");
            if (!parseResponseForSuccess(response)) {
//                Collections.emptyList();
            	return null;
            }
            String extras = parseResponseForExtra(response);
            return Arrays.asList(extras.split(" "));
        }
    }
    
    /**
     * Send a touch down event at the specified location.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     * @return success or not
     * @throws IOException on error communicating with the device
     */
    public boolean touchDown(int x, int y) throws IOException {
        return sendMonkeyEvent("touch down " + x + " " + y);
    }

    /**
     * Send a touch down event at the specified location.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     * @return success or not
     * @throws IOException on error communicating with the device
     */
    public boolean touchUp(int x, int y) throws IOException {
        return sendMonkeyEvent("touch up " + x + " " + y);
    }

    /**
     * Send a touch move event at the specified location.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     * @return success or not
     * @throws IOException on error communicating with the device
     */
    public boolean touchMove(int x, int y) throws IOException {
        return sendMonkeyEvent("touch move " + x + " " + y);
    }

    /**
     * Send a touch (down and then up) event at the specified location.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     * @return success or not
     * @throws IOException on error communicating with the device
     */
    public boolean touch(int x, int y) throws IOException {
        return sendMonkeyEvent("tap " + x + " " + y);
    }

    /**
     * Press a physical button on the device.
     * See also {@link KeyEvent}
     * @param name the name of the button (As specified in the protocol)
     * @return success or not
     * @throws IOException on error communicating with the device
     */
    public boolean press(String name) throws IOException {
        return sendMonkeyEvent("press " + name);
    }

    /**
     * Send a Key Down event for the specified button.
     *
     * @param name the name of the button (As specified in the protocol)
     * @return success or not
     * @throws IOException on error communicating with the device
     */
    public boolean keyDown(String name) throws IOException {
        return sendMonkeyEvent("key down " + name);
    }

    /**
     * Send a Key Up event for the specified button.
     *
     * @param name the name of the button (As specified in the protocol)
     * @return success or not
     * @throws IOException on error communicating with the device
     */
    public boolean keyUp(String name) throws IOException {
        return sendMonkeyEvent("key up " + name);
    }

    /**
     * Press a physical button on the device.
     *
     * @param button the button to press
     * @return success or not
     * @throws IOException on error communicating with the device
     */
    public boolean press(PhysicalButton button) throws IOException {
        return press(button.getKeyName());
    }

    /**
     * This function allows the communication bridge between the host and the device
     * to be invisible to the script for internal needs.
     * It splits a command into monkey events and waits for responses for each over an adb tcp socket.
     * Returns on an error, else continues and sets up last response.
     *
     * @param command the monkey command to send to the device
     * @return the (unparsed) response returned from the monkey.
     */
    private String sendMonkeyEventAndGetResponse(String command) throws IOException {
        command = command.trim();
        Log.i(TAG, "Monkey Command: " + command + ".");

        // send a single command and get the response
        monkeyWriter.write(command + "\n");
        monkeyWriter.flush();
        return monkeyReader.readLine();
    }

    /**
     * Parse a monkey response string to see if the command succeeded or not.
     *
     * @param monkeyResponse the response
     * @return true if response code indicated success.
     */
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

    /**
     * Parse a monkey response string to get the extra data returned.
     *
     * @param monkeyResponse the response
     * @return any extra data that was returned, or empty string if there was nothing.
     */
    private String parseResponseForExtra(String monkeyResponse) {
        int offset = monkeyResponse.indexOf(':');
        if (offset < 0) {
            return "";
        }
        return monkeyResponse.substring(offset + 1);
    }

    /**
     * This function allows the communication bridge between the host and the device
     * to be invisible to the script for internal needs.
     * It splits a command into monkey events and waits for responses for each over an
     * adb tcp socket.
     *
     * @param command the monkey command to send to the device
     * @return true on success.
     */
    private boolean sendMonkeyEvent(String command) throws IOException {
        synchronized (this) {
            String monkeyResponse = sendMonkeyEventAndGetResponse(command);
            return parseResponseForSuccess(monkeyResponse);
        }
    }

    /**
     * Close all open resources related to this device.
     */
//    public void close() {
//        try {
//            monkeySocket.close();
//        } catch (IOException e) {
//            LOG.log(Level.SEVERE, "Unable to close monkeySocket", e);
//        }
//        try {
//            monkeyReader.close();
//        } catch (IOException e) {
//            LOG.log(Level.SEVERE, "Unable to close monkeyReader", e);
//        }
//        try {
//            monkeyWriter.close();
//        } catch (IOException e) {
//            LOG.log(Level.SEVERE, "Unable to close monkeyWriter", e);
//        }
//    }

    public String listViews() throws IOException {
        synchronized (this) {
            String response = sendMonkeyEventAndGetResponse("listviews");
            if (!parseResponseForSuccess(response)) {
                return null;
            }
            return parseResponseForExtra(response);
        }
    }
    
    public String queryView() throws IOException {
        synchronized (this) {
            String response = sendMonkeyEventAndGetResponse("queryview");
            if (!parseResponseForSuccess(response)) {
                return null;
            }
            return parseResponseForExtra(response);
        }
    }
    
    public String getViewWithText(String text) throws IOException {
        synchronized (this) {
            String response = sendMonkeyEventAndGetResponse("getviewswithtext " + text);
            if (!parseResponseForSuccess(response)) {
                return null;
            }
            return parseResponseForExtra(response);
        }
    }
    
    public String deferreturn(String text) throws IOException {
        synchronized (this) {
            String response = sendMonkeyEventAndGetResponse("deferreturn screenchange " + text);
            if (!parseResponseForSuccess(response)) {
                return null;
            }
            return parseResponseForExtra(response);
        }
    }
    
    /**
     * Function to get a static variable from the device.
     *
     * @param name name of static variable to get
     * @return the value of the variable, or null if there was an error
     */
    public String getVariable(String name) throws IOException {
        synchronized (this) {
            String response = sendMonkeyEventAndGetResponse("getvar " + name);
            if (!parseResponseForSuccess(response)) {
                return null;
            }
            return parseResponseForExtra(response);
        }
    }

    /**
     * Function to get the list of static variables from the device.
     */
    public Collection<String> listVariable() throws IOException {
        synchronized (this) {
            String response = sendMonkeyEventAndGetResponse("listvar");
            if (!parseResponseForSuccess(response)) {
                return null;
            }
            String extras = parseResponseForExtra(response);
            return Arrays.asList(extras.split(" "));
        }
    }

    /**
     * Tells the monkey that we are done for this session.
     * @throws IOException
     */
    public void done() throws IOException {
        // this command just drops the connection, so handle it here
        synchronized (this) {
            sendMonkeyEventAndGetResponse("done");
        }
    }

    /**
     * Tells the monkey that we are done forever.
     * @throws IOException
     */
    public void quit() throws IOException {
        // this command drops the connection, so handle it here
        synchronized (this) {
            sendMonkeyEventAndGetResponse("quit");
        }
    }

    /**
     * Send a tap event at the specified location.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     * @return success or not
     * @throws IOException
     * @throws IOException on error communicating with the device
     */
    public boolean tap(int x, int y) throws IOException {
        return sendMonkeyEvent("tap " + x + " " + y);
    }

    /**
     * Type the following string to the monkey.
     *
     * @param text the string to type
     * @return success
     * @throws IOException
     */
    public boolean type(String text) throws IOException {
        // The network protocol can't handle embedded line breaks, so we have to handle it
        // here instead
        StringTokenizer tok = new StringTokenizer(text, "\n", true);
        while (tok.hasMoreTokens()) {
            String line = tok.nextToken();
            if ("\n".equals(line)) {
                boolean success = press(PhysicalButton.ENTER);
                if (!success) {
                    return false;
                }
            } else {
                boolean success = sendMonkeyEvent("type " + line);
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Type the character to the monkey.
     *
     * @param keyChar the character to type.
     * @return success
     * @throws IOException
     */
    public boolean type(char keyChar) throws IOException {
        return type(Character.toString(keyChar));
    }

    /**
     * Wake the device up from sleep.
     * @throws IOException
     */
    public void wake() throws IOException {
        sendMonkeyEvent("wake");
    }
    
    public enum PhysicalButton {
        HOME("home"),
        SEARCH("search"),
        MENU("menu"),
        BACK("back"),
        DPAD_UP("DPAD_UP"),
        DPAD_DOWN("DPAD_DOWN"),
        DPAD_LEFT("DPAD_LEFT"),
        DPAD_RIGHT("DPAD_RIGHT"),
        DPAD_CENTER("DPAD_CENTER"),
        ENTER("enter");

        private String keyName;

        private PhysicalButton(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }
    }
    
//    @Override
    public void drag(int startx, int starty, int endx, int endy, int steps, long ms) {
        final long iterationTime = ms / steps;

        LinearInterpolator lerp = new LinearInterpolator(steps);
        LinearInterpolator.Point start = new LinearInterpolator.Point(startx, starty);
        LinearInterpolator.Point end = new LinearInterpolator.Point(endx, endy);
        lerp.interpolate(start, end, new LinearInterpolator.Callback() {
            public void step(Point point) {
                try {
                    Monkey.this.touchMove(point.getX(), point.getY());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(iterationTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public void start(Point point) {
                try {
                    Monkey.this.touchDown(point.getX(), point.getY());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(iterationTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public void end(Point point) {
                try {
                    Monkey.this.touchUp(point.getX(), point.getY());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
