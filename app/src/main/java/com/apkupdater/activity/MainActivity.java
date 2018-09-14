package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.apkupdater.R;
import com.apkupdater.event.InstallAppEvent;
import com.apkupdater.event.PackageInstallerEvent;
import com.apkupdater.event.SnackBarEvent;
import com.apkupdater.fragment.AboutFragment;
import com.apkupdater.fragment.AboutFragment_;
import com.apkupdater.fragment.FilterFragment_;
import com.apkupdater.fragment.SettingsFragment_;
import com.apkupdater.fragment.UpdaterFragment_;
import com.apkupdater.model.AppState;
import com.apkupdater.model.DownloadInfo;
import com.apkupdater.model.LogMessage;
import com.apkupdater.receiver.BootReceiver_;
import com.apkupdater.service.UpdaterService_;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.InjektUtil;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.LogUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.ServiceUtil;
import com.apkupdater.util.SnackBarUtil;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_AUTO;
import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_NO;
import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_YES;

@EActivity
public class MainActivity extends AppCompatActivity {

	@ViewById(R.id.toolbar)
	Toolbar mToolbar;

	@ViewById(R.id.appBarLayout)
	AppBarLayout appBar;

	@Bean
	MyBus mBus;

	@Bean
	AppState mAppState;

	@Bean
    LogUtil mLog;

	UpdaterFragment_ updaterFragment = new UpdaterFragment_();
	final FragmentManager fm = getSupportFragmentManager();

	private int mRequestCode = 1000;

    //App Theme Constants
    public static final String APP_THEME = "app_theme";
    public static final String DEFAULT_THEME = "Light";
    public static final String AUTO_THEME = "Auto";
    public static final String DARK_THEME = "Dark";
    private String CURRENT_THEME;

	BottomSheetBehavior bottomSheetBehavior;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setThemeFromOptions();
		super.onCreate(savedInstanceState);

		// Inject Singletons
		InjektUtil.Companion.init();
		InjektUtil.Companion.addAppStateSingleton(mAppState);
		InjektUtil.Companion.addMyBusSingleton(mBus);
		InjektUtil.Companion.addLogUtilSingleton(mLog);
		InjektUtil.Companion.addActivitySingleton(this);

		setContentView(R.layout.activity_main);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

		mBus.register(this);

		// Clear updates unless we are coming from a notification
		boolean isFromNotification = false;
		try {
			isFromNotification = getIntent().getExtras().getBoolean("isFromNotification");
		} catch (Exception ignored) {}

		if (!isFromNotification) {
			//mAppState.clearUpdates();
		}

		// Simulate a boot com.apkupdater.receiver to set alarm
		new BootReceiver_().onReceive(getBaseContext(), null);

		if (new UpdaterOptions(this).updateOnStartup()){
			searchForUpdates();
		}
		fm.beginTransaction().add(R.id.main_container, updaterFragment, "1").commit();

		appBar = findViewById(R.id.appBarLayout);
		bottomSheetBehavior = BottomSheetBehavior.from(appBar);

		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setCheckedItem(R.id.navigation_updates);
		navigationView.setNavigationItemSelectedListener(menuItem -> {
			menuItem.setChecked(true);
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

			switch (menuItem.getItemId()){
				case R.id.navigation_updates:
					changeFragment(new UpdaterFragment_());
					break;
				case R.id.navigation_filter:
					changeFragment(new FilterFragment_());
					break;
				case R.id.navigation_settings:
					changeFragment(new SettingsFragment_());
					break;
				case R.id.navigation_about:
					changeFragment(new AboutFragment_());
					break;
			}
			return true;
		});
	}

	public void searchForUpdates(){
		if (!ServiceUtil.isServiceRunning(getBaseContext(), UpdaterService_.class)) {
			UpdaterService_.intent(getApplication()).start();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBus.unregister(this);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setThemeFromOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Load App Theme
        CURRENT_THEME = prefs.getString(APP_THEME, DEFAULT_THEME);
        switch (CURRENT_THEME) {
            case AUTO_THEME:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO);
                break;
            case DARK_THEME:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                break;
            case DEFAULT_THEME:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                break;
        }
	}

	@Subscribe
	public void onSnackBarEvent(SnackBarEvent ev) {
		SnackBarUtil.make(this, ev.getMessage());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onPackageInstallerEvent(
		PackageInstallerEvent ev
	) {
	    try {
            mAppState.getDownloadIds().put(mRequestCode, ev.getId());
            startActivityForResult(ev.getIntent(), mRequestCode);
            mRequestCode++;
        } catch (Exception e) {
            SnackBarUtil.make(this, String.valueOf(e));
            mLog.log("Error launching Package Installer", String.valueOf(e), LogMessage.SEVERITY_ERROR);
        }
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onActivityResult(
		int requestCode,
		int resultCode,
		Intent data
	) {
		super.onActivityResult(requestCode, resultCode, data);

		if (mAppState.getDownloadIds().containsKey(requestCode)) {
		    long id = mAppState.getDownloadIds().get(requestCode);
            if (mAppState.getDownloadInfo().containsKey(id)) {

                DownloadInfo i = mAppState.getDownloadInfo().get(mAppState.getDownloadIds().get(requestCode));

                if (i.getPackageName().equals(getPackageName())) {
                    // When self updating
                    String version = InstalledAppUtil.getAppVersionName(this, i.getPackageName());
                    mBus.post(new SnackBarEvent(version.equals(
                        i.getVersionName()) ? getString(R.string.install_success) : getString(R.string.install_failure)
                    ));
                } else {
                    // Normal installs coming with versionCode
                    int code = InstalledAppUtil.getAppVersionCode(this, i.getPackageName());
                    mBus.post(new InstallAppEvent(i.getPackageName(), id, code == i.getVersionCode()));
                }

                // Clean data
                mAppState.getDownloadInfo().remove(i);
                mAppState.getDownloadIds().remove(requestCode);
            }
		}
	}

	private void changeFragment(Fragment newFragment){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.main_container, newFragment);
		transaction.addToBackStack(null);
		//transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.commit();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
					bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
				} else {
					bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
				}

				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}