package nittcprocon.thetasremote;

/**
 * Created by khrom on 16/08/10.
 */
public class TheataRequest {

    /**
     * セッションを開始。セッションIDを発行する。
     */
    public static String getConnectRequest() {
        return "{\"name\": \"camera.startSession\" ,\"parameters\": {}}";
    }

    /**
     * 静止画撮影を開始する。
     */
    public static String getShutterRequest(String sessionId) {
        return "{\"name\": \"camera.takePicture\" ,\"parameters\": {\"sessionId\" :\"" + sessionId + "\"}}";
    }


    /**
     * v2.0
     * 連続撮影を開始する。
     */
    public static String getStartcaptureRequest(String sessionId) {
        return "{\"name\": \"camera._startCapture\" ,\"parameters\": {\"sessionId\" :\"" + sessionId + "\"}}";
    }

    /**
     * v2.0
     * 連続撮影を停止する。
     */
    public static String getStopcaptureRequest(String sessionId) {
        return "{\"name\": \"camera._stopCapture\" ,\"parameters\": {\"sessionId\" :\"" + sessionId + "\"}}";
    }

    /**
     * 撮影モード指定
     *
     * @param mode true:動画 false:画像
     */
    public static String getModeRequest(boolean mode, String sessionId) {
        String value;
        if (mode == true) {
            value = "_video";
        } else {
            value = "image";
        }

        return option("captureMode", value, sessionId);
    }

    private static String option(String option_name, String option_value, String sessionId) {
        return "{\"name\": \"camera.setOptions\" ,\"parameters\": {\"sessionId\" :\"" + sessionId + "\", \"options\": {\"" + option_name + "\": \"" + option_value + "\"}}}";
    }


}
