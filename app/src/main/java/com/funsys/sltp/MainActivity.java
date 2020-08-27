package com.funsys.sltp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    AlertDialog.Builder builder;
    AlertDialog videoDialog;
    VideoView videoView;
    ProgressDialog oDialog;

    Handler handler;

    String videoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //버튼 클릭시 촬영
        Button button = findViewById(R.id.captureButton);
        button.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        startActivityForResult(intent, 1);
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1) {
            //통신해서 라즈베리파이로 동영상 전송하기
            Uri vid = data.getData();
            videoPath = getRealPathFromURI(vid);

            ClientThread thread = new ClientThread(new File(videoPath));
            thread.start();

            //라즈베리파이에서 결과가 전달 되기전까지 dialog 띄우기
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
//        Handler mHandler = new Handler();
//        mHandler.postDelayed(new Runnable() {
//            public void run() {
//                videoDialog.dismiss();
//                oDialog.dismiss();
//            }
//        }, 3000);

    }

    class ClientThread extends Thread {

        File file;

        public ClientThread(File file) {
            this.file = file;
        }

        public void run() {
            Log.d("TCP", "스레드 시작");
            String host = "172.30.1.31";
            int port = 9999;

            try {
                Log.d("TCP", "츄라이 츄라이");
                Socket socket = new Socket(host, port);

                DataInputStream dis = new DataInputStream(new FileInputStream(file));
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                byte[] buf = new byte[1024];
                long totalReadBytes = 0;
                int readBytes;

                while ((readBytes = dis.read(buf)) > 0) {
                    dos.write(buf, 0, readBytes);
                    totalReadBytes += (long)readBytes;
                }
                dos.close();

            } catch (IOException e) {
                Log.d("TCP", "don't send a message");
                e.printStackTrace();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}