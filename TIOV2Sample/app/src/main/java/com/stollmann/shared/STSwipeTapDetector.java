package com.stollmann.shared;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


public class STSwipeTapDetector implements OnTouchListener {

	private GestureDetector _gestureDetector;
	
	public STSwipeTapDetector(Context context) {
		this._gestureDetector = new GestureDetector(context, new GestureListener());
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
//		STTrace.method("onTouch", event.getAction() + " " + event.getX() + "/" + event.getY());
		return this._gestureDetector.onTouchEvent(event);
	}

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return STSwipeTapDetector.this.onTap();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            STTrace.method("onFling", "event1 = " + e1.getAction() + " " + e1.getX() + "/" + e1.getY() + "; event2 = " + e2.getAction() + " " + e2.getX() + "/" + e2.getY() + "; velx = " + velocityX + "; vely = " + velocityY);
        	
        	// This method is called only if the last motion event is of action type 0 = UP; if it is 3 = CANCEL, the owning view has captured the event and handled it.
        	// (e.g. a list view may have detected vertical scrolling; Android 4.4 is pretty sensitive for that kind of capturing; horizontal swipes have to take place within an extremely slim Y axis corridor.)
        	
        	boolean result = false;
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
//            STTrace.line("diffX = " + diffX + "; diffY = " + diffY);
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                    	result = STSwipeTapDetector.this.onRightSwipe();
                    } else {
                    	result = STSwipeTapDetector.this.onLeftSwipe();
                    }
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        result = STSwipeTapDetector.this.onDownSwipe();
                    } else {
                    	result = STSwipeTapDetector.this.onUpSwipe();
                    }
                }
            }

            return result;
        }
    }

    public boolean onRightSwipe() { return false; }

    public boolean onLeftSwipe() { return false; }
    
    public boolean onDownSwipe() { return false; }

    public boolean onUpSwipe() { return false; }

    public boolean onTap() { return false; }
}
