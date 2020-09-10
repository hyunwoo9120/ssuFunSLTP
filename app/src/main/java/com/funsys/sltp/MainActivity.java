package com.funsys.sltp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    String videoPath = "";
    String receivedMessage = "";
    File file;
    long fileSize = 0;

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

            //동영상 uri를 절대경로로 바꾸기
            Uri vid = data.getData();
            videoPath = getRealPathFromURI(vid);
            file = new File(videoPath);
            fileSize = file.length();
            Client clientSocket = new Client(data, this);
            clientSocket.execute();
         }
    }

    public class Client extends AsyncTask{
        Intent data;
        AlertDialog.Builder builder;
        AlertDialog videoDialog;
        VideoView videoView;
        ProgressDialog oDialog;

        Client(Intent data, Context context){
            this.data = data;
            builder = new AlertDialog.Builder(context);
            videoView = new VideoView(context);
            oDialog = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog);
        }
        @Override //라즈베리파이에서 결과가 전달 되기전까지 dialog 띄우기
        protected void onPreExecute() {
            super.onPreExecute();

            videoView.setVideoURI(data.getData());
            videoView.start();
            builder.setView(videoView);
            videoDialog = builder.create();
            videoDialog.show();

            oDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            oDialog.setMessage("번역 중입니다.\n잠시만 기다려 주세요.");
            oDialog.setCancelable(false);

            oDialog.show();
        }

        @Override //통신해서 라즈베리파이로 동영상 전송하기
        protected Object doInBackground(Object[] objects) {
            Log.d("TCP", "스레드 시작");
            String host = "172.30.1.31";
            int port = 5172;

            try {
                Log.d("TCP", "츄라이 츄라이");
                Socket socket = new Socket(host, port);
                if (!socket.isConnected()) {
                    System.out.println("Socket Connect Error.");
                    System.exit(0);
                }

                DataInputStream fis = new DataInputStream(new FileInputStream(file));
                BufferedReader dis = new BufferedReader(new InputStreamReader(socket.getInputStream(), "EUC_KR"));
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                byte[] buf = new byte[1024];
                long totalReadBytes = 0;
                int readBytes;

                dos.writeLong(fileSize);
                dos.flush();

                while ((readBytes = fis.read(buf)) > 0) {
                    dos.write(buf, 0, readBytes);
                    totalReadBytes += (long) readBytes;
                    System.out.println("In progress: " + totalReadBytes + "/"
                            + fileSize + " Byte(s) ("
                            + (totalReadBytes * 100 / fileSize) + " %)");
                }
                System.out.println("File transfer completed.");
                fis.close();

                receivedMessage = dis.readLine();
                Log.d("TCP", "receive a message: " + receivedMessage);
                dis.close();
                dos.close();

                socket.close();
            } catch (IOException e) {
                Log.d("TCP", "don't send a message");
                e.printStackTrace();
            }
            return null;
        }

        @Override //dialog 종료하기
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            ((TextView)findViewById(R.id.text)).setText(receivedMessage);
            videoDialog.dismiss();
            oDialog.dismiss();
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