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
import com.lib.adloader.model.MediaModel;
import com.lib.adloader.utils.MLog;
import com.lib.adloader.utils.MToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class VidAdManagar implements AdEvent.AdEventListener, AdErrorEvent.AdErrorListener {

    private static final String LOGTAG = "VidAdManagar";
    private OrientationChangeListener listener;


    private Context mContext;
    // The container for the ad's UI.
    private ViewGroup mAdUiContainer;
    private PDVideoView mVideoPlayer;

    // Factory class for creating SDK objects.
    private ImaSdkFactory mSdkFactory;

    // The AdsLoader instance exposes the requestAds method.
    private AdsLoader mAdsLoader;

    // AdsManager exposes methods to control ad playback and listen to ad events.
    private AdsManager mAdsManager;

    private Boolean enabled = true;

    // Whether an ad is displayed.
    private boolean mIsAdDisplayed;
    private boolean mIsMidAdLoading,mIsMidAdLoaded,mIsMidAdShown;

    private AdStatus preAdStatus = AdStatus.AD_WAITING;
    private AdStatus midAdStatus = AdStatus.AD_WAITING;

    Long video_id;
    Long video_random;
    private JSONObject adtags;
    private JSONArray currentAdTags;
    private JSONObject currentAdTag;

    private AdType currentAdTagsType;
    private int currentAdIndex;
    private String currentAdTagUrl = "";

//    private String content_url = "";
//    private String ad_tag_pre_url = "";
    private String ad_tag_mid_url = "";



    private static VidAdManagar ourInstance = new VidAdManagar();

    public static VidAdManagar getInstance() {
        return ourInstance;
    }

    private VidAdManagar() {
        resetAdShownInformation();
    }

    public void setup(Context context, ViewGroup mAdUiContainer, PDVideoView mVideoPlayer) {
        this.mAdUiContainer = mAdUiContainer;
        this.mContext = context;
        this.mVideoPlayer = mVideoPlayer;

//        if(mAdsLoader == null) {
//            //startAdLoader();
//        }

        this.video_id = Long.parseLong("0");
        this.video_random = Long.parseLong("0");
    }

    public void setAdTags(JSONObject adTag, Long video_id, Long video_random) {
        this.adtags = adTag;
        this.video_id = video_id;
        this.video_random = video_random;
        startAdLoader();

//        // Set Pre ads on startup
//        setPreMidAdTags(AdType.PRE_AD);
//
//        if(currentAdTagUrl != "") {
//            requestAds(currentAdTagUrl);
//        }
    }

    public void setPreMidAdTags(AdType adType, int index, boolean autoRequestAd) {

        currentAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=\n" +
                "s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=";
        if (adtags == null) {
            return;
        }

        currentAdTagsType = (adType == AdType.PRE_AD) ? AdType.PRE_AD : AdType.MID_AD;
        currentAdIndex = index;

        try {
            currentAdTags = adtags.getJSONArray((adType == AdType.PRE_AD) ? "pre" : "mid");
            if(currentAdTags != null && index < currentAdTags.length()) {
                currentAdTag = currentAdTags.getJSONObject(index);
                currentAdTagUrl = currentAdTag.optString("tag","");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(currentAdTagUrl != "" && autoRequestAd) {
            requestAds(currentAdTagUrl);
        }

    }

    public void requestCurrentAd() {
        if(!currentAdTagUrl.equals("")) {
           // requestAds(currentAdTagUrl);
          // requestAds("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=");
            requestAds(currentAdTagUrl);
        }
    }

    private void startAdLoader() {

        MLog.e(LOGTAG, "AdsLoader Event: " + "createAdsLoader");

        // Create an AdsLoader.
        mSdkFactory = ImaSdkFactory.getInstance();
        mAdsLoader = mSdkFactory.createAdsLoader(mContext);

        // Add listeners for when ads are loaded and for errors.
        mAdsLoader.addAdErrorListener(this);
        mAdsLoader.addAdsLoadedListener(new AdsLoader.AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {

                MLog.e(LOGTAG, "AdsLoader Event: " + "onAdsManagerLoaded");

                // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                // events for ad playback and errors.
                mAdsManager = adsManagerLoadedEvent.getAdsManager();

                // Attach event and error event listeners.
                mAdsManager.addAdErrorListener(VidAdManagar.this);
                mAdsManager.addAdEventListener(VidAdManagar.this);
                mAdsManager.init();
            }
        });
    }

    public void startAdLoader(final AdsLoader.AdsLoadedListener var1) {

        MLog.e(LOGTAG, "AdsLoader Event: " + "createAdsLoader");

        // Create an AdsLoader.
        mSdkFactory = ImaSdkFactory.getInstance();
        mAdsLoader = mSdkFactory.createAdsLoader(mContext);

        // Add listeners for when ads are loaded and for errors.
        mAdsLoader.addAdErrorListener(this);
        mAdsLoader.addAdsLoadedListener(new AdsLoader.AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {

                MLog.e(LOGTAG, "AdsLoader Event: " + "onAdsManagerLoaded");

                // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                // events for ad playback and errors.
                mAdsManager = adsManagerLoadedEvent.getAdsManager();

                // Attach event and error event listeners.
                mAdsManager.addAdErrorListener(VidAdManagar.this);
                mAdsManager.addAdEventListener(VidAdManagar.this);
                mAdsManager.init();
                if (var1 != null) {
                    var1.onAdsManagerLoaded(adsManagerLoadedEvent);
                }
            }
        });
    }


    /**
     * Request video ads from the given VAST ad tag.
     * @param adTagUrl URL of the ad's VAST XML
     */
    private void requestAds(String adTagUrl) {

        if(!enabled) {
            // Video AD LOADER IS NOT ENABLED, please enable it first
            return;
        }

        if(mAdsLoader == null) {
            startAdLoader();
        }

        if(adTagUrl == "") {
            // Can Load next url in array
            return;
        }

        //TRACKER: Ad request
        if(currentAdTag != null)    // && mVideoPlayer != null
        {
            String tag_id = currentAdTag.optString("id","");
            //String tag_random = currentAdTag.optString("random","");// TRACKER

            Long video_id = currentAdTag.optLong("id",0);
            //String video_random = currentAdTag.optString("random","");// TRACKER

            PDVideoTacker.getInstance().onAdRequest(tag_id,
                    ""+video_id,
                    ""+video_random);
        }


        AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(mAdUiContainer);

        // Create the ads request.
        AdsRequest request = mSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(adDisplayContainer);
        request.setVastLoadTimeout(3500);
        request.setContentProgressProvider(new ContentProgressProvider() {
            @Override
            public VideoProgressUpdate getContentProgress() {
                if (mIsAdDisplayed || mVideoPlayer == null || mVideoPlayer.getDuration() <= 0) {
                    ///- MToast.makeText(mContext, "Ad : Video time not ready", Toast.LENGTH_SHORT).show();
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                return new VideoProgressUpdate(mVideoPlayer.getCurrentPosition(),
                        mVideoPlayer.getDuration());
            }
        });

        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader.requestAds(request);
        ///- MToast.makeText(mContext, "Ad Requested", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdEvent(AdEvent adEvent) {
        MLog.e(LOGTAG, "Event: " + adEvent.getType());

        // These are the suggested event types to handle. For full list of all ad event
        // types, see the documentation for AdEvent.AdEventType.
        switch (adEvent.getType()) {
            // Listen for the AD_BREAK_READY event.
            case AD_BREAK_READY:
                // Tell the SDK to play ads when we're ready. To skip this ad break,
                // simply return from this handler without calling mAdsManager.start().
                // mAdsManager.start();
                mIsMidAdLoaded = true;
                break;
            case LOADED:
                // AdEventType.LOADED will be fired when ads are ready to be played.
                // AdsManager.start() begins ad playback. This method is ignored for VMAP or
                // ad rules playlists, as the SDK will automatically start executing the
                // playlist.
                ///- MToast.makeText(mContext, "Ad Loaded & Started", Toast.LENGTH_SHORT).show();
                if (!mIsMidAdLoading) {
                    //playerControlView.hide(); // Hide Media Controls
                    mAdsManager.start();

                    //TRACKER: Ad Impression
                    if(currentAdTag != null)
                    {
                        String tag_id = currentAdTag.optString("id","");
                        PDVideoTacker.getInstance().onAdImpression(tag_id, ""+video_id, ""+video_random);
                    }

                } else {
                    mIsMidAdLoaded = true;
                }
                // mAdsManager.start();
                break;
            case CONTENT_PAUSE_REQUESTED:
                // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before a video
                // ad is played.
                mIsAdDisplayed = true;
                mVideoPlayer.pause();
                break;
            case CONTENT_RESUME_REQUESTED:
                // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is completed
                // and you should start playing your content.
                mIsAdDisplayed = false;

                //mVideoPlayer.seekTo(mVideoPlayer.getDuration()-2000);
                mVideoPlayer.start();
                break;
            case ALL_ADS_COMPLETED:
                if (mAdsManager != null) {
                    mAdsManager.destroy();
                    mAdsManager = null;
                }
                if (mIsMidAdLoaded == false
                        && mIsMidAdLoading == false
                        //&& currentAdTagsType == AdType.PRE_AD
                        && !ad_tag_mid_url.equals(""))
                {
                    mIsMidAdLoading = true;
                    startAdLoader();
                    //requestAds(ad_tag_mid_url);
                    setPreMidAdTags(AdType.MID_AD,0,true);
                }
                ///- MToast.makeText(mContext, "Ad Completed", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        MLog.e(LOGTAG, "Ad Error code: " + adErrorEvent.getError().getErrorCode());
        MLog.e(LOGTAG, "Ad Error: " + adErrorEvent.getError().getMessage());
        mVideoPlayer.start();
        ///- MToast.makeText(mContext, "Ad Error: "+adErrorEvent.getError().getMessage(), Toast.LENGTH_SHORT).show();

        // Loading Next Ad
        setPreMidAdTags(currentAdTagsType, currentAdIndex+1, true);
    }

    /*@Override
    public void onResume() {
        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager.resume();
        } else {
            mVideoPlayer.play();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager.pause();
        } else {
            mVideoPlayer.pause();
        }
        super.onPause();
    }*/

//    public void setOrientationChangeListener(OrientationChangeListener listener) {
//        this.listener = listener;
//    }

    private void resetAdShownInformation() {
        //mIsAdDisplayed = false;
        //mIsMidAdLoading = false;
        //mIsMidAdShown = false;

        preAdStatus = AdStatus.AD_WAITING;
        midAdStatus = AdStatus.AD_WAITING;
    }



    public interface OrientationChangeListener {
        /***
         * @param screenOrientation ActivityInfo.SCREEN_ORIENTATION_PORTRAIT or ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
         *                          or ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE or ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
//         * @param direction         PORTRAIT or REVERSE_PORTRAIT when screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//         *                          LANDSCAPE or REVERSE_LANDSCAPE when screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE.
         */
        void onOrientationChanged(int screenOrientation);
    }


    public enum AdStatus {
        AD_WAITING, AD_LOADING, AD_LOAD_FAILED, AD_LOADED, AD_DISPLAYED, AD_DISPLAY_FAILED
    }

    public enum AdType {
        PRE_AD, MID_AD, POST_AD
    }

}
