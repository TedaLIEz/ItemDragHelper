package com.hustunique.jianguo.photogallery.widget;

import android.animation.ObjectAnimator;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.v4.animation.AnimatorListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;

import com.almeros.android.multitouch.MoveGestureDetector;
import com.almeros.android.multitouch.RotateGestureDetector;
import com.hustunique.jianguo.photogallery.evaluators.MatrixEvaluator;
import com.hustunique.jianguo.photogallery.model.Layer;

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
    private static final int ACTION_STATE_DRAG = 1;
    private static final int ACTIVE_POINTER_ID_NONE = -1;

    private static final int ACTION_STATE_SCALE = 2;
    private static final int ANIMATION_TYPE_DRAG = 5;
    private static final int ANIMATION_TYPE_SCALE = 6;
    private static final long DEFAULT_DRAG_ANIMATION_DURATION = 200;
    private static final long DEFAULT_SCALE_ANIMATION_DURATION = 300;
    RecyclerView.ViewHolder mSelected;
    private int mActivePointerId;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private int mSelectedStartX;
    private int mSelectedStartY;

    private Layer mLayer;
    /**
     * Animation for recovery if we fail to open it.
     */
    private List<RecoverAnimation> mRecoverAnimations = new ArrayList<>();
    private final float[] mTmpPosition = new float[2];
    /**
     * diff between the last event and initial touch.
     */
    private float mDx, mDy;
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
                mInitialTouchX = e.getX();
                mInitialTouchY = e.getY();
                if (mSelected == null) {
                    final RecoverAnimation animation = findAnimation(e);
                    if (animation != null) {
                        mInitialTouchX -= animation.mX;
                        mInitialTouchY -= animation.mY;
//                        select(animation.mViewHolder, animation.mActionState);
                        updateDxDy(e, 0);
                    }
                }
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
        final int prevActionState = mActionState;
//        endRecoverAnimation(selected, true);
        mActionState = actionState;
        if (mSelected != null) {
            // TODO: play the restore animation.
            final RecyclerView.ViewHolder prevSelected = mSelected;
            final float targetTranslateX = 0, targetTranslateY = 0;
            int animationType;
            if (prevActionState == ACTION_STATE_DRAG) {
                animationType = ANIMATION_TYPE_DRAG;
            } else if (prevActionState == ACTION_STATE_SCALE) {
                animationType = ANIMATION_TYPE_SCALE;
            } else {
                animationType = -1;
            }

            getSelectedDxDy(mTmpPosition);
            final float currentTranslateX = mTmpPosition[0];
            final float currentTranslateY = mTmpPosition[1];

//            final RecoverAnimation rv = new RecoverAnimation(prevSelected, animationType, prevActionState,
//                    currentTranslateX, currentTranslateY, targetTranslateX, targetTranslateY);
//            final long duration = getAnimationDuration(mRecyclerView, animationType, targetTranslateX - currentTranslateX, targetTranslateY - currentTranslateY);
//            rv.setDuration(duration);
//            mRecoverAnimations.add(rv);
//            rv.start();
            mSelected = null;
        }
        if (selected != null) {
            mSelectedStartX = selected.itemView.getLeft();
            mSelectedStartY = selected.itemView.getTop();
            mSelected = selected;
            mLayer = new Layer(mSelected.itemView.getWidth(), mSelected.itemView.getHeight());
            final ViewParent rvParent = mRecyclerView.getParent();
            if (rvParent != null) {
                rvParent.requestDisallowInterceptTouchEvent(mSelected != null);
            }
        }
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

    private void playAnimation(Matrix from, Matrix to) {
        if (mSelected != null) {
            ObjectAnimator objectAnimator =
                    ObjectAnimator.ofObject((ImageView) mSelected.itemView, "ImageMatrix",
                            new MatrixEvaluator(), from, to);
            objectAnimator.setDuration(0);
            objectAnimator.start();
        }
    }


    private void getSelectedDxDy(float[] outPosition) {
        outPosition[0] = ViewCompat.getTranslationX(mSelected.itemView);
        outPosition[1] = ViewCompat.getTranslationY(mSelected.itemView);
    }


    private long getAnimationDuration(RecyclerView recyclerView, int animationType, float v, float v1) {
        final RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
        if (itemAnimator == null) {
            return animationType == ANIMATION_TYPE_DRAG ? DEFAULT_DRAG_ANIMATION_DURATION : DEFAULT_SCALE_ANIMATION_DURATION;
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
            if (hitTest(view, x, y, anim.mX, anim.mY)) {
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
            if (hitTest(view, x, y, anim.mX, anim.mY)) {
                return view;
            }
        }
        return mRecyclerView.findChildViewUnder(x, y);
    }


    private class RecoverAnimation implements AnimatorListenerCompat {

        final Matrix start;
        final Matrix to;


        final RecyclerView.ViewHolder mViewHolder;
        private final ObjectAnimator mObjectAnimator;
        float mX;
        float mFraction;

        float mY;

        boolean mEnded = false;

        public RecoverAnimation(RecyclerView.ViewHolder viewHolder, int actionState, Matrix start, Matrix to) {
            mActionState = actionState;
            mViewHolder = viewHolder;
            this.start = start;
            this.to = to;
            mObjectAnimator = ObjectAnimator.ofObject(mViewHolder.itemView,
                    "imageMatrix", new MatrixEvaluator(), start, to);

        }

        public void setDuration(long duration) {
            mObjectAnimator.setDuration(duration);
        }

        public void start() {
            mViewHolder.setIsRecyclable(false);
            mObjectAnimator.start();
        }

        public void cancel() {
            mObjectAnimator.cancel();
        }

        public void setFraction(float fraction) {
            mFraction = fraction;
        }


        @Override
        public void onAnimationStart(ValueAnimatorCompat animation) {

        }

        @Override
        public void onAnimationEnd(ValueAnimatorCompat animation) {
            if (!mEnded) {
                mViewHolder.setIsRecyclable(true);
            }
            mEnded = true;
        }

        @Override
        public void onAnimationCancel(ValueAnimatorCompat animation) {
            setFraction(1f); //make sure we recover the view's state.
        }

        @Override
        public void onAnimationRepeat(ValueAnimatorCompat animation) {

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
                            mDx = mDy = 0f;
                            Log.d(TAG, "start scale initialTouchX " + mInitialTouchX
                                    + " initialTouchY " + mInitialTouchY);
                            select(vh, ACTION_STATE_DRAG);
                        }
                    } else if (vh == mSelected) {
                        float scaleFactorDiff = detector.getScaleFactor();
                        // TODO: migrate with the same matrix in the move, rotate animation
                        // TODO: add holy scale value
                        Matrix start = ((ImageView) mSelected.itemView).getImageMatrix();
                        mLayer.postScale(scaleFactorDiff - 1.0f);
                        Matrix to = mLayer.getMatrix();
                        playAnimation(start, to);
                    } else {
                        select(vh, ACTION_STATE_DRAG);
                    }
                }
            }
            return Float.compare(mScaleFactor, 1.0f) != 0;
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
        if (mSelected != null) {
            float deltaX = delta.x;
            float deltaY = delta.y;
            if (Float.compare(deltaX, 0.0f) != 0 || Float.compare(deltaY, 0.0f) != 0) {
                Matrix from = mLayer.getMatrix();
                float moveX = deltaX / mSelected.itemView.getWidth();
                float moveY = deltaY / mSelected.itemView.getHeight();
                mLayer.postTranslate(moveX, moveY);
                Matrix to = mLayer.getMatrix();
                playAnimation(from, to);
            }
        }
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

