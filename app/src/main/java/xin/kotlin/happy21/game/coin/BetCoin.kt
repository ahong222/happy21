package xin.kotlin.happy21.game.coin

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.ifnoif.happy21.SoundPoolManager
import xin.kotlin.happy21.CommonUtils
import xin.kotlin.happy21.R
import xin.kotlin.happy21.game.GameView

/**
 * Created by shen on 17/5/28.
 */
class BetCoin : FrameLayout {


    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    val marginGap = CommonUtils.dpToPx(context, 5)

    var betCallback: BetCallback? = null

    fun addCoin(score: Int) {
        var view = View(context)
        view.setBackgroundResource(CoinView.Constants.coinMap[score] ?: 0)
        val width = CommonUtils.getScreenWidth(context) / 4 - (context.resources.getDimensionPixelSize(R.dimen.coin_padding) * 2)
        val layoutParams = FrameLayout.LayoutParams(width, width)
        layoutParams.gravity = Gravity.CENTER
        layoutParams.leftMargin = marginGap * Math.min(childCount, 5)
        view.isSoundEffectsEnabled = false
        view.setOnClickListener {
            SoundPoolManager.play(GameView.MusicType.Bet.name)
            removeView(view)
            betCallback?.onResetBet(score)
        }
        addView(view, layoutParams)
    }

    fun reset() {
        translationY = 0F
        scaleX = 1F
        scaleY = 1F
        removeAllViews()
    }

    fun removeClickCoinListener() {
        for (i in 0..childCount - 1) {
            getChildAt(i).setOnClickListener(null)
        }
    }

    interface BetCallback {
        /**
         * 减少跟住
         */
        fun onResetBet(score: Int)
    }
}