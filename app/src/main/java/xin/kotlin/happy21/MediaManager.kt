package xin.kotlin.happy21

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

/**
 * Created by shen on 17/6/3.
 */

class MediaManager {
    lateinit var mediaPlayer: MediaPlayer;

    init {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener({
            L.d("OnPreparedListener")
            mediaPlayer.start()
        })

        mediaPlayer.setOnErrorListener({
            mp, what, extra ->
            L.d("what:${what}, extra:${extra}");
            true;
        })
    }

    fun play(context: Context, resId: Int, loop:Boolean) {
        L.d("start play");
        mediaPlayer.setDataSource(context, Uri.parse("android.resource://${context.packageName}/${resId}"))
        mediaPlayer.isLooping = loop
        mediaPlayer.prepareAsync()
    }

    fun start() {
        L.d("start");
        mediaPlayer.start()
    }

    fun pause() {
        L.d("pause");
        mediaPlayer.pause();
    }

    fun stop() {
        L.d("stop");
        mediaPlayer.stop()
    }
}