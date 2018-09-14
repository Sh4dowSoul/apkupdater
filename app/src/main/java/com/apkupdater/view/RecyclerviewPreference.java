package com.apkupdater.view;


import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.apkupdater.R;
import com.apkupdater.adapter.InstalledAppAdapter;
import com.apkupdater.model.InstalledApp;

import java.util.List;

public class RecyclerviewPreference extends Preference {
    private RecyclerView recyclerView;

    public RecyclerviewPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWidgetLayoutResource(R.layout.fragment_installed_apps);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        recyclerView = (RecyclerView) holder.findViewById(R.id.list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //recyclerView.setAdapter(new InstalledAppAdapter());
    }

    public RecyclerView getRecyclerview() {
        return this.recyclerView;
    }

   public void setItems(List<InstalledApp> items){
       recyclerView.setAdapter(new InstalledAppAdapter(getContext(), recyclerView, items));
    }
}
