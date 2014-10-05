package com.cedar.autologin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

public class WifiStateReceiver extends BroadcastReceiver {

	static final String ssid = "seu-wlan";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (info == null) {
				Log.d("autologin", "NetworkInfo is null");
			}
			else if (info.isConnected()) {
				Log.d("info", info.getDetailedState().toString());
				WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				String ssid = wifi_service.getConnectionInfo().getSSID();
				if (ssid.startsWith("\"") && ssid.endsWith("\"")){
					ssid = ssid.substring(1, ssid.length()-1);
				}
				SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
				Set<String> ssidSet = sp.getStringSet("ssid", new HashSet<String>(Arrays.asList("seu-wlan")));
				if (ssidSet.contains(ssid)) {
					Log.d("autologin", "wifi connected to " + ssid);
					new LoginTask(context).execute();
				}
			}
			else if (info.getDetailedState() == DetailedState.DISCONNECTED) {
				Editor editor = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit();  
	            editor.putString("lastssid", "").apply();
				Log.d("info2", info.getDetailedState().toString());
			}
		} else if (intent.getAction().equals("com.cedar.autologin.unknownhostBroadcast")) {
			String retrys = intent.getStringExtra("retrys");
			int r = 0;
			try {
				r = Integer.parseInt(retrys);
			} catch (NumberFormatException e) {
				Log.d("WifiStateReceiver", "Integer.parseInt error " + e.toString());
			}
			if (r < 0 || r > 5)
				return;
			SystemClock.sleep(404 * (r+1) * (r+1));

			WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			String ssid = wifi_service.getConnectionInfo().getSSID();
			if (ssid.startsWith("\"") && ssid.endsWith("\"")){
				ssid = ssid.substring(1, ssid.length()-1);
			}
			SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
			Set<String> ssidSet = sp.getStringSet("ssid", new HashSet<String>(Arrays.asList("seu-wlan")));
			if (ssidSet.contains(ssid)) {
				BasicNameValuePair retrysInfo = new BasicNameValuePair("retrys", retrys);
				new LoginTask(context).execute(retrysInfo);
			}
		}

	}
}
