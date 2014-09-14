package com.cedar.autologin;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class WifiListFragment extends DialogFragment {

	onDlgListClick mCallback;
	
	Dialog dlg = null;
	
	ArrayList<String> connections = new ArrayList<String>();

	public interface onDlgListClick{
        public void addSsid(String ssid);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (onDlgListClick) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement addSsid");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

    	WifiManager mainWifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		List<ScanResult> wifiList = mainWifi.getScanResults();
   	 	for(int i = 0; i < wifiList.size(); i++) {
   	 		String ssid = wifiList.get(i).SSID;
   	 		if (!(ssid.equals("")) && !connections.contains(ssid))
   	 			connections.add(wifiList.get(i).SSID);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("WiFi List")
                .setItems(connections.toArray(new String[connections.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                    	mCallback.addSsid(connections.get(item));
                        getDialog().dismiss(); 
                        WifiListFragment.this.dismiss();
                        if (dlg != null)
                        	dlg.dismiss();

                    }
                }).create();

    }
}
