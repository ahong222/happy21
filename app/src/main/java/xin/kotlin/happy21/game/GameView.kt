package xin.kotlin.happy21.game

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.ifnoif.happy21.SoundPoolManager
import kotlinx.android.synthetic.main.card_layout.view.*
import kotlinx.android.synthetic.main.controller_layout.view.*
import kotlinx.android.synthetic.main.game_layout.view.*
import kotlinx.android.synthetic.main.information_layout.view.*
import xin.kotlin.happy21.CommonUtils
import xin.kotlin.happy21.L
import xin.kotlin.happy21.R
import xin.kotlin.happy21.game.coin.CoinView.CoinCallback
import java.util.*

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
    var isDouble = false
    var isSplit = false
    var isHitSecond = false

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

        userCards = arrayListOf()
        userCards2 = arrayListOf()
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
                coinView.setLocation(CommonUtils.getViewTop(userCardsLayout) + cardView.blackCard.intrinsicHeight / 2, CommonUtils.getViewTop(informationView.betLayout) + informationView.betLayout.height / 2)
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
                isDouble = false
                isSplit = false
                isHitSecond = false
                coinView.reset()
                coinView.setMaxCoin(currentScore)
                informationView.hideResult()
                informationView.alertInformation()
                controllerView.hideAllAction()
                informationView.showBetScore(0)

                cardView.reset()
                userCards.clear()
                userCards2.clear()

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
    lateinit var userCards2: ArrayList<Int>
    lateinit var bankerCards: ArrayList<Int>

    fun showUserCard(array: ArrayList<Int>) {
        cardView.setUserCard(userCardsLayout, array, View.OnClickListener { checkUserCards() })
        controllerView.hideAllAction()
    }

    fun showBankerCard(array: ArrayList<Int>) {
        cardView.setBankerCard(array)
    }

    fun checkUserCards() {
        var point = if (isHitSecond) calculatePoint(userCards2) else calculatePoint(userCards)
        showUserPoint(point)
        var isHitFirst = isSplit && !isHitSecond
        var isHitSecond = isSplit && isHitSecond
        //TODO 分牌检查
        if (point < 21) {
            //TODO 双倍
            if (isHitFirst) {
                showOperation(Operation.Hit, Operation.Stand)
            } else if (isHitSecond) {
                showOperation(Operation.Hit, Operation.Stand)
            } else {
                if (isDouble) {
                    stand()
                } else {
                    if (gameUtils.getPoint(userCards[0]) == gameUtils.getPoint(userCards[1]) && currentScore >= betScore) {
                        showOperation(Operation.Split, Operation.Double, Operation.Hit, Operation.Stand)
                    } else {
                        showOperation(Operation.Double, Operation.Hit, Operation.Stand)
                    }
                }
            }

        } else if (point > 21) {
            //玩家爆掉
            if (isHitFirst) {
                onHitSecond()
            } else {
                checkOverBankerCard(View.OnClickListener { checkWinner() })
            }
        } else {
            if (isHitFirst) {
                onHitSecond()
            } else if (isHitSecond) {
                stand()
            } else {
                //玩家21点,开始看庄家
                if (userCards.size == 2) {
                    controllerView.hideAllAction()
                    checkWinner()//black jack
                } else {
                    stand()
                }
            }
        }
    }

    fun onHitSecond() {
        isHitSecond = true
        cardView.isHitSecond = true

        hit()
    }

    fun hit() {
        var card = cardList.removeAt(0)
        if (isHitSecond) {
            userCards2.add(card)
            cardView.addUserCard(card, View.OnClickListener { checkUserCards() })
        } else {
            userCards.add(card)
            cardView.addUserCard(card, View.OnClickListener { checkUserCards() })
        }
    }

    /**
     * 检查庄家点数
     */
    fun checkBankCards() {
        L.d("GameView checkBankCards")
        //TODO 检查保险
        checkOverBankerCard(object : OnClickListener {
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

    fun checkOverBankerCard(callback: View.OnClickListener) {
        controllerView.hideAllAction()
        cardView.checkOverBankerCard(callback)
    }

    fun checkWinner() {
        var bankerPoint = calculatePoint(bankerCards)
        var userPoint = calculatePoint(userCards)
        var userPoint2 = calculatePoint(userCards2)
        L.d("GameView checkWinner,bankerPoint:${bankerPoint},userPoint:${userPoint},userPoint2:${userPoint2}")
        var raiseScore = 0
        if (isSplit) {
            var maxUserPoint = Math.max((if (userPoint > 21) 0 else userPoint), (if (userPoint2 > 21) 0 else userPoint2))
            if (userPoint > 21 && userPoint2 > 21) {
                L.d("玩家爆掉，庄家赢了")
                informationView.showResult(InformationView.PointResult.UserBobLose)
                raiseScore = -betScore

                SoundPoolManager.play(GameView.MusicType.Lost.name)
            } else if (bankerPoint > 21 || bankerPoint <= maxUserPoint) {
                L.d("庄家爆掉，玩家赢了")
                raiseScore = betScore
                if (isBlackJack(userPoint)) {
                    raiseScore += betScore / 4
                }

                if (isBlackJack(userPoint2)) {
                    raiseScore += betScore / 4
                }
                if (raiseScore > betScore) {
                    SoundPoolManager.play(GameView.MusicType.Win.name)
                    informationView.showResult(InformationView.PointResult.BlackJack)
                } else {
                    SoundPoolManager.play(GameView.MusicType.Cheer0.name)
                    informationView.showResult(InformationView.PointResult.BankerBobLose)
                }
            } else if (bankerPoint > maxUserPoint) {
                L.d("庄家赢了")
                informationView.showResult(InformationView.PointResult.BankerWin)
                raiseScore = -betScore
                SoundPoolManager.play(GameView.MusicType.Lost.name)
            }
        } else {
            if (userPoint > 21) {
                L.d("玩家爆掉，庄家赢了")
                informationView.showResult(InformationView.PointResult.UserBobLose)
                raiseScore = -betScore

                SoundPoolManager.play(GameView.MusicType.Lost.name)
            } else if (bankerPoint > 21) {
                L.d("庄家爆掉，玩家赢了")
                if (isBlackJack(userPoint)) {
                    raiseScore = betScore * 3 / 2
                    SoundPoolManager.play(GameView.MusicType.Win.name)
                    informationView.showResult(InformationView.PointResult.BlackJack)
                } else {
                    raiseScore = betScore
                    SoundPoolManager.play(GameView.MusicType.Cheer0.name)
                    informationView.showResult(InformationView.PointResult.BankerBobLose)
                }
            } else if (bankerPoint == userPoint && !isBlackJack(userPoint)) {
                L.d("平手")
                informationView.showResult(InformationView.PointResult.Draw)
                SoundPoolManager.play(GameView.MusicType.Draw.name)
            } else if (bankerPoint > userPoint) {
                L.d("庄家赢了")
                informationView.showResult(InformationView.PointResult.BankerWin)
                raiseScore = -betScore
                SoundPoolManager.play(GameView.MusicType.Lost.name)
            } else {
                L.d("玩家赢了")
                if (isBlackJack(userPoint)) {
                    raiseScore = betScore * 3 / 2
                    SoundPoolManager.play(GameView.MusicType.Win.name)
                    informationView.showResult(InformationView.PointResult.BlackJack)
                } else {
                    raiseScore = betScore
                    SoundPoolManager.play(GameView.MusicType.Cheer0.name)
                    informationView.showResult(InformationView.PointResult.UserWin)
                }
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
            var point = gameUtils.getPoint(card)
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
                    if (currentScore >= betScore) {
                        //积分足够
                        controllerView.actionDouble.visibility = View.VISIBLE
                    }
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
            coinView.coinCallback?.onPlayedCoin(betScore, betScore * 2)
            isSplit = true
            var card = userCards.removeAt(1)
            userCards2.add(card)
            cardView.onSplit(card)

            controllerView.onSplit()
            onHit()
        }

        override fun onDouble() {
            coinView.coinCallback?.onPlayedCoin(betScore, betScore * 2)
            //拿一张牌后停牌
            isDouble = true
            controllerView.hideAllAction()
            onHit()
        }

        override fun onHit() {
            hit()
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
        var blackJackList = if (Random().nextInt(20) == 1) gameUtils.blackJack(cardList) else null

        userCards = blackJackList ?: arrayListOf(cardList.removeAt(0), cardList.removeAt(0))//arrayListOf(1, 10)//
        bankerCards = arrayListOf(cardList.removeAt(0), cardList.removeAt(0))
        showUserCard(userCards!!)
        showBankerCard(bankerCards!!)
        coinView.onCompleteBet()

        informationView.onShuffle()
    }

    fun stand() {
        if (isSplit && !isHitSecond) {
            onHitSecond()
        } else {
            controllerView.hideAllAction()

            checkBankCards()
        }

    }

    /**
     * 庄家要牌
     */
    fun onBankerHit() {
        L.d("GameView onBankerHit")
        var card = cardList.removeAt(0)
        bankerCards.add(card)
        cardView.addBankerCard(card, View.OnClickListener {
            L.d("GameView onBankerHit callback");
            checkBankCards()
        })
    }

    fun isBlackJack(userPoint: Int): Boolean {
        if (userPoint == 21 && userCards.size == 2) {
            return true
        }
        return false
    }
}