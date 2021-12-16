package com.peninsula.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class WifiView extends AppCompatImageView {

    private WifiManager mWifiManager;

    private WifiStatus mWifiStatus = WifiStatus.WIFI_OFF;
    private WifiInfo mConnectionInfo;
    private PopupWindow mPopupWindow;
    private TextView mWifiTv;
    private String mStatusText = "";
    private WifiViewClickListener mWifiViewClickListener;

    public WifiView(Context context) {
        this(context, null);
    }

    public WifiView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WifiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (getDrawable() != null) {

            // 获取wifi状态
            mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (mWifiManager == null) {
                throw new RuntimeException("WifiManager is not supposed to be null");
            }
            refreshWifiIconState();
        }

        setOnClickListener(v->{
            if (mWifiViewClickListener != null){
                Log.e(TAG, "调用自定义onClick");
                mWifiViewClickListener.onClick();
            }else{
                Log.e(TAG, "无监听器设置,显示弹窗");
                showPopwindow();
            }
        });

        setOnLongClickListener(v->{
            // TODO goto setting
            if (mWifiViewClickListener != null){
                Log.e(TAG, "调用自定义onClick");
                mWifiViewClickListener.onLongClick();
            }else{
                Log.e(TAG, "无监听器设置,执行默认长按逻辑");

            }
            return true;
        });
    }

    public void registReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getContext().registerReceiver(broadcastReceiver, filter);
    }

    public void unRegistReceiver() {

    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.RSSI_CHANGED_ACTION)
                    || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
                    || action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                // 可以使用 获取wifi状态代码 相关代码
                refreshWifiIconState();
            }
        }
    };

    private void refreshWifiIconState() {
        mConnectionInfo = mWifiManager.getConnectionInfo();
        if (mConnectionInfo != null) {
            // 计算当前的信号强度
            int signalLevel = WifiManager.calculateSignalLevel(mConnectionInfo.getRssi(), 5);
            mStatusText = "连接至:" + mConnectionInfo.getSSID();
            mWifiStatus = WifiStatus.getByValue(signalLevel);
        }
        else {
            switch (mWifiManager.getWifiState()) {
                case WifiManager.WIFI_STATE_UNKNOWN:
                case WifiManager.WIFI_STATE_DISABLED:
                case WifiManager.WIFI_STATE_DISABLING:
                    mWifiStatus = WifiStatus.WIFI_OFF;
                    mStatusText = "WIFI已关闭";
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                case WifiManager.WIFI_STATE_ENABLING:
                    mWifiStatus = WifiStatus.WIFI_DISCONNECT;
                    mStatusText = "WIFI无连接";
                    break;
                default:
                    break;
            }
        }
        Log.e(TAG, "wifi level = " + mWifiStatus.ordinal());
        // 设置图片
        getDrawable().setLevel(mWifiStatus.ordinal());
    }

    private static final String TAG = WifiView.class.getName();

    private void showPopwindow(){
        if (mPopupWindow == null){
            mPopupWindow = new PopupWindow(getContext());
            // View view = LayoutInflater.from(getContext()).inflate(R.layout.pop_wifi,null, false);
            mWifiTv = new TextView(getContext());
            mWifiTv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mWifiTv.setTextSize(14);
            mWifiTv.setTextColor(Color.WHITE);
            // mWifiTv = view.findViewById(R.id.wifiTv);
            mPopupWindow.setContentView(mWifiTv);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setFocusable(true);
        }
        if (mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
        }else{
            mWifiTv.setText(mStatusText);
            mPopupWindow.showAsDropDown(this);
        }
    }

    public WifiViewClickListener getWifiViewClickListener() {
        return mWifiViewClickListener;
    }

    public void setWifiViewClickListener(WifiViewClickListener wifiViewClickListener) {
        mWifiViewClickListener = wifiViewClickListener;
    }

    enum WifiStatus {
        /**
         * 信号强度0
         */
        WIFI_SINGAL_0,
        /**
         * 信号强度1
         */
        WIFI_SINGAL_1,
        /**
         * 信号强度2
         */
        WIFI_SINGAL_2,
        /**
         * 信号强度3
         */
        WIFI_SINGAL_3,
        /**
         * 信号强度4
         */
        WIFI_SINGAL_4,
        /**
         * WIFI关闭
         */
        WIFI_OFF,
        /**
         * WIFI无连接
         */
        WIFI_DISCONNECT;

        public static WifiStatus getByValue(int value) {
            switch (value) {
                case 0:
                    return WIFI_SINGAL_0;
                case 1:
                    return WIFI_SINGAL_1;
                case 2:
                    return WIFI_SINGAL_2;
                case 3:
                    return WIFI_SINGAL_3;
                case 4:
                    return WIFI_SINGAL_4;
                case 5:
                    return WIFI_OFF;
                case 6:
                    return WIFI_DISCONNECT;
                default:
                    break;
            }
            return WIFI_OFF;
        }
    }

}
