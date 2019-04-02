/*
* Copyright (C) 2015 Author <dictfb#gmail.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obbtain a copy of the License at
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

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.lib.adloader.R;
import com.lib.adloader.model.MediaModel;
import com.lib.adloader.utils.MLog;
import com.lib.adloader.utils.Observable.ObservableInteger;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


public class PDVideoView
        extends SurfaceView
        implements PDMediaController.MediaPlayerControl, OrientationDetector.OrientationChangeListener
{
    private String TAG = "VID_PLAYER";
    // settable by the client
    private Uri mUri;

    // all possible internal states
    private static final int STATE_ERROR              = -1;
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private ObservableInteger mCurrentState;// = STATE_IDLE;
    private ObservableInteger mTargetState;//  = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private int         mAudioSession;
    private int         mVideoWidth;
    private int         mVideoHeight;
    private int         mSurfaceWidth;
    private int         mSurfaceHeight;
    private PDMediaController mMediaController;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private int         mCurrentBufferPercentage;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;
    private int         mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean     mCanPause;
    private boolean     mCanSeekBack;
    private boolean     mCanSeekForward;
    private boolean     mPreparedBeforeStart;
    private boolean     mAutoplayWhenPrepared;   // To Autoplay on prepared
    private Context mContext;
    private boolean     mFitXY = false;
    private boolean     mAutoRotation = false;
    private int  mVideoViewLayoutWidth = 0;
    private int  mVideoViewLayoutHeight = 0;


    private OrientationDetector mOrientationDetector;
    private VideoViewCallback videoViewCallback;
    private VideoTrackerCallback videoTackerCallback;


    private boolean     misPreAdCalled = false;
    private boolean     misMidAdCalled = false;

    private long    video_random = 0;
    private long    video_id = 0;

    public float    skipTime = 0;
    private float   previousTime = 0;
    public float    currentTime = 0;

    private boolean isPaused = false;

    public PDVideoView(Context context) {
        this(context,null);
    }

    public PDVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PDVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.PDVideoView,0,0);
        mFitXY = a.getBoolean(R.styleable.PDVideoView_uvv_fitXY, false);
        mAutoRotation = a.getBoolean(R.styleable.PDVideoView_uvv_autoRotation, false);
        a.recycle();
        mCurrentState = new ObservableInteger();
        mCurrentState.setValue(STATE_IDLE);
        mTargetState = new ObservableInteger();
        mTargetState.setValue(STATE_IDLE);
        initVideoView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mFitXY) {
            onMeasureFitXY(widthMeasureSpec, heightMeasureSpec);
        } else {
            onMeasureKeepAspectRatio(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void onMeasureFitXY(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private void onMeasureKeepAspectRatio(int widthMeasureSpec, int heightMeasureSpec) {
        //MLog.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if ( mVideoWidth * height  < width * mVideoHeight ) {
                    //MLog.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                    //MLog.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(PDVideoView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            info.setClassName(PDVideoView.class.getName());
        }
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState.setValue(STATE_IDLE);
        mTargetState.setValue(STATE_IDLE);

        //Default autoplay is false
        mAutoplayWhenPrepared = false;

        misPreAdCalled = false;
        misMidAdCalled = false;

        this.skipTime = 0;
        this.previousTime = 0;
        this.currentTime = 0;

        mCurrentState.deleteObservers();
        mCurrentState.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object newValue) {
                // a1 changed! (aka a changed)
                // newValue is the observable int value (it's the same as a1.getValue())
                Log.d(TAG, "mCurrentState has changed, new value:"+ (int) newValue + " - "+ getStatusType((int) newValue));
            }
        });

        mTargetState.deleteObservers();
        mTargetState.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object newValue) {
                // a1 changed! (aka a changed)
                // newValue is the observable int value (it's the same as a1.getValue())
                Log.d(TAG, "mTargetState has changed, new value::"+ (int) newValue + " - "+ getStatusType((int) newValue));
            }
        });
    }

    private String getStatusType(int type) {
        switch (type) {
            case STATE_ERROR: //-1
                return "STATE_ERROR";
            //break;
            case STATE_IDLE: //0
                return "STATE_IDLE";
            //break;
            case STATE_PREPARING: //1
                return "STATE_PREPARING";
            // break;
            case STATE_PREPARED: //2
                return "STATE_PREPARED";
            // break;
            case STATE_PLAYING: //3
                return "STATE_PLAYING";
            //break;
            case STATE_PAUSED: //4
                return "STATE_PAUSED";
            //break;
            case STATE_PLAYBACK_COMPLETED: //5
                return "STATE_PLAYBACK_COMPLETED";
            //break;
        }
        return "------";
    }

    @Override
    public void onOrientationChanged(int screenOrientation, OrientationDetector.Direction direction) {
        if (!mAutoRotation) {
            return;
        }

        if (direction == OrientationDetector.Direction.PORTRAIT) {
            setFullscreen(false, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (direction == OrientationDetector.Direction.REVERSE_PORTRAIT) {
            setFullscreen(false, ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else if (direction == OrientationDetector.Direction.LANDSCAPE) {
            setFullscreen(true, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (direction == OrientationDetector.Direction.REVERSE_LANDSCAPE) {
            setFullscreen(true, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }

    public void setFitXY(boolean fitXY) {
        mFitXY = fitXY;
    }

    public void setAutoRotation(boolean auto) {
        mAutoRotation = auto;
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
//    public void setVideoPath(String path) {
//        setVideoURI(Uri.parse(path));
//    }

    public void setVideoPath(String path, String posterPath, Long video_id, Long video_random) {
        setVideoPath(false, path, posterPath, video_id, video_random);
    }

    public void setVideoPath(Boolean autoPlayOnPrepared, String path, String posterPath, Long video_id, Long video_random) {

        if(autoPlayOnPrepared)
            mTargetState.setValue(STATE_PLAYING);
//        else
//            mTargetState.setValue(STATE_IDLE);

        setVideoURI(Uri.parse(path));
        mAutoplayWhenPrepared = autoPlayOnPrepared;
        mMediaController.loadPosterimage(posterPath);
        updatePoster();
        // mMediaController.showPoster();

        misPreAdCalled = false;
        misMidAdCalled = false;

        this.video_id = video_id;
        this.video_random = video_random;

        this.skipTime = 0;
        this.previousTime = 0;
        this.currentTime = 0;
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mSeekWhenPrepared = 0;
        clearCurrentFrame();
        openVideo();
        requestLayout();
        invalidate();
    }


    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState.setValue(STATE_IDLE); // = STATE_IDLE;
            mTargetState.setValue(STATE_IDLE); // = STATE_IDLE;
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }

        if (isPaused == true && mMediaPlayer != null) {
            MLog.w(TAG, "Can Resume Video: ");
            //return;
        }

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        try {
            mMediaPlayer = new MediaPlayer();

            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            //mMediaPlayer.setVi
            mMediaPlayer.setDataSource(mContext, mUri);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();


            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState.setValue(STATE_PREPARING); //= STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            MLog.w(TAG, "Unable to open content: " + mUri+" Exception: "+ ex.getLocalizedMessage());
            mCurrentState.setValue(STATE_ERROR); //= STATE_ERROR;
            mTargetState.setValue(STATE_ERROR); //= STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    public void setMediaController(PDMediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            mMediaController.setEnabled(isInPlaybackState());
            mMediaController.hide();
        }
    }

    public void clearCurrentFrame() {
//        setVisibility(GONE);
//        setVisibility(VISIBLE);

        // setZOrderOnTop(false);
        // setZOrderOnTop(true);

        // videoViewer.setBackgroundColor(getResources().getColor(R.color.custom_blue));
        // videoViewer.setBackgroundColor(Color.TRANSPARENT);

        //surfaceViewHolder.setFormat(PixelFormat.TRANSPARENT);
        //surfaceViewHolder.setFormat(PixelFormat.OPAQUE);

        //videoHolder.setFormat(PixelFormat.TRANSPARENT);
        //videoHolder.setFormat(PixelFormat.OPAQUE);
        //videoService.setVideoDisplay(videoHolder)
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    MLog.d(TAG, String.format("onVideoSizeChanged width=%d,height=%d", mVideoWidth, mVideoHeight));
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                        requestLayout();
                    }
                }
            };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState.setValue(STATE_PREPARED); // = STATE_PREPARED;

            mCanPause = mCanSeekBack = mCanSeekForward = true;

            if (videoTackerCallback != null) {
                videoTackerCallback.onVideoLoad(video_random);
            }

            mPreparedBeforeStart = true;
            if (mMediaController != null && isPlaying()) {
                mMediaController.showComplete();
                //mMediaController.hideComplete();
            }

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }

            Log.d(TAG, "changed, OnPreparedListener  mVideoWidth="+mVideoWidth
                    +", mSurfaceWidth="+mSurfaceWidth
                    +", mVideoHeight="+mVideoHeight
                    +", mSurfaceHeight="+mSurfaceHeight
                    +", mCurrentState="+getStatusType(mCurrentState.getValue())
                    +", mTargetState="+getStatusType(mTargetState.getValue()));
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                //MLog.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                //if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the video here instead of in the callback.
                    if (mTargetState.getValue() == STATE_PLAYING) {
                        start();
                        if (mMediaController != null) {
                            mMediaController.show();
                            mMediaController.hidePoster();
                        }
                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null) {
                            // Show the media controls when we're paused into a video and make 'em stick.
                            mMediaController.show(0);
                        }
                    }
                //} else {}
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                Log.d(TAG, "changed, mTargetState check 2:"+ mTargetState + " - "+ getStatusType(mTargetState.getValue()));
                if (mTargetState.getValue() == STATE_PLAYING) {
                    start();
                    mMediaController.hidePoster();
                } else {
                    Log.d(TAG, "changed, mTargetState check 2:"+ mTargetState + " - "+ getStatusType(mTargetState.getValue()));
                }
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mCurrentState.setValue(STATE_PLAYBACK_COMPLETED); //= STATE_PLAYBACK_COMPLETED;
                    mTargetState.setValue(STATE_PLAYBACK_COMPLETED); //= STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null) {
                        boolean a = mMediaPlayer.isPlaying();
                        int b = mCurrentState.getValue();
                        mMediaController.showComplete();
                        //FIXME After the play is complete, a play button will be displayed in the center of the video. Click the play button to call start replay.
                        // But after the start, it was actually pulled back here,
                        // resulting in the first click of the button will not play the video,
                        // you need to click the second time.
                        MLog.d(TAG, String.format("a=%s,b=%d", a, b));
                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }

                    // TRACKER
                    // PDVideoTacker.getInstance().onViewDuration("",""+video_random,""+currentTime,""+skipTime);
                }
            };


    private MediaPlayer.OnInfoListener mInfoListener =
            new MediaPlayer.OnInfoListener() {
                public  boolean onInfo(MediaPlayer mp, int what, int extra){
                    boolean handled = false;
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            MLog.d(TAG, "onInfo MediaPlayer.MEDIA_INFO_BUFFERING_START");
                            if (videoViewCallback != null) {
                                videoViewCallback.onBufferingStart(mMediaPlayer);
                            }
                            if (mMediaController != null) {
                                mMediaController.showLoading();
                            }
                            handled = true;
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            MLog.d(TAG, "onInfo MediaPlayer.MEDIA_INFO_BUFFERING_END");
                            if (videoViewCallback != null) {
                                videoViewCallback.onBufferingEnd(mMediaPlayer);
                            }
                            if (mMediaController != null) {
                                mMediaController.hideLoading();
                            }
                            handled = true;
                            break;
                    }
                    if (mOnInfoListener != null) {
                        return mOnInfoListener.onInfo(mp, what, extra) || handled;
                    }
                    return handled;
                }
            };

    private MediaPlayer.OnErrorListener mErrorListener =
            new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {

                    switch(framework_err){
                        case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                            MLog.e(TAG, "unknown media error what=["+framework_err+"] extra=["+impl_err+"]");
                            break;
                        case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                            MLog.e(TAG, "Streaming source Server died what=["+framework_err+"] extra=["+impl_err+"]");
                            break;
                        default:
                            MLog.e(TAG, "Default Problems what=["+ framework_err +"] extra=["+impl_err+"]");
                    }

                    if (impl_err == MediaPlayer.MEDIA_ERROR_SERVER_DIED
                            || impl_err == MediaPlayer.MEDIA_ERROR_MALFORMED) {
                        MLog.e(TAG, "Error : MEDIA_ERROR_SERVER_DIED or MEDIA_ERROR_MALFORMED");
                        //sendPlayerStatus("erroronplaying");
                    } else if (impl_err == MediaPlayer.MEDIA_ERROR_IO) {
                        MLog.e(TAG, "Error : MEDIA_ERROR_IO");
                        //sendPlayerStatus("erroronplaying");
                        return false;
                    }

                    MLog.e(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState.setValue(STATE_ERROR); // = STATE_ERROR;
                    mTargetState.setValue(STATE_ERROR); // = STATE_ERROR;
                    if (mMediaController != null)
                    {
                        mMediaController.showError();
                    }

            /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

            /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
//                    if (getWindowToken() != null) {
//                        Resources r = mContext.getResources();
//                        int messageId;
//
//                        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
//                            messageId = com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
//                        } else {
//                            messageId = com.android.internal.R.string.VideoView_error_text_unknown;
//                        }
//
//                        new AlertDiaMLog.Builder(mContext)
//                                .setMessage(messageId)
//                                .setPositiveButton(com.android.internal.R.string.VideoView_error_button,
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int whichButton) {
//                                        /* If we get here, there is no onError listener, so
//                                         * at least inform them that the video is over.
//                                         */
//                                                if (mOnCompletionListener != null) {
//                                                    mOnCompletionListener.onCompletion(mMediaPlayer);
//                                                }
//                                            }
//                                        })
//                                .setCancelable(false)
//                                .show();
//                    }
                    return true;
                }
            };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                    MLog.i(TAG, "Buffered additional=["+percent+"%]");
                    if (percent != 0) {
                        float per = ((float)percent)/100;
                        PDVideoTacker.getInstance().onVideoBuffer(""+video_id,""+video_random, per);
                    }

                }
            };


    public void updatePoster() {
        if (mMediaController != null &&
                (mCurrentState.getValue() == STATE_IDLE
                        || mCurrentState.getValue() == STATE_PREPARING
                        || mCurrentState.getValue() == STATE_PREPARED)) {
            mMediaController.showPoster();
        } else {
            mMediaController.hidePoster();
        }
    }

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(MediaPlayer.OnErrorListener l)
    {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback()
    {
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int w, int h)
        {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState =  (mTargetState.getValue() == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder)
        {
            mSurfaceHolder = holder;
            openVideo();
            enableOrientationDetect();
        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaController != null) mMediaController.hide();
            release(true);
            disableOrientationDetect();
        }
    };

    private void enableOrientationDetect() {
        if (mAutoRotation && mOrientationDetector == null) {
            mOrientationDetector = new OrientationDetector(mContext);
            mOrientationDetector.setOrientationChangeListener(PDVideoView.this);
            mOrientationDetector.enable();
        }
    }

    private void disableOrientationDetect() {
        if (mOrientationDetector != null) {
            mOrientationDetector.disable();
        }
    }

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState.setValue(STATE_IDLE); // = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState.setValue(STATE_IDLE); // = STATE_IDLE;
            }
            updatePoster();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisibility();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisibility();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisibility();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisibility() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    public long getVideoRandom() {
        return video_random;
    }


    @Override
    public void start() {
        if (!mPreparedBeforeStart && mMediaController != null) {
            mMediaController.showLoading();
        }

        if(mCurrentState.getValue() == STATE_PREPARED)
        {
            PDVideoTacker.getInstance().onPlayVideo(""+video_id,""+video_random);
        }

        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState.setValue(STATE_PLAYING); // = STATE_PLAYING;
            if (this.videoViewCallback != null) {
                this.videoViewCallback.onStart(mMediaPlayer);
                if(!misPreAdCalled) {
                    misPreAdCalled = true;
                    this.videoViewCallback.onPreAdLoading(mMediaPlayer);
                }
            }
        }
        mTargetState.setValue(STATE_PLAYING); // = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState.setValue(STATE_PAUSED); // = STATE_PAUSED;
                isPaused = true;
                if (this.videoViewCallback != null) {
                    this.videoViewCallback.onPause(mMediaPlayer);
                }
            }
        }
        mTargetState.setValue(STATE_PAUSED); // = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        //start();
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
        // Seek event

        if(this.currentTime > this.previousTime){
            this.skipTime = (this.skipTime + (this.currentTime - this.previousTime));
        } else {
            this.skipTime = (this.skipTime - (this.previousTime - this.currentTime));
        }

        if(this.skipTime < 0) this.skipTime = 0;
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState.getValue() != STATE_ERROR &&
                mCurrentState.getValue() != STATE_IDLE &&
                mCurrentState.getValue() != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public void closePlayer() {
        release(true);
    }

    @Override
    public void setFullscreen(boolean fullscreen) {
        int screenOrientation = fullscreen ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        setFullscreen(fullscreen, screenOrientation);
    }

    @Override
    public void setFullscreen(boolean fullscreen, int screenOrientation) {
        // Activity Need to be set to: android:configChanges="keyboardHidden|orientation|screenSize"
        Activity activity = (Activity) mContext;

        if (fullscreen) {
            if (mVideoViewLayoutWidth == 0 && mVideoViewLayoutHeight == 0) {
                ViewGroup.LayoutParams params = getLayoutParams();
                mVideoViewLayoutWidth = params.width;//Save parameters before full screen
                mVideoViewLayoutHeight = params.height;
            }
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setRequestedOrientation(screenOrientation);
        } else {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = mVideoViewLayoutWidth;//Parameters before using full screen
            params.height = mVideoViewLayoutHeight;
            setLayoutParams(params);

            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setRequestedOrientation(screenOrientation);
        }
        mMediaController.toggleButtons(fullscreen);
        if (videoViewCallback != null) {
            videoViewCallback.onScaleChange(fullscreen);
        }
    }

    @Override
    public void playNextVideo() {
        if (videoViewCallback != null) {
            videoViewCallback.onNextVideo(mMediaPlayer);
        }
    }

    @Override
    public void playPreviousVideo() {
        if (videoViewCallback != null) {
            videoViewCallback.onPreviousVideo(mMediaPlayer);
        }
    }

    @Override
    public void onPlaylistVideoSelected(int videoIndex) {
        if (videoViewCallback != null) {
            videoViewCallback.onPlaylistVideoSelected(videoIndex);
        }
    }

    @Override
    public void onRelatedVideoSelected(JSONObject video, int videoIndex) {
        if (videoViewCallback != null) {
            videoViewCallback.onRelatedVideoSelected(video,videoIndex);
        }
    }

    @Override
    public boolean isMidAdCalled() {
        return misMidAdCalled;
    }

    @Override
    public void showMidAdsNow() {

        if (this.videoViewCallback != null) {
            if(!misMidAdCalled) {
                misMidAdCalled = true;
                this.videoViewCallback.onMidAdLoading(mMediaPlayer);
            }
        }
        // WE should have to set it to true so, player controller wont request to sho mid ads on video progresses change
        misMidAdCalled = true;
    }

    @Override
    public void setUpdatedTime(float currentTime) {
        this.previousTime = this.currentTime;
        this.currentTime = currentTime;
    }

    /*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void switchTitleBar(boolean show) {
        if (mContext instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity)mContext;
            android.support.v7.app.ActionBar supportActionBar = activity.getSupportActionBar();
            if (supportActionBar != null) {
                if (show) {
                    supportActionBar.show();
                } else {
                    supportActionBar.hide();
                }
            }
        }else if (mContext instanceof Activity) {
            Activity activity = (Activity)mContext;
            if(activity.getActionBar() != null) {
                if (show) {
                    activity.getActionBar().show();
                } else {
                    activity.getActionBar().hide();
                }
            }
        }
    }
*/


    public interface VideoViewCallback {
        void onScaleChange(boolean isFullscreen);
        void onPause(final MediaPlayer mediaPlayer);
        void onStart(final MediaPlayer mediaPlayer);
        void onPreAdLoading(final MediaPlayer mediaPlayer);
        void onMidAdLoading(final MediaPlayer mediaPlayer);
        void onBufferingStart(final MediaPlayer mediaPlayer);
        void onBufferingEnd(final MediaPlayer mediaPlayer);
        void onNextVideo(final MediaPlayer mediaPlayer);
        void onPreviousVideo(final MediaPlayer mediaPlayer);
        void onPlaylistVideoSelected(int videoIndex);
        void onRelatedVideoSelected(JSONObject video, int videoIndex);
    }

    public interface VideoTrackerCallback {
        void onVideoLoad(long random);
//        void onPlayerError(final MediaPlayer mediaPlayer);
//        void onVideoError(final MediaPlayer mediaPlayer);
//        void onTimeupdate(final MediaPlayer mediaPlayer);
    }

    public void setVideoViewCallback(VideoViewCallback callback) {
        this.videoViewCallback = callback;
    }

    public void setVideoTrackerCallback(VideoTrackerCallback callback) {
        this.videoTackerCallback = callback;
    }
}
