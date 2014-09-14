package com.cedar.autologin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class AddSsidDialog extends DialogFragment {
	
	FragmentManager fm;
	onDlgListClick mCallback;
	
    public static AddSsidDialog newInstance() {
    	AddSsidDialog frag = new AddSsidDialog();
        return frag;
    }
    

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_ssid, container);
        
        getDialog().setTitle("Ìí¼ÓSSID");
        
        Button selectButton = (Button) view.findViewById(R.id.button_select);
		selectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				WifiListFragment wifiFragment = new WifiListFragment();
				wifiFragment.dlg = getDialog();
				wifiFragment.show(fm, "show wifi list");
			}
		});
		
		final EditText ssidText = (EditText) view.findViewById(R.id.ssidText);
		
		Button OkButton = (Button) view.findViewById(R.id.button_ok);
		OkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mCallback.addSsid(ssidText.getText().toString());
				getDialog().dismiss();
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