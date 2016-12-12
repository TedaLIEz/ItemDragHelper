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
import android.view.ViewParent;
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

public class ItemDragHelper {
    private static final String TAG = "ItemDragHelper";
    private static final int ACTION_STATE_IDLE = 0;
    private static final int ACTION_STATE_ANIMATED = 1;
    private static final int ACTIVE_POINTER_ID_NONE = -1;

    private static final int ANIMATION_TYPE_DRAG = 5;
    private static final int ANIMATION_TYPE_SCALE = 6;
    private static final long DEFAULT_ANIMATION_DURATION = 100;
    RecyclerView.ViewHolder mSelected;
    private int mActivePointerId;
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
     * We cache the position of the overdraw child to avoid recalculating it each time child
     * position callback is called. This value is invalidated whenever a child is attached or
     * detached.
     */
    int mOverdrawChildPosition = -1;

    /**
     * Animation for recovery if we fail to open it.
     */
    private List<RecoverAnimation> mRecoverAnimations = new ArrayList<>();
    private final float[] mTmpPosition = new float[2];
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
//            mRotateGestureDetector.onTouchEvent(e);
//            mMoveGestureDetector.onTouchEvent(e);
            final int action = MotionEventCompat.getActionMasked(e);
            if (action == MotionEvent.ACTION_DOWN) {
                mActivePointerId = e.getPointerId(0);
//                mInitialTouchX = e.getX();
//                mInitialTouchY = e.getY();
//                if (mSelected == null) {
//                    final RecoverAnimation animation = findAnimation(e);
//                    if (animation != null) {
//                        mInitialTouchX -= animation.mDx;
//                        mInitialTouchY -= animation.mDy;
////                        select(animation.mViewHolder, animation.mActionState);
//                        updateDxDy(e, 0);
//                    }
//                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP) {
                mActivePointerId = ACTIVE_POINTER_ID_NONE;
                select(null, ACTION_STATE_IDLE);
            }
            return mSelected != null;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            mScaleGestureDetector.onTouchEvent(e);
//            mRotateGestureDetector.onTouchEvent(e);
//            mMoveGestureDetector.onTouchEvent(e);
            if (mActivePointerId == ACTIVE_POINTER_ID_NONE) return;
            final int action = MotionEventCompat.getActionMasked(e);
            final int activePointerIndex = e.findPointerIndex(mActivePointerId);
            RecyclerView.ViewHolder viewHolder = mSelected;
            if (viewHolder == null) return;
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    if (e.getPointerCount() == 2 && activePointerIndex >= 0) {
                        updateDxDy(e, activePointerIndex);
                        // TODO: move viewHolder in this case.
                        Log.d(TAG, "action_move startX " + mInitialTouchX + " startY " + mInitialTouchY
                                + " mDx " + mDx + " mDy " + mDy);

                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    select(null, ACTION_STATE_IDLE);
                    mActivePointerId = ACTIVE_POINTER_ID_NONE;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    final int pointerIndex = MotionEventCompat.getActionIndex(e);
                    final int pointerId = e.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mActivePointerId = e.getPointerId(newPointerIndex);
                        updateDxDy(e, pointerIndex);
                    }
                    break;
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            // FIXME: 12/10/16 maybe conflict with other motionEvent
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
            // TODO: play the restore animation.
            final RecyclerView.ViewHolder prevSelected = mSelected;

            getSelectedDxDy(mTmpPosition);

            final RecoverAnimation rv = new RecoverAnimation(prevSelected, mDx, mDy, mRotate, mScale);
            rv.setDuration(DEFAULT_ANIMATION_DURATION);
            mRecoverAnimations.add(rv);
            rv.start();
            mSelected = null;
        }
        if (selected != null) {
            mSelectedStartX = selected.itemView.getLeft();
            mSelectedStartY = selected.itemView.getTop();
            mSelected = selected;
            final ViewParent rvParent = mRecyclerView.getParent();
            if (rvParent != null) {
                rvParent.requestDisallowInterceptTouchEvent(mSelected != null);
            }
        }
        mRecyclerView.invalidate();
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

    private void updateDxDy(MotionEvent ev, int pointerIndex) {

        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);
        mDx = x - mInitialTouchX;
        mDy = y - mInitialTouchY;
    }


    private void getSelectedDxDy(float[] outPosition) {
        outPosition[0] = ViewCompat.getTranslationX(mSelected.itemView);
        outPosition[1] = ViewCompat.getTranslationY(mSelected.itemView);
    }


    private long getAnimationDuration(RecyclerView recyclerView, int animationType, float v, float v1) {
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

    RecoverAnimation findAnimation(MotionEvent e) {
        if (mRecoverAnimations.isEmpty()) {
            return null;
        }
        View target = findChildView(e);
        for (int i = mRecoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation recoverAnimation = mRecoverAnimations.get(i);
            if (recoverAnimation.mViewHolder.itemView == target) {
                return recoverAnimation;
            }
        }
        return null;
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

    View findChildView(MotionEvent event) {
        // first check elevated views, if none, then call RV
        final float x = event.getX();
        final float y = event.getY();
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

        public RecoverAnimation(RecyclerView.ViewHolder viewHolder, float dx, float dy, float degrees, float scaleFactor) {
            mViewHolder = viewHolder;
            mAnimator = ViewCompat.animate(viewHolder.itemView).scaleXBy(1.0f - scaleFactor)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setListener(this)
                    .scaleYBy(1.0f - scaleFactor);
            this.mDx = dx;
            this.mDy = dy;
            this.degrees = degrees;
            this.scaleFactor = scaleFactor;
        }

        public void setDuration(long duration) {
            mAnimator.setDuration(duration);
        }

        public void start() {
            mViewHolder.setIsRecyclable(false);
            mAnimator.start();
        }

        public void cancel() {
            mAnimator.cancel();
        }


        @Override
        public void onAnimationStart(View view) {

        }

        @Override
        public void onAnimationEnd(View view) {
            if (!mEnded) {
                mViewHolder.setIsRecyclable(true);
            }
            mEnded = true;
        }

        @Override
        public void onAnimationCancel(View view) {

        }
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float mScaleFactor = 1.0f;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.isInProgress()) {
                View child = findChildView(detector);
                if (child != null) {
                    RecyclerView.ViewHolder vh = mRecyclerView.getChildViewHolder(child);
                    if (mSelected == null) {
                        if (Float.compare(detector.getScaleFactor(), 1.0f) != 0) {
                            mInitialTouchX = vh.itemView.getX();
                            mInitialTouchY = vh.itemView.getY();
                            mScale = 1.0f;
                            mDx = mDy = 0f;
                            Log.d(TAG, "start scale initialTouchX " + mInitialTouchX
                                    + " initialTouchY " + mInitialTouchY);
                            select(vh, ACTION_STATE_ANIMATED);
                        }
                    } else if (vh == mSelected) {
                        float scaleFactorDiff = detector.getScaleFactor();
                        // TODO: add holy scale value
                        // TODO: use ViewCompat to play scale animation
                        handleScale(scaleFactorDiff);
                    } else {
                        select(vh, ACTION_STATE_ANIMATED);
                    }
                }
            }
            return Float.compare(mScaleFactor, 1.0f) != 0;
        }
    }

    private void handleScale(float scaleFactorDiff) {
        if (mSelected != null) {
            mScale = scaleFactorDiff;
            ViewCompat.animate(mSelected.itemView)
                    .scaleX(scaleFactorDiff)
                    .scaleY(scaleFactorDiff)
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                            view.bringToFront();
                        }

                        @Override
                        public void onAnimationEnd(View view) {

                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    })
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(0)
                    .start();
        }
    }


    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
//            handleTranslate(detector.getFocusDelta());
            return mSelected != null;
        }
    }

    private void handleTranslate(PointF delta) {
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if (mSelected != null) {
                detector.getRotationDegreesDelta();
            }
            return mSelected != null;
        }
    }
}

