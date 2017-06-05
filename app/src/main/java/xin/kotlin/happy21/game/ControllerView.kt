package xin.kotlin.happy21.game

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.controller_layout.view.*
import xin.kotlin.happy21.CommonUtils
import xin.kotlin.happy21.L
import xin.kotlin.happy21.R

/**
 * Created by shen on 17/5/29.
 */
class ControllerView : FrameLayout {
    var isShowingActionShuffle = true
    var isShowingActionStart = true
    val DURATION = 500L

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.controller_layout, this)

        var layoutParams = bottomController.layoutParams
        layoutParams.height = CommonUtils.getScreenWidth(context!!) / 4

        actionSplit.setOnClickListener {
            actionCallback?.onSplit()
        }

        actionDouble.setOnClickListener {
            actionCallback?.onDouble()
        }

        actionHit.setOnClickListener {
            actionCallback?.onHit()
        }

        actionStand.setOnClickListener {
            actionCallback?.onStand()
        }

        actionStart.setOnClickListener {
            actionCallback?.onStart()
        }

        actionInsurance.setOnClickListener {
            actionCallback?.onInsurance()
        }

        actionShuffle.setOnClickListener {
            actionCallback?.onShuffle()
            hideRightLayout(DURATION)
        }

        hideAllAction(0L)
    }

    var actionCallback: ActionCallback? = null

    interface ActionCallback {
        fun onShuffle()
        fun onInsurance()
        fun onStart()
        fun onHit()
        fun onStand()
        fun onDouble()
        fun onSplit()
    }

    fun hideAllAction() {
        hideAllAction(DURATION)
    }

    fun hideAllAction(duration: Long) {
        actionInsurance.visibility = View.INVISIBLE
        actionSplit.visibility = View.INVISIBLE
        actionDouble.visibility = View.INVISIBLE
        actionHit.visibility = View.INVISIBLE
        actionStand.visibility = View.INVISIBLE
        hideRightLayout(duration)
    }

    fun showActionStart() {
        if (isShowingActionStart) {
            return
        }
        isShowingActionStart = true

        L.d("ControllerView showActionStart")
        if (isShowingActionShuffle) {
            isShowingActionShuffle = false
        } else {
            showRightLayout()
        }

        actionStart.visibility = View.VISIBLE
        actionShuffle.visibility = View.GONE
    }

    fun showActionShuffle() {
        if (isShowingActionShuffle) {
            return
        }
        isShowingActionShuffle = true
        L.d("ControllerView showActionShuffle")
        if (isShowingActionStart) {
            isShowingActionStart = false
        } else {
            showRightLayout()
        }

        actionStart.visibility = View.GONE
        actionShuffle.visibility = View.VISIBLE
    }

    fun hideActionShuffle() {
        hideRightLayout(DURATION)
    }

    fun showRightLayout() {
        var animation: Animation = CommonUtils.getAnimationShowFromRight()
        animation.duration = DURATION
        animation.fillAfter = true
        actionRightLayout.startAnimation(animation)
    }

    fun hideRightLayout(duration: Long) {
        if (!isShowingActionStart && !isShowingActionShuffle) {
            return
        }
        L.d("ControllerView hideRightLayout duration:${duration}")
        var animation: Animation = CommonUtils.getAnimationHideToRight()
        animation.duration = duration
        animation.fillAfter = true
        actionRightLayout.startAnimation(animation)

        isShowingActionShuffle = false
        isShowingActionStart = false
    }

    fun showHalfBlackBackground() {
        setBackgroundResource(R.color.half_back)
    }

    fun hideHalfBlackBackground() {
        setBackgroundResource(R.color.transparent)
    }

    fun onSplit() {
        hideAction(actionSplit)
        hideAction(actionDouble)
    }

    fun hideAction(actionView:View) {
        actionView.visibility = View.INVISIBLE
    }
}