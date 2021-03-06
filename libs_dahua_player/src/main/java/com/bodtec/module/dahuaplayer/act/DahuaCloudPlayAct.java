package com.bodtec.module.dahuaplayer.act;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.business.adapter.DataAdapterImpl;
import com.android.business.adapter.DataAdapterInterface;
import com.android.business.entity.ChannelInfo;
import com.android.dahua.dhplaycomponent.IMediaPlayListener;
import com.android.dahua.dhplaycomponent.IOperationListener;
import com.android.dahua.dhplaycomponent.ITalkListener;
import com.android.dahua.dhplaycomponent.PlayManagerProxy;
import com.android.dahua.dhplaycomponent.camera.RTCamera.CloudBaseRTCamera;
import com.android.dahua.dhplaycomponent.camera.RTCamera.CloudBaseRTCameraParam;
import com.android.dahua.dhplaycomponent.camera.inner.Camera;
import com.android.dahua.dhplaycomponent.common.Err;
import com.android.dahua.dhplaycomponent.common.PlayStatusType;
import com.android.dahua.dhplaycomponent.windowcomponent.entity.ControlType;
import com.android.dahua.dhplaycomponent.windowcomponent.window.PlayWindow;
import com.bodtec.module.dahuaplayer.R;

import java.util.ArrayList;
import java.util.List;

public class DahuaCloudPlayAct extends AppCompatActivity implements View.OnClickListener {

    public static final String KEY_SN_CODE = "key_sn_code";
    public static final String KEY_TOKEN = "key_token";

    private ImageView mIvBack;
    private View mClTitle;
    private PlayWindow mPlayWindow;
    private TextView mTvChannelName;
    private ImageView mIvFullScreen;

    private static final String TAG = "PlayOnlineActivity";
    public static final int Stream_Assist_Type = 2;        //????????? // auxiliary stream

    public static final int KEY_Handler_Stream_Played = 1;
    public static final int KEY_Handler_First_Frame = 2;
    public static final int KEY_Handler_Net_Error = 3;
    public static final int KEY_Handler_Play_Failed = 4;

    protected PlayManagerProxy mPlayManager;
    private DataAdapterInterface dataAdapterInterface;
    private ChannelInfo mChannelInfo;
    private String mToken;
    private List<ChannelInfo> channelInfoList = new ArrayList<>();
    private String encryptKey = "";
    private int mScreenHeight;
    private int mScreenWidth;
    private int mPermission;
    private boolean isFull = false;
    private boolean isStartTalk = false;
    private boolean isPlaying = false;
    protected String[] recordPath;

    public static void open(Context context, String token, String snCode) {
        Intent intent = new Intent(context, DahuaCloudPlayAct.class);
        intent.putExtra(KEY_TOKEN, token);
        intent.putExtra(KEY_SN_CODE, snCode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dahua_online_player);
        initView();
        initData();
        doSomething();
    }

    protected void initView() {
        mIvBack = findViewById(R.id.iv_back);
        mClTitle = findViewById(R.id.cl_title);
        mPlayWindow = findViewById(R.id.playWindow);
        mTvChannelName = findViewById(R.id.tv_channel_name);
        mIvFullScreen = findViewById(R.id.iv_full_screen);
        mIvBack.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);
    }

    protected void initData() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay()
                .getMetrics(metric);
        mScreenWidth = metric.widthPixels; // ????????????????????????
        mScreenHeight = metric.heightPixels; // ????????????????????????
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void doSomething() {
        mChannelInfo = new ChannelInfo();
        mChannelInfo.setChnSncode(getIntent().getStringExtra(KEY_SN_CODE));
        mToken = getIntent().getStringExtra(KEY_TOKEN);
        dataAdapterInterface = DataAdapterImpl.getInstance();

        mPlayManager = new PlayManagerProxy();
        //????????????????????????????????????4??????????????????16???????????????????????????????????????1
        mPlayManager.init(this, mPlayWindow);
        //??????????????????
        // set play monitor.
        mPlayManager.setOnMediaPlayListener(iMediaPlayListener);
        //????????????????????????
        mPlayManager.setOnOperationListener(iOperationListener);

        // set the intercom monitor.
        mPlayManager.setOnTalkListener(new ITalkListener());

        initCommonWindow();

        channelInfoList.add(mChannelInfo);

        mPlayManager.addCameras(getCameras());
    }

    @SuppressLint("HandlerLeak")
    protected Handler mPlayOnlineHander = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KEY_Handler_Stream_Played:
                    isPlaying = true;
                    int winIndex = (Integer) msg.obj;

                    if (winIndex != mPlayManager.getSelectedWindowIndex()) return;
                    if (channelInfoList != null && channelInfoList.size() == 1) {
                        mPlayManager.maximizeWindow(winIndex);
                        mPlayManager.setEZoomEnable(winIndex, true);
                    }
                    if (mPlayManager.isNeedOpenAudio(winIndex)) openAudio(winIndex);
                    break;
                case KEY_Handler_First_Frame:
                    mPlayManager.playCurpage();
                    break;
                case KEY_Handler_Net_Error:
                case KEY_Handler_Play_Failed:
                    isPlaying = false;
                    winIndex = (Integer) msg.obj;
                    stopPlay(winIndex);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        replay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayManager != null) {
            mPlayManager.unitPlayManager();
            mPlayManager = null;
        }
    }

    private List<Camera> getCameras() {
        List<Camera> cameras = new ArrayList<>();
        if (channelInfoList != null) {
            for (ChannelInfo channelInfo : channelInfoList) {
                cameras.add(getCamera(channelInfo));
            }
        }
        return cameras;
    }

    public boolean openAudio(int winIndex) {

        return mPlayManager.openAudio(winIndex) == Err.OK;
    }

    private Camera getCamera(ChannelInfo channelInfo) {
        //????????????Camera??????
        CloudBaseRTCameraParam cloudBaseCameraParam = new CloudBaseRTCameraParam();
        //??????????????????????????????ID
        cloudBaseCameraParam.setCameraID(channelInfo.getChnSncode());
        //??????????????????
        int mStreamType = ChannelInfo.ChannelStreamType.getValue(channelInfo.getStreamType());
        if (mStreamType > Stream_Assist_Type) mStreamType = Stream_Assist_Type;
        cloudBaseCameraParam.setStreamType(mStreamType - 1);

        cloudBaseCameraParam.setDpRestToken(mToken);
        cloudBaseCameraParam.setServerIp("218.108.24.20");
        cloudBaseCameraParam.setServerPort(8282);

        cloudBaseCameraParam.setRoute(false);
        cloudBaseCameraParam.setUserId("");
        cloudBaseCameraParam.setDomainId("");
        cloudBaseCameraParam.setRegionId("");
        cloudBaseCameraParam.setLocation("");

        cloudBaseCameraParam.setUseHttps(1);

        return new CloudBaseRTCamera(cloudBaseCameraParam);

    }


    /**
     * ????????????
     */
    private void startPlay(int winIndex) {
        mPlayManager.playSingle(winIndex);
    }

    /**
     * ????????????
     */
    private void replay() {
        mPlayManager.playCurpage();
    }

    /**
     * ????????????
     */
    private void stopPlay(int winIndex) {
        mPlayManager.stopSingle(winIndex);
    }

    /**
     * ????????????????????????
     */
    private void stopAll() {
        mPlayManager.stopAll();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.iv_full_screen) {
            switchScreen();
        }
    }

    /**
     * ?????????????????????
     */
    public void initCommonWindow() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mPlayWindow.getLayoutParams();
        lp.width = isFull ? mScreenHeight : mScreenWidth;
        lp.height = isFull ? mScreenWidth : mScreenHeight / 3;
        mPlayWindow.setLayoutParams(lp);
        mPlayWindow.forceLayout(lp.width, lp.height);
    }

    /**
     * ???????????????
     */
    public void switchScreen() {
        if (isFull) {
            isFull = false;
            mClTitle.setVisibility(View.VISIBLE);
            mIvFullScreen.setImageResource(R.drawable.icon_full_screen);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            isFull = true;
            mClTitle.setVisibility(View.GONE);
            mIvFullScreen.setImageResource(R.drawable.icon_exit_full_screen);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        initCommonWindow();
    }


    @Override
    public void onBackPressed() {
        if (isFull) {
            switchScreen();
        } else {
            this.finish();
        }
    }


    private IMediaPlayListener iMediaPlayListener = new IMediaPlayListener() {
        @Override
        public void onPlayeStatusCallback(int winIndex, PlayStatusType type, int code) {
            Message msg = Message.obtain();
            msg.obj = winIndex;
            if (type == PlayStatusType.eStreamPlayed) {
                msg.what = KEY_Handler_Stream_Played;
                if (mPlayOnlineHander != null) mPlayOnlineHander.sendMessage(msg);
            } else if (type == PlayStatusType.ePlayFirstFrame) {
                msg.what = KEY_Handler_First_Frame;
                if (mPlayOnlineHander != null) mPlayOnlineHander.sendMessage(msg);
            } else if (type == PlayStatusType.eNetworkaAbort) {
                msg.what = KEY_Handler_Net_Error;
                if (mPlayOnlineHander != null) mPlayOnlineHander.sendMessage(msg);
            } else if (type == PlayStatusType.ePlayFailed) {
                msg.what = KEY_Handler_Play_Failed;
                if (mPlayOnlineHander != null) mPlayOnlineHander.sendMessage(msg);
            }
        }
    };

    private IOperationListener iOperationListener = new IOperationListener() {
        @Override
        public void onWindowSelected(int position) {
            Log.d(TAG, "onWindowSelected" + position);
        }

        @Override
        public void onPageChange(int newPage, int prePage, int type) {
            Log.d(TAG, "onPageChange" + newPage + prePage + type);
            if (type == 0) {
                if (mPlayManager.getPageCellNumber() == 1) {
                    mPlayManager.setEZoomEnable(prePage, false);
                    mPlayManager.setEZoomEnable(newPage, true);
                }
            }
        }

        @Override
        public void onSplitNumber(int nCurCellNumber, int nCurPage, int nPreCellNumber, int nPrePage) {
            Log.d(TAG, "onSplitNumber" + nCurCellNumber);
        }

        @Override
        public void onControlClick(int nWinIndex, ControlType type) {
            Log.d(TAG, "onControlClick" + type);
            if (type == ControlType.Control_Reflash) {
                startPlay(nWinIndex);
            }
        }

        @Override
        public void onSelectWinIndexChange(int newWinIndex, int oldWinIndex) {
            Log.d(TAG, "onSelectWinIndexChange:" + newWinIndex + ":" + oldWinIndex);
            if (!mPlayManager.hasTalking()) {
                if (mPlayManager.isOpenAudio(oldWinIndex)) {
                    mPlayManager.closeAudio(oldWinIndex);
                    mPlayManager.setNeedOpenAudio(oldWinIndex, true);
                }

                if (mPlayManager.isPlaying(newWinIndex) && mPlayManager.isNeedOpenAudio(newWinIndex))
                    mPlayManager.openAudio(newWinIndex);
            }
        }

        @Override
        public void onWindowDBClick(int winIndex, int type) {
            Log.d(TAG, "onWindowDBClick" + type + " winIndex:" + winIndex + " isWindowMax:" + mPlayManager.isWindowMax(winIndex));
            if (mPlayManager.isOpenPTZ(winIndex)) {
                if (mPlayManager.setPTZEnable(winIndex, false) == Err.OK) {
                    mPlayManager.setResumeFlag(winIndex, false);
                }
            }
            mPlayManager.setEZoomEnable(winIndex, type == 0);
        }

        @Override
        public void onMoveWindowBegin(int winIndex) {
            Log.d(TAG, "onMoveWindowBegin");
        }

        @Override
        public void onMovingWindow(int winIndex, float x, float y) {
            Log.d(TAG, "onMovingWindow x:" + x + " y:" + y);
        }

        @Override
        public boolean onMoveWindowEnd(int winIndex, float x, float y) {
            Log.d(TAG, "onMoveWindowEnd x:" + x + " y:" + y);
            return false;
        }
    };

}
