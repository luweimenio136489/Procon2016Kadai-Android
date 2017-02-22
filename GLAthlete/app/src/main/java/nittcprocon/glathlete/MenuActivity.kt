package nittcprocon.glathlete

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MenuActivity : AppCompatActivity() {
    private val TAG = "MenuActivity"
    private var ipAddrTextView: TextView? = null
    private var uriEditText: EditText? = null
    private var startButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // 初回起動時のみデフォルトの設定値を端末に保存する
        // アプリのエントリポイントにはこれを書いておくこと
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        MyContexts.applicationContext = applicationContext

        uriEditText = findViewById(R.id.uriEditText) as EditText
        startButton = findViewById(R.id.startButton) as Button
        ipAddrTextView = findViewById(R.id.ipAddrTextView) as TextView

        ipAddrTextView!!.text = localIpAddress
        startButton!!.setOnClickListener { startVRActivity(uriEditText!!.text.toString()) }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }

            else -> return super.onOptionsItemSelected(menuItem)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun startVRActivity(uri: String) {
        val intent = Intent(this, VRActivity::class.java)
        intent.putExtra("uri", uri)
        startActivity(intent)
    }

    // http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
    // FIXME: Wi-Fiにしか対応してない
    // FIXME: deprecatedなメソッドを使ってる
    private val localIpAddress: String
        get() {
            val wm = getSystemService(Context.WIFI_SERVICE) as WifiManager
            return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        }
}
