package com.anitha.custom.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by anitham on 29/1/16.
 */
public class CircularListViewGroup extends ViewGroup {

    public static final int HORIZONTAL_ORIENTATION = 1;
    public static final int VERTICAL_ORIENTATION = 2;
    public static final int UNDEFINED = -1;

    private int radius = 0;
    private int orientation = HORIZONTAL_ORIENTATION;
    private int childOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
    private int childWidth = 0;
    private int childHeight = 0;
    private boolean makeRoundedChild = false;
    private boolean isScrollingEnabled = false;

    private Context context;
    private static final String TAG = "CircularViewGroup";

    private boolean isFlingOver = true, isChildSizeVariable = true, isChildMeasured = false;
    private float lastX = 0, lastY = 0, lastTouchX = 0, lastTouchY = 0, viewGestureOffset = 1 / 10;
    private int viewHeight = 0, viewWidth = 0, viewGroupHeight = 0, viewGroupWidth = 0, viewTopOffset = 0, viewLeftOffset = 0;
    private int leftBoundary = UNDEFINED, rightBoundary = UNDEFINED, topBoundary = UNDEFINED, bottomBoundary = UNDEFINED;
    private int childAnimation = UNDEFINED, selectedViewIndex = -1, flingDirection = 1; // -1 is bottom-top / right-left

    private Adapter adapter;
    private OnItemInteractionListener onItemInteractionListener;
    private View selectedView = null;
    private VelocityTracker velocityTracker;

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
                    if (getChildAt(getChildCount() - 1).getRight() < rightBoundary + childOffset) { //right to left
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
        initialiseAttributes(attrs, defStyleAttr);

        if (childWidth > 0 && childHeight > 0) {
            viewWidth = childWidth;
            viewHeight = childHeight;
            isChildSizeVariable = false;
        }

    }


    public void initialiseAttributes(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            final TypedArray attributes = context.obtainStyledAttributes(attrs,
                    R.styleable.CircularListViewGroup, defStyleAttr, 0);

            radius = ((int) attributes.getDimension(R.styleable.CircularListViewGroup_child_corner_radius, UNDEFINED) == UNDEFINED) ? radius : (int) attributes.getDimension(R.styleable.CircularListViewGroup_child_corner_radius, UNDEFINED);
            orientation = attributes.getInt(R.styleable.CircularListViewGroup_view_orientation, HORIZONTAL_ORIENTATION);
            childOffset = ((int) attributes.getDimension(R.styleable.CircularListViewGroup_child_offset, UNDEFINED) == UNDEFINED) ? childOffset : (int) attributes.getDimension(R.styleable.CircularListViewGroup_child_offset, UNDEFINED);
            childHeight = ((int) attributes.getDimension(R.styleable.CircularListViewGroup_child_height, UNDEFINED) == UNDEFINED) ? childHeight : (int) attributes.getDimension(R.styleable.CircularListViewGroup_child_height, UNDEFINED);
            childWidth = ((int) attributes.getDimension(R.styleable.CircularListViewGroup_child_width, UNDEFINED) == UNDEFINED) ? childWidth : (int) attributes.getDimension(R.styleable.CircularListViewGroup_child_width, UNDEFINED);
            makeRoundedChild = attributes.getBoolean(R.styleable.CircularListViewGroup_make_rounded_children, false);
            isScrollingEnabled = attributes.getBoolean(R.styleable.CircularListViewGroup_enable_scrolling, false);
            childAnimation = attributes.getResourceId(R.styleable.CircularListViewGroup_child_animation, UNDEFINED);

            attributes.recycle();

            TypedArray arr = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.layout_width, android.R.attr.layout_height});
            try {
                viewGroupWidth = arr.getInt(0, 0);
            } catch (NumberFormatException e) {
                viewGroupWidth = (int) arr.getDimension(0, 0);
            }
            try {
                viewGroupHeight = arr.getInt(1, 0);  // -2 wrap_content & 0 match_parent
            } catch (NumberFormatException e) {
                viewGroupHeight = (int) arr.getDimension(1, 0);
            }
        }
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        if (orientation == VERTICAL_ORIENTATION || orientation == HORIZONTAL_ORIENTATION) {
            this.orientation = orientation;
        } else {
            Log.e(TAG, "Orientation is not set properly. Set either VERTICAL_ORIENTATION or HORIZONTAL_ORIENTATION.");
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }


    public int getChildOffset() {
        return childOffset;
    }

    public void setChildOffset(int childOffset) {
        this.childOffset = this.childOffset;
    }

    public boolean isMakeRoundedChild() {
        return makeRoundedChild;
    }

    public void setMakeRoundedChild(boolean makeRoundedChild) {
        this.makeRoundedChild = makeRoundedChild;
    }

    public boolean isScrollingEnabled() {
        return isScrollingEnabled;
    }

    public void setIsScrollingEnabled(boolean isScrollingEnabled) {
        this.isScrollingEnabled = isScrollingEnabled;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public void setAdapter(Adapter adapter) {
        removeAllViewsInLayout();
        this.adapter = adapter;
        requestLayout();
    }

    public View getSelectedView() {
        return selectedView;
    }

    public void setSelection(int i) {
        selectedView = getChildAt(i);
    }

    public void setOnItemInteractionListener(OnItemInteractionListener onItemInteractionListener) {
        this.onItemInteractionListener = onItemInteractionListener;
    }

    public OnItemInteractionListener getOnItemInteractionListener() {
        return onItemInteractionListener;
    }

    public void setChildAnimation(int animation) {
        childAnimation = animation;
    }

    public Bitmap getRoundedCornerBitmap(View view) {

        Drawable drawable = null;
        if (view instanceof TextView) {
            drawable = view.getBackground();
        } else if (view instanceof ImageView) {
            drawable = ((ImageView) view).getDrawable();
        }

        if (drawable != null) {
            int width = view.getLayoutParams().width, height = view.getLayoutParams().height;
            Bitmap foreground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas foregroundCanvas = new Canvas(foreground);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(foregroundCanvas);

            Bitmap output = Bitmap.createBitmap(foreground.getWidth(), foreground.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawARGB(0, 0, 0, 0);

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            Rect rect = new Rect(0, 0, foreground.getWidth(), foreground.getHeight());
            canvas.drawRoundRect(new RectF(rect), radius, radius, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(foreground, rect, rect, paint);

            return output;
        }
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() == 0) {
            if (adapter != null) {
                for (int position = 0; position < adapter.getCount(); position++) {
                    View view = adapter.getView(position, null, this);
                    addAndMeasureChild(view, position, true);
                    if (makeRoundedChild && isChildSizeVariable) {
                        radius = Math.min(view.getLayoutParams().height, view.getLayoutParams().width) / 2;
                    }
                    Bitmap bitmap = getRoundedCornerBitmap(view);
                    if (bitmap != null) {
                        view.setBackground(new BitmapDrawable(getResources(), bitmap));
                    }
                }
                isChildMeasured = true;
            } else {
                Log.e(TAG, "Adapter not set.");
            }

        } else if (!isChildMeasured) {
            for (int position = 0; position < getChildCount(); position++) {
                View view = getChildAt(position);
                addAndMeasureChild(view, position, false);
                if (makeRoundedChild && isChildSizeVariable) {
                    radius = Math.min(view.getLayoutParams().height, view.getLayoutParams().width) / 2;
                }
                Bitmap bitmap = getRoundedCornerBitmap(view);
                if (bitmap != null) {
                    view.setBackground(new BitmapDrawable(getResources(), bitmap));
                }
            }
            isChildMeasured = true;
        }

        if (viewGroupWidth == LayoutParams.WRAP_CONTENT) {
            if (orientation == HORIZONTAL_ORIENTATION) {
                setRight(getLeft() + getChildCount() * (viewWidth + childOffset) + childOffset);
            } else {
                setRight(getLeft() + viewWidth + 2 * childOffset);
            }
        } else if (viewGroupWidth == LayoutParams.MATCH_PARENT) {
            setRight(getMeasuredWidth());
        } else {
            setRight(getLeft() + viewGroupWidth);
        }

        if (viewGroupHeight == LayoutParams.WRAP_CONTENT) {
            if (orientation == HORIZONTAL_ORIENTATION) {
                setBottom(getTop() + viewHeight + 2 * childOffset);
            } else {
                setBottom(getTop() + getChildCount() * (viewHeight + childOffset) + childOffset);
            }
        } else if (viewGroupHeight == LayoutParams.MATCH_PARENT) {
            setBottom(getMeasuredHeight());
        } else {
            setBottom(getTop() + viewGroupHeight);
        }

        if (leftBoundary == UNDEFINED) {
            leftBoundary = getLeft() + childOffset;
            topBoundary = getTop() + childOffset;
            rightBoundary = getRight() - childOffset;
            bottomBoundary = getBottom() - childOffset;
            if (makeRoundedChild && !isChildSizeVariable) {
                radius = Math.min(viewWidth, viewHeight) / 2;
            }
        }

        setMeasuredDimension(getRight() - getLeft(), getBottom() - getTop());
        Log.d(TAG, "OnMeasure");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        viewTopOffset = (viewGroupHeight > 0) ? ((viewGroupHeight - viewHeight) / 2) : childOffset;
        viewLeftOffset = (viewGroupWidth > 0) ? ((viewGroupWidth - viewWidth) / 2) : childOffset;

        for (int position = 0; position < getChildCount(); position++) { //positions are relative to parent
            int l, t, r, b;
            View view = getChildAt(position);
            if (orientation == HORIZONTAL_ORIENTATION) {
                l = childOffset + position * (view.getLayoutParams().width + childOffset);
                t = viewTopOffset;
            } else {
                l = viewLeftOffset;
                t = childOffset + position * (view.getLayoutParams().height + childOffset);
            }
            b = t + view.getLayoutParams().height;
            r = l + view.getLayoutParams().width;
            view.layout(l, t, r, b);
        }
    }

    private void addAndMeasureChild(View child, int index, boolean addViewInLayout) {
        LayoutParams params = child.getLayoutParams();

        if (isChildSizeVariable) {
            if (params == null) {
                params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            }
        } else {
            if (params == null) {
                params = new LayoutParams(viewWidth, viewHeight);
            } else {
                params.height = viewHeight;
                params.width = viewWidth;
            }
        }

        if (childAnimation != UNDEFINED) {
            Animation anim = AnimationUtils.loadAnimation(context, childAnimation);
            child.setAnimation(anim);
        }

        if (isChildSizeVariable) {
            viewHeight = Math.max(viewHeight, params.height);
            viewWidth = Math.max(viewWidth, params.width);
        }

        if (addViewInLayout) {
            addViewInLayout(child, index, params, false);
        }
        child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isScrollingEnabled) {
            touchEventForHandlingScrolling(event);
        } else {
            touchEventForHandlingClick(event);
        }
        return true;
    }

    private void touchEventForHandlingClick(MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getRawX();
                lastTouchY = event.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                if ((lastTouchX - event.getRawX() == 0) && (lastTouchY - event.getRawY() == 0)) {
                    setCurrentViewBasedOnClick((int) lastTouchX, (int) lastTouchY);
                    if (selectedView != null && onItemInteractionListener != null) {
                        onItemInteractionListener.onItemClick(selectedViewIndex, selectedView);
                    }
                }
                break;
        }
    }

    private void touchEventForHandlingScrolling(MotionEvent event) {
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
                    } else {
                        left = child.getLeft();
                        top = child.getTop() + (int) differenceInY;
                    }
                    right = left + child.getWidth();
                    bottom = top + child.getHeight();
                    child.layout(left, top, right, bottom);
                }

                invalidate();

                lastX = event.getRawX();
                lastY = event.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                if ((lastTouchX - event.getRawX() == 0) && (lastTouchY - event.getRawY() == 0)) {
                    setCurrentViewBasedOnClick((int) lastTouchX, (int) lastTouchY);
                    if (selectedView != null && onItemInteractionListener != null) {
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
}