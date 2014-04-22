package com.cedar.autologin;

import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

public class WifiStateReceiver extends BroadcastReceiver {

	static final String ssid = "\"seu-wlan\"";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
				WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifi_service.getConnectionInfo();
				if (wifiInfo.getSSID().equals(ssid)) {
					Log.d("autologin", "wifi connected " + wifiInfo.getSSID());
					new LoginTask(context).execute();
				}
			}
		} else if (intent.getAction().equals("com.cedar.autologin.unknownhostBroadcast")) {
			WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifi_service.getConnectionInfo();
			if (wifiInfo.getSSID().equals(ssid)) {
				SystemClock.sleep(1000);
				String retrys = intent.getStringExtra("retrys");
				BasicNameValuePair retrysInfo = new BasicNameValuePair("retrys", retrys);
				new LoginTask(context).execute(retrysInfo);
			}
		}

	}
}