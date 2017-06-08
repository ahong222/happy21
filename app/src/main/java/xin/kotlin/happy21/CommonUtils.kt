package xin.kotlin.happy21

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation

/**
 * Created by shen on 17/5/29.
 */
class CommonUtils {
    companion object {
        fun getScreenWidth(context: Context): Int {
            return context.resources.displayMetrics.widthPixels
        }

        fun getScreenHeight(context: Context): Int {
            return context.resources.displayMetrics.heightPixels
        }

        fun getStatusBarHeight(context: Context): Int {
            return dpToPx(context, 48)
        }

        fun dpToPx(context: Context, dp: Int): Int {
            return (context.resources.displayMetrics.density * dp).toInt()
        }

        fun getViewTop(view: View): Int {
            var array = IntArray(2)
            view.getLocationInWindow(array)
            return array[1]
        }

        fun removeGlobalLayoutListener(view: View, onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener) {
            try {
                view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
            } catch (e: Exception) {
                view.viewTreeObserver.removeGlobalOnLayoutListener(onGlobalLayoutListener)
            }
        }

        fun getAnimationHideToTop(): Animation {
            return TranslateAnimation(TranslateAnimation.ABSOLUTE, 0F, TranslateAnimation.ABSOLUTE, 0F,
                    TranslateAnimation.RELATIVE_TO_SELF, 0F, TranslateAnimation.RELATIVE_TO_SELF, -1F)
        }

        fun getAnimationShowFromTop(): Animation {
            return TranslateAnimation(TranslateAnimation.ABSOLUTE, 0F, TranslateAnimation.ABSOLUTE, 0F,
                    TranslateAnimation.RELATIVE_TO_SELF, -1F, TranslateAnimation.RELATIVE_TO_SELF, 0F)
        }

        fun getAnimationHideToTop(marginTop: Float): Animation {
            return TranslateAnimation(TranslateAnimation.ABSOLUTE, 0F, TranslateAnimation.ABSOLUTE, 0F,
                    TranslateAnimation.RELATIVE_TO_SELF, 0F, TranslateAnimation.ABSOLUTE, -marginTop)
        }

        fun getAnimationShowFromTop(marginTop: Float): Animation {
            return TranslateAnimation(TranslateAnimation.ABSOLUTE, 0F, TranslateAnimation.ABSOLUTE, 0F,
                    TranslateAnimation.ABSOLUTE, -marginTop, TranslateAnimation.RELATIVE_TO_SELF, 0F)
        }

        fun getAnimationHideToRight(): Animation {
            return TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0F, TranslateAnimation.RELATIVE_TO_SELF, 1F, TranslateAnimation.ABSOLUTE, 0F, TranslateAnimation.ABSOLUTE, 0F)
        }

        fun getAnimationShowFromRight(): Animation {
            return TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 1F, TranslateAnimation.RELATIVE_TO_SELF, 0F, TranslateAnimation.ABSOLUTE, 0F, TranslateAnimation.ABSOLUTE, 0F)
        }

        fun getScaleShow(): Animation {
            return ScaleAnimation(0f, 1f, 0f, 1f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
        }

        fun getScaleHide(): Animation {
            return ScaleAnimation(1f, 0f, 1f, 0f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
        }

        fun getScaleAnimation(fromScale: Float, targetScale: Float): Animation {
            return ScaleAnimation(fromScale, targetScale, fromScale, targetScale,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
        }

        /**
         * 扑克旋转从可见到隐藏
         */
        fun getRotateHideAnimation(): Animation {
            return ScaleAnimation(1f, 0f, 1f, 1f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
        }

        fun getRotateShowAnimation(): Animation {
            return ScaleAnimation(0f, 1f, 1f, 1f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
        }

        fun playHideViewAndShowViewFromTop(view: View, marginTop: Float, delay: Long, callback: View.OnClickListener?) {
            var hideAnimation = CommonUtils.getAnimationHideToTop(marginTop)
            hideAnimation.duration = 0
            hideAnimation.fillAfter = true
            hideAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {
                    var showAnimation = CommonUtils.getAnimationShowFromTop(marginTop)
                    showAnimation.duration = 500
                    showAnimation.fillAfter = true
                    showAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            callback?.onClick(view)
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                        }

                        override fun onAnimationStart(animation: Animation?) {
                            view.visibility = View.VISIBLE
                        }
                    })

                    if (delay > 0) {
                        view.postDelayed({ view.startAnimation(showAnimation) }, delay)
                    } else {
                        view.startAnimation(showAnimation)
                    }

                }

                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationStart(animation: Animation?) {

                }
            })
            view.startAnimation(hideAnimation)
        }
    }

}