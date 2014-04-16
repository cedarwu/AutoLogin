package com.cedar.autologin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class LoginTask extends AsyncTask<BasicNameValuePair, Integer, Boolean> {
	Context context;
	String account;
	String passwd;
	
	public LoginTask(Context context) {

	    this.context = context;
	}
	
	protected Boolean doInBackground(BasicNameValuePair... params) {
		if (!checkLogin()) {
			if (params.length > 0) {
				account = params[0].getName();
				passwd = params[0].getValue();
			} else {
				SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
				account = sp.getString("account", "");  
				passwd = sp.getString("passwd", "");
				if (account.equals("") || passwd.equals("")) {
					Log.d("autologin", "account or passwd is empty");
					return false;
				}
			}
			if (login()) {
				Log.d("autologin", "login succeed");
				return true;
			} else {
				Log.d("autologin", "login failed");
				return false;
			}
		}
		return false;
	}

	protected void onPostExecute(Boolean result) {
		if (result) {
			Toast.makeText(context.getApplicationContext(),
					"sign into seu-wlan succeed !", Toast.LENGTH_LONG)
					.show();
		} else {
			//Toast.makeText(context.getApplicationContext(),
			//		"sign into seu-wlan failed !", Toast.LENGTH_LONG)
			//		.show();
		}
	}

	public Boolean checkLogin() {

		BufferedReader in = null;
		String data = null;

		try {
			HttpClient client = new DefaultHttpClient();
			URI website = new URI("https://w.seu.edu.cn/portal/init.php");
			HttpGet request = new HttpGet();
			request.setURI(website);
			HttpResponse response = client.execute(request);
			// int statusCode = response.getStatusLine().getStatusCode();

			in = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String l = "";
			String nl = System.getProperty("line.separator");
			while ((l = in.readLine()) != null) {
				sb.append(l + nl);
			}
			in.close();
			data = sb.toString();
			Log.d("autologin", data);
			if (data.contains("notlogin")) {
				// Log.d("autologin", "have not login");
				return false;
			} else {
				Log.d("autologin", "already logged in");
				return true;
			}

		} catch (Exception e) {
			Log.d("autologin", "Error in http connection " + e.toString());
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					Log.d("autologin", e.getMessage());
				}
			}
		}
	}

	public Boolean login() {
		//EditText accountText = (EditText) findViewById(R.id.account_message);
		//EditText passwdText = (EditText) findViewById(R.id.passwd_message);
		//String account = accountText.getText().toString();
		//String passwd = passwdText.getText().toString();
		// Log.d("autologin", account + ":" + passwd);

		try {
			HttpClient client = new DefaultHttpClient();
			URI website = new URI("https://w.seu.edu.cn/portal/login.php");
			HttpPost request = new HttpPost(website);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
					2);
			nameValuePairs.add(new BasicNameValuePair("username", account));
			nameValuePairs.add(new BasicNameValuePair("password", passwd));
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == 200) {
				Log.d("autologin",
						"logged in " + String.valueOf(statusCode));
				return true;
			} else {
				Log.d("autologin",
						"logged failed " + String.valueOf(statusCode));
				return false;
			}
		} catch (Exception e) {
			Log.d("autologin", "Error in http connection " + e.toString());
			return false;
		}
	}
}