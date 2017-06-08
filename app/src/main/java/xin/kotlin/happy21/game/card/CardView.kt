package xin.kotlin.happy21.game.card

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ifnoif.happy21.SoundPoolManager
import kotlinx.android.synthetic.main.back_card_layout.view.*
import kotlinx.android.synthetic.main.card_layout.view.*
import xin.kotlin.happy21.CommonUtils
import xin.kotlin.happy21.L
import xin.kotlin.happy21.R
import xin.kotlin.happy21.game.GameView


/**
 * Created by shen on 17/5/29.
 */
class CardView : FrameLayout {
    var isHitSecond = false

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.card_layout, this)
        var layoutParams = userCardsLayout.layoutParams as LinearLayout.LayoutParams
        layoutParams.bottomMargin = CommonUtils.getScreenWidth(context!!) / 2

        var layoutParams2 = userCardsLayout2.layoutParams as LinearLayout.LayoutParams
        layoutParams2.bottomMargin = layoutParams.bottomMargin

        blackCard = context.resources.getDrawable(R.drawable.card_back, context.theme)
        bankerCardsLayout.minimumHeight = blackCard.intrinsicHeight
    }

    var resArray: Array<String> = arrayOf("card_diamonds_", "card_clubs_", "card_hearts_", "card_spades_")
    var marginPx = CommonUtils.dpToPx(context, 25)
    lateinit var blackCard: Drawable
    fun getCardRes(card: Int): Int {
        var cardValue = card % 13
        cardValue = (if (cardValue == 0) 13 else cardValue)
        var color = (card - 1) / 13
        return resources.getIdentifier(resArray[color] + cardValue.toString(), "drawable", context.packageName)
    }

    fun setUserCard(viewGroup: ViewGroup, cards: ArrayList<Int>, callback: OnClickListener?) {
        viewGroup.removeAllViews()
        viewGroup.layoutParams.width = 0

        var pointWidth = addPointHintView(viewGroup, "")
        viewGroup.layoutParams.width = pointWidth

        for (i in 0..cards.size - 1) {
            var imageView = ImageView(context)
            var drawable = context.getDrawable(getCardRes(cards[i]))
            imageView.setImageDrawable(drawable)
            if (cards.size > 1) {
                imageView.visibility = View.GONE
            }

            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.CENTER
            layoutParams.leftMargin = if (i == 0) 0 else -(drawable.intrinsicWidth - marginPx)

            updateViewWidth(viewGroup, if (i == 0) drawable.intrinsicWidth else marginPx)
            viewGroup.addView(imageView, layoutParams)

            if (cards.size > 1) {
                CommonUtils.playHideViewAndShowViewFromTop(imageView, CommonUtils.getViewTop(viewGroup).toFloat(), if (i == cards.size - 1) (if (cards.size == 1) 500L else 60) else 0, if (i == cards.size - 1) callback else null)
            }
        }
    }

    fun addUserCard(card: Int, callback: View.OnClickListener?) {
        L.d("CardView addUserCard card:${card}")
        addCard(card, if (isHitSecond) userCardsLayout2 else userCardsLayout, callback)
    }

    fun addCard(card: Int, viewGroup: ViewGroup, callback: OnClickListener?) {
        var imageView = ImageView(context)
        var drawable = context.getDrawable(getCardRes(card))
        imageView.setImageDrawable(drawable)
        imageView.visibility = View.GONE

        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.leftMargin = -(drawable.intrinsicWidth - marginPx)

        updateViewWidth(viewGroup, marginPx)
        viewGroup.addView(imageView, layoutParams)
        SoundPoolManager.play(GameView.MusicType.ShuffleSingle.name)

        CommonUtils.playHideViewAndShowViewFromTop(imageView, CommonUtils.getViewTop(viewGroup).toFloat(), 0L, callback)
    }

    fun updateViewWidth(view: View, addWidth: Int) {
        var layoutParams = view.layoutParams
        L.d("updateViewWidth width:${layoutParams.width}, addWidth:${addWidth}")
        layoutParams.width = (if (layoutParams.width > 0) layoutParams.width else 0) + addWidth
    }

    fun setBankerCard(cards: ArrayList<Int>) {
        bankerCardsLayout.removeAllViews()
        bankerCardsLayout.layoutParams.width = 0

        var pointWidth = addPointHintView(bankerCardsLayout, "")
        bankerCardsLayout.layoutParams.width = pointWidth

        for (i in 0..cards.size - 1) {
            var itemView: View
            var drawable: Drawable = context.getDrawable(getCardRes(cards[i]))
            if (i != 0) {
                var view = View.inflate(context, R.layout.back_card_layout, null)
                view.backView.setImageDrawable(blackCard)
                view.frontView.setImageDrawable(drawable)
                view.setTag(R.id.cardView, cards[i])

                itemView = view
            } else {
                var imageView = ImageView(context)
                imageView.setImageDrawable(drawable)
                itemView = imageView
            }

            itemView.visibility = View.GONE

            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.leftMargin = if (i == 0) 0 else -(drawable.intrinsicWidth - marginPx)

            updateViewWidth(bankerCardsLayout, if (i == 0) drawable.intrinsicWidth else marginPx)
            bankerCardsLayout.addView(itemView, layoutParams)

            if (i == 0) {
                CommonUtils.playHideViewAndShowViewFromTop(itemView, CommonUtils.getViewTop(bankerCardsLayout).toFloat(), 0L, null)
            } else {
                CommonUtils.playHideViewAndShowViewFromTop(itemView, CommonUtils.getViewTop(bankerCardsLayout).toFloat(), 60L, null)
            }
        }
    }

    fun addBankerCard(card: Int, callback: View.OnClickListener?) {
        L.d("CardView addBankerCard card:${card}")
        addCard(card, bankerCardsLayout, callback)
    }

    //翻开庄家第二张牌
    fun checkOverBankerCard(callback: View.OnClickListener) {
        if (bankerCardsLayout.childCount == 3) {
            var view: View = bankerCardsLayout.getChildAt(2)
            var tag = view.getTag(R.id.cardView)
            if (tag is Int) {
                turnCard(view, R.id.backView, R.id.frontView, callback)
                view.setTag(R.id.cardView, null)
                return
            }
        }
        callback.onClick(this)
    }

    fun reset() {
        userCardsLayout.removeAllViews()
        userCardsLayout.layoutParams.width = 0
        userCardsLayout2.removeAllViews()
        userCardsLayout2.layoutParams.width = 0
        bankerCardsLayout.removeAllViews()
        bankerCardsLayout.layoutParams.width = 0

        userCardsLayout2.visibility = View.GONE
        isHitSecond = false
    }

    fun turnCard(cardRootView: View, backId: Int, frontId: Int, callbacks: View.OnClickListener) {
        val hideAnimation = CommonUtils.getRotateHideAnimation()
        hideAnimation.duration = 500
        hideAnimation.fillAfter = true

        val showAnimation = CommonUtils.getRotateShowAnimation()
        showAnimation.duration = 500
        showAnimation.fillAfter = true

        val frontView = cardRootView.findViewById(frontId)
        val animationListener = object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                callbacks.onClick(cardRootView)
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
            }
        }
        showAnimation.setAnimationListener(animationListener)

        hideAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                frontView.startAnimation(showAnimation)
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
                var hideAnimation = CommonUtils.getRotateHideAnimation()
                hideAnimation.duration = 0
                hideAnimation.fillAfter = true
                frontView.startAnimation(hideAnimation)
            }
        })
        cardRootView.findViewById(backId).startAnimation(hideAnimation)
    }

    fun showUserPoint(point: Int) {
        if (isHitSecond) {
            if (userCardsLayout2.childCount > 0 && userCardsLayout2.getChildAt(0) is TextView) {
                (userCardsLayout2.getChildAt(0) as TextView).text = point.toString()
            }
        } else {
            if (userCardsLayout.childCount > 0 && userCardsLayout.getChildAt(0) is TextView) {
                (userCardsLayout.getChildAt(0) as TextView).text = point.toString()
            }
        }
    }

    fun showBankerPoint(point: Int) {
        if (bankerCardsLayout.childCount > 0 && bankerCardsLayout.getChildAt(0) is TextView) {
            (bankerCardsLayout.getChildAt(0) as TextView).text = point.toString()
        }
    }

    fun addPointHintView(viewGroup: ViewGroup, initStr: String?): Int {
        var pointHintView = TextView(context)
        pointHintView.gravity = Gravity.CENTER
        var drawable = context.getDrawable(R.drawable.alert_small_bg)
        pointHintView.background = drawable
        initStr?.let { pointHintView.text = it }

        var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.topMargin = CommonUtils.dpToPx(context, 5)
        viewGroup.addView(pointHintView, layoutParams)
        return drawable.intrinsicWidth
    }

    fun onSplit(card: Int) {
        var view = userCardsLayout.getChildAt(2)
        userCardsLayout.removeView(view)

        userCardsLayout2.visibility = View.VISIBLE
        setUserCard(userCardsLayout2, arrayListOf(card), null)
    }
}