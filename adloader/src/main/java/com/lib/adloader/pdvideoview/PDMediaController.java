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
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.lib.adloader.Adapter.PlaylistAdapter;
import com.lib.adloader.Adapter.PosterAdapater;
import com.lib.adloader.Adapter.onRecyclerViewItemClickListener;
import com.lib.adloader.R;
import com.lib.adloader.model.MediaModel;
import com.lib.adloader.utils.DownloadImageTask;
import com.lib.adloader.utils.MLog;
import com.lib.adloader.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class PDMediaController extends FrameLayout {

    private String TAG = "VID_PLAYER";

    private MediaPlayerControl mPlayer;

    private Context mContext;

    private ProgressBar mProgress;

    private TextView mEndTime, mCurrentTime;

    private TextView mTitle;

    private boolean mShowing = true;

    private boolean mDragging;

    private boolean mScalable = false;
    private boolean mIsFullScreen = false;
//    private boolean mFullscreenEnabled = false;


    private static final int sDefaultTimeout = 3000;

    private static final int STATE_PLAYING = 1;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_LOADING = 3;
    private static final int STATE_ERROR = 4;
    private static final int STATE_COMPLETE = 5;

    private int mState = STATE_LOADING;


    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int SHOW_LOADING = 3;
    private static final int HIDE_LOADING = 4;
    private static final int SHOW_ERROR = 5;
    private static final int HIDE_ERROR = 6;
    private static final int SHOW_COMPLETE = 7;
    private static final int HIDE_COMPLETE = 8;
    private static final int SHOW_POSTER = 9;
    private static final int HIDE_POSTER = 10;

    StringBuilder mFormatBuilder;

    Formatter mFormatter;

    private ImageButton mTurnButton;// 开启暂停按钮

    private ImageButton mScaleButton;

    private View mBackButton;// 返回按钮

    private ViewGroup posterLayout;
    private ImageView posterImageview;

    private ViewGroup loadingLayout;

    private ViewGroup errorLayout;

    private View mTitleLayout;
    private View mControlLayout;

    private View mCenterPlayButton;
    public ImageButton mCenterNextButton;
    public ImageButton mCenterPreviousButton;

    private Bitmap posterImage;

    public ImageButton mCenterPlaylistButton;
    public List<MediaModel.Medium> videos;
    public List<MediaModel.Medium> relatedVideos;
    public boolean isPlayingRelatedVideo = false;

    public String playlistTitle;
    public DrawerLayout mDrawerLayout;
    public RecyclerView mDrawerRecycle;
    public TextView mPlaylistTitle;
    private boolean isVideoPlayingWhenPlaylistOpens = false;

    private ViewGroup similarvideoLayout;
    public RecyclerView mSimilorVideoRecycle;

    //private float xCurrentPos, yCurrentPos;
    private ImageView mCenterPlayerLogo;
    private ImageView mPlayerLogo;

    //ImageButton homeButton;
    public RelativeLayout relativeDrawer;
    public boolean isOutSideClicked = false;

    public PDMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.PDMediaController);
        mScalable = a.getBoolean(R.styleable.PDMediaController_uvv_scalable, false);
        a.recycle();
        init(context);
    }

    public PDMediaController(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRoot = inflater.inflate(R.layout.uvv_player_controller, this);
        viewRoot.setOnTouchListener(mTouchListener);
        initControllerView(viewRoot);
    }


    private void initControllerView(View v) {
        mTitleLayout = v.findViewById(R.id.title_part);
        mControlLayout = v.findViewById(R.id.control_layout);
        posterLayout = (ViewGroup) v.findViewById(R.id.poster_layout);
        posterImageview = (ImageView) v.findViewById(R.id.poster_image);
        loadingLayout = (ViewGroup) v.findViewById(R.id.loading_layout);
        errorLayout = (ViewGroup) v.findViewById(R.id.error_layout);
        mTurnButton = (ImageButton) v.findViewById(R.id.turn_button);
        mScaleButton = (ImageButton) v.findViewById(R.id.scale_button);
        mCenterPlayButton = v.findViewById(R.id.center_play_btn);
        mCenterNextButton = (ImageButton) v.findViewById(R.id.next_button);
        mCenterPreviousButton = (ImageButton) v.findViewById(R.id.prev_button);
        mBackButton = v.findViewById(R.id.back_btn);

        mCenterPlayerLogo = (ImageView) v.findViewById(R.id.imgPlayerControlLogo);
        mPlayerLogo = (ImageView) v.findViewById(R.id.imgPlayerLogoBG);
        //mCenterPlayerLogo.setVisibility(INVISIBLE);
        mPlayerLogo.setVisibility(VISIBLE);

        mCenterPlaylistButton = (ImageButton) v.findViewById(R.id.playlist_btn);
        relativeDrawer = (RelativeLayout) v.findViewById(R.id.relativeDrawer);
        mDrawerLayout = (DrawerLayout) v.findViewById(R.id.drawer_layout);
        mDrawerRecycle = (RecyclerView) v.findViewById(R.id.right_drawer);
        mPlaylistTitle = (TextView) v.findViewById(R.id.txt_playlistTitle);

        similarvideoLayout = (ViewGroup) v.findViewById(R.id.similarVideo);
        mSimilorVideoRecycle = (RecyclerView) v.findViewById(R.id.rv_posters);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerRecycle.setLayoutManager(llm);


        ViewGroup.LayoutParams params = mDrawerRecycle.getLayoutParams();
        //(int) (Utility.getWindowWidth(mContext) * 0.70f
        params.width = Utility.toPixels(240, mContext);
        //mDrawerRecycle.setLayoutParams(params);


        mDrawerLayout.setVisibility(INVISIBLE);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                //Called when a drawer's position changes.
                MLog.w(TAG, "onDrawerSlide:  slideOffset = "+slideOffset);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                // disable swiping so that the drawer can't be closed by accident when scrolling through webview
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);

//                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                //Called when a drawer has settled in a completely open state.
                //The drawer is interactive at this point.
                // If you have 2 drawers (left and right) you can distinguish
                // them by using id of the drawerView. int id = drawerView.getId();
                // id will be your layout's id: for example R.id.left_drawer
                MLog.w(TAG, "onDrawerOpened: Open");
                mDrawerLayout.setTag(0);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                // allow swiping to open the drawer
                //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mDrawerLayout.setVisibility(INVISIBLE);

                // allow swiping to open the drawer
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                // Called when a drawer has settled in a completely closed state.
                MLog.w(TAG, "onDrawerClosed: Close");
                if((int)mDrawerLayout.getTag() != 1) {
                    //
                }

//                if (mPlayer != null && isVidioPlyingWhenPaylistOpens == true) {
//                    //mPlayer.start();
//                    doPauseResume();
//                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
                if( newState == DrawerLayout.STATE_DRAGGING && !mDrawerLayout.isDrawerOpen(GravityCompat.END) ) {
                    // this where Drawer start opening
                    MLog.w(TAG, "onDrawerStateChanged: Open");
                } else if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    //Close Drawer
                    MLog.w(TAG, "onDrawerStateChanged: Close");
                }
            }
        });


        mCenterPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                    //mDrawerLayout.setVisibility(INVISIBLE);

                    MLog.e(TAG,"Play Center");
                    if (isVideoPlayingWhenPlaylistOpens) {
                        isVideoPlayingWhenPlaylistOpens = false;
                        mPlayer.start();
                    }
                } else {
                    if (mPlayer != null && mPlayer.isPlaying()) {
                        isVideoPlayingWhenPlaylistOpens = true;
                        mPlayer.pause();
                    }
                    mDrawerLayout.setVisibility(VISIBLE);
                    mDrawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });

        relativeDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mDrawerLayout.setVisibility(VISIBLE);

                if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    //MToast.makeText(getContext(), "inside click layout", Toast.LENGTH_SHORT).show();
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                    //mDrawerLayout.closeDrawer(GravityCompat.END,false);

                } else {
                    //Toast.makeText(getContext(), "outside click layout", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
//            mDrawerLayout.closeDrawers();
//        }

        if (mTurnButton != null) {
            mTurnButton.requestFocus();
            mTurnButton.setOnClickListener(mPauseListener);
        }

        if (mScalable) {
            if (mScaleButton != null) {
                mScaleButton.setVisibility(VISIBLE);
                mScaleButton.setOnClickListener(mScaleListener);
            }
        } else {
            if (mScaleButton != null) {
                mScaleButton.setVisibility(GONE);
            }
        }

        // REMARK : Can hide mCenterPlayButton button intially still user dont press start button
        if (mCenterPlayButton != null) {//Resume playback
            mCenterPlayButton.setOnClickListener(mCenterPlayListener);
        }

        if (mCenterPreviousButton != null) {//Back button visible only in full screen
            mCenterPreviousButton.setOnClickListener(mPreviousVideoListener);
        }

        if (mCenterNextButton != null) {//Back button visible only in full screen
            mCenterNextButton.setOnClickListener(mNextVideoListener);
        }

        if (mBackButton != null) {//Back button visible only in full screen
            mBackButton.setOnClickListener(mBackListener);
            mBackButton.setVisibility(View.INVISIBLE);
        }

        View bar = v.findViewById(R.id.seekbar);
        mProgress = (ProgressBar) bar;
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.duration);
        mCurrentTime = (TextView) v.findViewById(R.id.has_played);
        mTitle = (TextView) v.findViewById(R.id.title);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {

                View content = findViewById(R.id.right_drawer);
                int[] contentLocation = new int[2];
                content.getLocationOnScreen(contentLocation);
                Rect rect = new Rect(contentLocation[0],
                        contentLocation[1],
                        contentLocation[0] + content.getWidth(),
                        contentLocation[1] + content.getHeight());

              /* View toolbarView = findViewById(R.id.toolbar);
                int[] toolbarLocation = new int[2];
                toolbarView.getLocationOnScreen(toolbarLocation);
                Rect toolbarViewRect = new Rect(toolbarLocation[0],
                        toolbarLocation[1],
                        toolbarLocation[0] + toolbarView.getWidth(),
                        toolbarLocation[1] + toolbarView.getHeight());
                toolbarViewRect.contains((int) event.getX(), (int) event.getY());*/

                if (!(rect.contains((int) event.getX(), (int) event.getY())) ) {
                    isOutSideClicked = true;
                } else {
                    isOutSideClicked = false;
                }

                Rect viewRect = new Rect();
                mDrawerRecycle.getGlobalVisibleRect(viewRect);
                Rect viewRect2 = new Rect();
                viewRect2.set(0,viewRect.top,viewRect.left,viewRect.bottom);

                if(viewRect.contains((int) event.getRawX(), (int) event.getRawY()))
                {
                    //Toast.makeText(mContext, "Inside..", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(mContext, "Outside..", Toast.LENGTH_SHORT).show();
                }

                if (isOutSideClicked
                        && viewRect2.contains((int) event.getRawX(), (int) event.getRawY()))
                {
                    // hide your navigation view here.
                    // make http call/db request
                    //Toast.makeText(mContext, "Hello..", Toast.LENGTH_SHORT).show();
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                    //mDrawerLayout.closeDrawer(GravityCompat.END,false);
                    //mDrawerLayout.setVisibility(INVISIBLE);
                    if(isVideoPlayingWhenPlaylistOpens) {
                        doPauseResume();
                    }
                }

            } else {
                return super.dispatchTouchEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_DOWN && isOutSideClicked) {
            isOutSideClicked = false;
            return super.dispatchTouchEvent(event);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && isOutSideClicked) {
            return super.dispatchTouchEvent(event);
        }


        return super.dispatchTouchEvent(event);
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mTurnButton != null && mPlayer != null && !mPlayer.canPause()) {
                mTurnButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        // It is only responsible for the display of the upper and lower bar,
        // not responsible for the central loading, error, playBtn display.
        if (!mShowing) {
            setProgress();
            if (mTurnButton != null) {
                mTurnButton.requestFocus();
            }
            disableUnsupportedButtons();
            mShowing = true;
        }
        updatePausePlay();
        updateBackButton();

        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
        if (mTitleLayout.getVisibility() != VISIBLE)
               // && (mIsFullScreen || (mTitle != null  && mTitle.equals(""))))
        {
            mTitleLayout.setVisibility(VISIBLE);
        }
        if (mControlLayout.getVisibility() != VISIBLE) {
            mControlLayout.setVisibility(VISIBLE);
        }

        if (mPlayerLogo.getVisibility() == VISIBLE) {
            mPlayerLogo.setVisibility(INVISIBLE);
        }

        // cause the progress bar to be updated even if mShowing
        // was already true. This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }


    public void hide() {
        //Only responsible for hiding the upper and lower bar,
        // is not responsible for the central loading, error, hidden playBtn
        if (mShowing) {
            mHandler.removeMessages(SHOW_PROGRESS);
            mHandler.removeMessages(SHOW_POSTER);
            mTitleLayout.setVisibility(GONE);
            mControlLayout.setVisibility(GONE);
            mShowing = false;
            mPlayerLogo.setVisibility(VISIBLE);
        }
    }

    private String getMessageType(int type) {
        switch (type) {
            case FADE_OUT: //1
                return "FADE_OUT";
                //break;
            case SHOW_PROGRESS: //2
                return "SHOW_PROGRESS";
                //break;
            case SHOW_LOADING: //3
                return "SHOW_LOADING";
               // break;
            case SHOW_COMPLETE: //7
                return "SHOW_COMPLETE";
                // break;
            case SHOW_ERROR: //5
                return "SHOW_ERROR";
                //break;
            case HIDE_LOADING: //4
            case HIDE_ERROR: //6
            case HIDE_COMPLETE: //8
                return "HIDE_LOADING, ERROR, COMPLETE";
            //break;
            case SHOW_POSTER: //9
                return "SHOW_POSTER";
            //break;
            case HIDE_POSTER: //10
                return "HIDE_POSTER";
            // break;
        }
        return "------";
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MLog.w(TAG, "changed, handleMessage: " + msg.what + "  " + getMessageType(msg.what));
            int pos;
            switch (msg.what) {
                case FADE_OUT: //1
                    hide();
                    break;
                case SHOW_PROGRESS: //2
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer != null && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));

                        //showCenterView(R.id.loading_layout);
                    }
                    break;
                case SHOW_LOADING: //3
                    show();
                    showCenterView(R.id.loading_layout);
                    break;
                case SHOW_COMPLETE: //7
                    showCenterView(R.id.center_play_btn);
                    break;
                case SHOW_ERROR: //5
                    show();
                    showCenterView(R.id.error_layout);
                    break;
                case HIDE_LOADING: //4
                case HIDE_ERROR: //6
                case HIDE_COMPLETE: //8
                    hide();
                    hideCenterView();
                    break;
                case SHOW_POSTER: //9
                    showCenterView(R.id.poster_layout);
                    break;
                case HIDE_POSTER: //10
                    hide();
                    hidePosterView();
                    break;
            }
        }
    };

    private void showCenterView(int resId) {
        // Show Poster : play starts
        if (resId == R.id.poster_layout) {

            if (posterLayout.getVisibility() != VISIBLE) {
                posterLayout.setVisibility(VISIBLE);
            }
            if (mCenterPlayButton.getVisibility() != VISIBLE) {
                mCenterPlayButton.setVisibility(VISIBLE);
            }

            if (loadingLayout.getVisibility() == VISIBLE) {
                loadingLayout.setVisibility(GONE);
            }
            if (errorLayout.getVisibility() == VISIBLE) {
                errorLayout.setVisibility(GONE);
            }

        }
        // Show Loading :
        else if (resId == R.id.loading_layout) {
            if (posterLayout.getVisibility() == VISIBLE) {
                posterLayout.setVisibility(GONE);
            }
//            if (mCenterPlayButton.getVisibility() == VISIBLE) {
//                mCenterPlayButton.setVisibility(GONE);
//            }

            if (loadingLayout.getVisibility() != VISIBLE) {
                loadingLayout.setVisibility(VISIBLE);
            }
            if (errorLayout.getVisibility() == VISIBLE) {
                errorLayout.setVisibility(GONE);
            }

        }
        // Show Center Play button
        else if (resId == R.id.center_play_btn) {

            if (posterLayout.getVisibility() != VISIBLE) {
                posterLayout.setVisibility(VISIBLE);
            }
            if (mCenterPlayButton.getVisibility() != VISIBLE) {
                mCenterPlayButton.setVisibility(VISIBLE);
            }

            if (loadingLayout.getVisibility() == VISIBLE) {
                loadingLayout.setVisibility(GONE);
            }
            if (errorLayout.getVisibility() == VISIBLE) {
                errorLayout.setVisibility(GONE);
            }

        }
        // Show error layout
        else if (resId == R.id.error_layout) {
            if (posterLayout.getVisibility() == VISIBLE) {
                posterLayout.setVisibility(GONE);
            }
            if (errorLayout.getVisibility() != VISIBLE) {
                errorLayout.setVisibility(VISIBLE);
            }
//            if (mCenterPlayButton.getVisibility() == VISIBLE) {
//                mCenterPlayButton.setVisibility(GONE);
//            }
            if (loadingLayout.getVisibility() == VISIBLE) {
                loadingLayout.setVisibility(GONE);
            }
        }
    }


    private void hideCenterView() {

        if (posterLayout.getVisibility() == VISIBLE) {
            posterLayout.setVisibility(GONE);
            //posterImageview.setImageBitmap(posterImage);
        }

//        if (mCenterPlayButton.getVisibility() == VISIBLE) {
//            mCenterPlayButton.setVisibility(GONE);
//        }


        // REMARK: TRY UNCOMMENT THIS I POSTER SHOWING AFTER VIDEO PLAYS
        // hidePosterView();

        if (errorLayout.getVisibility() == VISIBLE) {
            errorLayout.setVisibility(GONE);
        }
        if (loadingLayout.getVisibility() == VISIBLE) {
            loadingLayout.setVisibility(GONE);
        }
    }

    void resizeDrawerMenu() {
        resizeDrawerMenu(-1);
    }

    private void resizeDrawerMenu( int screenOrientation ) {
        if(isFullScreen()
                || (screenOrientation != -1
                && (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)))
        {
            //(int) (Utility.getWindowWidth(mContext) * 0.50f
            ViewGroup.LayoutParams params = mDrawerRecycle.getLayoutParams();
            params.width = Utility.toPixels( 320, mContext);
            //mDrawerRecycle.setLayoutParams(params);
        } else {
            //(int) (Utility.getWindowWidth(mContext) * 0.70f)
            ViewGroup.LayoutParams params = mDrawerRecycle.getLayoutParams();
            params.width = Utility.toPixels( 240, mContext);
            //mDrawerRecycle.setLayoutParams(params);
        }
    }

    private void hidePosterView() {
        if (posterLayout.getVisibility() == VISIBLE) {
            posterLayout.setVisibility(GONE);
        }
    }

    public void reset() {
        mCurrentTime.setText("00:00");
        mEndTime.setText("00:00");
        mProgress.setProgress(0);
        mTurnButton.setImageResource(R.drawable.uvv_player_player_btn);
        setVisibility(View.VISIBLE);
        hideLoading();
        hidePoster();
    }


    public void setplayListVideos(List<MediaModel.Medium> videos, String playlistTitle) {

        if(videos == null || (videos != null && videos.size() == 0)) {
            return;
        }

        this.videos = videos;
        mCenterPlaylistButton.setVisibility(VISIBLE);

        PlaylistAdapter adapater = new PlaylistAdapter(mContext,videos );
        mDrawerRecycle.setAdapter(adapater);

        // Show scrollbar in playlist
        mDrawerRecycle.setVerticalScrollBarEnabled(true);

        mPlaylistTitle.setText(playlistTitle);

        adapater.setOnItemClickListener(new onRecyclerViewItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                MLog.e(TAG,"Select Playlist Video");
                mDrawerLayout.closeDrawer(GravityCompat.END);
                if (mPlayer != null) {
                    isVideoPlayingWhenPlaylistOpens = false;
                    hideSimilarVideos();
                    mPlayer.onPlaylistVideoSelected(position);
                }
            }
        });
    }

    public void setRelatedVideos(List<MediaModel.Medium> videos) {
        if(relatedVideos == null || (relatedVideos != null && relatedVideos.size() == 0)) {
            return;
        }

        this.relatedVideos = videos;
        //mCenterPlaylistButton.setVisibility(VISIBLE);

        int mNoOfColumns = Utility.calculateNoOfColumns(mContext);

        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(mContext, mNoOfColumns);
        mSimilorVideoRecycle.setLayoutManager(recyclerViewLayoutManager);

        PosterAdapater posterAdapater = new PosterAdapater(mContext, videos);
        mSimilorVideoRecycle.setAdapter(posterAdapater);

        posterAdapater.setOnItemClickListener(new onRecyclerViewItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                mDrawerLayout.closeDrawer(GravityCompat.END);
                if (mPlayer != null) {
                   // mPlayer.onRelatedVideoSelected(relatedVideos.get(position),position);
                }
            }
        });
    }


    public void showRelatedVideos() {
        if (relatedVideos == null || (relatedVideos != null && relatedVideos.size() == 0)) {
            return;
        }

        if (mShowing) {
            hide();
        }
        similarvideoLayout.setVisibility(VISIBLE);

    }


    public void hideSimilarVideos() {

        if (similarvideoLayout.getVisibility() == VISIBLE) {
            similarvideoLayout.setVisibility(INVISIBLE);
        }

    }


    public void setPosterimage(Bitmap image) {
        posterImage = image;

        if (posterLayout.getVisibility() == VISIBLE) {
            posterImageview.setImageBitmap(posterImage);
        }
    }

    public void loadPosterimage(String image_url) {
        new DownloadImageTask((ImageView) findViewById(R.id.poster_image), BitmapFactory.decodeResource(getResources(), R.drawable.img_video_placeholder))
                .execute(image_url);
    }

    public void loadPlayerLogoImage(String logo_url) {
        new DownloadImageTask((ImageView) findViewById(R.id.imgPlayerControlLogo), null)
                .execute(logo_url);
        new DownloadImageTask((ImageView) findViewById(R.id.imgPlayerLogoBG), null)
                .execute(logo_url);
    }


    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        mPlayer.setUpdatedTime(mPlayer.getCurrentPosition());

        long totalDuration = mPlayer.getDuration();
        long currentDuration = mPlayer.getCurrentPosition();
        if (!mPlayer.isMidAdCalled()
                && currentDuration > (totalDuration/2) ) {

            mPlayer.showMidAdsNow();

//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    // things to do on the main thread
//                    // Request Mid Video Ad
//                    //requestAds(getString(R.string.ad_tag_pre_url));
//                    //playerControlView.hide();
//                    //mAdsManager.start();
//                    //mIsMidAdShown = true;
//
//                    ///- MToast.makeText(mContext, "Can Show Mid Ad Here", Toast.LENGTH_SHORT).show();
//                }
//            });
        }

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                show(0); // show until hide is called
                handled = false;
                break;
            case MotionEvent.ACTION_UP:
                if (!handled) {
                    handled = false;
                    show(sDefaultTimeout); // start timeout
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                hide();
                break;
            default:
                break;
        }
        return true;
    }

    boolean handled = false;
    //If it is showing, make it disappear
    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    hide();
                    handled = true;
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mTurnButton != null) {
                    mTurnButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                hideCenterView();
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer != null) {
                doPauseResume();
                show(sDefaultTimeout);
            }
        }
    };

    private OnClickListener mScaleListener = new OnClickListener() {
        public void onClick(View v) {
            mIsFullScreen = !mIsFullScreen;
            updateScaleButton();
            updateBackButton();
            try {
                mPlayer.setFullscreen(mIsFullScreen);
            } catch (Exception e){
                Log.e(TAG,"No Video");
            }

            resizeDrawerMenu();
        }
    };

    //Return button only when full screen
    private OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            if (mIsFullScreen) {
                mIsFullScreen = false;
                updateScaleButton();
                updateBackButton();
                mPlayer.setFullscreen(false);
                resizeDrawerMenu();
            }

        }
    };

    private OnClickListener mNextVideoListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer != null) {
                //doPauseResume();
                //show(sDefaultTimeout);
                mPlayer.playNextVideo();
            }
        }
    };

    private OnClickListener mPreviousVideoListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer != null) {
                //doPauseResume();
                //show(sDefaultTimeout);
                mPlayer.playPreviousVideo();
            }
        }
    };

    private OnClickListener mCenterPlayListener = new OnClickListener() {
        public void onClick(View v) {
            hideCenterView();
            mPlayer.start();
        }
    };

    private void updatePausePlay() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mTurnButton.setImageResource(R.drawable.uvv_stop_btn);
//            mCenterPlayButton.setVisibility(GONE);
        } else {
            mTurnButton.setImageResource(R.drawable.uvv_player_player_btn);
//            mCenterPlayButton.setVisibility(VISIBLE);
        }
    }

    void updateScaleButton() {
        if (mIsFullScreen) {
            mScaleButton.setImageResource(R.drawable.uvv_star_zoom_in);
        } else {
            mScaleButton.setImageResource(R.drawable.uvv_player_scale_btn);
        }
    }

    void toggleButtons(boolean isFullScreen) {
        mIsFullScreen = isFullScreen;
        updateScaleButton();
        updateBackButton();
    }

    void updateBackButton() {
        //mBackButton.setVisibility(mIsFullScreen ? View.VISIBLE : View.INVISIBLE);
    }

    boolean isFullScreen() {
        return mIsFullScreen;
    }

    public void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            hideCenterView();
            mPlayer.start();
        }
        updatePausePlay();
    }


    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        int newPosition = 0;

        boolean change = false;

        public void onStartTrackingTouch(SeekBar bar) {
            if (mPlayer == null) {
                return;
            }
            show(3600000);

            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null || !fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            newPosition = (int) newposition;
            change = true;
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (mPlayer == null) {
                return;
            }
            if (change) {
                mPlayer.seekTo(newPosition);
                if (mCurrentTime != null) {
                    mCurrentTime.setText(stringForTime(newPosition));
                }
            }
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mShowing = true;
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
//        super.setEnabled(enabled);
        if (mTurnButton != null) {
            mTurnButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        if (mScalable) {
            mScaleButton.setEnabled(enabled);
        }
        mBackButton.setEnabled(true);// Back button in the upper right corner is always available in full screen mode.
    }

    public void showPoster() {
        mHandler.sendEmptyMessage(SHOW_POSTER);
    }

    public void hidePoster() {
        mHandler.sendEmptyMessage(HIDE_POSTER);
    }

    public void showLoading() {
        mHandler.sendEmptyMessage(HIDE_POSTER);
        mHandler.sendEmptyMessage(SHOW_LOADING);
    }

    public void hideLoading() {
        mHandler.sendEmptyMessage(HIDE_LOADING);
    }


    public void hideProgress(){
        loadingLayout.setVisibility(GONE);
    }

    public void showError() {
        mHandler.sendEmptyMessage(SHOW_ERROR);
    }

    public void hideError() {
        mHandler.sendEmptyMessage(HIDE_ERROR);
    }

    public void showComplete() {
        mHandler.sendEmptyMessage(SHOW_COMPLETE);
    }

    public void hideComplete() {
        mHandler.sendEmptyMessage(HIDE_COMPLETE);
    }

    public void setTitle(String titile) {
        mTitle.setText(titile);
    }

//    public void setFullscreenEnabled(boolean enabled) {
//        mFullscreenEnabled = enabled;
//        mScaleButton.setVisibility(mIsFullScreen ? VISIBLE : GONE);
//    }


    public void setOnErrorView(int resId) {
        errorLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(resId, errorLayout, true);
    }

    public void setOnErrorView(View onErrorView) {
        errorLayout.removeAllViews();
        errorLayout.addView(onErrorView);
    }

    public void setOnLoadingView(int resId) {
        loadingLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(resId, loadingLayout, true);
    }

    public void setOnLoadingView(View onLoadingView) {
        loadingLayout.removeAllViews();
        loadingLayout.addView(onLoadingView);
    }

    public void setOnErrorViewClick(OnClickListener onClickListener) {
        errorLayout.setOnClickListener(onClickListener);
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        void closePlayer();//关闭播放视频,使播放器处于idle状态

        void setFullscreen(boolean fullscreen);


        /***
         *
         * @param fullscreen
         * @param screenOrientation valid only fullscreen=true.values should be one of
         *                          ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
         *                          ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
         *                          ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
         *                          ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
         */
        void setFullscreen(boolean fullscreen, int screenOrientation);

        void playNextVideo();
        void playPreviousVideo();

        void onPlaylistVideoSelected(int videoIndex);
        void onRelatedVideoSelected(JSONObject video, int videoIndex);

        boolean isMidAdCalled();
        public void showMidAdsNow();

        void setUpdatedTime(float currentTime);
    }
}
