package com.funsys.sltp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {
    AlertDialog.Builder builder;
    AlertDialog videoDialog;
    VideoView videoView;
    ProgressDialog oDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //통신해서 라즈베리파이로 동영상 전송해야함

        //라즈베리파이에서 결과가 전달 되기전까지 dialog 띄우기
        if (resultCode == RESULT_OK && requestCode == 1) {
            builder = new AlertDialog.Builder(this);
            videoView = new VideoView(this);
            videoView.setVideoURI(data.getData());
            videoView.start();
            builder.setView(videoView);
            videoDialog = builder.create();
            videoDialog.show();

            oDialog = new ProgressDialog(this,
                    android.R.style.Theme_DeviceDefault_Light_Dialog);
            oDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            oDialog.setMessage("번역 중입니다.\n잠시만 기다려 주세요.");
            oDialog.setCancelable(false);

            oDialog.show();
        }

        /*라즈베리파이에서 결과가 오면 dialog 를 지우고 번역결과를 출력해야 함*/
        //현재는 3초 후에 dialog 를 dismiss 한다.
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable()  {
            public void run() {
                videoDialog.dismiss();
                oDialog.dismiss();
            }
        }, 3000);

    }
}