package test.framework.java.utils;

/**
 * this class is used to record device operations
 * @author zhangxin
 *
 */
public class Recorder {
    
    public void start() {
        
        RootCmd.execCmd("getevent -t ");
    }
}
