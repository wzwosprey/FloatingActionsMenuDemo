package floating;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.example.uf.floatingactionsmenudemo.R;

/**
 * @author wangzhiwen(email:wangzhiwen@anhao.cn)
 * @date 2015-11-10 12:56
 * @package floating
 * @description FloatingActionMenu  TODO(悬浮弹出式菜单)
 * @params TODO(进入界面传参描述)
 */
public class FloatingActionMenu extends ViewGroup {

    //展开方向（上下左右）
    public static final int EXPAND_UP = 0;
    public static final int EXPAND_DOWN = 1;
    public static final int EXPAND_LEFT = 2;
    public static final int EXPAND_RIGHT = 3;

    private static final int ANIMATION_DURATION = 300;//动画持续时间（毫秒ms）
    private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);

    private View mBaseView;//菜单中放入的第一个基础控件

    private int mExpandDirection;//展开方向
    private int mChildviewSpacing;//空白距离

    private boolean mExpanded;//菜单是否打开
    private int mMaxnWidth;//总的最大宽度
    private int mMaxHeight;//总的最大高度
    private int mChildCount;//item数

    private TouchDelegateGroup mTouchDelegateGroup;//委派类 夸大view的点击范围

    private OnFloatingActionsMenuUpdateListener mListener;//展开或收起更新监听

    public interface OnFloatingActionsMenuUpdateListener {
        void onMenuExpanded();//打开

        void onMenuCollapsed();//收起
    }


    public FloatingActionMenu(Context context) {
        this(context, null);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mChildviewSpacing = 8;
        mTouchDelegateGroup = new TouchDelegateGroup(this);
        setTouchDelegate(mTouchDelegateGroup);//设置委派 扩大view的点击范围

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
//        mAddButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.color.holo_blue_dark));
//        mAddButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressed, getColor(android.R.color.holo_blue_light));
//        mAddButtonStrokeVisible = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_addButtonStrokeVisible, true);
        mExpandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP);
        attr.recycle();
        //添加一个基础控件，用于点击展开和收起菜单
        createBaseView(context);
    }

    private void createBaseView(Context context) {
        //可以设置各种布局或控件为菜单的打开控件
         mBaseView = LayoutInflater.from(context).inflate(R.layout.baseview_layout,null);
         //mBaseView = new Button(context);
         // mBaseView.setBackgroundResource(R.drawable.bg_childview);
        mBaseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
        addView(mBaseView, super.generateDefaultLayoutParams());
        mChildCount++;
    }

    /**
     * 添加控件（或组合控件）
     * @param view
     */
    public void addActionsView(View view) {
        addView(view, mChildCount - 1);
        mChildCount++;

    }
    private void toggle() {
        if (mExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        if (!mExpanded) {
            mExpanded = true;
            mTouchDelegateGroup.setEnabled(true);
            mCollapseAnimation.cancel();
            mExpandAnimation.start();

            if (mListener != null) {
                mListener.onMenuExpanded();
            }
        }
    }


    public void collapse() {
        collapse(false);
        //collapse(true);//立刻收起（瞬间）
    }

    private void collapse(boolean immediately) {
        if (mExpanded) {
            mExpanded = false;
            mTouchDelegateGroup.setEnabled(false);
            mCollapseAnimation.setDuration(immediately ? 0 : ANIMATION_DURATION);
            mCollapseAnimation.start();
            mExpandAnimation.cancel();

            if (mListener != null) {
                mListener.onMenuCollapsed();
            }
        }
    }

    /**
     * 测量
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;

        mMaxHeight = 0;
        mMaxnWidth = 0;

        for (int i = 0; i < mChildCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            switch (mExpandDirection) {
                case EXPAND_UP:
                case EXPAND_DOWN:
                    mMaxnWidth = Math.max(mMaxnWidth, child.getMeasuredWidth());
                    height += child.getMeasuredHeight();
                    break;
                case EXPAND_LEFT:
                case EXPAND_RIGHT:
                    width += child.getMeasuredWidth();
                    mMaxHeight = Math.max(mMaxHeight, child.getMeasuredHeight());
                    break;
            }

        }

        if (!expandsHorizontally()) {
            width = mMaxnWidth > 0 ? mMaxnWidth : 0;
        } else {
            height = mMaxHeight;
        }

        switch (mExpandDirection) {
            case EXPAND_UP:
            case EXPAND_DOWN:
                height += mChildviewSpacing * (mChildCount - 1);
                height = adjustForOvershoot(height);
                break;
            case EXPAND_LEFT:
            case EXPAND_RIGHT:
                width += mChildviewSpacing * (mChildCount - 1);
                width = adjustForOvershoot(width);
                break;
        }

        setMeasuredDimension(width, height);
    }

    /**
     * 是否水平方向展开菜单
     * @return
     */
    private boolean expandsHorizontally() {
        return mExpandDirection == EXPAND_LEFT || mExpandDirection == EXPAND_RIGHT;
    }

    /**
     * 弹出一段距离，然后回到原处（一种反弹效果）
     * @param dimension
     * @return
     */
    private int adjustForOvershoot(int dimension) {
        return dimension * 12 / 10;
    }

    /**
     * 布局
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        switch (mExpandDirection) {
            case EXPAND_UP:
            case EXPAND_DOWN:
                boolean expandUp = mExpandDirection == EXPAND_UP;

                if (changed) {
                    mTouchDelegateGroup.clearTouchDelegates();
                }

                int addButtonY = expandUp ? b - t - mBaseView.getMeasuredHeight() : 0;
                // Ensure mAddButton is centered on the line where the buttons should be
                int buttonsHorizontalCenter = mMaxnWidth / 2;
                int addButtonLeft = buttonsHorizontalCenter - mBaseView.getMeasuredWidth() / 2;
                mBaseView.layout(addButtonLeft, addButtonY, addButtonLeft + mBaseView.getMeasuredWidth(), addButtonY + mBaseView.getMeasuredHeight());

                int nextY = expandUp ?
                        addButtonY - mChildviewSpacing :
                        addButtonY + mBaseView.getMeasuredHeight() + mChildviewSpacing;

                for (int i = mChildCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);

                    if (child == mBaseView || child.getVisibility() == GONE) continue;

                    int childX = buttonsHorizontalCenter - child.getMeasuredWidth() / 2;
                    int childY = expandUp ? nextY - child.getMeasuredHeight() : nextY;
                    child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

                    float collapsedTranslation = addButtonY - childY;
                    float expandedTranslation = 0f;

                    //Y轴上移动
                    child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
                    child.setAlpha(mExpanded ? 1f : 0f);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
                    params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
                    params.setAnimationsTarget(child);


                    nextY = expandUp ?
                            childY - mChildviewSpacing :
                            childY + child.getMeasuredHeight() + mChildviewSpacing;
                }
                break;

            case EXPAND_LEFT:
            case EXPAND_RIGHT:
                boolean expandLeft = mExpandDirection == EXPAND_LEFT;

                int addButtonX = expandLeft ? r - l - mBaseView.getMeasuredWidth() : 0;
                // Ensure mAddButton is centered on the line where the buttons should be
                int addButtonTop = b - t - mMaxHeight + (mMaxHeight - mBaseView.getMeasuredHeight()) / 2;
                mBaseView.layout(addButtonX, addButtonTop, addButtonX + mBaseView.getMeasuredWidth(), addButtonTop + mBaseView.getMeasuredHeight());

                int nextX = expandLeft ?
                        addButtonX - mChildviewSpacing :
                        addButtonX + mBaseView.getMeasuredWidth() + mChildviewSpacing;

                for (int i = mChildCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);

                    if (child == mBaseView || child.getVisibility() == GONE) continue;

                    int childX = expandLeft ? nextX - child.getMeasuredWidth() : nextX;
                    int childY = addButtonTop + (mBaseView.getMeasuredHeight() - child.getMeasuredHeight()) / 2;
                    child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

                    float collapsedTranslation = addButtonX - childX;
                    float expandedTranslation = 0f;

                    child.setTranslationX(mExpanded ? expandedTranslation : collapsedTranslation);
                    child.setAlpha(mExpanded ? 1f : 0f);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
                    params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
                    params.setAnimationsTarget(child);

                    nextX = expandLeft ?
                            childX - mChildviewSpacing :
                            childX + child.getMeasuredWidth() + mChildviewSpacing;
                }

                break;
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }
    //OvershootInterpolator向前甩一定值后再回到原来位置
    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

    private class LayoutParams extends ViewGroup.LayoutParams {

        private ObjectAnimator mExpandDir = new ObjectAnimator();
        private ObjectAnimator mExpandAlpha = new ObjectAnimator();
        private ObjectAnimator mCollapseDir = new ObjectAnimator();
        private ObjectAnimator mCollapseAlpha = new ObjectAnimator();
        private boolean animationsSetToPlay;

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);

            //android interpolator含义和用法Interpolator 被用来修饰动画效果，定义动画的变化率，
            //可以使存在的动画效果accelerated(加速)，decelerated(减速),repeated(重复),bounced(弹跳)等。
            mExpandDir.setInterpolator(sExpandInterpolator);//设置甩出一段距离再回到原来位置
            mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
            mCollapseDir.setInterpolator(sCollapseInterpolator);
            mCollapseAlpha.setInterpolator(sCollapseInterpolator);

            mCollapseAlpha.setProperty(View.ALPHA);
            mCollapseAlpha.setFloatValues(1f, 0f);

            mExpandAlpha.setProperty(View.ALPHA);
            mExpandAlpha.setFloatValues(0f, 1f);

            switch (mExpandDirection) {
                case EXPAND_UP:
                case EXPAND_DOWN:
                    mCollapseDir.setProperty(View.TRANSLATION_Y);
                    mExpandDir.setProperty(View.TRANSLATION_Y);
                    break;
                case EXPAND_LEFT:
                case EXPAND_RIGHT:
                    mCollapseDir.setProperty(View.TRANSLATION_X);
                    mExpandDir.setProperty(View.TRANSLATION_X);
                    break;
            }
        }

        public void setAnimationsTarget(View view) {
            mCollapseAlpha.setTarget(view);
            mCollapseDir.setTarget(view);
            mExpandAlpha.setTarget(view);
            mExpandDir.setTarget(view);

            // Now that the animations have targets, set them to be played
            if (!animationsSetToPlay) {
                addLayerTypeListener(mExpandDir, view);
                addLayerTypeListener(mCollapseDir, view);

                mCollapseAnimation.play(mCollapseAlpha);
                mCollapseAnimation.play(mCollapseDir);
                mExpandAnimation.play(mExpandAlpha);
                mExpandAnimation.play(mExpandDir);
                animationsSetToPlay = true;
            }
        }

        private void addLayerTypeListener(Animator animator, final View view) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setLayerType(LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    view.setLayerType(LAYER_TYPE_HARDWARE, null);
                }
            });
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        bringChildToFront(mBaseView);
        mChildCount = getChildCount();

    }

    //设置菜单展开方向
    public void setmExpandDirection(int mExpandDirection) {
        this.mExpandDirection = mExpandDirection;
    }
}

