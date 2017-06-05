package xin.kotlin.happy21.game

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.ifnoif.happy21.SoundPoolManager
import kotlinx.android.synthetic.main.game_layout.view.*
import kotlinx.android.synthetic.main.information_layout.view.*
import xin.kotlin.happy21.CommonUtils
import xin.kotlin.happy21.R

/**
 * Created by shen on 17/5/28.
 */
class InformationView : FrameLayout {
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.information_layout, this)

        (betLayout.layoutParams as RelativeLayout.LayoutParams).bottomMargin = CommonUtils.getScreenWidth(context!!) / 4 + resources.getDimension(R.dimen.information_bottom_height).toInt()
        hideAlertInformation(0L)

        (resultInformation.layoutParams as RelativeLayout.LayoutParams).topMargin = (CommonUtils.getScreenHeight(context!!) - CommonUtils.getScreenWidth(context!!) / 4) / 2
        hideResult(0L)
    }

    fun showScore(score: Int) {
        scoreView.text = context.getString(R.string.current_score, score.toString())
    }

    fun showBetScore(score: Int) {
        betScoreView.text = resources.getString(R.string.bet_score, score.toString())
    }

    enum class Info {
        BET,
    }

    fun alertInformation(text: String) {
        topAlertTextView.text = text
        var animation: Animation = CommonUtils.getAnimationShowFromTop()
        animation.duration = 500
        animation.fillAfter = true
        topAlertLayout.startAnimation(animation)
    }

    fun hideAlertInformation(duration: Long) {
        var animation: Animation = CommonUtils.getAnimationHideToTop()
        animation.duration = duration
        animation.fillAfter = true
        topAlertLayout.startAnimation(animation)
    }

    fun onShuffle() {
        SoundPoolManager.play(GameView.MusicType.ShuffleAll.name)
        hideAlertInformation(500L)
    }

    fun showResult(pointResult: PointResult) {
        resultInformation.setImageResource(pointResult.resId)
        var animation = CommonUtils.getScaleShow()
        animation.duration = 500
        animation.fillAfter = true
        resultInformation.startAnimation(animation)

        if (parent?.parent is GameView) {
            (parent.parent as GameView).controllerView.showHalfBlackBackground()
        }
    }

    fun hideResult() {
        hideResult(500)
    }

    fun hideResult(duration: Long) {
        var animation = CommonUtils.getScaleHide()
        animation.duration = duration
        animation.fillAfter = true
        resultInformation.startAnimation(animation)
        if (parent?.parent is GameView) {
            (parent.parent as GameView).controllerView.hideHalfBlackBackground()
        }
    }

    enum class PointResult(val resId: Int) {
        UserWin(R.drawable.point_user_win),
        UserBobLose(R.drawable.point_user_bob_lose),
        BankerBobLose(R.drawable.point_banker_bob_lose),
        BankerWin(R.drawable.point_banker_win),
        Draw(R.drawable.point_draw),
        BlackJack(R.drawable.point_black_jack),
    }
}