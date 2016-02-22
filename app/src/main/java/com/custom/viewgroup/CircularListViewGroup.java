package com.custom.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
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

import com.custom.R;

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
    private int selectedViewIndex = -1;
    private VelocityTracker velocityTracker;
    private boolean isFlingOver = true;
    private Context context;
    private float lastX = 0, lastY = 0, lastTouchX = 0, lastTouchY = 0, viewGestureOffset = 1 / 10;
    private int viewHeight = 0, viewWidth = 0, viewGroupHeight = 0, viewGroupWidth = 0, leftBoundary, rightBoundary, topBoundary, bottomBoundary, offset = 10, radius = 0;

    private Handler flingHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int distance = msg.arg1;
            Log.d(TAG, distance + " distance in fling.");
            if (orientation == HORIZONTAL_ORIENTATION) {

                for (int childCount = 0; childCount < getChildCount(); childCount++) {
                    View child = getChildAt(childCount);

                    // controls fling gap
                    if (childCount == 0) {
                        if (child.getLeft() > (viewGestureOffset * rightBoundary)) {
                            isFlingOver = true;
                            break;
                        } else if ((getChildAt(getChildCount() - 1).getLeft() + distance
                                + getChildAt(getChildCount() - 1).getWidth()) < ((1 - viewGestureOffset) * rightBoundary)) {
                            isFlingOver = true;
                            break;
                        }
                    }

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

    private Handler bounceViewsHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int direction = message.arg1;
            int distance = 0;
            if (orientation == HORIZONTAL_ORIENTATION) {
                if (direction == -1) {
                    if (getChildAt(getChildCount() - 1).getRight() < rightBoundary + offset) { //right to left
                        distance = rightBoundary
                                - getChildAt(getChildCount() - 1).getWidth()
                                - getChildAt(getChildCount() - 1).getLeft();
                    }
                } else if (getChildAt(0).getLeft() > leftBoundary) { //left to right
                    distance = leftBoundary - getChildAt(0).getLeft();
                }
                if (distance != 0) {
                    for (int childCount = 0; childCount < getChildCount(); childCount++) {
                        View child = getChildAt(childCount);
                        child.layout(child.getLeft() + distance, child.getTop(),
                                child.getLeft() + distance + child.getWidth(),
                                child.getTop() + child.getHeight());
                    }
                }
            } else {

                if (direction == -1) {
                    if (getChildAt(getChildCount() - 1).getBottom() < bottomBoundary)
                        distance = bottomBoundary
                                - getChildAt(getChildCount() - 1).getHeight()
                                - getChildAt(getChildCount() - 1).getTop();

                } else {
                    if (getChildAt(0).getTop() > topBoundary)
                        distance = topBoundary - getChildAt(0).getTop();
                }

                for (int childCount = 0; childCount < getChildCount(); childCount++) {
                    View child = getChildAt(childCount);
                    child.layout(child.getLeft(), child.getTop() + distance,
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

        if (attrs != null) {
            final TypedArray attributes = context.obtainStyledAttributes(attrs,
                    R.styleable.CircularListViewGroup, defStyleAttr, 0);

            radius = (int) attributes.getDimension(R.styleable.CircularListViewGroup_radius, 0);
            orientation = attributes.getInt(R.styleable.CircularListViewGroup_view_orientation, 0);
            offset = (int) attributes.getDimension(R.styleable.CircularListViewGroup_view_offset, 0f);

            // TODO: 18/2/16 margin  

            attributes.recycle();

            int[] attb = new int[]{android.R.attr.layout_height, android.R.attr.layout_height};

            TypedArray arr = context.obtainStyledAttributes(attrs, attb);

            viewGroupHeight = arr.getIndex(0);
            viewGroupWidth = arr.getIndex(1);
            Log.d(TAG, "height = " + viewGroupHeight + " width " + viewGroupWidth);
            // 0 wrap_content & 2 match parent

        }

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

        if (getChildCount() == 0) {

            leftBoundary = left + offset;
            topBoundary = top + offset;
            rightBoundary = right - offset;
            bottomBoundary = bottom - offset;

            for (int i = 0; i < adapter.getCount(); i++) {

                View view = adapter.getView(i, null, this);
                addAndMeasureChild(view, i);
            }
        }

        if (orientation == VERTICAL_ORIENTATION && viewGroupWidth == 0) {
            setRight(viewWidth + 2 * offset);
        } else if (orientation == HORIZONTAL_ORIENTATION && viewGroupHeight == 0) {
            setBottom(viewHeight + 2 * offset);
        }

        for (int i = 0; i < adapter.getCount(); i++) {
            final int position = i;
            final boolean[] longPress = {false};
            View view = getChildAt(i);
            int l, t, r, b;
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
            view.layout(l, t, r, b);
        }
    }

    private void addAndMeasureChild(View child, int index) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
        }

        int childWidth = Math.round(params.width);
        int childHeight = Math.round(params.height);

        if (orientation == HORIZONTAL_ORIENTATION) {
            viewHeight = Math.max(viewHeight, childHeight);
        } else {
            viewWidth = Math.max(viewWidth, childWidth);
        }

        View view = child;
        if (radius != 0) {
            view = new CircularView(context, child, radius, childWidth, childHeight);
        }

        addViewInLayout(view, index, params, true);

        view.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
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

                lastX = lastTouchX = event.getRawX();
                lastY = lastTouchY = event.getRawY();

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

                if ((lastTouchX - event.getRawX() == 0) && (lastTouchY - event.getRawY() == 0)) {
                    setCurrentViewBasedOnClick((int) lastTouchX, (int) lastTouchY);
                    if (selectedView != null) {
                        onItemInteractionListener.onItemClick(selectedViewIndex, selectedView);
                    }
                }


                velocityTracker.computeCurrentVelocity(1000, ViewConfiguration.get(context).getScaledMaximumFlingVelocity());
                float velocityInX = velocityTracker.getXVelocity();
                float velocityInY = velocityTracker.getYVelocity();
                float ppi = getResources().getDisplayMetrics().density * 160.0f;
                float acceleration = ViewConfiguration.getScrollFriction() * 9.8f * 39.37f * ppi; // a = Mg (Inch/meter * pixel/inch)

                float velocity;
                if (orientation == HORIZONTAL_ORIENTATION) {
                    flingDirection = (velocityInX > 0) ? 1 : -1;
                    velocity = Math.abs(velocityInX);
                } else {
                    flingDirection = (velocityInY > 0) ? 1 : -1;
                    velocity = Math.abs(velocityInY);
                }
                Log.d(TAG, velocity + " velocity of this fling.");

                if (velocity > (ViewConfiguration.get(context).getScaledMinimumFlingVelocity() * 2)) {
                    new Thread(new FlingThread((int) velocity, acceleration, flingDirection)).start();
                    isFlingOver = false;
                } else if (isFlingOver) {
                    Message bounceMsg = new Message();
                    bounceMsg.arg1 = flingDirection;
                    bounceViewsHandler.sendMessage(bounceMsg);
                }

                velocityTracker.recycle();
                velocityTracker = null;

                break;
        }

        return true;
    }

    private void setCurrentViewBasedOnClick(int x, int y) {
        Rect scrollBounds = new Rect();
        getHitRect(scrollBounds);

        boolean isVisible = false, isViewSelected = false;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view.getLocalVisibleRect(scrollBounds)) {
                isVisible = true;
                int location[] = new int[2];
                view.getLocationOnScreen(location);
                int viewX = location[0];
                int viewY = location[1];
                if (orientation == HORIZONTAL_ORIENTATION) {
                    if ((x > viewX && x < (viewX + view.getWidth()))) {
                        selectedViewIndex = i;
                        selectedView = view;
                        isViewSelected = true;
                        break;
                    }
                } else {
                    if ((y > viewY && y < (viewY + view.getWidth()))) {
                        selectedViewIndex = i;
                        selectedView = view;
                        isViewSelected = true;
                        break;
                    }
                }
            } else {
                if (isVisible) {
                    break; // the visible sequence is over. following views will be off the screen.
                }
            }
        }
        if (!isViewSelected) {
            selectedViewIndex = -1;
            selectedView = null;
        }
    }


    public interface OnItemInteractionListener {
        public void onItemClick(int position, View view);
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

                    if (difference != 0) {
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

            Message bounceMsg = new Message();
            bounceMsg.arg1 = flingDirection;
            bounceViewsHandler.sendMessage(bounceMsg);

        }

    }

    private class CircularView extends View {

        private RectF rect;
        private int radius;
        private Paint paint;

        public CircularView(Context context, View view, int radius, int width, int height) {
            super(context);

            this.radius = radius;

            rect = new RectF(0, 0, width, height);
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache(true);
            Bitmap image = view.getDrawingCache();
            view.setDrawingCacheEnabled(false);

            BitmapShader shader = new BitmapShader(image, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setShader(shader);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRoundRect(rect, radius, radius, paint);
        }
    }

}
