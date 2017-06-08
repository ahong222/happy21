package xin.kotlin.happy21

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_help.view.*
import xin.kotlin.happy21.game.GameViewCallback
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by shen on 17/5/26.
 */

class MainActivity : Activity() {
    /**
     * 每天最多免费赠送次数
     */
    val MAX_FREE_COUNT_PER_DAY = 3
    /**
     * 每次赠送筹码数
     */
    val FREE_SCORE = 1000


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        helpView.setItemClickListener(helpViewOnClickListener)
        gameView.gameViewCallback = gameViewCallback

        var score = getScore()
        gameView.updateScore(score)
        gameView.init()

    }

    val helpViewOnClickListener: View.OnClickListener = View.OnClickListener({
        v: View? ->
        when (v!!.id) {
            R.id.help -> openHelpPage()
            R.id.back -> finish()
        }
    })

    fun openHelpPage() {
        var dialog = Dialog(this, R.style.TransparentDialog)
        var view = LayoutInflater.from(this).inflate(R.layout.dialog_help, null, false)
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(true)
        view.dialogRootView.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    val gameViewCallback: GameViewCallback = object : GameViewCallback {
        override fun alertText(text: String) {
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show();
        }

        override fun onScoreChanged(newScore: Int) {
            saveScore(newScore)
            if (newScore == 0) {
                var freeScore = getFreeScore()
                if (freeScore == 0) {
                    alertText("您今天的筹码已经用光，请明天再来！")
                } else {
                    alertText("您获得了" + freeScore.toString() + "赠送筹码，请您享用")
                    gameView.post {
                        gameView.updateScore(freeScore)
                        gameView.init()
                    }
                }
            }
        }
    }

    fun getScore(): Int {
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("score", FREE_SCORE)
    }

    fun saveScore(score: Int) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("score", score).apply()
    }

    fun getFreeScore(): Int {
        var simpleDateFormat = SimpleDateFormat("yyyyMMdd")
        var dateStr = simpleDateFormat.format(Date())
        var freeDateTimes = PreferenceManager.getDefaultSharedPreferences(this).getString("free_score", "")
        var freeDate: String? = null
        var lastFreeCount: Int = 0
        if (freeDateTimes != null && freeDateTimes.length > 9) {
            freeDate = freeDateTimes.substring(0, 8)
            lastFreeCount = freeDateTimes.substring(9).toInt()
        }
        if (dateStr.equals(freeDate)) {
            if (lastFreeCount < MAX_FREE_COUNT_PER_DAY) {
                lastFreeCount++
            } else {
                return 0
            }
        } else {
            lastFreeCount = 1
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("free_score", dateStr + "_" + lastFreeCount).apply()
        return FREE_SCORE
    }
}