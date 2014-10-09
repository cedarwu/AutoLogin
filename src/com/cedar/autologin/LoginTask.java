package com.cedar.autologin;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class LoginTask extends AsyncTask<BasicNameValuePair, Integer, Boolean> {

	final static String version = "2.2";
	
	Context context;
	String account;
	String passwd;
	int retrys = 0;
	Boolean exceedError = false;
	Boolean passwdError = false;
	Boolean alreadyLoggedIn = false;
	Boolean retry = false;
	
	Boolean foreground = false;
	
	public LoginTask(Context context) {

	    this.context = context;
	}
	
	protected Boolean doInBackground(BasicNameValuePair... params) {
		for (BasicNameValuePair param : params) {
			if (param.getName().equals("retrys")) {
				try {
					retrys = Integer.parseInt(param.getValue()) + 1;
				} catch (NumberFormatException e) {
					Log.d("autologin", "Integer.parseInt error " + e.toString());
					return false;
				}
			}
			else if (param.getName().equals("foreground")) {
				foreground = true;
			}
		}
		if (checkLogin())
			return false;
		else if (retry) {
			retry();
			return false;
		}
		else {
			SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
			account = sp.getString("account", "");  
			passwd = sp.getString("passwd", "");
			if (account.equals("") || passwd.equals("")) {
				Log.d("autologin", "account or passwd is empty");
				return false;
			}
			if (login()) {
				Log.d("autologin", "login succeed");
				return true;
			} else if (retry) {
				retry();
				return false;
			} else if (exceedError) {
				MainActivity.NicFragment.NicTask nicTask = MainActivity.NicFragment.newInstance(0).new NicTask(context, "offlineCurrentAndLogin");
				nicTask.foreground = foreground;
				nicTask.execute();
				return false;
			} else {
				Log.d("autologin", "login failed");
				return false;
			}
		}
	}

	protected void onPostExecute(Boolean result) {
		if (retry)
			return;
		SQLiteHelper db = new SQLiteHelper(context);
		
		if (result) {
			db.addLog("登录成功");
			Toast.makeText(context.getApplicationContext(),
					"AutoLogin: 登录成功~", Toast.LENGTH_LONG)
					.show();
			WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
			sp.edit().putString("lastssid", wifi_service.getConnectionInfo().getSSID()).apply();
		} else if (retrys >= 3){
			Log.d("autologin", "retrys too many times");
			db.addLog("登录失败");
			if (foreground)
				Toast.makeText(context.getApplicationContext(),
						"AutoLogin: 登录失败!", Toast.LENGTH_LONG).show();
			else
				showNotification("AutoLogin: 登录失败");
		} else if (exceedError) {
			/* -> moved to MainActivity.NicFragment.NicTask offlineCurrentAndLogin
			db.addLog("并发登录超过最大限制 ");
			Toast.makeText(context.getApplicationContext(),
					"AutoLogin: 并发登录超过最大限制 !", Toast.LENGTH_LONG)
					.show();
					*/
		} else if (passwdError) {
			db.addLog("用户名密码错误 ");
			if (foreground)
				Toast.makeText(context.getApplicationContext(),
						"AutoLogin: 用户名密码错误 !", Toast.LENGTH_LONG).show();
			else
				showNotification("AutoLogin: 用户名密码错误 ");
		} else if (alreadyLoggedIn) {
			db.addLog("已登录 ");
			
			SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
			String lastssid = sp.getString("lastssid", "");

			WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			String currentssid = wifi_service.getConnectionInfo().getSSID();
			
			if (!currentssid.equals(lastssid)) {
				Toast.makeText(context.getApplicationContext(),
						"AutoLogin: 已登录~", Toast.LENGTH_LONG)
						.show();
				sp.edit().putString("lastssid", currentssid).apply();
			}
		}
	}

	public Boolean checkLogin() {
		try {
			HttpClient client = new DefaultHttpClient();
			URI website = new URI("https://w.seu.edu.cn/portal/init.php");
			HttpGet request = new HttpGet();
			request.setHeader("User-Agent", "Mozilla/5.0 (Android " + android.os.Build.VERSION.RELEASE + ") AutoLogin/" + version);
			request.setURI(website);
			HttpResponse response = client.execute(request);
			// int statusCode = response.getStatusLine().getStatusCode();

			String responseStr = EntityUtils.toString(response.getEntity());
			//Log.d("autologin", responseStr);
			if (responseStr.contains("login_username")) {
				alreadyLoggedIn = true;
				Log.d("autologin", "already logged in");
				return true;
			} else {
				// Log.d("autologin", "have not login");
				return false;
			}

		} catch (Exception e) {
			Log.d("checkLogin", "Error in http connection " + e.toString());
			retry = true;
			return false;
		}
	}

	public Boolean login() {
		try {
			HttpClient client = new DefaultHttpClient();
			URI website = new URI("https://w.seu.edu.cn/portal/login.php");
			HttpPost request = new HttpPost(website);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", account));
			nameValuePairs.add(new BasicNameValuePair("password", passwd));
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			request.setHeader("User-Agent", "Mozilla/5.0 (Android " + android.os.Build.VERSION.RELEASE + ") AutoLogin/" + version);

			// Execute HTTP Post Request
			HttpResponse response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == 200) {
				String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
				//Log.d("autologin", "认证结果  " + responseStr);
				if (responseStr.contains("login_username")) {
					//Log.d("autologin", "logged in " + String.valueOf(statusCode));
					return true;
				} else if (responseStr.contains("\\u5e76\\u53d1\\u767b\\u5f55\\u8d85\\u8fc7\\u6700\\u5927\\u9650\\u5236")) {
					Log.d("autologin", "error: \u5e76\u53d1\u767b\u5f55\u8d85\u8fc7\u6700\u5927\u9650\u5236");
					exceedError = true;
				} else if (responseStr.contains("\\u7528\\u6237\\u540d\\u5bc6\\u7801\\u9519\\u8bef")) {
					Log.d("autologin", "error: \u7528\u6237\u540d\u5bc6\u7801\u9519\u8bef");
					passwdError = true;
				}
				return false;
			} else {
				Log.d("autologin", "logged failed, statusCode:" + String.valueOf(statusCode));
				return false;
			}
		} catch (Exception e) {
			Log.d("login", "Error in http connection " + e.toString());
			retry = true;
			return false;
		}
	}
	
	public void retry() {
		if (retrys < 3) {
			Log.d("autologin", "retrys " + String.valueOf(retrys));
			String UNIQUE_STRING = "com.cedar.autologin.unknownhostBroadcast";
			Intent intent = new Intent(UNIQUE_STRING);
			intent.putExtra("retrys", String.valueOf(retrys));
			if (foreground)
				intent.putExtra("foreground", "true");
			context.sendBroadcast(intent);
		} else
			retry = false;
	}
	
	public void showNotification(String content) {

		int notifyId = 1;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		
		if (mNotificationManager == null || mBuilder == null)
			return;
		
		mBuilder.setContentTitle(content)
		.setTicker(content)
		.setAutoCancel(true)
		.setSmallIcon(R.drawable.ic_launcher);;
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pendingIntent);
		mNotificationManager.notify(notifyId, mBuilder.build());
	}
}