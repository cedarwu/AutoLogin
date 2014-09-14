package com.cedar.autologin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SelectSsidActivity extends ActionBarActivity implements WifiListFragment.onDlgListClick, AddSsidDialog.onDlgListClick {
	
	ListView ssidList = null;
	SsidAdapter adapter = null;
	Button addButton = null;
	ArrayList<String> ssidArray = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_ssid);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		
		SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		Set<String> ssidSet = sp.getStringSet("ssid", new HashSet<String>(Arrays.asList("seu-wlan")));
		ssidArray = new ArrayList<String>();
		ssidArray.addAll(ssidSet);
		
		ssidList = (ListView) findViewById(R.id.ssidList);
		if (ssidList != null) {
			adapter = new SsidAdapter(this, ssidArray);
			ssidList.setAdapter(adapter);
		}
		
		addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	AddSsidDialog addDialog = AddSsidDialog.newInstance();
            	addDialog.fm = getSupportFragmentManager();
            	addDialog.show(getSupportFragmentManager(), "fragment_add_ssid");
            }
        });
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		Set<String> ssidSet = new HashSet<String>();
		for (int i=0; i<adapter.getCount(); i++) {
			ssidSet.add(adapter.getItem(i));
		}
		
	    SharedPreferences.Editor editor = getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit();
	    editor.putStringSet("ssid", ssidSet);

	    editor.commit();
	    Log.d("ssidSet", ssidSet.toString());
	}

	@Override
	public void addSsid(String ssid) {
		Log.d("addSsid", ssid);
		if (ssid.equals("") || ssidArray.contains(ssid))
			return;
		ssidArray.add(ssid);
		 
		adapter = new SsidAdapter(this, ssidArray);
		ssidList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
	}

	private class SsidAdapter extends BaseAdapter {

		private List<String> listItems;
		private LayoutInflater inflater;

		public SsidAdapter(Context context, List<String> listItems) {
			inflater = LayoutInflater.from(context);
			this.listItems = listItems;
		}

		public int getCount() {
			return listItems.size();
		}

		public String getItem(int position) {
			return listItems.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final int selectID = position;
			
		    if (convertView == null) {
		    	convertView = inflater.inflate(R.layout.ssid_info, parent, false);
		    }

		    TextView ssidText = (TextView) convertView.findViewById(R.id.ssid_name);
		    Button rmButton = (Button) convertView.findViewById(R.id.button_rm);


			ssidText.setText((String) listItems.get(position));

			rmButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					rmSsid(selectID);
				}
			});

			return convertView;
		}

		private void rmSsid(int clickID) {
			Log.d("listItems", listItems.toString());
			Log.d(String.valueOf(clickID), listItems.get(clickID));
			listItems.remove(clickID);
			Log.d("listItems", listItems.toString());
			this.notifyDataSetChanged();
		}
	}
}
