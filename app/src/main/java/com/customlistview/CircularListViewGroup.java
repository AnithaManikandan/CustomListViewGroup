package com.customlistview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Adapter;
import android.widget.AdapterView;

/**
 * Created by anitham on 29/1/16.
 */
public class CircularListViewGroup extends AdapterView {


    public static final int VERTICAL_ORIENTATION = 0;
    public static final int HORIZONTAL_ORIENTATION = 1;
    private String TAG = "CircularViewGroup";
    private Adapter adapter;
    private int orientation = HORIZONTAL_ORIENTATION, flingDirection = 1; // -1 is bottom-top / right-left
    private OnItemInteractionListener onItemInteractionListener;
    private View selectedView = null;
    private VelocityTracker velocityTracker;
    private boolean isFlingOver = true;
    private Context context;
    private float lastX = 0, lastY = 0, flingStartX = 0, flingStartY = 0;

    private Handler flingHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int distance = msg.arg1;
            if (orientation == HORIZONTAL_ORIENTATION) {
                for (int childCount = 0; childCount < getChildCount(); childCount++) {
                    View child = getChildAt(childCount);
                    child.layout(child.getLeft() + distance,
                            child.getTop(), child.getLeft() + distance
                                    + child.getWidth(),
                            child.getTop() + child.getHeight());
                }
            } else {
                for (int childCount = 0; childCount < getChildCount(); childCount++) {
                    View child = getChildAt(childCount);
                    child.layout(child.getLeft(), child.getTop()
                                    + distance,
                            child.getLeft() + child.getWidth(), child.getTop()
                                    + distance + child.getHeight());
                }
            }
            invalidate();
            return true;
        }
    });


    public CircularListViewGroup(Context context) {
        this(context, null, 0);
    }

    public CircularListViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularListViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setOrientation(int orientation) {
        if (orientation == VERTICAL_ORIENTATION || orientation == HORIZONTAL_ORIENTATION) {
            this.orientation = orientation;
        } else {
            Log.e(TAG, "Orientation is not set properly. Set either VERTICAL_ORIENTATION or HORIZONTAL_ORIENTATION.");
        }

    }

    @Override
    public Adapter getAdapter() {
        return adapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public View getSelectedView() {
        return selectedView;
    }

    @Override
    public void setSelection(int i) {
        selectedView = getChildAt(i);
    }

    public void setOnItemInteractionListener(OnItemInteractionListener onItemInteractionListener) {
        this.onItemInteractionListener = onItemInteractionListener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // TODO: 01-02-2016 Handle wrap content for height / width

        if (getChildCount() == 0) {
            for (int i = 0; i < adapter.getCount(); i++) {
                View view = adapter.getView(i, null, this);
                addAndMeasureChild(view, i);
            }
        }

        for (int i = 0; i < adapter.getCount(); i++) {
            final int position = i;
            final boolean[] longPress = {false};
            View view = getChildAt(i);
            int l, t, r, b, offset = 10;
            if (orientation == HORIZONTAL_ORIENTATION) {
                l = left + offset + position * (view.getMeasuredWidth() + offset);
                t = top + offset;
                b = t + view.getMeasuredHeight();
                r = l + view.getMeasuredWidth();
            } else {
                l = left + offset;
                t = top + position * (view.getMeasuredHeight() + offset);
                b = t + view.getMeasuredHeight();
                r = l + view.getMeasuredWidth();
            }

            Log.d(TAG, "l=" + l + " t=" + t + " b=" + b + " r=" + r);
            view.layout(l, t, r, b);


//            view.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    setSelection(position);
//                    if (onItemInteractionListener != null && !longPress[0]) {
//                        onItemInteractionListener.onItemClick(position, view);
//                    }
//                }
//            });
//            view.setOnLongClickListener(new OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//
//                    if (onItemInteractionListener != null) {
//                        longPress[0] = true;
//                        onItemInteractionListener.onItemLongPress(position, view);
//                    }
//                    return false;
//                }
//            });

        }


    }

    private void addAndMeasureChild(View child, int index) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
        }
        addViewInLayout(child, index, params, true);
        int childWidth = Math.round(params.width); //getWidth()*
        int childHeight = Math.round(params.height); //getHeight()*
        child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (event.getAction()) {



            case MotionEvent.ACTION_DOWN:

                if (!isFlingOver) {
                    isFlingOver = true;
                }

                lastX = event.getRawX();
                lastY = event.getRawY();

                flingStartX = event.getRawX();
                flingStartY = event.getRawY();


                break;

            case MotionEvent.ACTION_MOVE:


                float differenceInX = event.getRawX() - lastX;
                float differenceInY = event.getRawY() - lastY;
                int top, right, bottom, left;

                for (int childCount = 0; childCount < getChildCount(); childCount++) {
                    View child = getChildAt(childCount);
                    if (orientation == HORIZONTAL_ORIENTATION) {
                        left = child.getLeft() + (int) differenceInX;
                        top = child.getTop();
                        right = left + child.getWidth();
                        bottom = top + child.getHeight();
                    } else {
                        left = child.getLeft();
                        top = child.getTop() + (int) differenceInY;
                        right = left + child.getWidth();
                        bottom = top + +(int) differenceInY + child.getHeight();
                    }
                    child.layout(left, top, right, bottom);
                }

                invalidate();

                lastX = event.getRawX();
                lastY = event.getRawY();


                break;

            case MotionEvent.ACTION_UP:

                velocityTracker.computeCurrentVelocity(1000, ViewConfiguration.get(context).getScaledMaximumFlingVelocity());
                float velocityInX = velocityTracker.getXVelocity();
                float velocityInY = velocityTracker.getYVelocity();
                float ppi = getResources().getDisplayMetrics().density * 160.0f;
                float acceleration = ViewConfiguration.getScrollFriction() * 9.8f * 39.37f * ppi; // a = Mg (Inch/meter * pixel/inch)

                float velocity;
                if (orientation == HORIZONTAL_ORIENTATION) {
                    flingDirection = (velocityInX > 0) ? 1 : -1;
                    velocity = velocityInX;
                } else {
                    flingDirection = (velocityInY > 0) ? 1 : -1;
                    velocity = velocityInY;
                }

                if (Math.abs(velocityInX) > ViewConfiguration.get(context).getScaledMinimumFlingVelocity() || Math.abs(velocityInY) > ViewConfiguration.get(context).getScaledMinimumFlingVelocity()) {
                    new Thread(new FlingThread((int) velocity, acceleration, flingDirection)).start();
                    isFlingOver = false;
                }

                velocityTracker.recycle();
                velocityTracker = null;

                break;
        }

        return true;
    }


    class FlingThread implements Runnable {

        private float totalDuration;
        private int initialVelocity;
        private int finalVelocity;
        private float acceleration;
        private float timeSpent;
        ;
        private float startTime;
        private int difference = 0, prevDistance = 0, direction, orientation;

        public FlingThread(int velocity, float acceleration, int direction) {
            this.direction = direction;
            initialVelocity = velocity;
            initialVelocity = Math.min(Math.max(0, initialVelocity), 1500);
            finalVelocity = 0;
            this.acceleration = acceleration;
            totalDuration = Math.abs((initialVelocity - finalVelocity)
                    / this.acceleration);
        }

        public void run() {
            isFlingOver = false;
            startTime = SystemClock.uptimeMillis();
            startTime /= 1000.0f;

            while (!isFlingOver) {
                timeSpent = SystemClock.uptimeMillis() / 1000.0f - startTime;
                if (timeSpent >= totalDuration) {
                    isFlingOver = true;
                    continue;
                }
                if (!isFlingOver) {
                    int distance = (int) ((initialVelocity * timeSpent) - (0.5f
                            * acceleration * timeSpent * timeSpent)); // s = vt - 0.5at square ()

                    difference = distance - prevDistance;

                    if (difference > 0) {
                        Message msg = new Message();
                        msg.arg1 = direction * difference;
                        flingHandler.sendMessage(msg);
                        prevDistance = distance;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

}
