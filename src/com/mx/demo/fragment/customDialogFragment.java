package com.mx.demo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mx.demo.R;

public class customDialogFragment extends DialogFragment {

	public interface onPositiveListener {
		void onPositive();
	}
	
	@Override  
    public Dialog onCreateDialog(Bundle savedInstanceState)  
    {  
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());  
        // Get the layout inflater  
        LayoutInflater inflater = getActivity().getLayoutInflater();  
        View view = inflater.inflate(R.layout.dialog_normal_layout, null);
        // Inflate and set the layout for the dialog  
        // Pass null as the parent view because its going in the dialog layout  
        builder.setView(view)  
                // Add action buttons  
                .setPositiveButton("Sign in",  
                        new DialogInterface.OnClickListener()  
                        {  
                            @Override  
                            public void onClick(DialogInterface dialog, int id)  
                            {  
                            	onPositiveListener listener = (onPositiveListener) getActivity();  
                                listener.onPositive();
                            }  
                        }).setNegativeButton("Cancel", null);  
        return builder.create();  
    }  
}
