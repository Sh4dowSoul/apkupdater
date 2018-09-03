package com.apkupdater.adapter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.AnimationUtil;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.PixelConversion;

import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class InstalledAppAdapter extends RecyclerView.Adapter<InstalledAppAdapter.InstalledAppViewHolder>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private List<InstalledApp> mApps;
	private Context mContext;
	private RecyclerView mView;
    private InstalledAppAdapter mAdapter;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public class InstalledAppViewHolder
		extends RecyclerView.ViewHolder
	{
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		private View mView;
        private TextView mName;
        private TextView mVersion;
        private ImageView mIcon;
        private ImageView mActionOneButton;
        private TextView source;

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		InstalledAppViewHolder(
			View view
		) {
			super(view);
			mView = view;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		public void bind(final InstalledApp app, int position) {
		    // Get views
            mName = mView.findViewById(R.id.installed_app_name);
            mVersion = mView.findViewById(R.id.installed_app_version);
            mIcon = mView.findViewById(R.id.installed_app_icon);
            mActionOneButton = mView.findViewById(R.id.actionButton);
            source = mView.findViewById(R.id.newVersion);

            mName.setText(app.getName());
            mVersion.setText(app.getVersion());
            source.setVisibility(View.GONE);
			mActionOneButton.setImageResource(R.drawable.ic_eye_black_24dp);
			mActionOneButton.setVisibility(View.VISIBLE);
			mView.setOnClickListener(v -> {
				// Get the ignore list from the options
				UpdaterOptions options = new UpdaterOptions(mContext);
				List<String> ignore_list = options.getIgnoreList();

				// If it's on the ignore remove, otherwise add it
				if (ignore_list.contains(app.getPname())) {
					ignore_list.remove(app.getPname());
				} else {
					ignore_list.add(app.getPname());
				}

				// Update the list
				options.setIgnoreList(ignore_list);
				notifyItemChanged(position);
			});

            // Make the ignore overlay visible if this app is on the ignore list
            UpdaterOptions options = new UpdaterOptions(mContext);
            if (options.getIgnoreList().contains(app.getPname())) {
                mView.setAlpha(0.5f);
            } else {
                mView.setAlpha(1);
            }

            try {
                Drawable icon = mContext.getPackageManager().getApplicationIcon(app.getPname());
                mIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
            	mIcon.setImageResource(R.drawable.ic_android);
            }
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private void setTopMargin(
            int margin
        ) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            params.topMargin = (int) PixelConversion.convertDpToPixel(margin, mContext);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public InstalledAppAdapter(
		Context context,
		RecyclerView view,
		List<InstalledApp> apps
	) {
		mContext = context;
		mAdapter = this;
		mView = view;
		AnimationUtil.startSlideAnimation(mContext, mView);
		mApps = InstalledAppUtil.sort(mContext, apps);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public InstalledAppViewHolder onCreateViewHolder(
		ViewGroup parent,
		int viewType
	) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.updater_item_new, parent, false);
		return new InstalledAppViewHolder(v);
	}

	@Override
	public void onBindViewHolder(
		InstalledAppViewHolder holder,
		int position
	) {
		holder.bind(mApps.get(position), position);

        if (position == 0) {
            holder.setTopMargin(8);
        }
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getItemCount(
	) {
		return mApps.size();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////