package com.playrtc.simplechat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sktelecom.playrtc.PlayRTC;
import com.sktelecom.playrtc.PlayRTCFactory;
import com.sktelecom.playrtc.config.PlayRTCSettings;
import com.sktelecom.playrtc.exception.RequiredConfigMissingException;
import com.sktelecom.playrtc.exception.RequiredParameterMissingException;
import com.sktelecom.playrtc.exception.UnsupportedPlatformVersionException;
import com.sktelecom.playrtc.observer.PlayRTCObserver;
import com.sktelecom.playrtc.stream.PlayRTCMedia;
import com.sktelecom.playrtc.util.ui.PlayRTCVideoView;

import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private AlertDialog closeAlertDialog;

    private PlayRTC playrtc;
    private PlayRTCObserver playrtcObserver;

    private boolean isCloseActivity = false;
    private boolean isChannelConnected = false;
    private PlayRTCVideoView localView;
    private PlayRTCVideoView remoteView;
    private PlayRTCMedia localMedia;
    private PlayRTCMedia remoteMedia;
    private String channelId;

    private RelativeLayout videoViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.createPlayRTCObserverInstance();

        this.createPlayRTCInstance();

        this.setPlayRTCConfiguration();

        this.setToolbar();

        this.setFragmentNavigationDrawer();

        this.setOnClickEventListenerToButton();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Make the videoView at the onWindowFocusChanged time.
        if (hasFocus && this.localView == null) {
            this.createVideoView();
        }
    }

    @Override
    protected void onDestroy() {
        playrtc = null;
        playrtcObserver = null;
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isCloseActivity) {
            super.onBackPressed();
        } else {
            this.createCloseAlertDialog();
            closeAlertDialog.show();
        }

        //
        //moveTaskToBack(true);
//        finish();
//        System.exit(0);
//        android.os.Process.killProcess(android.os.Process.myPid());
//        ActivityManager am = (ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE);
//        am.restartPackage(getPackageName());
//        am.killBackgroundProcesses(getPackageName());
    }

    private void createPlayRTCObserverInstance() {
        playrtcObserver = new PlayRTCObserver() {
            @Override
            public void onConnectChannel(final PlayRTC obj, final String channelId, final String channelCreateReason) {
                isChannelConnected = true;

                // Fill the channelId to the channel_id TextView.
                TextView channelIdTextView = (TextView) findViewById(R.id.channel_id);
                channelIdTextView.setText(channelId);
            }

            @Override
            public void onAddLocalStream(final PlayRTC obj, final PlayRTCMedia playRTCMedia) {
                long delayTime = 0;

                localMedia = playRTCMedia;
                localView.show(delayTime);

                // Link the media stream to the view.
                playRTCMedia.setVideoRenderer(localView.getVideoRenderer());
            }

            @Override
            public void onAddRemoteStream(final PlayRTC obj, final String peerId, final String peerUserId, final PlayRTCMedia playRTCMedia) {
                long delayTime = 0;

                remoteMedia = playRTCMedia;
                remoteView.show(delayTime);

                // Link the media stream to the view.
                playRTCMedia.setVideoRenderer(remoteView.getVideoRenderer());

            }

            @Override
            public void onDisconnectChannel(final PlayRTC obj, final String disconnectReason) {
                long delayTime = 0;

                isChannelConnected = false;
                remoteView.hide(delayTime);
                localView.hide(delayTime);

                // Clean the channel_id TextView.
                TextView ChannelIdTextView = (TextView) findViewById(R.id.channel_id);
                ChannelIdTextView.setText(null);

                // Create PlayRTC instance again.
                // Because at the disconnect moment, the PlayRTC instance has removed.
                createPlayRTCInstance();
                setPlayRTCConfiguration();
            }

            @Override
            public void onOtherDisconnectChannel(final PlayRTC obj, final String peerId, final String peerUserId) {
                remoteView.hide(0);

                // Delete channel and call onDisconnectChannel.
                // Because there is a bug which is killed app. If remote peer reconnect after disconnect channel.
                // This is going to patch at 2.0.1.
                playrtc.deleteChannel();
            }
        };
    }

    private void createPlayRTCInstance() {
        try {
            playrtc = PlayRTCFactory.newInstance(playrtcObserver);
        } catch (UnsupportedPlatformVersionException e) {
            e.printStackTrace();
        } catch (RequiredParameterMissingException e) {
            e.printStackTrace();
        }
    }

    private void setPlayRTCConfiguration() {
        PlayRTCSettings settings = playrtc.getSettings();

        // PlayRTC instance have to get the application context.
        settings.android.setContext(getApplicationContext());

        // T Developers Project Key.
        settings.setTDCProjectId("60ba608a-e228-4530-8711-fa38004719c1");

        settings.setAudioEnable(true);
        settings.setVideoEnable(true);
        settings.video.setFrontCameraEnable(true);
        settings.video.setBackCameraEnable(true);
        settings.setDataEnable(false);
        settings.log.console.setLevel(PlayRTCSettings.DEBUG);
    }

    private void createVideoView() {
        // Set the videoViewGroup which is contained local and remote video views.
        videoViewGroup = (RelativeLayout) findViewById(R.id.video_view_group);

        if (localView != null) {
            return;
        }

        // Set the size.
        Point screenDimensions = new Point();
        screenDimensions.x = videoViewGroup.getWidth();
        screenDimensions.y = videoViewGroup.getHeight();

        if (remoteView == null) {
            createRemoteVideoView(screenDimensions, videoViewGroup);
        }

        if (localView == null) {
            createLocalVideoView(screenDimensions, videoViewGroup);
        }
    }

    private void createLocalVideoView(final Point screenDimensions, RelativeLayout videoViewGroup) {
        if (localView == null) {
            // Set the size.
            Point displaySize = new Point();
            displaySize.x = (int) (screenDimensions.x * 0.3);
            displaySize.y = (int) (screenDimensions.y * 0.3);

            // Set the position.
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(displaySize.x, displaySize.y);
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            param.setMargins(30, 30, 30, 30);

            // Create the localView.
            localView = new PlayRTCVideoView(videoViewGroup.getContext(), displaySize);

            // Set the layout parameters and add the view to the videoViewGrop.
            localView.setLayoutParams(param);
            videoViewGroup.addView(localView);

            // Set the z-order.
            localView.setZOrderMediaOverlay(true);
        }
    }

    private void createRemoteVideoView(final Point screenDimensions, RelativeLayout viewGroup) {
        if (remoteView == null) {
            // Set the size.
            Point displaySize = new Point();
            displaySize.x = (int) (screenDimensions.x);
            displaySize.y = (int) (screenDimensions.y);

            // Set the position.
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

            // Create the remoteView.
            remoteView = new PlayRTCVideoView(viewGroup.getContext(), displaySize);

            // Set the layout parameters and add the view to the videoViewGroup.
            remoteView.setLayoutParams(param);
            viewGroup.addView(remoteView);
        }
    }

    private void setOnClickEventListenerToButton() {
        // Add a create channel event listener.
        Button createButton = (Button) findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    playrtc.createChannel(new JSONObject());
                } catch (RequiredConfigMissingException e) {
                    e.printStackTrace();
                }
            }
        });

        // Add a connect channel event listener.
        Button connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    TextView ChannelIdInput = (TextView) findViewById(R.id.connect_channel_id);
                    channelId = ChannelIdInput.getText().toString();
                    playrtc.connectChannel(channelId, new JSONObject());
                } catch (RequiredConfigMissingException e) {
                    e.printStackTrace();
                }
            }
        });

        // Add a exit channel event listener.
        Button exitButton = (Button) findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // null means my user id.
                playrtc.disconnectChannel(null);
            }
        });
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
    }

    private void setFragmentNavigationDrawer() {
        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
    }

    private void createCloseAlertDialog() {
        // Create the Alert Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Set a Alert.
        alertDialogBuilder.setTitle(R.string.alert_title);
        alertDialogBuilder.setMessage(R.string.alert_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                dialogInterface.dismiss();
                if (isChannelConnected == true) {
                    isCloseActivity = false;

                    // null means my user id.
                    playrtc.disconnectChannel(null);
                } else {
                    isCloseActivity = true;
                    onBackPressed();
                }
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                dialogInterface.dismiss();
                isCloseActivity = false;
            }
        });

        // Create the Alert.
        closeAlertDialog = alertDialogBuilder.create();
    }
}
