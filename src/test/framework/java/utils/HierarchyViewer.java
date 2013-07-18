package test.framework.java.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

import com.android.hierarchyviewerlib.device.ViewNode;

public class HierarchyViewer {
    private static final String TAG = HierarchyViewer.class.getSimpleName();
    public static ViewNode loadViewTreeData(final InputStream is/*Window window*/) {

        //        mFilterText = ""; //$NON-NLS-1$

        ViewNode viewNode = loadWindowData(is);//DeviceBridge.loadWindowData(window);
//        if (viewNode != null) {
//            DeviceBridge.loadProfileData(window, viewNode);
//            viewNode.setViewCount();
//            TreeViewModel.getModel().setData(window, viewNode);
//        }
        return viewNode;
    }

    public static ViewNode loadWindowData(InputStream is) {

        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(is,
                    "utf-8"));
            ViewNode currentNode = null;
            int currentDepth = -1;
            String line;
            while ((line = in.readLine()) != null) {
                if ("DONE.".equalsIgnoreCase(line)) {
                    break;
                }
                int depth = 0;
                while (line.charAt(depth) == ' ') {
                    depth++;
                }
                while (depth <= currentDepth) {
                    currentNode = currentNode.parent;
                    currentDepth--;
                }
                currentNode = new ViewNode(null/* window */, currentNode,
                        line.substring(depth));
                currentDepth = depth;
            }
            if (currentNode == null) {
                return null;
            }
            while (currentNode.parent != null) {
                currentNode = currentNode.parent;
            }
            // ViewServerInfo serverInfo =
            // getViewServerInfo(window.getDevice());
            // if (serverInfo != null) {
            // currentNode.protocolVersion = serverInfo.protocolVersion;
            // }
            return currentNode;
        } catch (Exception e) {
            Log.e(TAG, "Unable to load window data for window ");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
