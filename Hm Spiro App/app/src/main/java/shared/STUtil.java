package shared;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class STUtil {

	public static void showAlert(String message, String title, Context context) {
		
		new AlertDialog.Builder(context)
	    .setTitle(title)
	    .setMessage(message)
	    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        }
	     })
	     .show();		
	}
	
	public static void showErrorAlert(String message, Context context) {
		STUtil.showAlert(message, "Error", context);
	}
	
	private STUtil() {}
}
