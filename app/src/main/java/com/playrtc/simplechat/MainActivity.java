package com.playrtc.simplechat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
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
import com.sktelecom.playrtc.PlayRTC.PlayRTCAudioType;
import com.sktelecom.playrtc.util.android.PlayRTCAudioManager;
import com.sktelecom.playrtc.util.android.PlayRTCAudioManager.AudioDevice;

import org.json.JSONObject;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    // Please change this key for your own project.
    private final String T_DEVELOPERS_PROJECT_KEY = "60ba608a-e228-4530-8711-fa38004719c1";

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

    private PlayRTCAudioManager pAudioManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createPlayRTCObserverInstance();

        createPlayRTCInstance();

        setToolbar();

        setFragmentNavigationDrawer();

        setOnClickEventListenerToButton();

        setAudioManager();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Make the videoView at the onWindowFocusChanged time.
        if (hasFocus && this.localView == null) {
            createVideoView();
        }
    }

    @Override
    protected void onDestroy() {
        if (pAudioManager != null) {
            pAudioManager.close();
            pAudioManager = null;
        }
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
            createCloseAlertDialog();
            closeAlertDialog.show();
        }
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
            }

            @Override
            public void onOtherDisconnectChannel(final PlayRTC obj, final String peerId, final String peerUserId) {
                remoteView.hide(0);

                // Delete channel and call onDisconnectChannel.
                playrtc.deleteChannel();
            }
        };
    }

    private void createPlayRTCInstance() {
        try {
            PlayRTCSettings settings = createPlayRTCConfiguration();
            playrtc = PlayRTCFactory.newInstance(settings, playrtcObserver);
        } catch (UnsupportedPlatformVersionException e) {
            e.printStackTrace();
        } catch (RequiredParameterMissingException e) {
            e.printStackTrace();
        }
    }

    private PlayRTCSettings createPlayRTCConfiguration() {
        PlayRTCSettings settings = new PlayRTCSettings();

        // PlayRTC instance have to get the application context.
        settings.android.setContext(getApplicationContext());

        // T Developers Project Key.
        settings.setTDCProjectId(T_DEVELOPERS_PROJECT_KEY);

        settings.setAudioEnable(true);
        settings.setVideoEnable(true);
        settings.video.setFrontCameraEnable(true);
        settings.video.setBackCameraEnable(false);
        settings.setDataEnable(false);

        // File logging setting
        File logPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + getPackageName() + "/files/log/");
        if (logPath.exists() == false) {
            logPath.mkdirs();
        }
        settings.log.file.setLogPath(logPath.getAbsolutePath());
        settings.log.file.setLevel(PlayRTCSettings.DEBUG);

        return settings;
    }

    private void createVideoView() {
        // Set the videoViewGroup which is contained local and remote video views.
        RelativeLayout myVideoViewGroup = (RelativeLayout) findViewById(R.id.video_view_group);

        if (localView != null) {
            return;
        }

        // Give my screen size to child view.
        Point myViewDimensions = new Point();
        myViewDimensions.x = myVideoViewGroup.getWidth();
        myViewDimensions.y = myVideoViewGroup.getHeight();

        if (remoteView == null) {
            createRemoteVideoView(myViewDimensions, myVideoViewGroup);
        }

        if (localView == null) {
            createLocalVideoView(myViewDimensions, myVideoViewGroup);
        }
    }

    private void createLocalVideoView(final Point parentViewDimensions, RelativeLayout parentVideoViewGroup) {
        if (localView == null) {
            // Create the video size variable.
            Point myVideoSize = new Point();
            myVideoSize.x = (int) (parentViewDimensions.x * 0.3);
            myVideoSize.y = (int) (parentViewDimensions.y * 0.3);

            // Create the view parameter.
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(myVideoSize.x, myVideoSize.y);
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            param.setMargins(30, 30, 30, 30);

            // Create the localViews.
            localView = new PlayRTCVideoView(parentVideoViewGroup.getContext(), myVideoSize);

            // Set the layout parameters.
            localView.setLayoutParams(param);

            // Add the view to the parentVideoViewGrop.
            parentVideoViewGroup.addView(localView);

            // Set the z-order.
            localView.setZOrderMediaOverlay(true);
        }
    }

    private void createRemoteVideoView(final Point parentViewDimensions, RelativeLayout parentVideoViewGroup) {
        if (remoteView == null) {
            // Create the video size variable.
            Point myVideoSize = new Point();
            myVideoSize.x = parentViewDimensions.x;
            myVideoSize.y = parentViewDimensions.y;

            // Create the view parameters.
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

            // Create the remoteView.
            remoteView = new PlayRTCVideoView(parentVideoViewGroup.getContext(), myVideoSize);

            // Set the layout parameters.
            remoteView.setLayoutParams(param);

            // Add the view to the videoViewGroup.
            parentVideoViewGroup.addView(remoteView);
        }
    }

    private void setAudioManager() {
        pAudioManager = PlayRTCAudioManager.create(this, new Runnable() {
            @Override
            public void run() {

                // Search audio devices and send to PlayRTCV
                AudioDevice audioDivece = pAudioManager.getSelectedAudioDevice();
                if (playrtc != null) {
                    if (audioDivece == AudioDevice.WIRED_HEADSET) {
                        // ear-Phone
                        playrtc.notificationAudioType(PlayRTCAudioType.AudioReceiver);
                    } else if (audioDivece == AudioDevice.SPEAKER_PHONE) {
                        // Speaker-Phone
                        playrtc.notificationAudioType(PlayRTCAudioType.AudioSpeaker);
                    } else if (audioDivece == AudioDevice.EARPIECE) {
                        // Ear-Speakerphone
                        playrtc.notificationAudioType(PlayRTCAudioType.AudioEarphone);
                    } else if (audioDivece == AudioDevice.BLUETOOTH) {
                        // Bluetooth
                        playrtc.notificationAudioType(PlayRTCAudioType.AudioBluetooth);
                    }
                }
            }
        });

        // PlayRTCAudioManager run
        pAudioManager.init();
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
                playrtc.deleteChannel();
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
