package com.hustunique.jianguo.photogallery.widget;

import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.almeros.android.multitouch.MoveGestureDetector;
import com.almeros.android.multitouch.RotateGestureDetector;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by JianGuo on 12/9/16.
 * DragHelper, much similar to the {@link ItemTouchHelper}, only support for {@link android.widget.ImageView}
 * currently
 */
// FIXME: 12/13/16 ArrayIndexOutOfBoundsException when scrolls after dragging one pic
public class ItemDragHelper {
    private static final String TAG = "ItemDragHelper";
    private static final int ACTION_STATE_IDLE = 0;
    private static final int ACTION_STATE_ANIMATED = 1;
    private static final long DEFAULT_ANIMATION_DURATION = 100;
    private static final float MIN_SCALE = 0.4f;
    RecyclerView.ViewHolder mSelected;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private int mSelectedStartX;
    private int mSelectedStartY;

    /**
     * This keeps a reference to the child dragged by the user. Even after user stops dragging,
     * until view reaches its final position (end of recover animation), we keep a reference so
     * that it can be drawn above other children.
     */
    View mOverdrawChild = null;
    /**
     * If drag & drop is supported, we use child drawing order to bring them to front.
     */
    private RecyclerView.ChildDrawingOrderCallback mChildDrawingOrderCallback = null;

    /**
     * Animation for recovery if we fail to open it.
     */
    private List<RecoverAnimation> mRecoverAnimations = new ArrayList<>();
    /**
     * diff between the last event and initial touch.
     */
    private float mDx, mDy;
    /**
     * diff between the last scale factor and initial factor(1f)
     */
    private float mScale;
    private float mRotate;
    private RecyclerView mRecyclerView;
    private ScaleGestureDetector mScaleGestureDetector;
    private RotateGestureDetector mRotateGestureDetector;
    private MoveGestureDetector mMoveGestureDetector;

    private RecyclerView.OnItemTouchListener mOnItemTouchListener = new RecyclerView.OnItemTouchListener() {


        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            mScaleGestureDetector.onTouchEvent(e);
            mRotateGestureDetector.onTouchEvent(e);
            mMoveGestureDetector.onTouchEvent(e);
            final int action = MotionEventCompat.getActionMasked(e);

            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP
                    || action == MotionEvent.ACTION_UP) {
                select(null, ACTION_STATE_IDLE);
            }
            return mSelected != null;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            mScaleGestureDetector.onTouchEvent(e);
            mRotateGestureDetector.onTouchEvent(e);
            mMoveGestureDetector.onTouchEvent(e);
            final int action = MotionEventCompat.getActionMasked(e);
            RecyclerView.ViewHolder viewHolder = mSelected;
            if (viewHolder == null) return;
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    select(null, ACTION_STATE_IDLE);
                    break;
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (!disallowIntercept) {
                return;
            }
            select(null, ACTION_STATE_IDLE);
        }
    };
    private int mActionState;


    public void attachToRecyclerView(RecyclerView rv) {
        if (mRecyclerView == rv) {
            return;
        }
        mRecyclerView = rv;
        mRecyclerView.addOnItemTouchListener(mOnItemTouchListener);
        initGestureDetector();
    }


    private void select(RecyclerView.ViewHolder selected, int actionState) {
        if (selected == mSelected && actionState == mActionState) {
            return;
        }
        endRecoverAnimation(selected);
        mActionState = actionState;
        if (actionState == ACTION_STATE_ANIMATED) {
            // we remove after animation is complete. this means we only elevate the last drag
            // child but that should perform good enough as it is very hard to start dragging a
            // new child before the previous one settles.
            mOverdrawChild = selected.itemView;
            addChildDrawingOrderCallback();
        }
        if (mSelected != null) {
            final RecyclerView.ViewHolder prevSelected = mSelected;
            final RecoverAnimation rv = new RecoverAnimation(prevSelected, ACTION_STATE_ANIMATED,
                    mDx, mDy, mRotate, mScale);
            rv.setDuration(getAnimationDuration(mRecyclerView));
            mRecoverAnimations.add(rv);
            rv.start();
            mSelected = null;
        }
        if (selected != null) {
            mSelectedStartX = selected.itemView.getLeft();
            mSelectedStartY = selected.itemView.getTop();
            mInitialTouchX = selected.itemView.getX();
            mInitialTouchY = selected.itemView.getY();
            mSelected = selected;
        }
        mRecyclerView.invalidate();
    }

    private void removeChildDrawingOrderCallbackIfNecessary(View itemView) {
        if (itemView == mOverdrawChild) {
            mOverdrawChild = null;
            if (mChildDrawingOrderCallback != null) {
                mRecyclerView.setChildDrawingOrderCallback(null);
            }
        }
    }

    private void addChildDrawingOrderCallback() {
        if (mChildDrawingOrderCallback == null) {
            mChildDrawingOrderCallback = new RecyclerView.ChildDrawingOrderCallback() {
                @Override
                public int onGetChildDrawingOrder(int childCount, int i) {
                    if (mOverdrawChild == null) {
                        return i;
                    }
                    int childPosition = mRecyclerView.indexOfChild(mOverdrawChild);
                    if (i == childCount - 1) {
                        return childPosition;
                    }
                    return i < childPosition ? i : i + 1;
                }
            };
        }
        mRecyclerView.setChildDrawingOrderCallback(mChildDrawingOrderCallback);
    }


    private void initGestureDetector() {
        if (mScaleGestureDetector == null) {
            mScaleGestureDetector =
                    new ScaleGestureDetector(mRecyclerView.getContext(), new ScaleListener());
        }
        if (mMoveGestureDetector == null) {
            mMoveGestureDetector =
                    new MoveGestureDetector(mRecyclerView.getContext(), new MoveListener());
        }
        if (mRotateGestureDetector == null) {
            mRotateGestureDetector =
                    new RotateGestureDetector(mRecyclerView.getContext(), new RotateListener());
        }
    }


    private long getAnimationDuration(RecyclerView recyclerView) {
        final RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
        if (itemAnimator == null) {
            return DEFAULT_ANIMATION_DURATION;
        } else {
            return itemAnimator.getMoveDuration();
        }
    }

    void endRecoverAnimation(RecyclerView.ViewHolder viewHolder) {
        final int recoverAnimSize = mRecoverAnimations.size();
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            if (anim.mViewHolder == viewHolder) {
                if (!anim.mEnded) {
                    anim.cancel();
                }
                mRecoverAnimations.remove(i);
            }
        }
    }

    private static boolean hitTest(View child, float x, float y, float left, float top) {
        return x >= left &&
                x <= left + child.getWidth() &&
                y >= top &&
                y <= top + child.getHeight();
    }


    View findChildView(ScaleGestureDetector detector) {
        final float x = detector.getFocusX();
        final float y = detector.getFocusY();
        if (mSelected != null) {
            final View selectedView = mSelected.itemView;
            if (hitTest(selectedView, x, y, mSelectedStartX + mDx, mSelectedStartY + mDy)) {
                return selectedView;
            }
        }
        for (int i = mRecoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            final View view = anim.mViewHolder.itemView;
            if (hitTest(view, x, y, anim.mDx, anim.mDy)) {
                return view;
            }
        }
        return mRecyclerView.findChildViewUnder(x, y);
    }


    private class RecoverAnimation implements ViewPropertyAnimatorListener {

        final RecyclerView.ViewHolder mViewHolder;
        private final ViewPropertyAnimatorCompat mAnimator;
        float mDx;
        float mDy;
        float degrees;
        float scaleFactor;
        boolean mEnded = false;
        int mActionState;

        RecoverAnimation(RecyclerView.ViewHolder viewHolder, int actionState, float dx, float dy, float degrees, float scaleFactor) {
            mViewHolder = viewHolder;
            mActionState = actionState;
            mAnimator = ViewCompat.animate(viewHolder.itemView).scaleXBy(1.0f - scaleFactor)
                    .rotationBy(-degrees)
                    .translationXBy(-dx)
                    .translationYBy(-dy)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .scaleYBy(1.0f - scaleFactor)
                    .setListener(this);
            this.mDx = dx;
            this.mDy = dy;
            this.degrees = degrees;
            this.scaleFactor = scaleFactor;
        }

        void setDuration(long duration) {
            mAnimator.setDuration(duration);
        }

        void start() {
            mAnimator.start();
        }

        void cancel() {
            mAnimator.cancel();
        }

        @Override
        public void onAnimationStart(View view) {
        }

        @Override
        public void onAnimationEnd(View view) {

        }

        @Override
        public void onAnimationCancel(View view) {

        }
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float mScaleFactor = 1.0f;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // FIXME: 12/12/16 find a better way to select a dragged viewHolder
            if (detector.isInProgress()) {
                View child = findChildView(detector);
                if (child != null) {
                    RecyclerView.ViewHolder vh = mRecyclerView.getChildViewHolder(child);
                    if (vh != mSelected) {
                        if (Float.compare(detector.getScaleFactor(), 1.0f) != 0) {
                            mInitialTouchX = vh.itemView.getX();
                            mInitialTouchY = vh.itemView.getY();
                            mScale = 1.0f;
                            mDx = mDy = 0f;
                            mRotate = 0.0f;
                            Log.d(TAG, "start scale initialTouchX " + mInitialTouchX
                                    + " initialTouchY " + mInitialTouchY);
                            select(vh, ACTION_STATE_ANIMATED);
                        }
                    } else {
                        float scaleFactorDiff = detector.getScaleFactor();
                        handleScale(scaleFactorDiff);
                    }
                }
            }
            return Float.compare(mScaleFactor, 1.0f) != 0;
        }
    }

    private void handleScale(float scaleFactorDiff) {
        if (mSelected != null && scaleFactorDiff >= MIN_SCALE) {
            mScale = scaleFactorDiff;
            ViewCompat.animate(mSelected.itemView)
                    .scaleX(scaleFactorDiff)
                    .scaleY(scaleFactorDiff)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(0)
                    .start();
        }
    }


    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            handleTranslate(detector.getFocusDelta());
            return mSelected != null;
        }
    }

    private void handleTranslate(PointF delta) {
        if (mSelected != null) {
            mDx += delta.x;
            mDy += delta.y;
            ViewCompat.animate(mSelected.itemView)
                    .translationX(mDx)
                    .translationY(mDy)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(0)
                    .start();
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if (mSelected != null) {

                handleRotate(detector.getRotationDegreesDelta());
            }
            return mSelected != null;
        }
    }

    private void handleRotate(float degrees) {
        if (mSelected != null) {
            mRotate -= degrees;
            ViewCompat.animate(mSelected.itemView)
                    .rotation(mRotate)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(0)
                    .start();
        }
    }
}

