package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.apkupdater.R;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class SnackBarUtil {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public void make(
		Activity activity,
		String text
	) {
		try {
			if (text == null || text.isEmpty() || activity == null) {
				return;
			}

			final Snackbar bar = Snackbar.make(activity.findViewById(R.id.container), text, Snackbar.LENGTH_LONG);
			bar.setAction(activity.getString(R.string.snackbar_close), view -> bar.dismiss()).show();
			View view = bar.getView();
			view.setBackgroundColor(ColorUtil.getColorFromTheme(activity.getTheme(), R.attr.colorPrimary));
			TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
			tv.setTextColor(ColorUtil.getColorFromContext(activity, android.R.attr.textColorPrimary));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
