package xin.kotlin.happy21

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.VideoView
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.WindowManager



/**
 * Created by shen on 17/6/2.
 */
class CustomVideoView : VideoView {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        val width = View.getDefaultSize(0, widthMeasureSpec)
        val height = View.getDefaultSize(0, heightMeasureSpec)
        setMeasuredDimension(width, height)

//        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        val width = wm.defaultDisplay.width
//        val height = wm.defaultDisplay.height
//        setMeasuredDimension(width, height)

    }
}