package com.example.patrickgross.retrosquash;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
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

    @Override
    protected void onStop(){
        super.onStop();
        while(true){
            squashCourtView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        squashCourtView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        squashCourtView.resume();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            squashCourtView.pause();
            finish();
            return true;
        }
        return false;

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

        public void controlFPS(){
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 15 - timeThisFrame;
            if(timeThisFrame > 0){
                fps = (int)(1000/timeThisFrame);
            }
            if (timeToSleep > 0){
                try {
                    ourThread.sleep(timeToSleep);
                } catch(InterruptedException e){}
            }
        }

        public void pause(){
            playingSquash = false;
            try{
                ourThread.join();
            } catch (InterruptedException e){}
        }

        public void resume() {
            playingSquash = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){
            switch(motionEvent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    if (motionEvent.getX() >= screenWidth / 2){
                        racketIsMovingRight = true;
                        racketIsMovingLeft = false;
                    } else{
                        racketIsMovingRight = false;
                        racketIsMovingLeft = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    racketIsMovingLeft = false;
                    racketIsMovingRight = false;
                    break;
            }
            return true;
        }

        public void drawCourt(){
            if (ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(45);
                canvas.drawText("Score: " + score + " Lives: " + lives + " FPS: " + fps, 20, 40, paint);
                canvas.drawRect(racketPosition.x - (racketWidth / 2), racketPosition.y - (racketHeight / 2),
                        racketPosition.x + (racketWidth / 2), racketPosition.y + racketHeight, paint);
                canvas.drawRect(ballPosition.x, ballPosition.y, ballPosition.x + ballWidth, ballPosition.y + ballWidth, paint);
                ourHolder.unlockCanvasAndPost(canvas);
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
            }

            if(ballPosition.x < 0){
                ballIsMovingLeft = false;
                ballIsMovingRight = true;
            }

            if(ballPosition.y > screenHeight - ballWidth ){
                lives -= 1;
                if (lives == 0) {
                    lives = 3;
                    score = 0;
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

            if(ballPosition.y <= 0){
                ballIsMovingDown = true;
                ballIsMovingUp = false;
                ballPosition.y = 1;
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

            if (ballPosition.y + ballWidth >= (racketPosition.y - racketHeight / 2 )){
                int halfRacket = racketWidth/2;
                if (ballPosition.x + ballWidth > (racketPosition.x - halfRacket)
                        && ballPosition.x - ballWidth < (racketPosition.x + halfRacket)){
                    score++;
                    ballIsMovingUp = true;
                    ballIsMovingDown = false;

                    if (ballPosition.x > racketPosition.x) {
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                    } else {
                        ballIsMovingRight = false;
                        ballIsMovingLeft = true;
                    }
                }
            }
        }
    }
}
