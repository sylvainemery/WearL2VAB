package com.hackncheese.wearl2vab.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hackncheese.wearl2vab.R;


public class HomeFragment extends Fragment {

    private TextView mBalanceLabel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        mBalanceLabel = (TextView) view.findViewById(R.id.balance_label);
        return view;
    }

    public void setBalanceText(String balance) {
        mBalanceLabel.setText(getString(R.string.balance, balance));
    }
}