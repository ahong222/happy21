package xin.kotlin.happy21.game

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.Toast
import com.ifnoif.happy21.SoundPoolManager
import kotlinx.android.synthetic.main.card_layout.view.*
import kotlinx.android.synthetic.main.controller_layout.view.*
import kotlinx.android.synthetic.main.game_layout.view.*
import kotlinx.android.synthetic.main.information_layout.view.*
import xin.kotlin.happy21.CommonUtils
import xin.kotlin.happy21.L
import xin.kotlin.happy21.R
import xin.kotlin.happy21.game.coin.CoinView.CoinCallback

/**
 * Created by shen on 17/5/28.
 */
class GameView : FrameLayout {
    var gameUtils = GameUtils()
    /**
     * 最开始时扑克牌总数
     */
    var totalCard = 0
    /**
     * 剩余的牌
     */
    lateinit var cardList: ArrayList<Int>

    /**
     * 状态机
     */
    var state = State.IDLE
    /**
     * 当前积分
     */
    var currentScore: Int = 0
    var betScore: Int = 0

    var gameViewCallback: GameViewCallback? = null

    enum class MusicType(val resId: Int) {
        Bet(R.raw.sound_bet),
        ShuffleSingle(R.raw.sound_shuffle_single),
        Count(R.raw.sound_count),
        Win(R.raw.sound_win),
        Background(R.raw.sound_background),
        Pressed(R.raw.sound_pressed),
        Cheer0(R.raw.sound_cheer0),
        ShuffleAll(R.raw.sound_shuffle_all),
        Lost(R.raw.sound_lost),
        Draw(R.raw.sound_draw)
    }

    val musicArray = arrayOf(MusicType.Bet, MusicType.ShuffleSingle, MusicType.Count, MusicType.Win, MusicType.Background, MusicType.Pressed, MusicType.Cheer0, MusicType.ShuffleAll, MusicType.Lost, MusicType.Draw)


    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.game_layout, this)

        initData()
        initView()
    }

    fun initData() {
        gameUtils.includeKing = false
        gameUtils.cardCount = 4
        cardList = gameUtils.getWashedCards()
        totalCard = cardList.size

        for (musicType in musicArray) {
            SoundPoolManager.init(context, musicType.name, musicType.resId)
        }
    }

    fun initView() {
        coinView.coinCallback = object : CoinCallback {
            override fun onPlayedCoin(betCoin: Int, totalBetCoin: Int) {
                updateShuffleButton(totalBetCoin > 0)
                betScore = totalBetCoin
                informationView.showBetScore(betScore)
                informationView.showScore(currentScore - betScore)
            }
        }

        controllerView.actionCallback = actionCallback

        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    val globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            //设置筹码的位置
            var userCardsLayoutTop = CommonUtils.getViewTop(userCardsLayout)
            if (userCardsLayoutTop != 0) {
                CommonUtils.removeGlobalLayoutListener(this@GameView, this)
                L.d("initView userCardsLayoutTop:$userCardsLayoutTop")
                coinView.setLocation(CommonUtils.getViewTop(userCardsLayout) + cardView.blackCard.intrinsicHeight * 3 / 2, CommonUtils.getViewTop(informationView.betLayout) + informationView.betLayout.height / 2)
            }
        }
    }


    enum class State {
        IDLE,
        DONE
    }


    fun init() {
        updateDate(State.DONE)
    }

    fun updateScore(score: Int) {
        currentScore = score

        informationView.showScore(currentScore)
    }

    fun updateDate(state: State) {
        this.state = state
        when (state) {
            State.DONE -> {
                betScore = 0
                coinView.reset()
                coinView.setMaxCoin(currentScore)
                informationView.hideResult()
                informationView.alertInformation("请下注")
                controllerView.hideAllAction()
                informationView.showBetScore(0)

                cardView.reset()

            }
        }
    }

    fun updateShuffleButton(show: Boolean) {
        if (show) {
            controllerView.showActionShuffle()
        } else {
            controllerView.hideActionShuffle()
        }
    }

    lateinit var userCards: ArrayList<Int>
    lateinit var bankerCards: ArrayList<Int>

    fun showUserCard(array: ArrayList<Int>) {
        cardView.setUserCard(array)
        checkUserCards()
    }

    fun showBankerCard(array: ArrayList<Int>) {
        cardView.setBankerCard(array)
    }

    fun checkUserCards() {
        controllerView.hideAllAction()

        var point = calculatePoint(userCards)
        showUserPoint(point)
        //TODO 分牌检查
        if (point < 21) {
            //TODO 双倍
            showOperation(Operation.Hit, Operation.Stand)
        } else if (point > 21) {
            //玩家爆掉
            cardView.checkOverBankerCard(View.OnClickListener { checkWinner() })
        } else {
            //玩家21点,开始看庄家
            stand()
        }
    }

    /**
     * 检查庄家点数
     */
    fun checkBankCards() {
        L.d("checkBankCards")
        //TODO 检查保险
        cardView.checkOverBankerCard(object:OnClickListener {
            override fun onClick(v: View?) {
                var point = calculatePoint(bankerCards)
                showBankerPoint(point)
                if (point <= 16) {
                    //小于16点必须
                    onBankerHit()
                } else {
                    checkWinner()
                }
            }
        })
    }

    fun checkWinner() {
        L.d("checkWinner")

        var bankerPoint = calculatePoint(bankerCards)
        var userPoint = calculatePoint(userCards)
        var raiseScore = 0
        if (userPoint > 21) {
            //玩家爆掉，庄家赢了
            informationView.showResult(InformationView.PointResult.UserBobLose)
            raiseScore = -betScore

            SoundPoolManager.play(GameView.MusicType.Lost.name)
        } else if (bankerPoint > 21) {
            //庄家爆掉，玩家赢了
            informationView.showResult(InformationView.PointResult.BankerBobLose)
            raiseScore = betScore
            if (isBlackJack(userPoint)) {
                SoundPoolManager.play(GameView.MusicType.Win.name)
            } else {
                SoundPoolManager.play(GameView.MusicType.Cheer0.name)
            }
        } else if (bankerPoint == userPoint) {
            //平手
            informationView.showResult(InformationView.PointResult.Draw)
            SoundPoolManager.play(GameView.MusicType.Draw.name)
        } else if (bankerPoint > userPoint) {
            //庄家赢了
            informationView.showResult(InformationView.PointResult.BankerWin)
            raiseScore = -betScore
            SoundPoolManager.play(GameView.MusicType.Lost.name)
        } else {
            //玩家赢了
            informationView.showResult(InformationView.PointResult.UserWin)
            raiseScore = betScore
            if (isBlackJack(userPoint)) {
                SoundPoolManager.play(GameView.MusicType.Win.name)
            } else {
                SoundPoolManager.play(GameView.MusicType.Cheer0.name)
            }

        }

        var newScore = currentScore + raiseScore
        updateScore(newScore)
        gameViewCallback?.onScoreChanged(newScore)
        controllerView.showActionStart()
    }

    /**
     * 计算牌点数
     */
    fun calculatePoint(cards: ArrayList<Int>): Int {
        var ACount = 0//有几张A
        var result = 0
        for (card in cards) {
            var point = getPoint(card)
            result += point
            if (point == 1) {
                ACount++
            }
        }
        if (result <= 11 && ACount > 0) {
            result += 10;
        }
        return result
    }

    /**
     * 单张牌所表示的点数
     */
    fun getPoint(card: Int): Int {
        var cardValue = card % 13
        cardValue = if (cardValue == 0) 13 else cardValue
        return (if (cardValue > 10) 10 else cardValue)
    }

    fun showUserPoint(point: Int) {
        cardView.showUserPoint(point)
    }

    fun showBankerPoint(point: Int) {
        cardView.showBankerPoint(point)
    }

    fun showOperation(vararg operations: Operation) {
        for (operation in operations) {
            when (operation) {
                Operation.Insurance -> {
                    //TODO 显示保险
                    controllerView.actionInsurance.visibility = View.VISIBLE
                }
                Operation.Split -> {
                    controllerView.actionSplit.visibility = View.VISIBLE
                }
                Operation.Double -> {
                    controllerView.actionDouble.visibility = View.VISIBLE
                }
                Operation.Hit -> {
                    controllerView.actionHit.visibility = View.VISIBLE
                }
                Operation.Stand -> {
                    controllerView.actionStand.visibility = View.VISIBLE
                }
            }
        }
    }

    enum class Operation {
        Hit, //拿牌
        Stand, //停牌
        Double, //双倍
        Split, //分牌
        Insurance//保险
    }

    var actionCallback = object : ControllerView.ActionCallback {
        override fun onInsurance() {


        }

        override fun onStart() {
            init()
            SoundPoolManager.play(GameView.MusicType.Pressed.name)
        }

        override fun onShuffle() {
            shuffle()
        }

        override fun onSplit() {

        }

        override fun onDouble() {


        }

        override fun onHit() {
            var card = cardList.removeAt(0)
            userCards.add(card)
            cardView.addUserCard(card)
            checkUserCards()
        }

        override fun onStand() {
            stand()
        }
    }

    /**
     * 发牌
     */
    fun shuffle() {
        if (cardList.size < totalCard / 2) {
            cardList = gameUtils.getWashedCards()
        }
        userCards = arrayListOf(cardList.removeAt(0), cardList.removeAt(0))//arrayListOf(1, 10)//
        bankerCards = arrayListOf(cardList.removeAt(0), cardList.removeAt(0))
        showUserCard(userCards!!)
        showBankerCard(bankerCards!!)
        coinView.onCompleteBet()

        informationView.onShuffle()
    }

    fun stand() {
        controllerView.hideAllAction()

        checkBankCards()
    }

    /**
     * 专家要牌
     */
    fun onBankerHit() {
        var card = cardList.removeAt(0)
        bankerCards.add(card)
        cardView.addBankerCard(card, View.OnClickListener { checkBankCards() })

    }

    fun isBlackJack(userPoint: Int): Boolean {
        if (userPoint == 21 && userCards.size == 2) {
            return true
        }
        return false
    }
}