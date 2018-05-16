package com.zgkxzx.modbustest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import android.widget.MediaController;
import android.widget.VideoView;
import android.media.MediaPlayer;
import com.zgkxzx.modbus4And.requset.ModbusParam;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import java.util.Arrays;

import java.util.Timer;
import java.util.TimerTask;
import java.net.InetAddress;


public class MainActivity extends Activity {

    private final static String TAG = MainActivity.class.getSimpleName();

    // modbus registers
    private long theHEARTBEATvalue = 0;
    private int theLEDvalue = 0;
    private int theBUTTONvalue = 0;

    private Timer modbusRefreshTimer;
    private TimerTask modbusPolling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modbusInit();

        modbusRefreshTimer = new Timer();
        modbusPolling = new TimerTask() {
            @Override
            public void run() {

                try {
                    InetAddress inet = InetAddress.getByName("192.168.5.211");
                    if (inet.isReachable(500)) {
                        // Log.i(TAG, "ping OK");
                    } else {
                        Log.i(TAG, "ping NG");
                        return;
                    }
                } catch (Exception ignored) {
                    Log.i(TAG, "cannot ping");
                }
                ;

                try {
                    ModbusReq.getInstance().readHoldingRegisters(new OnRequestBack<short[]>() {
                        @Override
                        public void onSuccess(short[] data) {
                            // Log.d(TAG, "readHoldingRegisters onSuccess " + Arrays.toString(data));
                            long d0 = data[0], d1 = data[1];
                            if (d0 >= 0) {
                                theHEARTBEATvalue = d0;
                            } else {
                                theHEARTBEATvalue = 65536 + d0;
                            }
                            if (d1 >= 0) {
                                theHEARTBEATvalue += 65536 * d1;
                            } else {
                                theHEARTBEATvalue += 65536 * (65536 + d1);
                            }
                            theLEDvalue = data[2];
                            theBUTTONvalue = data[3];

                            Button theHEARTBEATbutton = (Button) findViewById(R.id.theHEARTBEAT);
                            Button theLEDbutton = (Button) findViewById(R.id.theLED);
                            Button theBUTTONbutton = (Button) findViewById(R.id.theBUTTON);
                            Button theBUTTONtitle = (Button) findViewById(R.id.buttonTitle);

                            theHEARTBEATbutton.setText(Long.toString(theHEARTBEATvalue));
                            // theBUTTONbutton.setBackgroundColor(Color.rgb(255, 170, 0));
                            theHEARTBEATbutton.setBackgroundResource(R.color.colorMustard);
                            theBUTTONtitle.setBackgroundResource(R.color.colorSkyBlue);

                            if (theBUTTONvalue != 0) {
                                theBUTTONbutton.setText("red ON");
                                // theBUTTONbutton.setBackgroundColor(Color.rgb(255, 0, 0));
                                theBUTTONbutton.setBackgroundResource(R.color.colorRed);
                            } else {
                                theBUTTONbutton.setText("red off");
                                // theBUTTONbutton.setBackgroundColor(Color.rgb(127, 127, 127));
                                theBUTTONbutton.setBackgroundResource(R.color.colorGray);
                            }
                            if (theLEDvalue != 0) {
                                theLEDbutton.setText("blu ON");
                                // theLEDbutton.setBackgroundColor(Color.BLUE);
                                theLEDbutton.setBackgroundResource(R.color.colorLgtBlue);
                            } else {
                                theLEDbutton.setText("blu off");
                                // theLEDbutton.setBackgroundColor(Color.rgb(127, 127, 127));
                                theLEDbutton.setBackgroundResource(R.color.colorGray);
                            }

                            ViewGroup vg = (ViewGroup) findViewById(R.id.mainLayout);
                            vg.postInvalidate();
                        }

                        @Override
                        public void onFailed(String msg) {
                            Log.e(TAG, "readHoldingRegisters onFailed " + msg);

                            ViewGroup vg = (ViewGroup) findViewById(R.id.mainLayout);
                            vg.postInvalidate();
//                            thePollingStatus.postInvalidate();

                        }
                    }, 1, 0, 4);
                } catch (Exception ignored) {
                    Log.i(TAG, "cannot readHoldingRegisters");
                }
            }
        };
        modbusRefreshTimer.schedule(modbusPolling, 500, 500);

        final VideoView videoView = (VideoView)findViewById(R.id.videoView);
        videoView.setVideoPath("http://192.168.5.211/webmi/video.mp4");
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                videoView.start(); //need to make transition seamless.
            }
        });
        videoView.start();
    }

    private void modbusInit() {

        ModbusReq.getInstance().setParam(new ModbusParam()
                .setHost("192.168.5.211")
                .setPort(502)
                .setEncapsulated(false)
                .setKeepAlive(true)
                .setTimeout(2000)
                .setRetries(0))
                .init(new OnRequestBack<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "onSuccess " + s);
            }

            @Override
            public void onFailed(String msg) {
                Log.d(TAG, "onFailed " + msg);
            }
        });


    }

    public void readCoilClickEvent(View view) {

        ModbusReq.getInstance().readCoil(new OnRequestBack<boolean[]>() {
            @Override
            public void onSuccess(boolean[] booleen) {
                Log.d(TAG, "readCoil onSuccess " + Arrays.toString(booleen));
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readCoil onFailed " + msg);
            }
        }, 1, 1, 2);


    }

    public void readDiscreteInputClickEvent(View view) {

        ModbusReq.getInstance().readDiscreteInput(new OnRequestBack<boolean[]>() {
            @Override
            public void onSuccess(boolean[] booleen) {
                Log.d(TAG, "readDiscreteInput onSuccess " + Arrays.toString(booleen));
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readDiscreteInput onFailed " + msg);
            }
        }, 1, 1, 5);


    }

    public void readHoldingRegistersClickEvent(View view) {

        //readHoldingRegisters
        ModbusReq.getInstance().readHoldingRegisters(new OnRequestBack<short[]>() {
            @Override
            public void onSuccess(short[] data) {
                Log.d(TAG, "readHoldingRegisters onSuccess " + Arrays.toString(data));
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readHoldingRegisters onFailed " + msg);
            }
        }, 1, 2, 8);


    }

    public void readInputRegistersClickEvent(View view) {


        ModbusReq.getInstance().readInputRegisters(new OnRequestBack<short[]>() {
            @Override
            public void onSuccess(short[] data) {
                Log.d(TAG, "readInputRegisters onSuccess " + Arrays.toString(data));
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readInputRegisters onFailed " + msg);
            }
        }, 1, 2, 8);


    }

    public void writeCoilClickEvent(View view) {


        ModbusReq.getInstance().writeCoil(new OnRequestBack<String>() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "writeCoil onSuccess " + s);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "writeCoil onFailed " + msg);
            }
        }, 1, 1, true);


    }

    public void writeRegisterClickEvent(View view) {

        ModbusReq.getInstance().writeRegister(new OnRequestBack<String>() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "writeRegister onSuccess " + s);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "writeRegister onFailed " + msg);
            }
        }, 1, 1, 234);


    }

    public void writeRegistersClickEvent(View view) {

        ModbusReq.getInstance().writeRegisters(new OnRequestBack<String>() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "writeRegisters onSuccess " + s);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "writeRegisters onFailed " + msg);
            }
        }, 1, 2, new short[]{211, 52, 34});


    }

    public void theLEDClickEvent(View view) {

        short newLEDvalue;

        if (theLEDvalue == 0) {
            newLEDvalue = 1;
        } else {
            newLEDvalue = 0;
        }

        ModbusReq.getInstance().writeRegister(new OnRequestBack<String>() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "writeRegister onSuccess " + s);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "writeRegister onFailed " + msg);
            }
        }, 1, 2, newLEDvalue);

    }

    public void theNullEvent(View view) {

    }
}
