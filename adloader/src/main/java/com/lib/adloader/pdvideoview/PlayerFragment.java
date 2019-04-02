package com.lib.adloader.pdvideoview;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.lib.adloader.ApiInterface;
import com.lib.adloader.R;
import com.lib.adloader.model.MediaModel;
import com.lib.adloader.retrofit.ApiClient;
import com.lib.adloader.utils.MLog;
import com.lib.adloader.utils.ServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The main fragment for displaying video content.
 */
public class PlayerFragment extends Fragment implements
        PDVideoView.VideoViewCallback,
        PDVideoView.VideoTrackerCallback {

    private static final String TAG = "VID_AD_PLAYER";
    private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";

    public PDVideoView mVideoView;
    public PDMediaController mMediaController;
    private String license;

    //View mBottomLayout;
    private ViewGroup mVideoLayout;
    //private ViewGroup mUiContainer;
    TextView mStart;

    private VideoFragmentCallback videoFragmentCallback;

    public int mSeekPosition;
    public int cachedHeight;
    public boolean isFullscreen;

    private List<MediaModel.Medium> videos;//= new JSONArray();
   // private ArrayList<MediaModel.Playlist> videos;//= new JSONArray();
    private String VIDEO_URL = "https://s0.2mdn.net/4253510/google_ddm_animation_480P.mp4";
    private String POSTER_URL = "";
    private int currentVideoIndex = -1;

    private List<MediaModel.Medium> relatedVideos;

    private Boolean autoplay = false;
    private Boolean repeat = false;

    private Boolean isLoaded = false;
    private Boolean isStopped = false;

    private boolean isPaused = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.uvv_container, container, false);

        mVideoLayout = rootView.findViewById(R.id.video_layout);
        //mUiContainer = ) rootView.findViewById(R.id.videoPlayerWithAdPlayback);
        mVideoView = rootView.findViewById(R.id.videoView);
        mMediaController = rootView.findViewById(R.id.media_controller);

        VidAdManagar.getInstance().setup(getContext(),  mVideoLayout, mVideoView);

        mVideoView.setMediaController(mMediaController);
        setVideoAreaSize();
        mVideoView.setVideoViewCallback(this);
        mVideoView.setVideoTrackerCallback(this);

        load(getContext(), "fg09h8fh");


        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                MLog.d(TAG, "Error: " + what + "," + extra);

                // TRACKER
                // PDVideoTacker.getInstance().onMediaError(""+mVideoView.getVideoRandom(),  ""+mVideoView.getVideoRandom(), 0, "Error: " + what + "," + extra);
                return false;
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MLog.d(TAG, "onCompletion ");
                onCompletionListener(mp);
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (autoplay) {
                    mp.start();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        MLog.d(TAG, "onPause ");
        if (mVideoView != null && mVideoView.isPlaying()) {
            mSeekPosition = mVideoView.getCurrentPosition();
            MLog.d(TAG, "onPause mSeekPosition=" + mSeekPosition);
            isPaused = true;
            mVideoView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (isPaused) {
                mVideoView.seekTo(mSeekPosition);
                mVideoView.start();
                isPaused = false;
            } else
                mVideoView.start();
        } catch (Exception e) {
            MLog.d(TAG, "onResume mVideoView = " + e.getMessage());
        }

    }

    /**
     * Set the size of the video area
     */
    private void setVideoAreaSize() {
        mVideoLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = mVideoLayout.getWidth();
                cachedHeight = (int) (width * 405f / 720f);
                ViewGroup.LayoutParams videoLayoutParams = mVideoLayout.getLayoutParams();
                videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                videoLayoutParams.height = cachedHeight;
                mVideoLayout.setLayoutParams(videoLayoutParams);
                //mVideoView.setVideoPath(VIDEO_URL,POSTER_URL);
                updateSkipButtons();
                mVideoView.requestFocus();
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MLog.d(TAG, "onSaveInstanceState Position=" + mVideoView.getCurrentPosition());
        outState.putInt(SEEK_POSITION_KEY, mSeekPosition);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mSeekPosition = savedInstanceState.getInt(SEEK_POSITION_KEY, 0);
            MLog.d(TAG, "onRestoreInstanceState Position=" + mSeekPosition);
        }
    }


    //@Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoLayout.setLayoutParams(layoutParams);
            //mBottomLayout.setVisibility(View.GONE);

        } else {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = this.cachedHeight;
            mVideoLayout.setLayoutParams(layoutParams);
            //mBottomLayout.setVisibility(View.VISIBLE);
        }

        mMediaController.resizeDrawerMenu();
        if (videoFragmentCallback != null) {
            videoFragmentCallback.onFullScreenModeChanged(isFullscreen);
        }

    }



    @Override
    public void onPause(MediaPlayer mediaPlayer) {
        MLog.d(TAG, "onPause callback");
    }

    @Override
    public void onStart(MediaPlayer mediaPlayer) {
        MLog.d(TAG, "onStart callback");
        VidAdManagar.getInstance().setPreMidAdTags(VidAdManagar.AdType.PRE_AD, 0, true);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // things to do on the main thread
                VidAdManagar.getInstance().requestCurrentAd();
            }
        });

    }

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {
        MLog.d(TAG, "onBufferingStart callback");
    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {
        MLog.d(TAG, "onBufferingEnd callback");
    }

    @Override
    public void onNextVideo(MediaPlayer mediaPlayer) {
        MLog.d(TAG, "onNextVideo callback");

        if (videos != null && currentVideoIndex >= 0) {
            if (videos.size() > currentVideoIndex + 1) {
                currentVideoIndex++;
                loadVideo(videos.get(currentVideoIndex), mediaPlayer.isPlaying());   //autoplay
            } else if (repeat) {
                currentVideoIndex = 0;
                loadVideo(videos.get(currentVideoIndex), mediaPlayer.isPlaying());   //autoplay
            } else {
                //mPlayButton.setVisibility(View.INVISIBLE);
                //mVideoPlayer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onPreviousVideo(MediaPlayer mediaPlayer) {
        MLog.d(TAG, "onPreviousVideo callback");

        if (videos != null && currentVideoIndex >= 0) {
            currentVideoIndex--;
            loadVideo(videos.get(currentVideoIndex), mediaPlayer.isPlaying());   //autoplaytrue);
        }
    }

    @Override
    public void onPreAdLoading(MediaPlayer mediaPlayer) {
        ///- MToast.makeText(getContext(), "Can Load Pre ad now", Toast.LENGTH_SHORT).show();
        VidAdManagar.getInstance().requestCurrentAd();
    }

    @Override
    public void onMidAdLoading(MediaPlayer mediaPlayer) {
        ///- MToast.makeText(getContext(), "Can Load Mid ad now", Toast.LENGTH_SHORT).show();
        VidAdManagar.getInstance().startAdLoader(new AdsLoader.AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {

            }
        });

        // Set Pre ads on startup
        VidAdManagar.getInstance().setPreMidAdTags(VidAdManagar.AdType.MID_AD, 0, true);

    }



    // TRACKER
    @Override
    public void onVideoLoad(long random) {
        //TRACKER:
        //PDVideoTacker.getInstance().onPlaylistItem("",""+random);
    }

    public void load(Context context, String license) {

        if (this.license != null
                && this.license.equals(license)
                && isLoaded == true) {
            // Video Plaer is already loaded
            return;
        }

        this.license = license;

        VidAdManagar.getInstance().setPreMidAdTags(VidAdManagar.AdType.PRE_AD, 0, true);

        if (autoplay) mVideoView.start();

        VidAdManagar.getInstance().requestCurrentAd();


        ApiClient apiClient = new ApiClient();

        ApiInterface mInterface = apiClient.createService(ApiInterface.class);

        mInterface.configuration("mhtevqg9qgs").enqueue(new Callback<MediaModel>() {
            @Override
            public void onResponse(Call<MediaModel> call, Response<MediaModel> response) {


                if (response.body() != null) {
                    if (response.isSuccessful()) {

                        // TRACKER
                        PDVideoTacker.getInstance().setup(getActivity());



                        try {
                            autoplay =   response.body().getAutoplay();
                          //  repeat = result.getBoolean("repeat");
                           repeat = response.body().getPlaylist().getRepeat();
                           // videos = result.getJSONArray("playlist");
                            videos = response.body().getMedia();


                            //String playlistTitle = result.optString("playlist_name", "");
                            String playlistTitle = response.body().getPlaylist().getName();

                            //String logoUrl = result.optString("logo", "");
                            String logoUrl = response.body().getLogo().getUrl();

                            repeat = false;
                            //autoplay = false;
                            if (videos.size() > 0) {
                                isLoaded = true;
                                currentVideoIndex = 0;
                                mMediaController.isPlayingRelatedVideo = false;

                                mMediaController.loadPlayerLogoImage(logoUrl);

                                if (videos.size() > 1) {

                                    //TODO for show the playlist
                                  //  mMediaController.setplayListVideos(videos, playlistTitle);
                                    mMediaController.setRelatedVideos(videos);
                                    relatedVideos = videos;
                                    mMediaController.relatedVideos = videos;
                                }

                                loadVideo(videos.get(0), autoplay);
                                //playerControlView.show();
                                VidAdManagar.getInstance().setPreMidAdTags(VidAdManagar.AdType.PRE_AD,0,true);

                                if (autoplay) mVideoView.start();
                                VidAdManagar.getInstance().requestCurrentAd();

                                updateSkipButtons();

                                //Start Loading Related Videos
                               /* new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        loadRelatedToVideo(videos.get(0));
                                    }
                                }, 1000);*/
                            }

                            //TRACKER:
                           // PDVideoTacker.getInstance().onReady(true,true, Utility.getScreenWidth(), Utility.getScreenHeight(),Utility.getScreenWidth());

                        } catch (Exception e) {
                            e.printStackTrace();
                            MLog.i("LOGTAG", "Event: " + e.getLocalizedMessage() + "\n\n" + e.getStackTrace());

                            //TRACKER:
                            //PDVideoTacker.getInstance().onPlayerError(license,  e.getLocalizedMessage());
                        }

                        Log.d("Response Success", response.message());


                    } else {
                        Log.d("Response False", response.message());
                        Toast.makeText(getContext(), "Response False", Toast.LENGTH_LONG).show();

                    }
                } else {
                    Log.d("Null body", response.message());
                    Toast.makeText(getContext(), "Null body", Toast.LENGTH_LONG).show();

                }

            }

            @Override
            public void onFailure(Call<MediaModel> call, Throwable t) {
                Log.d("onFailure", t.toString());
                Toast.makeText(getContext(), t.toString(), Toast.LENGTH_LONG).show();


            }
        });


    }

/*
    public void loadRelatedToVideo(JSONArray video) {

        List<JSONArray> result = new ServiceHandler().makeHttpRequestForArray("http://" + ServiceHandler.host + "/related-videos.php");
        if (result == null) {
            relatedVideos = result;
            ///- MToast.makeText(context, "Failed to start", Toast.LENGTH_LONG);
            return;
        }
        MLog.e("test", "Related Video Aresult: " + result.toString());

        try {
            relatedVideos = result;
//            relatedVideos = result.getJSONArray("playlist");
            //String playlistTitle = result.optString("playlist_name","");

            if (relatedVideos.size() > 0) {
                mMediaController.setRelatedVideos(relatedVideos);
            }

            //TRACKER: related video loaded
            //PDVideoTacker.getInstance().onReady(true,true, Utility.getScreenWidth(), Utility.getScreenHeight(),Utility.getScreenWidth());

        } catch (Exception e) {
            e.printStackTrace();
            MLog.i("LOGTAG", "Event: " + e.getLocalizedMessage() + "\n\n" + e.getStackTrace());

            //TRACKER:
            //PDVideoTacker.getInstance().onPlayerError(license,  e.getLocalizedMessage());
        }
    }
*/

    public void loadVideo(MediaModel.Medium video, Boolean autoPlay) {

        // String content_url = video.optString("file","");
        String content_url = video.getFile();
        String poster_url = video.getThumbnail();// TRACKER
        Long video_id = video.getId();
       // Long video_random = video.get("random", 0);
        Long video_random = 1L;
        String title = video.getTitle();
        title = (title.isEmpty() || title.equals("")) ? "No title" : title;

        //http://techslides.com/demos/sample-videos/small.mp4
        //"http://35.154.179.171/1.mp4"
        //Uri videoUri = Uri.parse(content_url);
        try {
            VIDEO_URL = content_url;
            POSTER_URL = poster_url;
            if (mVideoView != null) {
                //VideoView.setVideoPath(VIDEO_URL, poster_url, video_id, video_random);
                mVideoView.setVideoPath(autoPlay, VIDEO_URL, poster_url, video_id, video_random);
                //mMediaController.loadPosterimage(poster_url);
            }

            mMediaController.setTitle(title);

            // Set current Video in Vid Manager
          //  VidAdManagar.getInstance().setAdTags(video.getAds(), video_id, video_random);

            // Set Pre ads on startup
            VidAdManagar.getInstance().setPreMidAdTags(VidAdManagar.AdType.PRE_AD, 0, false);

            updateSkipButtons();
            mMediaController.hideSimilarVideos();

            //this.setVideoURI(videoUri);
        } catch (Exception e) {
            e.printStackTrace();
            MLog.d(TAG, " printStackTrace() " + e.getLocalizedMessage());
        }
    }

    /**
     * VideoView method (setVideoURI)
     */
//    public void setVideoURI(Uri uri) throws RuntimeException  {
//        MLog.d(TAG, "setVideoURI");
//
//        if (mVideoView != null) {
//            mVideoView.setVideoPath(VIDEO_URL);
////            if (mSeekPosition > 0) {
////                mVideoView.seekTo(mSeekPosition);
////            }
////            mVideoView.start();
//        } else throw new RuntimeException("Media Player is not initialized");
//    }
    public void onCompletionListener(MediaPlayer mp) {

        // If Playing Related Video
        if (mMediaController.isPlayingRelatedVideo) {
            //mPlayButton.setVisibility(View.INVISIBLE);
            //mVideoPlayer.setVisibility(View.INVISIBLE);
            //mMediaController.setRelatedVideos(relatedVideos);
            mMediaController.showRelatedVideos();
            return;
        }

        // If Playing Playlist or normal video
        if (videos != null && currentVideoIndex >= 0) {
            if (autoplay == false) {
                //mMediaController.relatedVideos = relatedVideos;
                //mMediaController.setRelatedVideos(relatedVideos);
                mMediaController.showRelatedVideos();
                return;
            }

            //If autoplay == true
            if (videos.size() > currentVideoIndex + 1) {
                currentVideoIndex++;
                loadVideo(videos.get(currentVideoIndex), autoplay);
            } else if (repeat) {
                currentVideoIndex = 0;
                loadVideo(videos.get(currentVideoIndex), autoplay);
            } else if (relatedVideos != null) {
                //mPlayButton.setVisibility(View.INVISIBLE);
                //mVideoPlayer.setVisibility(View.INVISIBLE);
                mMediaController.relatedVideos = relatedVideos;
                mMediaController.showRelatedVideos();
                return;
            }
        }
    }

    public void updateSkipButtons() {
        if (mMediaController == null
                || videos == null)
            return;

        if (mMediaController.isPlayingRelatedVideo) {
            mMediaController.mCenterPreviousButton.setClickable(false);
            mMediaController.mCenterPreviousButton.setColorFilter((false) ? Color.WHITE : Color.DKGRAY);

            mMediaController.mCenterNextButton.setClickable(false);
            mMediaController.mCenterNextButton.setColorFilter((false) ? Color.WHITE : Color.DKGRAY);
            return;
        }

        Boolean allowPrevious = (currentVideoIndex != 0);
        Boolean allowNext = (videos.size() > currentVideoIndex + 1); //|| (repeat);

        mMediaController.mCenterPreviousButton.setClickable(allowPrevious);
        mMediaController.mCenterPreviousButton.setColorFilter((allowPrevious) ? Color.WHITE : Color.DKGRAY);

        mMediaController.mCenterNextButton.setClickable(allowNext);
        mMediaController.mCenterNextButton.setColorFilter((allowNext) ? Color.WHITE : Color.DKGRAY);

    }

    public interface VideoFragmentCallback {
        void onFullScreenModeChanged(boolean isFullscreen);

    }

    public void setVideoFragmentCallback(VideoFragmentCallback callback) {
        this.videoFragmentCallback = callback;
    }

    @Override
    public void onPlaylistVideoSelected(int videoIndex) {
        if (videos != null) {
            if (mMediaController.isPlayingRelatedVideo
                    || currentVideoIndex != videoIndex) {
                mMediaController.isPlayingRelatedVideo = false;
                currentVideoIndex = videoIndex;
                loadVideo(videos.get(currentVideoIndex), true);
            } else {
                mMediaController.doPauseResume();
            }
        }
    }

    @Override
    public void onRelatedVideoSelected(JSONObject video, int videoIndex) {
        if (relatedVideos != null) {
            mMediaController.isPlayingRelatedVideo = true;
            currentVideoIndex = videoIndex;
            loadVideo(relatedVideos.get(currentVideoIndex), true);
          //  loadRelatedToVideo(video);
        }
    }



    public boolean isPlayerLoded() {
        return isLoaded;
    }
}
