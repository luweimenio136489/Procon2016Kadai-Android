package nittcprocon.glathlete;

import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private TextView ipAddrTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ipAddrTextView = (TextView)findViewById(R.id.ipAddrTextView);
        ipAddrTextView.setText(getLocalIpAddress());
    }

    // http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
    // FIXME: Wi-Fiにしか対応してない
    // FIXME: deprecatedなメソッドを使ってる
    private String getLocalIpAddress(){
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
}
