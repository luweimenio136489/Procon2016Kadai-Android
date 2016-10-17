package nittcprocon.glathlete;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private TextView ipAddrTextView;
    private EditText uriEditText;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        uriEditText = (EditText)findViewById(R.id.uriEditText);
        startButton = (Button)findViewById(R.id.startButton);
        ipAddrTextView = (TextView)findViewById(R.id.ipAddrTextView);

        ipAddrTextView.setText(getLocalIpAddress());
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVRActivity(uriEditText.getText().toString());
            }
        });
    }

    private void startVRActivity(String uri) {
        Intent intent = new Intent(this, VRActivity.class);
        intent.putExtra("uri", uri);
        startActivity(intent);
    }

    // http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
    // FIXME: Wi-Fiにしか対応してない
    // FIXME: deprecatedなメソッドを使ってる
    private String getLocalIpAddress(){
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
}
