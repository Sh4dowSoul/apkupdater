package com.apkupdater.fragment;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Layout;
import android.util.Log;

import com.apkupdater.R;
import com.apkupdater.adapter.InstalledAppAdapter;
import com.apkupdater.event.InstalledAppTitleChange;
import com.apkupdater.event.UpdateInstalledAppsEvent;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.GenericCallback;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.view.RecyclerviewPreference;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EFragment(R.layout.fragment_installed_apps)
public class FilterFragment extends PreferenceFragmentCompat {

    @Bean
    InstalledAppUtil mInstalledAppUtil;

    RecyclerviewPreference recyclerviewPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.filter_preferences, rootKey);

        recyclerviewPreference = (RecyclerviewPreference) findPreference("customList");

        SwitchPreference switchPreference = (SwitchPreference) findPreference("customFilter");
        switchPreference.setOnPreferenceChangeListener((preference1, newValue) -> {
            boolean visible = (boolean) newValue;

            recyclerviewPreference.setVisible((Boolean) newValue);
            if (visible){
                mInstalledAppUtil.getInstalledAppsAsync(getContext(), items -> {
                    //((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                   Log.d("SIZE", String.valueOf(items.size()));
                    setListAdapter(items);
                    //preference.setItems(items);
                });
            }
           // updateInstalledApps(new UpdateInstalledAppsEvent());
            return true;
        });
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    protected void setListAdapter(List<InstalledApp> items) {
        recyclerviewPreference.setItems(items);
        Log.d("SIZE", String.valueOf(items.size()));
    }
}
