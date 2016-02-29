package com.example.patrickgross.retrosquash;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;


public class MainActivity extends Activity {

    Canvas canvas;
    SquashCourtView squashCourtView;

    //Sound
    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    //Display
    Display display;
    Point size;
    int screenWidth;
    int screenHeight;

    //Racket Variables
    int racketWidth;
    int racketHeight;
    Point racketPosition;

    //Ball Variables
    Point ballPosition;
    int ballWidth;
    boolean ballIsMovingLeft;
    boolean ballIsMovingRight;
    boolean ballIsMovingUp;
    boolean ballIsMovingDown;

    boolean racketIsMovingLeft;
    boolean racketIsMovingRight;

    //stat variables
    long lastFrameTime;
    int fps;
    int score;
    int lives;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        squashCourtView = new SquashCourtView(this);
        setContentView(squashCourtView);

        //Set up sound
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try{
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;
            descriptor = assetManager.openFd("Sample1.ogg");
            sample1 = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("Sample2.ogg");
            sample2 = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("Sample3.ogg");
            sample3 = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("Sample4.ogg");
            sample4 = soundPool.load(descriptor, 0);
        } catch(Exception e){}

        //Set up variables
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        racketPosition = new Point();
        racketPosition.x = screenWidth / 2;
        racketPosition.y = screenHeight - 20;
        racketWidth = screenWidth / 8;
        racketHeight = 10;

        ballWidth = screenWidth / 35;
        ballPosition = new Point();
        ballPosition.x = screenWidth / 2;
        ballPosition.y = 1 + ballWidth;

        lives = 3;


    }

    class SquashCourtView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSquash;
        Paint paint;

        public SquashCourtView(Context context){
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            ballIsMovingDown = true;

            Random randomNumber = new Random();
            int ballDirection = randomNumber.nextInt(3);

            switch(ballDirection){
                case 0:
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;
                case 1:
                    ballIsMovingRight = true;
                    ballIsMovingLeft = false;
                    break;
                case 2:
                    ballIsMovingRight = false;
                    ballIsMovingLeft = false;
                    break;
            }
        }

        @Override
        public void run() {
            while (playingSquash) {
                updateCourt();
                drawCourt();
                controlFPS();
            }
        }

        public void updateCourt(){
            if (racketIsMovingRight) {
                racketPosition.x = racketPosition.x + 10;
            }

            if (racketIsMovingLeft) {
                racketPosition.x = racketPosition.x - 10;
            }

            if(ballPosition.x + ballWidth > screenWidth){
                ballIsMovingLeft = true;
                ballIsMovingRight = false;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            if(ballPosition.x < 0){
                ballIsMovingLeft = false;
                ballIsMovingRight = true;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            if(ballPosition.y > screenHeight - ballWidth ){
                lives -= 1;
                if (lives == 0) {
                    lives = 3;
                    score = 0;
                    soundPool.play(sample4, 1, 1, 0, 0, 1);
                }
                ballPosition.y = 1 + ballWidth;
                Random randomNumber = new Random();
                int startX = randomNumber.nextInt(screenWidth - ballWidth) + 1;
                ballPosition.y = startX + ballWidth;

                int ballDirection = randomNumber.nextInt(3);

                switch(ballDirection){
                    case 0:
                        ballIsMovingLeft = true;
                        ballIsMovingRight = false;
                        break;
                    case 1:
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                        break;
                    case 2:
                        ballIsMovingRight = false;
                        ballIsMovingLeft = false;
                        break;
                }
            }

            if(ballPosition.y < = 0){
                ballIsMovingDown = true;
                ballIsMovingUp = false;
                ballPosition.y = 1;
                soundPool.play(sample2, 1, 1, 0, 0, 1);
            }

            if (ballIsMovingUp) {
                ballPosition.y -= 10;
            }

            if (ballIsMovingDown) {
                ballPosition.y += 6;
            }

            if (ballIsMovingLeft) {
                ballPosition.x -= 12;
            }

            if (ballIsMovingRight) {
                ballPosition.x += 12;
            }
        }
    }
}
