package com.bytedance.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.Constraints;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MyPlayer extends AppCompatActivity {
    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SurfaceHolder holder;
    private  SeekBar seekbar;
    private Boolean isChanging = false;
    private Timer timer;
    private int position;
    private Button turn;
    private Boolean isPortrait = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MediaPlayer");

        setContentView(R.layout.activity_myplayer);
        seekbar = findViewById(R.id.seekBar);
        surfaceView = findViewById(R.id.surfaceView);
        turn = findViewById(R.id.button2);
        player = new MediaPlayer();

        try {
            if (getIntent() != null && getIntent().getData() != null) {
                player.setDataSource(this, getIntent().getData());
            } else {
                player.setDataSource(getResources().openRawResourceFd(R.raw.haha));
            }
            holder = surfaceView.getHolder();
            holder.addCallback(new PlayerCallBack());
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 自动播放
                    player.seekTo(position,MediaPlayer.SEEK_CLOSEST);
                    player.start();
                    player.setLooping(true);
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (!isChanging) {
                                if (player != null) {
                                    if (player.isPlaying()) {
                                        seekbar.setProgress(player.getCurrentPosition());
                                    }
                                }
                            }
                        }
                    }, 0, 1000);
                }
            });
//            if (savedInstanceState != null) {
//                // 得到进度
//                Log.d("AAAAAA","00:"+savedInstanceState);
//                int ss = savedInstanceState.getInt("aa");
//                // 接着播放
//                Log.d("AAAAAA","ss:"+ss);
//                player.seekTo(ss);
//            }
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    System.out.println(percent);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        initSeekBar();

        findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
            }
        });

        findViewById(R.id.buttonPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });
        turn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPortrait){
                    isPortrait = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                }
                else{
                    isPortrait = true;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
            }
        });
    }

//    @Override
//    protected void onConfigurationChanged(Configuration newConfig){
//        super.onConfigurationChanged(newConfig);
//        if (newConfig.orientation == this.getResources().getConfiguration().ORIENTATION_PORTRAIT) {
//            isPortrait = true;
//
//        }
//    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        Log.d("AAAAAA","00:");
//        // TODO Auto-generated method stub
//        super.onSaveInstanceState(outState);
//        // 记录当前播放进度
//        Log.d("AAAAAA","00");
//        outState.putInt("aa", player.getCurrentPosition());
//    }

    private void initSeekBar() {
        final int duration = player.getDuration();
        seekbar.setMax(duration);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                isChanging= true;
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onStopTrackingTouch(SeekBar seekBar) {
                isChanging = false;
                player.seekTo(seekBar.getProgress(),MediaPlayer.SEEK_CLOSEST);//在当前位置播放
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            position = player.getCurrentPosition();
            player.stop();
            player.reset();
            player.release();
            player = null;
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    private class PlayerCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (player != null) {
            position = player.getCurrentPosition();
            player.stop();
            player.reset();
            player.release();
            player = null;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", position);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt("position");
        } else {
            position = 0;
        }
    }
}
