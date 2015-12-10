package xyz.codeme.smart321;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import xyz.codeme.smart321.utils.HttpUtils;


public class MainActivity extends AppCompatActivity {

    private HttpUtils mHttp;
    private SharedPreferences mPreferences;
    private String mUserName;
    private String[] mMusicName;

    private Button mRomateOpen;
    private Button mLoaclOpen;
    private Button mJustOpen;
    private Button mJustClose;
    private Button mJustAdjuct;
    private Button mMusic;
    private TextView mTextUserName;
    private Spinner mMusicSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRomateOpen = (Button) findViewById(R.id.btn_remote_open);
        mLoaclOpen = (Button) findViewById(R.id.btn_local_open);
        mJustOpen = (Button) findViewById(R.id.btn_just_open);
        mJustClose = (Button) findViewById(R.id.btn_just_close);
        mJustAdjuct = (Button) findViewById(R.id.btn_adjust);
        mMusic = (Button) findViewById(R.id.btn_music);
        mTextUserName = (TextView) findViewById(R.id.text_user);
        mMusicSpinner = (Spinner) findViewById(R.id.spinner_music);

        mHttp = new HttpUtils(new MessageHandler());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        getUserName();
        mTextUserName.setText(mUserName);

        setOnClickListener();
    }

    private void setOnClickListener() {
        mRomateOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOpenOrder(true);
            }
        });
        mLoaclOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inTheLocal()) {
                    showToast(R.string.info_not_local);
                }
                sendOpenOrder(false);
            }
        });
        mJustOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLocalOrder(HttpUtils.ORDER_OPEN_DOOR);
            }
        });
        mJustClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLocalOrder(HttpUtils.ORDER_CLOSE_DOOR);
            }
        });
        mJustAdjuct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLocalOrder(HttpUtils.ORDER_ADJUST_DOOR);
            }
        });
        mMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPlayMusicOrder();
            }
        });
    }

    /**
     * 获取用户名
     */
    private void getUserName()
    {
        mUserName = mPreferences.getString("UserName", "");
        if(mUserName.length() < 1) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(i, 0);
        } else {
            mHttp.setIdentify(mUserName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null)
            return;
        if(requestCode == 0) {
            mUserName = data.getStringExtra(LoginActivity.EXTRA_USER_NAME);
            mTextUserName.setText(mUserName);
            mHttp.setIdentify(mUserName);
            mPreferences.edit().putString("UserName", mUserName).apply();
        }
    }

    private void sendOpenOrder(boolean romate) {
        if(romate)
            mHttp.sendRomateOpenOrder();
        else
            mHttp.sendLocalOpenOrder();
    }

    private void sendLocalOrder(String order) {
        if (!inTheLocal()) {
            showToast(R.string.info_not_local);
        }
        mHttp.sendLocalOrder(order);
    }

    private void sendPlayMusicOrder() {
        if (!inTheLocal()) {
            showToast(R.string.info_not_local);
        }

        // 加载音乐文件名
        if(mMusicName == null) {
            mMusicName = getResources().getStringArray(R.array.music_file);
        }

        int musicIndex = (int) mMusicSpinner.getSelectedItemId();
        mHttp.playMusic(mMusicName[musicIndex]);
    }

    private boolean inTheLocal() {
        WifiManager wifiManager;
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            if ((ip & 0xFF) == 192 && ((ip >> 8) & 0xFF) == 168
                    && ((ip >> 16) & 0xFF) == 5) {
                return true;
            }
        }
        return false;
    }

    private void showToast(int resourceId) {
        Toast.makeText(MainActivity.this, resourceId, Toast.LENGTH_SHORT).show();
    }

    private class MessageHandler extends Handler {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HttpUtils.SEND_SUCCESS:
                    showToast(R.string.send_success);
                    break;
                case HttpUtils.SEND_TIME_OUT:
                    showToast(R.string.send_timeout);
                    break;
                case HttpUtils.SEND_FAILED:
                    showToast(R.string.send_failed);
                    break;
            }
        }
    };
}
