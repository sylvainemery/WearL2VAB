
package com.hackncheese.wearl2vab.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.ActionPage;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hackncheese.wearl2vab.MainActivity;
import com.hackncheese.wearl2vab.R;


public class ActionFragment extends Fragment {

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ActionPage navActionPage = (ActionPage) inflater.inflate(
                R.layout.gridpager_action, container, false);

        navActionPage.setColor(getResources().getColor(R.color.l2v_pink));

        mContext = getActivity();

        navActionPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mContext).startOpenFridge();
            }
        });

        return navActionPage;
    }
}