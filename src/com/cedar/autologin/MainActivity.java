package com.cedar.autologin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;

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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener {

	static final String ssid = "\"seu-wlan\"";
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
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
		if( tab.getPosition() == 1) {
			imm.hideSoftInputFromWindow(
					this.getCurrentFocus().getWindowToken(), 0);
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
				return LogFragment.newInstance(position + 1);
			}
			return AccountFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
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
			ListView logList = (ListView) rootView.findViewById(R.id.logList);
			String dateStamp = new SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(Calendar.getInstance().getTime());
			button.setText(dateStamp);
			SQLiteHelper db = new SQLiteHelper(getActivity());
			List<String> dbLogList = db.getLogsByDate(dateStamp);
			String[] logs = dbLogList.toArray(new String[dbLogList.size()]);
			logList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, logs));
			return rootView;
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
			//LayoutInflater inflater = getLayoutInflater();

			//final View textEntryView = factory.inflate(R.layout.landmark_new_dialog, null);

			//View rootView = inflater.inflate(R.layout.fragment_log, container, false);
			//Button button = (Button) rootView.findViewById(R.id.button_changeDate);
			//String dateStamp = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(Calendar.getInstance().getTime());
			//button.setText(dateStamp);
			Log.d("autologin", ("DatePickered" + String.valueOf(year) + String.valueOf(month) + String.valueOf(day)));
		}
	}

	public void accountOK(View view) {

		EditText accountText = (EditText) findViewById(R.id.account_message);
		EditText passwdText = (EditText) findViewById(R.id.passwd_message);
		String account = accountText.getText().toString();
		String passwd = passwdText.getText().toString();
		Log.d("autologin", ssid);
		if (account.isEmpty() || passwd.isEmpty()) {
			Toast.makeText(getApplicationContext(), "Account and password can not be empty !",
					Toast.LENGTH_LONG).show();
			return;
		} else {
			Editor editor = getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit();  
            editor.putString("account", account);  
            editor.putString("passwd",passwd);  
            editor.commit();  
			if (getSSID().equals(ssid)) {
				BasicNameValuePair userInfo = new BasicNameValuePair(account, passwd);
				new LoginTask(getApplicationContext()).execute(userInfo);
			}
			Toast.makeText(getApplicationContext(), "You can exit now ~",
					Toast.LENGTH_LONG).show();
		}

	}

	public void changeDate(View view) {
		DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "datePicker");
	}

	public String getSSID() {
		WifiManager wifi_service = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifi_service.getConnectionInfo();
		Log.d("autologin", "SSID: " + wifiInfo.getSSID());
		return wifiInfo.getSSID();
	}

}
