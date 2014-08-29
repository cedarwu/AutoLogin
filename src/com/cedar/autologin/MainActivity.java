package com.cedar.autologin;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener {

	static final String ssid = "seu-wlan";
	static String version;
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		version = getVersion(this);
		setTitle(getString(R.string.app_name) + "  v" + version);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_about) {
			Uri uri = Uri.parse("http://autologin.cedar.tk");  
		    startActivity(new Intent(Intent.ACTION_VIEW, uri));  
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		//InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		//if( tab.getPosition() == 1) {
		//	imm.hideSoftInputFromWindow(
		//			this.getCurrentFocus().getWindowToken(), 0);
		//}
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			switch (position) {
			case 0:
				return AccountFragment.newInstance(position + 1);
			case 1:
				return NicFragment.newInstance(position + 1);
			case 2:
				return LogFragment.newInstance(position + 1);
			}
			return AccountFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			//Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1);
			case 1:
				return getString(R.string.title_section2);
			case 2:
				return getString(R.string.title_section3);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class AccountFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static AccountFragment newInstance(int sectionNumber) {
			AccountFragment fragment = new AccountFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public AccountFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			EditText accountText = (EditText) rootView.findViewById(R.id.account_message);
			EditText passwdText = (EditText) rootView.findViewById(R.id.passwd_message);

			SharedPreferences sp = this.getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
			accountText.setText(sp.getString("account", ""));  
			passwdText.setText(sp.getString("passwd", ""));

	        return rootView;
		}
	}

	public static class NicFragment extends Fragment {

		private static final String ARG_SECTION_NUMBER = "section_number";

		String account = new String("");
		String passwd = new String("");
		
		String accountState = new String("");
		String onlineState = new String("");
		String usage = new String("");
		String expireDate = new String("");
		String remainMoney = new String("");
		
		ArrayList<OnlineDevice> onlineDevices = new ArrayList<OnlineDevice>();

		DefaultHttpClient client = new DefaultHttpClient();
		
		TextView accountCardText;
		TextView accountStateText;
		TextView onlineStateText;
		TableLayout onlineTable;
		TextView usageText;
		Button button_unlock;
		TextView expireDateText;
		Button button_payfee;
		TextView remainMoneyText;
		Button buttonRecharge;

		public static NicFragment newInstance(int sectionNumber) {
			NicFragment fragment = new NicFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public NicFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_nic, container, false);

	        return rootView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			accountCardText = (TextView) getActivity().findViewById(R.id.accountCard);
			accountStateText = (TextView) getActivity().findViewById(R.id.accountState);
			onlineStateText = (TextView) getActivity().findViewById(R.id.onlineState);
			onlineTable = (TableLayout) getActivity().findViewById(R.id.onlineTable);
			usageText = (TextView) getActivity().findViewById(R.id.usage);
			button_unlock = (Button) getActivity().findViewById(R.id.button_unlock);
			expireDateText = (TextView) getActivity().findViewById(R.id.expireDate);
			button_payfee = (Button) getActivity().findViewById(R.id.button_payfee);
			remainMoneyText = (TextView) getActivity().findViewById(R.id.remainMoney);
			buttonRecharge = (Button) getActivity().findViewById(R.id.buttonRecharge);
			
			new NicTask(getActivity().getApplicationContext(), "initial").execute();
		}
		
		public class OnlineDevice {
			String ip = new String("");
			String mac = new String("");
			String position = new String("");
			String session_id = new String("");
			String nas_ip = new String("");
			
			public OnlineDevice(String ip, String mac, String position, String session_id, String nas_ip) {
				this.ip = ip;
				this.mac = mac;
				this.position = position;
				this.session_id = session_id;
				this.nas_ip = nas_ip;
			}
		}

		public class NicTask extends AsyncTask<Void, Void, Boolean> {

			Context context;
			String action;
			
			Boolean stopped = false;

			Boolean exceedError = false;
			Boolean passwdError = false;
			Boolean alreadyLoggedIn = false;

			public NicTask(Context context, String action) {
				this.context = context;
				this.action = action;
			}

			protected Boolean doInBackground(Void... params) {
				if (account.equals("") || passwd.equals("")) {
					SharedPreferences sp = context.getSharedPreferences(
							"userInfo", Context.MODE_PRIVATE);
					account = sp.getString("account", "");
					passwd = sp.getString("passwd", "");
					if (account.equals("") || passwd.equals("")) {
						Log.d("nic", "account or passwd is empty");
						return false;
					}
				}
				if (!checkCookie(client)) {
					if (login()) {
						Log.d("nic", "login succeed");
					} else {
						Log.d("nic", "login failed");
						return false;
					}
				}
				if (!stopped)
					getStates();
				return true;
			}

			protected void onPostExecute(Boolean result) {
				accountCardText.setText(account);
				if (!stopped) {
					accountStateText.setText(accountState);
					onlineStateText.setText(onlineState);
					usageText.setText(usage);
					expireDateText.setText(expireDate);
					remainMoneyText.setText(remainMoney);
					
					if (onlineDevices.size() > 0) {
						TableRow row = new TableRow(context);

						TextView t1 = new TextView(context);
						setTextView(t1, "\t位置");
						row.addView(t1);
						
						TextView t2 = new TextView(context);
						setTextView(t2, "\tIP地址");
						row.addView(t2);
						
						TextView t3 = new TextView(context);
						setTextView(t3, "\tMAC地址");
						row.addView(t3);
						
						onlineTable.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
					}
					for (OnlineDevice device : onlineDevices) {
						TableRow row = new TableRow(context);

						TextView t1 = new TextView(context);
						setTextView(t1, device.position);
						row.addView(t1);
						
						TextView t2 = new TextView(context);
						setTextView(t2, device.ip);
						row.addView(t2);
						
						TextView t3 = new TextView(context);
						setTextView(t3, device.mac);
						row.addView(t3);
						
						Button b = new Button(context);
						b.setText("下线");
						row.addView(b);
						
						onlineTable.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
					}
				} else {
					accountStateText.setText("欠费停用,请缴纳月租费");
				}
				
				/*
				SQLiteHelper db = new SQLiteHelper(context);
				if (result) {
					db.addLog("登录成功");
					Toast.makeText(context.getApplicationContext(),
							"AutoLogin: 登录成功~", Toast.LENGTH_LONG).show();
				} else if (exceedError) {
					db.addLog("并发登录超过最大限制 ");
					Toast.makeText(context.getApplicationContext(),
							"AutoLogin: 并发登录超过最大限制 !", Toast.LENGTH_LONG)
							.show();
				} else if (passwdError) {
					db.addLog("用户名密码错误 ");
					Toast.makeText(context.getApplicationContext(),
							"AutoLogin: 用户名密码错误 !", Toast.LENGTH_LONG).show();
				} else if (alreadyLoggedIn) {
					db.addLog("已登录 ");
					Toast.makeText(context.getApplicationContext(),
							"AutoLogin: 已登录~", Toast.LENGTH_LONG).show();
				} else {
					db.addLog("未知错误 ");
					Toast.makeText(context.getApplicationContext(),
							"AutoLogin: 未知错误 !", Toast.LENGTH_LONG).show();
				}
				*/
			}

			public Boolean login() {
				try {
					
					URI website = new URI(
							"https://nic.seu.edu.cn/selfservice/campus_login.php");
					
					HttpPost request = new HttpPost(website);
					setPostHeader(request);
					
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("error_info",
							""));
					nameValuePairs.add(new BasicNameValuePair("username",
							account));
					nameValuePairs.add(new BasicNameValuePair("password",
							passwd));
					request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					
					//HttpHost proxy = new HttpHost("192.168.1.5", 8080);
					//client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
					
					HttpResponse response = client.execute(request);
					int statusCode = response.getStatusLine().getStatusCode();
					
					if (statusCode != 200) {
						Log.d("nic", "logged failed, statusCode:"
								+ String.valueOf(statusCode));
						return false;
					}
					
					if (!checkCookie(client)) {
						Log.d("nic", "cookie error");
						return false;
					}

					String responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");
					//Log.d("autologin", "login response:" + responseStr);
					if (responseStr.contains("到期时间")) {
						Pattern p = Pattern.compile("<br>到期时间为<br>([\\d-]+)</td>");
						Matcher m = p.matcher(responseStr);
						if (m.find()) {
							expireDate = m.group(1);
						}
						
						Log.d("nic", "logged in");
						return true;
					} else if (responseStr.contains("欠费停用")) {
						stopped = true;
						Log.d("nic", "logged in");
						return true;
					} else
						return false;
				} catch (Exception e) {
					Log.d("autologin",
							"Error in http connection " + e.toString());
					return false;
				}
			}

			public Boolean getStates() {
				try {
					URI website = new URI(
							"https://nic.seu.edu.cn/selfservice/service_manage_status_web.php");
					
					HttpGet request = new HttpGet(website);
					setGetHeader(request);
					
					HttpResponse response = client.execute(request);
					int statusCode = response.getStatusLine().getStatusCode();
					
					if (statusCode != 200) {
						Log.d("nic", "getStates failed, statusCode:"
								+ String.valueOf(statusCode));
						return false;
					}

					String responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");
					//Log.d("autologin", "getStates response:" + responseStr);

					accountState = findInStr(responseStr, 
							"<strong>账户状态</strong></td>\\s+<td width=\"50%\" align=\"center\" bgcolor=\"#FFFFFF\">(\\S+)</td>");
					
					onlineState = findInStr(responseStr, 
							"<strong>在线状态</strong></td>\\s+<td width=\"60%\" align=\"center\" bgcolor=\"#FFFFFF\" colspan=\"3\"><font color=\"#FF0000\">(\\S+)</font></td>");

					usage = findInStr(responseStr, 
							"<strong>已使用流量</strong></td>\\s+<td width=\"50%\" align=\"center\" bgcolor=\"#FFFFFF\">\\s+([\\d\\.]+ \\w+)\\s+</td>");
					
					Pattern p = Pattern.compile("<td width=\"30%\" align=\"center\" bgcolor=\"#FFFFFF\">(\\S+)</td>\\s+<td width=\"30%\" align=\"center\" bgcolor=\"#FFFFFF\">([\\d\\.]+)</td>\\s+<td width=\"30%\" align=\"center\" bgcolor=\"#FFFFFF\">([\\d\\w\\.]+)</td>\\s+<td width=\"20%\" align=\"center\" bgcolor=\"#FFFFFF\">\\s+<a href=\"javascript:offline\\( '[\\d\\.]+','([\\d]+)', '([\\d\\.]+)' \\)\">");
					Matcher m = p.matcher(responseStr);
					while (m.find() && m.groupCount() == 5) {
						onlineDevices.add(new OnlineDevice(m.group(2), m.group(3), m.group(1), m.group(4), m.group(5)));
					}
					
					website = new URI(
							"https://nic.seu.edu.cn/selfservice/service_fee_index.php");
					request.setURI(website);
					response = client.execute(request);
					statusCode = response.getStatusLine().getStatusCode();
					
					if (statusCode != 200) {
						Log.d("nic", "getStates2 failed, statusCode:"
								+ String.valueOf(statusCode));
						return false;
					}

					responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");
					
					remainMoney = findInStr(responseStr, 
							"<strong>帐户余额</strong></td>\\s+<td align=\"center\" bgcolor=\"#FFFFFF\">([\\d\\.]+ \\S+)</td>");
					
					return true;
				} catch (Exception e) {
					Log.d("autologin",
							"Error in http connection " + e.toString());
					return false;
				}
			}
			
			private Boolean checkCookie(DefaultHttpClient client) {
				ArrayList<String> cookieNames = new ArrayList<String>();
				for(Cookie cookie: client.getCookieStore().getCookies()) {
					cookieNames.add(cookie.getName());
				}
				
				if(cookieNames.contains("PHPSESSID") && cookieNames.contains("iPlanetDirectoryPro"))
					return true;
				else {
					Log.d("nic", "cookies:" + cookieNames.toString());
					return false;
				}
			}

			private void setGetHeader(HttpGet request) {
				request.setHeader("User-Agent", "Mozilla/5.0 (Android) AutoLogin/" + version);
				request.setHeader("Referer", "https://nic.seu.edu.cn/selfservice/campus_login.php");
				request.setHeader("Accept",
		                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
				request.setHeader("Accept-Encoding", "gzip,deflate");
				request.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		    }
			private void setPostHeader(HttpPost request) {
				request.setHeader("User-Agent", "Mozilla/5.0 (Android) AutoLogin/" + version);
				request.setHeader("Referer", "https://nic.seu.edu.cn/selfservice/campus_login.php");
				request.setHeader("Accept",
		                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
				request.setHeader("Accept-Encoding", "gzip,deflate");
				request.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		    }
			
			String findInStr(String str, String pattern) {
				Pattern p = Pattern.compile(pattern);
				Matcher m = p.matcher(str);
				if (m.find()) {
					return m.group(1);
				}
				else
					return "";
			}
			
			void setTextView(TextView t, String s) {

				t.setText(s);
				t.setTextAppearance(context, android.R.style.TextAppearance_Medium);
				t.setTextColor(Color.BLACK);
				
				TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(20, 0, 20, 0);
				t.setLayoutParams(params);
			}
		}
	}

	public static class LogFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static LogFragment newInstance(int sectionNumber) {
			LogFragment fragment = new LogFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public LogFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_log, container, false);
			Button button = (Button) rootView.findViewById(R.id.button_changeDate);
			String dateStamp = new SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(Calendar.getInstance().getTime());
			button.setText(dateStamp);
			return rootView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
		}

		/** Fragement 进入可视状态 */
		@Override
		public void onStart() {
			super.onStart();
			SQLiteHelper db = new SQLiteHelper(getActivity());
			String dateStamp = new SimpleDateFormat("yyyyMMdd",
					java.util.Locale.getDefault()).format(Calendar
					.getInstance().getTime());
			List<String> dbLogList = db.getLogsByDate(dateStamp);
			String[] logs = dbLogList.toArray(new String[dbLogList.size()]);
			ListView logList = (ListView) getActivity().findViewById(
					R.id.logList);
			logList.setAdapter(new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, logs));
		}

		/** Fragement 进入激活状态 */
		@Override
		public void onResume() {
			super.onResume();
		}
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			SQLiteHelper db = new SQLiteHelper(getActivity());
			String dateStamp = String.format("%2s", year).replace(' ', '0') + String.format("%2s", month+1).replace(' ', '0') + String.format("%2s", day).replace(' ', '0');

			Button button = (Button) getActivity().findViewById(R.id.button_changeDate);
			button.setText(dateStamp);
			
			List<String> dbLogList = db.getLogsByDate(dateStamp);
			String[] logs = dbLogList.toArray(new String[dbLogList.size()]);
			ListView logList = (ListView) getActivity().findViewById(R.id.logList);
			logList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, logs));
			Log.d("autologin", ("DatePickered" + dateStamp));
		}
	}

	public void accountOK(View view) {

		EditText accountText = (EditText) findViewById(R.id.account_message);
		EditText passwdText = (EditText) findViewById(R.id.passwd_message);
		String account = accountText.getText().toString();
		String passwd = passwdText.getText().toString();
		//Log.d("autologin", ssid);
		if (account.isEmpty() || passwd.isEmpty()) {
			Toast.makeText(getApplicationContext(), "Account and password can not be empty !",
					Toast.LENGTH_LONG).show();
			return;
		} else {
			Editor editor = getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit();  
            editor.putString("account", account);  
            editor.putString("passwd",passwd);  
            editor.commit();
            WifiManager wifi_service = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifi_service.getConnectionInfo();
			if (wifiInfo.getSSID().equals(ssid) || wifiInfo.getSSID().equals("\"" + ssid + "\"")) {
				Log.d("autologin", "wifi connected " + ssid);
				new LoginTask(getApplicationContext()).execute();
			}
			else {
				Toast.makeText(getApplicationContext(), "You can exit now ~", Toast.LENGTH_LONG).show();
			}
		}
	}

	public void changeDate(View view) {
		DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "datePicker");
	}
	
	public String getVersion(Context context)
    {  
        try {  
            PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
            return pi.versionName;  
        } catch (NameNotFoundException e) {  
        	Log.d("autologin", "getVersion failed");
            return "?";  
        }  
    }
}
