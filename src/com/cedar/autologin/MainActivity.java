package com.cedar.autologin;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener {

	final static String version = "2.1";
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

	static AccountFragment accountFragment = null;
	static NicFragment nicFragment = null;
	static LogFragment logFragment = null;

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
		if (id == R.id.select_ssid) {
		    startActivity(new Intent(this, SelectSsidActivity.class));  
			return true;
		}
		else if (id == R.id.action_about) {
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
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if( this.getCurrentFocus() != null) {
			imm.hideSoftInputFromWindow(
					this.getCurrentFocus().getWindowToken(), 0);
		}
		if (tab.getPosition() == 2 && logFragment != null) {
			logFragment.refresh();
		}
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		if (logFragment != null) {
			logFragment.refresh();
		}
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
			default:
				accountFragment = AccountFragment.newInstance(position + 1);
				return accountFragment;
			case 1:
				nicFragment = NicFragment.newInstance(position + 1);
				return nicFragment;
			case 2:
				logFragment = LogFragment.newInstance(position + 1);
				return logFragment;
			}
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
		String ecardMoney = new String("");
		
		ArrayList<OnlineDevice> onlineDevices = new ArrayList<OnlineDevice>();

		DefaultHttpClient client = getHttpClient();
		
	    private SwipeRefreshLayout mSwipeRefreshLayout;
		
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
		//TextView ecardMoneyText;

		AlertDialog.Builder builder;
		AlertDialog.Builder unlockBuilder;
		NicFragment nic = this;
		
		String kick_ip_address = new String("");
		String session_id = new String("");
		String nas_ip_address = new String("");
		
		int payFeeMonths = 0;
		String queryPasswd = new String("");
   	 	String rechargeMoney = new String("");
   	 	String verifyCode = new String("");

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
			
			mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
			mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright, 
	                android.R.color.holo_green_light, 
	                android.R.color.holo_orange_light, 
	                android.R.color.holo_red_light);

			mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
	            @Override
	            public void onRefresh() {
	                refresh();
	            }
	        });
			
	        return rootView;
		}
		

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
		}
		
		@Override
		public void onStart() {
			super.onStart();
			
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
			//ecardMoneyText = (TextView) getActivity().findViewById(R.id.ecardMoney);
			
			onlineTable.setStretchAllColumns(true);
			
			builder = new AlertDialog.Builder(getActivity());
			unlockBuilder = new AlertDialog.Builder(getActivity());
			new NicTask(getActivity().getApplicationContext(), "initial").execute();

			button_unlock.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					unlockBuilder.setTitle("解锁")
	               	    .setMessage("您确认要解锁web认证服务？")
	               	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	               	        public void onClick(DialogInterface dialog, int which) {
	               		        new NicTask(getActivity().getApplicationContext(), "unlock").execute();
	               	        }
	               	     })
	               	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	               	        public void onClick(DialogInterface dialog, int which) { 
	               	            // do nothing
	               	        }
	               	     })
	               	    .setIcon(android.R.drawable.ic_dialog_alert)
	               	    .show();
				}
			});

			button_payfee.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	                 FragmentManager fm = getActivity().getSupportFragmentManager();
	                 PayFeeDialog editNameDialog = PayFeeDialog.newInstance(accountState);
	                 editNameDialog.show(fm, "fragment_edit_name");
	                 
	             }
	         });
			
			buttonRecharge.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	                 FragmentManager fm = getActivity().getSupportFragmentManager();
	                 RechargeDialog rechargeDialog = RechargeDialog.newInstance(account);
	                 rechargeDialog.show(fm, "fragment_recharge");
	                 
	             }
	         });
		}
		
		private void refresh() {
	        clearInfo();
	        new NicTask(getActivity().getApplicationContext(), "initial").execute();
	    }
		
	    private void onRefreshComplete() {
	        mSwipeRefreshLayout.setRefreshing(false);
	    }
	    
	    private void clearInfo() {
	    	accountCardText.setText("");
	    	accountStateText.setText("");
	    	onlineStateText.setText("");
	    	usageText.setText("");
	    	expireDateText.setText("");
	    	remainMoneyText.setText("");
	    	//ecardMoneyText.setText("");
	    	
	    	onlineTable.removeAllViews();
	    	onlineTable.removeAllViewsInLayout();
	    	
			account = new String("");
			passwd = new String("");
			
			accountState = new String("");
			onlineState = new String("");
			usage = new String("");
			expireDate = new String("");
			remainMoney = new String("");
			ecardMoney = new String("");
			onlineDevices = new ArrayList<OnlineDevice>();
			
			payFeeMonths = 0;
			queryPasswd = new String("");
	   	 	rechargeMoney = new String("");
	   	 	verifyCode = new String("");
			
			client = getHttpClient();
	    }

		public DefaultHttpClient getHttpClient() {
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
			HttpConnectionParams.setSoTimeout(httpParams, 10000);
			DefaultHttpClient client = new DefaultHttpClient(httpParams);
			return client;
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

			Boolean networkError = false;
			String errorInfo = new String("");

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
				SQLiteHelper db = new SQLiteHelper(context);
				if (action.equals("offline")) {
					if (offline())
						db.addLog("下线 " + kick_ip_address + " 成功");
				}
				else if (action.equals("offlineCurrentAndLogin")) {
					if (offlineCurrentAndLogin()) {
						db.addLog("下线当前设备 成功");
						return true;
					}
					else
						return false;
				}
				else if (action.equals("unlock")) {
					if (unlock())
						db.addLog("流量解锁 成功");
				}
				else if (action.equals("payfee")) {
					if (payFee())
						db.addLog("缴月租费  " + String.valueOf(payFeeMonths) + " 个月 成功");
					getExpireDate();
				}
				else if (action.equals("recharge")) {
					if (recharge()) {
						db.addLog("在线充值  " + rechargeMoney + " 元  成功");
					}
				}
				// default action is "initial"
				getStates();
				return true;
			}

			protected void onPostExecute(Boolean result) {
				if (action.equals("offlineCurrentAndLogin"))
					return;
				if (accountCardText == null)
					return;
				accountCardText.setText(account);
				if (!stopped) {
					accountStateText.setText(accountState);
					if (accountState.equals("超流量锁定"))
						accountStateText.setTextColor(Color.RED);
					else
						accountStateText.setTextColor(Color.BLACK);
					onlineStateText.setText(onlineState);
					usageText.setText(usage);
					expireDateText.setText(expireDate);
					remainMoneyText.setText(remainMoney);
					//ecardMoneyText.setText(ecardMoney);
					
					onlineTable.removeAllViews();
					onlineTable.removeAllViewsInLayout();
					
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
						
						onlineTable.addView(row,new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)); 
					}
					
					WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					String macAddr = wifi_service.getConnectionInfo().getMacAddress();
					macAddr = macAddr.replaceAll(":", "");
					
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
						
						if (device.mac.replaceAll("\\.", "").equals(macAddr)) {
							t1.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
							t2.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
							t3.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
						}
						
						Button b = new Button(context);
						b.setText("下线");
						final OnlineDevice dev = device;
						b.setOnClickListener(new View.OnClickListener() {
				             public void onClick(View v) {
				            	 builder.setTitle("下线")
				               	    .setMessage("您确认要将IP地址为：" + dev.ip + " 的机器下线吗？")
				               	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				               	        public void onClick(DialogInterface dialog, int which) {
				               	        	kick_ip_address = dev.ip;
				               	        	session_id = dev.session_id;
				               	        	nas_ip_address = dev.nas_ip;
				               		        new NicTask(getActivity().getApplicationContext(), "offline").execute();
				               		        //nic.refresh();
				               	        }
				               	     })
				               	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				               	        public void onClick(DialogInterface dialog, int which) { 
				               	            // do nothing
				               	        }
				               	     })
				               	    .setIcon(android.R.drawable.ic_dialog_alert)
				               	    .show();

				             }
				         });
						row.addView(b);
						
						onlineTable.addView(row,new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)); 
					}
				} else {
					accountStateText.setText("欠费停用,请缴纳月租费");
					remainMoneyText.setText(remainMoney);
				}
				
				if (networkError) {
					Toast.makeText(context.getApplicationContext(),
							"网络错误 !", Toast.LENGTH_LONG).show();
				} else if (!errorInfo.equals("")) {
					Toast.makeText(context.getApplicationContext(),
							errorInfo, Toast.LENGTH_LONG).show();
				}
				
	            onRefreshComplete();
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
					
					HttpResponse response = client.execute(request);
					int statusCode = response.getStatusLine().getStatusCode();
					
					if (statusCode == 403) {
						errorInfo = "请使用校园网";
						response.getEntity().consumeContent();
						return false;
					} else if (statusCode != 200) {
						Log.d("nic", "logged failed, statusCode:"
								+ String.valueOf(statusCode));
						response.getEntity().consumeContent();
						return false;
					}

					/*
					if (!checkCookie(client)) {
						Log.d("nic", "cookie error");
						return false;
					}
					*/

					String responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");
					//Log.d("autologin", "login response:" + responseStr);
					if (responseStr.contains("到期时间")) {
						Pattern p = Pattern.compile("<br>到期时间为<br>([\\d-]+)</td>");
						Matcher m = p.matcher(responseStr);
						if (m.find()) {
							expireDate = m.group(1);
						}
						//Log.d("nic", "logged in");
						return true;
					} else if (responseStr.contains("欠费停用")) {
						stopped = true;
						//Log.d("nic", "logged in");
						return true;
					} else {
						Pattern p = Pattern.compile("id=\"error_info\" name=\"error_info\" value=\"(\\S+)\">");
						Matcher m = p.matcher(responseStr);
						if (m.find()) {
							errorInfo = m.group(1);
						}
						return false;
					}
				} catch (Exception e) {
					Log.d("autologin",
							"Error in http connection " + e.toString());
					networkError = true;
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
					if (responseStr.contains("超流量锁定"))
						accountState = "超流量锁定";
					
					onlineState = findInStr(responseStr, 
							"<strong>在线状态</strong></td>\\s+<td width=\"\\d0%\" align=\"center\" bgcolor=\"#FFFFFF\" colspan=\"3\"><font color=\"#FF0000\">(\\S+)</font></td>");
					
					if (onlineState.equals(""))
						onlineState = findInStr(responseStr, 
							"<strong>在线状态</strong></td>\\s+<td width=\"\\d0%\" align=\"center\" bgcolor=\"#FFFFFF\" colspan=\"3\">(\\S+)</td>");

					usage = findInStr(responseStr, 
							"<strong>已使用流量</strong></td>\\s+<td width=\"50%\" align=\"center\" bgcolor=\"#FFFFFF\">\\s+([\\d\\.]+ \\w+)\\s+</td>");
					
					onlineDevices.clear();
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
					networkError = true;
					return false;
				}
			}

			public Boolean getExpireDate() {
				try {
					URI website = new URI(
							"https://nic.seu.edu.cn/selfservice/service_manage_index.php");
					
					HttpGet request = new HttpGet(website);
					setGetHeader(request);
					
					HttpResponse response = client.execute(request);
					int statusCode = response.getStatusLine().getStatusCode();
					
					if (statusCode != 200) {
						Log.d("nic", "getExpireDate failed, statusCode:"
								+ String.valueOf(statusCode));
						return false;
					}

					String responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");

					expireDate = findInStr(responseStr, 
							"<br>到期时间为<br>([\\d-]+)</td>");
					
					return true;
				} catch (Exception e) {
					Log.d("getExpireDate",
							"Error in http connection " + e.toString());
					networkError = true;
					return false;
				}
			}
			
			//not used
			public Boolean getCardMoney() {
				if (networkError)
					return false;
				try {

					DefaultHttpClient seuClient = getHttpClient();

					//seuClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,CookiePolicy.BROWSER_COMPATIBILITY);
					//seuClient.setCookieStore(new BasicCookieStore());
					HttpHost proxy = new HttpHost("192.168.155.1", 8080);
					seuClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);

					URI website = new URI(
							"http://my.seu.edu.cn/");
					
					HttpGet request2 = new HttpGet(website);
					setGetHeader(request2);

					HttpResponse response = seuClient.execute(request2);
					int statusCode = response.getStatusLine().getStatusCode();
					
					if (statusCode != 200) {
						Log.d("myseu", "logged2 failed, statusCode:"
								+ String.valueOf(statusCode));
						return false;
					}

					response.getEntity().consumeContent();
					
					website = new URI(
							"http://my.seu.edu.cn/userPasswordValidate.portal");

					HttpPost request = new HttpPost(website);
					setPostHeader(request);
					
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("Login.Token1",
							account));
					nameValuePairs.add(new BasicNameValuePair("Login.Token2",
							passwd));
					request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					
					response = seuClient.execute(request);
					statusCode = response.getStatusLine().getStatusCode();
					
					if (statusCode != 200) {
						Log.d("myseu", "logged failed, statusCode:"
								+ String.valueOf(statusCode));
						return false;
					}
					
					response.getEntity().consumeContent();
					
					//CookieStore cookie = seuClient.getCookieStore();

					//Log.d("old cookie", cookie.getCookies().toString());
					
					//Log.d("cookie", seuClient.getCookieStore().toString());
					
					website = new URI(
							"http://allinonecard.seu.edu.cn/accounttranUser.action");
					
					request2 = new HttpGet(website);
					setGetHeader(request2);
					response = seuClient.execute(request2);
					
					statusCode = response.getStatusLine().getStatusCode();
					
					if (statusCode != 200) {
						Log.d("myseu", "getEMoney failed, statusCode:"
								+ String.valueOf(statusCode));
						return false;
					}

					String responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");

					ecardMoney = findInStr(responseStr, 
							"电子钱包</div></td>\\s+<td width = \"25%\" height=\"16\" align=\"right\" valign=\"middle\"><div align=\"center\">([\\d\\.])+</div></td>");
					
					return true;

				} catch (Exception e) {
					Log.d("getCardMoney",
							"Error in http connection " + e.toString());
					networkError = true;
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
					//Log.d("nic", "cookies:" + cookieNames.toString());
					return false;
				}
			}

			String userAgent = new String("Mozilla/5.0 (Android " + android.os.Build.VERSION.RELEASE + ") AutoLogin/" + version);
			//String userAgent = new String("Mozilla/5.0 (Windows NT 6.2; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");

			private void setGetHeader(HttpGet request) {
				request.setHeader("User-Agent", userAgent);
				request.setHeader("Referer", "https://nic.seu.edu.cn/selfservice/campus_login.php");
				request.setHeader("Accept",
		                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
				request.setHeader("Accept-Encoding", "gzip,deflate");
				request.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		    }
			private void setPostHeader(HttpPost request) {
				request.setHeader("User-Agent", userAgent);
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
				//t.setTextAppearance(context, android.R.style.TextAppearance_Medium);
				t.setTextColor(Color.BLACK);
				
				TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(10, 0, 10, 0);
				t.setLayoutParams(params);
			}

			Boolean offline() {
				Log.d("offline", kick_ip_address);
				
				if (kick_ip_address.equals("") || session_id.equals("") || nas_ip_address.equals(""))
					return false;

				try {
					URI website = new URI(
							"https://nic.seu.edu.cn/selfservice/service_manage_status_web.php");
					HttpPost request = new HttpPost(website);
					setPostHeader(request);

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("operation",
							"offline"));
					nameValuePairs.add(new BasicNameValuePair("item", ""));
					nameValuePairs
							.add(new BasicNameValuePair("error_info", ""));
					nameValuePairs.add(new BasicNameValuePair(
							"kick_ip_address", kick_ip_address));
					nameValuePairs.add(new BasicNameValuePair("session_id",
							session_id));
					nameValuePairs.add(new BasicNameValuePair("nas_ip_address",
							nas_ip_address));
					request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					HttpResponse response = client.execute(request);
					int statusCode = response.getStatusLine().getStatusCode();

					if (statusCode != 200) {
						Log.d("nic", "offline " + kick_ip_address + " failed, statusCode:" + String.valueOf(statusCode));
						return false;
					}
					
					String responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");
					errorInfo = findInStr(responseStr, "id=\"error_info\" name=\"error_info\" value=\"(\\S+)\">");
					if (errorInfo.equals(""))
						return true;
				} catch (Exception e) {
					Log.d("offline", "Error in http connection " + e.toString());
				}
				return false;
			}
			
			Boolean offlineCurrentAndLogin() {
				WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				String macAddr = wifi_service.getConnectionInfo().getMacAddress();
				Log.d("macAddr:", macAddr);
				macAddr = macAddr.replaceAll(":", "");
				
				if (macAddr.equals("")) {
					Log.d("offlineCurrentAndLogin", "getMacAddress is empty");
					return false;
				}
				
				for (OnlineDevice device : onlineDevices) {
					if (device.mac.replaceAll("\\.", "").equals(macAddr)) {
						kick_ip_address = device.ip;
           	        	session_id = device.session_id;
           	        	nas_ip_address = device.nas_ip;
           	        	if (offline()) {
           	        		Log.d("offlineCurrent", "succeed");
               	        	new LoginTask(context).execute();
               	        	return true;
           	        	}
					}
				}
				return false;
			}

			Boolean unlock() {
				try {
					URI website = new URI(
							"https://nic.seu.edu.cn/selfservice/service_manage_status_web.php");
					HttpPost request = new HttpPost(website);
					setPostHeader(request);

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("operation", "unlock"));
					nameValuePairs.add(new BasicNameValuePair("item", "web"));
					nameValuePairs
							.add(new BasicNameValuePair("error_info", ""));
					nameValuePairs.add(new BasicNameValuePair("kick_ip_address", ""));
					nameValuePairs.add(new BasicNameValuePair("session_id", ""));
					nameValuePairs.add(new BasicNameValuePair("nas_ip_address", ""));
					request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					HttpResponse response = client.execute(request);
					int statusCode = response.getStatusLine().getStatusCode();

					if (statusCode != 200) {
						Log.d("nic", "unlock failed, statusCode:" + String.valueOf(statusCode));
						return false;
					}
					
					String responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");
					errorInfo = findInStr(responseStr, "id=\"error_info\" name=\"error_info\" value=\"(\\S+)\">");
					if (errorInfo.equals(""))
						return true;
				} catch (Exception e) {
					Log.d("offline", "Error in http connection " + e.toString());
				}
				return false;
			}

			Boolean payFee() {
				String months = String.valueOf(payFeeMonths);
				//Log.d("payFee", months);
				
				if (payFeeMonths <= 0)
					return false;

				try {
					URI website = new URI(
							"https://nic.seu.edu.cn/selfservice/service_manage_index.php");
					HttpPost request = new HttpPost(website);
					setPostHeader(request);

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("operation", "web_delay"));
					nameValuePairs.add(new BasicNameValuePair("item", "web"));
					nameValuePairs.add(new BasicNameValuePair("error_info", ""));
					nameValuePairs.add(new BasicNameValuePair("web_sel", months));
					request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					HttpResponse response = client.execute(request);
					int statusCode = response.getStatusLine().getStatusCode();

					if (statusCode != 200) {
						Log.d("nic", "payFee " + months + " failed, statusCode:" + String.valueOf(statusCode));
						return false;
					}

					String responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");
					errorInfo = findInStr(responseStr, "id=\"error_info\" name=\"error_info\" value=\"(\\S+)\">");
					if (errorInfo.equals(""))
						return true;
				} catch (Exception e) {
					Log.d("payFee", "Error in http connection " + e.toString());
				}
				return false;
			}
			
			Boolean recharge() {
				if (queryPasswd.equals("") || rechargeMoney.equals("") || verifyCode.equals(""))
					return false;

				try {
					URI website = new URI(
							"https://nic.seu.edu.cn/selfservice/service_recharge_rfid.php");
					HttpPost request = new HttpPost(website);
					setPostHeader(request);

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("password", queryPasswd));
					nameValuePairs.add(new BasicNameValuePair("amount", rechargeMoney));
					nameValuePairs.add(new BasicNameValuePair("verify_code", verifyCode));
					request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					HttpResponse response = client.execute(request);
					int statusCode = response.getStatusLine().getStatusCode();

					if (statusCode != 200) {
						Log.d("nic", "recharge failed, statusCode:" + String.valueOf(statusCode));
						return false;
					}

					String responseStr = EntityUtils.toString(
							response.getEntity(), "gb2312");
					//Log.d("recharge", responseStr);
					if (responseStr.contains("<img src=\"image/recharge_success.gif\">")) {
						Pattern pattern = Pattern.compile("电子钱包.+帐户余额</strong></td>\\s+<td align=\"center\" bgcolor=\"#FFFFFF\">([\\d\\.]+ 元)</td>", Pattern.DOTALL);
						Matcher matcher = pattern.matcher(responseStr);
						if (matcher.find()) {
							errorInfo = "充值成功，电子钱包余额为：" +  matcher.group(1);
						}
						return true;
					}
					else {
						errorInfo = findInStr(responseStr, "id=\"error_info\" value=\"(\\S+)\">");
						return false;
					}
				} catch (Exception e) {
					Log.d("payFee", "Error in http connection " + e.toString());
				}
				return false;
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

		public void refresh() {
			FragmentActivity activity = getActivity();
			if (activity == null)
				return;
			SQLiteHelper db = new SQLiteHelper(activity);
			String dateStamp = new SimpleDateFormat("yyyyMMdd",
					java.util.Locale.getDefault()).format(Calendar
					.getInstance().getTime());
			List<String> dbLogList = db.getLogsByDate(dateStamp);
			String[] logs = dbLogList.toArray(new String[dbLogList.size()]);
			ListView logList = (ListView) getActivity().findViewById(
					R.id.logList);
			activity = getActivity();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
					android.R.layout.simple_list_item_1, logs);
			if (adapter != null && logList != null)
				logList.setAdapter(adapter);
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
	
	public static class PayFeeDialog extends DialogFragment {
		
	    public PayFeeDialog() {
	    }

	    public static PayFeeDialog newInstance(String accountState) {
	    	PayFeeDialog frag = new PayFeeDialog();
	        Bundle args = new Bundle();
	        args.putString("accountState", accountState);
	        frag.setArguments(args);
	        return frag;
	    }

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View view = inflater.inflate(R.layout.fragment_edit_name, container);
	        
	        final EditText mEditText = (EditText) view.findViewById(R.id.timeEditDialog);
	        getDialog().setTitle("缴月租费");
	        // Show soft keyboard automatically
	        mEditText.requestFocus();
	        getDialog().getWindow().setSoftInputMode(
	                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

	        TextView mTextView = (TextView) view.findViewById(R.id.timeTextDialog);
	        mTextView.setText("个月");
	        
	        final String accountState = getArguments().getString("accountState", "");
	        final Dialog dlg = getDialog();


	        Button backButton = (Button) view.findViewById(R.id.backButton);
	        backButton.setText("返回");
	        backButton.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	            	 dlg.dismiss();
	             }
	        });
	        
	        Button payButton = (Button) view.findViewById(R.id.payButton);
	        payButton.setText("缴费");
	        payButton.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
					try {
						final int months = Integer.parseInt(mEditText.getText()
								.toString());
						if (months <= 0) {
							Toast.makeText(v.getContext(), "月份必须为正 ！",
									Toast.LENGTH_LONG).show();
							return;
						}
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("确认缴费")
								.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												nicFragment.payFeeMonths = months;
												nicFragment.new NicTask(getActivity().getApplicationContext(), "payfee").execute();
												dlg.dismiss();
											}
										})
								.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												// do nothing
											}
										})
								.setIcon(android.R.drawable.ic_dialog_alert);
						if (accountState.equals("正常")) {
							builder.setMessage("您正在进行web认证服务缴纳月租费操作\n\n续租时长："
									+ months + "个月   总计费用：" + months * 5
									+ "元\n\n校园网账户扣除费用：" + months * 5
									+ "元\n\n请注意：费用扣除后将不予以退还，是否继续执行此操作？");
						} else {
							builder.setMessage("您正在进行web认证服务开通操作\n\n开通时长："
									+ months + "个月   总计费用：" + months * 5
									+ "元\n\n校园网账户扣除费用：" + months * 5
									+ "元\n\n请注意：费用扣除后将不予以退还，是否继续执行此操作？");
						}
						builder.show();
					}
					catch (Exception e) {
						Log.d("PayFeeDialog", "exception:" + e.toString());
					}
	             }
	         });
	        
	        return view;
	    }
	    
	    @Override
	    public void onStop() {
	        if( getActivity() != null) {
	            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	            View v = getActivity().getCurrentFocus();
	            Window w = getActivity().getWindow();
	            if (v != null) 
	            	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	            if (w != null)
	            	w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	        }
	        super.onStop();
	    }
	}
	
	public static class RechargeDialog extends DialogFragment {
		
	    public RechargeDialog() {
	    }

	    public static RechargeDialog newInstance(String accountCard) {
	    	RechargeDialog frag = new RechargeDialog();
	        Bundle args = new Bundle();
	        args.putString("accountCard", accountCard);
	        frag.setArguments(args);
	        return frag;
	    }

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View view = inflater.inflate(R.layout.fragment_recharge, container);
	        
	        new DownloadImageTask((ImageView) view.findViewById(R.id.verifyImage))
            .execute("https://nic.seu.edu.cn/selfservice/verifyCode.php");
	        
	        getDialog().setTitle("在线充值");

	        TextView accountCardText = (TextView) view.findViewById(R.id.accountCardRecharge);
	        String accountCard = getArguments().getString("accountCard", "");
	        accountCardText.setText(accountCard);
    
	        final Dialog dlg = getDialog();

	        Button backButton = (Button) view.findViewById(R.id.buttonBack);
	        backButton.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	            	 dlg.dismiss();
	             }
	         });
	        
	        Button yesButton = (Button) view.findViewById(R.id.buttonYes);
	        yesButton.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	            	 String passwd = ((TextView)dlg.findViewById(R.id.queryPasswd)).getText().toString();
	            	 String money = ((TextView)dlg.findViewById(R.id.rechargeMoney)).getText().toString();
	            	 String code = ((TextView)dlg.findViewById(R.id.verifyCode)).getText().toString();
	            	 nicFragment.queryPasswd = passwd;
	            	 nicFragment.rechargeMoney = money;
	            	 nicFragment.verifyCode = code;
	            	 nicFragment.new NicTask(getActivity().getApplicationContext(), "recharge").execute();
	            	 dlg.dismiss();
	             }
	         });
	        
	        return view;
	    }
	    
	    @Override
	    public void onStop() {
	        if( getActivity() != null) {
	            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	            View v = getActivity().getCurrentFocus();
	            Window w = getActivity().getWindow();
	            if (v != null) 
	            	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	            if (w != null)
	            	w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	        }
	        super.onStop();
	    }

		private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
			ImageView bmImage;

			public DownloadImageTask(ImageView bmImage) {
				this.bmImage = bmImage;
			}

			protected Bitmap doInBackground(String... urls) {
				String urldisplay = urls[0];
				Bitmap mIcon = null;
				try {
					HttpGet request = new HttpGet(urldisplay);
					HttpResponse response = nicFragment.client.execute(request);
					int statusCode = response.getStatusLine().getStatusCode();
					
					if (statusCode != 200) {
						Log.d("DownloadImageTask", "failed, statusCode:"
								+ String.valueOf(statusCode));
						throw new IOException("Data Not Found");
					}
					mIcon = BitmapFactory.decodeStream(response.getEntity().getContent());
					response.getEntity().consumeContent();
				} catch (Exception e) {
					Log.e("Error", e.getMessage());
					e.printStackTrace();
				}
				return mIcon;
			}

			protected void onPostExecute(Bitmap result) {
				Bitmap scaledBitmap = null;
				if (result != null) {
					scaledBitmap = Bitmap.createScaledBitmap(result, bmImage.getWidth()*2, result.getHeight()*2*bmImage.getWidth()/result.getWidth(), true);
					bmImage.setImageBitmap(scaledBitmap);
				}
			}
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
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if( view.getWindowToken() != null) {
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
			
			Editor editor = getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit();  
            editor.putString("account", account);  
            editor.putString("passwd",passwd);  
            editor.commit();
            nicFragment.refresh();
            WifiManager wifi_service = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			String ssid = wifi_service.getConnectionInfo().getSSID();
			if (ssid.startsWith("\"") && ssid.endsWith("\"")){
				ssid = ssid.substring(1, ssid.length()-1);
			}
			if (checkSsid(ssid)) {
				Log.d("autologin", "wifi connected to " + ssid);
				new LoginTask(getApplicationContext()).execute();
			}
			Toast.makeText(getApplicationContext(), "保存成功,程序将自动完成Web认证 ~", Toast.LENGTH_LONG).show();
		}
	}

	public void changeDate(View view) {
		DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "datePicker");
	}
	
	public boolean checkSsid(String ssid) {
		SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		Set<String> ssidSet = sp.getStringSet("ssid", new HashSet<String>(Arrays.asList("seu-wlan")));
		if (ssidSet.contains(ssid))
			return true;
		else
			return false;
	}
}
