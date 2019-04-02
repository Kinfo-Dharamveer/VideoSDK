/*
 * Copyright (C) 2015 Andy Ke <dictfb@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.demo.videoadsloader;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.lib.adloader.pdvideoview.PlayerFragment;


public class MainActivity extends AppCompatActivity  implements PlayerFragment.VideoFragmentCallback {

    private static final String TAG = "MainActivity";

    PlayerFragment mPlayerFragment;
    RelativeLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frame =  findViewById(R.id.frame_container);

        final PlayerFragment mFragment = new PlayerFragment();

        if (savedInstanceState == null) {

            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.frame_container, mFragment).commit();
            manager.executePendingTransactions();
        }


    }



    @Override
    protected void onResume() {
        super.onResume();

    }


    private void switchTitleBar(boolean show) {
        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            if (show) {
                supportActionBar.show();
                // uncomment this for second video
                //frame2.setVisibility(View.VISIBLE);
            } else {
                supportActionBar.hide();
                // uncomment this for second video
                //frame2.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mPlayerFragment.isFullscreen) {
            mPlayerFragment.mVideoView.setFullscreen(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFullScreenModeChanged(boolean isFullscreen) {
        switchTitleBar(!isFullscreen);
    }
}
