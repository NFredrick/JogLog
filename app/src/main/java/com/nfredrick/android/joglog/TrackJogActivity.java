package com.nfredrick.android.joglog;

import android.support.v4.app.Fragment;

public class TrackJogActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return TrackJogFragment.newInstance();
    }
}
