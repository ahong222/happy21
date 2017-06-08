package xin.kotlin.happy21

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.Animation
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : Activity() {

    var mediaManager: MediaManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        start.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    var animation = CommonUtils.getScaleAnimation(1f, 0.8f)
                    animation.duration = 500
                    animation.fillAfter = true
                    start.startAnimation(animation)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    var animation = CommonUtils.getScaleAnimation(0.8f, 1f)
                    animation.duration = 200
                    animation.fillAfter = true
                    start.startAnimation(animation)
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            MainActivity.start(this@WelcomeActivity);
                        }

                        override fun onAnimationRepeat(animation: Animation?) {

                        }

                        override fun onAnimationStart(animation: Animation?) {

                        }
                    })

                    true
                }
                else -> true
            }
        }


        mediaManager = MediaManager()
        mediaManager?.play(applicationContext, R.raw.sound_background, true)
    }

    override fun onResume() {
        super.onResume()
        mediaManager?.start()
    }

    override fun onPause() {
        super.onPause()
        mediaManager?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaManager?.stop()
    }


}
