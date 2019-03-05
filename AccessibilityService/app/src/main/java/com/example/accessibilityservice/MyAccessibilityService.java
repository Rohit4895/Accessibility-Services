package com.example.accessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayDeque;
import java.util.Deque;

public class MyAccessibilityService extends AccessibilityService {
    private FrameLayout mLayout;
    private AccessibilityNodeInfo accessibilityNodeInfo;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        
        String message = "";
       int type = event.getEventType();
        Log.d("checkStatus"," Boolean: "+event.getSource().isPassword());

        Log.d("checkStatus"," Focus: "+event.getSource().findFocus(AccessibilityNodeInfo.FOCUS_INPUT));
       event.getSource().findFocus(AccessibilityNodeInfo.FOCUS_INPUT);

           accessibilityNodeInfo = event.getSource();
           Log.d("checkStatus"," Package: "+event.getSource().getPackageName());
           if (event.getSource().getPackageName().equals("com.example.demo2")){
               message = "SecondDemo@gmail.com";
           }else if (event.getSource().getPackageName().equals("com.example.demo")){
               message = "FirstDemo@gmail.com";
           }else if (event.getSource().getPackageName().equals("com.facebook.katana")){
               if (event.getSource().isPassword()){
                   message = "password@123";
               }else {
                   message = "facebook12345@gmail.com";
               }
           }
           

           pasteText(accessibilityNodeInfo, message);

    }

    public void pasteText(AccessibilityNodeInfo node, String text) {
        Bundle arguments = new Bundle();
        arguments.putString(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        node.performAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT, arguments);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        Log.d("checkStatus","onServiceConnected");

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        layoutInflater.inflate(R.layout.action_bar,mLayout);
        wm.addView(mLayout,lp);

        configurePowerButton();
        configureVolumeButton();
        configureScrollButton();
        configureSwipeButton();
    }

    private void configurePowerButton() {
        Button powerButton = (Button) mLayout.findViewById(R.id.power);
        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
                if (accessibilityNodeInfo != null){

                }
            }
        });
    }

    public void configureVolumeButton(){
        Button volumeUpButton = (Button) mLayout.findViewById(R.id.volume_up);

        volumeUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
            }
        });
    }

    private AccessibilityNodeInfo findScrollableNode(AccessibilityNodeInfo rooot){
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(rooot);
        while (!deque.isEmpty()){
            AccessibilityNodeInfo nodeInfo = deque.removeFirst();
            if (nodeInfo.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)){
                return nodeInfo;
            }

            for (int i=0; i<nodeInfo.getChildCount(); i++){
                deque.addLast(nodeInfo.getChild(i));
            }
        }
        return null;
    }

    private void configureScrollButton() {
        Button scrollButton = (Button) mLayout.findViewById(R.id.scroll);
        scrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccessibilityNodeInfo scrollable = findScrollableNode(getRootInActiveWindow());
                if (scrollable != null) {
                    scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
                }
            }
        });
    }

    private void configureSwipeButton() {
        Button swipeButton = (Button) mLayout.findViewById(R.id.swipe);
        swipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Path swipePath = new Path();
                swipePath.moveTo(1000, 1000);
                swipePath.lineTo(100, 1000);
                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
                dispatchGesture(gestureBuilder.build(), null, null);
            }
        });
    }
}
