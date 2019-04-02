/*
* Copyright (C) 2015 Author <dictfb#gmail.com>
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

package com.lib.adloader.pdvideoview;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.lib.adloader.utils.MLog;
import com.lib.adloader.utils.ServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PDVideoTacker {

    private static final String LOGTAG = "PDVideoTacker";

    // http://35.154.179.171/index.php
    // http://13.126.141.166/index.php
    // http://13.126.141.166/tracking.php
    private static final String Tracker_URL = "http://"+ServiceHandler.host+"/tracking.php";

    private Context mContext;

    private Boolean enabled = false;

    private static PDVideoTacker ourInstance = new PDVideoTacker();

    public static PDVideoTacker getInstance() {
        return ourInstance;
    }

    private PDVideoTacker() {
        enabled = false;
    }

    public void setup(Context context) {
        this.mContext = context;

    }

    public void setAdTags(JSONObject adTag) {
        //this.adtags = adTag;
    }

    /*public void onReady(boolean is_player_ready, boolean viewable, int width, int height, int total_width) {

        if (!enabled) return;

        JSONObject param=new JSONObject();
        try {
            param.put("type", "ready");
            param.put("is_player_ready", is_player_ready);
            param.put("viewable", viewable);
            param.put("width", width);
            param.put("height",  height);
            param.put("total_width",  total_width);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject result = ServiceHandler.makeRequest(Tracker_URL, param.toString());

        MLog.e(LOGTAG, "onReady result: " + result.toString());
    }*/

    /*public void onPlayerError(String id, String error_desc) {

        if (!enabled) return;

        JSONObject param=new JSONObject();
        try {
            param.put("type", "error");
            param.put("id", id);                    // player_tracker_id
            param.put("error_desc",  error_desc);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject result = ServiceHandler.makeRequest(Tracker_URL, param.toString());

        MLog.e(LOGTAG, "onPlayerError result: " + result.toString());
    }*/

    /*public void onMediaError(String video_id, String video_random, int id, String error_desc) {

        if (!enabled) return;

        JSONObject param=new JSONObject();
        try {
            param.put("type", "error");
            param.put("video_id", video_id);        // random
            param.put("video_random", video_random);
            param.put("id", id);                    // player_tracker_id
            param.put("error_desc",  error_desc);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject result = ServiceHandler.makeRequest(Tracker_URL, param.toString());

        MLog.e(LOGTAG, "onMediaError result: " + result.toString());
    }*/

    /*public void onPlaylistItem(String video_id, String video_random) {

        if (!enabled) return;

        JSONObject param = new JSONObject();
        try {
            param.put("type", "playlistItem");
            param.put("video_id", video_id);        // random
            param.put("video_random", video_random);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject result = ServiceHandler.makeRequest(Tracker_URL, param.toString());

        MLog.e(LOGTAG, "onPlaylistItem result: " + result.toString());
    }*/

    public void onPlayVideo(String video_id, String video_random) {

        if (!enabled) return;

        JSONObject param = new JSONObject();
        try {
            param.put("type", "play");
            param.put("video_id", video_id);        // random
            param.put("video_random", video_random);
            param.put("started", 1);
            param.put("app", "android");

            JSONObject result = ServiceHandler.makeRequest(Tracker_URL, param.toString());

            MLog.e(LOGTAG, "onPlayVideo result: ");// + ((result != null) ? result.toString() : ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onVideoBuffer(String video_id, String video_random, float percent) {

        if (!enabled) return;

        JSONObject param = new JSONObject();
        try {
            param.put("type", "buffer");
            param.put("video_id", video_id);        // random
            param.put("video_random", video_random);
            param.put("started", 1);
            param.put("percentage", ""+percent);       // 0 to 1 (0.11 for 11%)
            param.put("app", "android");

            JSONObject result = ServiceHandler.makeRequest(Tracker_URL, param.toString());

            MLog.e(LOGTAG, "onPlayVideo result: ");// + result.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /*public void onViewDuration(String video_id, String video_random, String actual_view, String skipped_time) {

        if (!enabled) return;

        JSONObject param = new JSONObject();
        try {
            param.put("type", "viewDuration");
            param.put("video_id", video_id);        // random
            param.put("video_random", video_random);
            param.put("actual_view", actual_view);
            param.put("skipped_time", skipped_time);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject result = ServiceHandler.makeRequest(Tracker_URL, param.toString());

        MLog.e(LOGTAG, "onPlayVideo result: " + result.toString());
    }*/

    // --------------------------------------------------------
    // VIDEO AD TRACKER
    // --------------------------------------------------------
    // - adRequest
    // - adMeta
    // - adloaded
    // - adPlay
    // - adTime ( This will count actual played duration of ad )
    // - adClick
    // - adPause
    // - adSkipped
    // - adError
    // - adComplete
    // - adImpression

    public void onAdRequest(String tag_id, String video_id, String video_random) {

        if (!enabled) return;

        JSONObject param = new JSONObject();
        try {
            param.put("type", "adRequest");       // viewDuration
            param.put("tag_id", tag_id);                // tag_id
            param.put("video_id", video_id);            // video_id
            param.put("video_random", video_random);
            param.put("impression", 1);
            param.put("app", "android");

            JSONObject result = ServiceHandler.makeRequest(Tracker_URL, param.toString());

            MLog.e(LOGTAG, "onAdRequest result: ");// + result.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void onAdImpression(String tag_id, String video_id, String video_random) {

        if (!enabled) return;

        JSONObject param = new JSONObject();
        try {
            param.put("type", "adImpression");       // viewDuration
            param.put("tag_id", tag_id);                // tag_id
            param.put("video_id", video_id);            // video_id
            param.put("video_random", video_random);
            param.put("impression", 1);
            param.put("app", "android");

            JSONObject result = ServiceHandler.makeRequest(Tracker_URL, param.toString());

            MLog.e(LOGTAG, "onAdRequest result: ");// + result.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
