package it.fast4x.rimusic.extensions.nextvisualizer.painters.waveform

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.media3.exoplayer.ExoPlayer
import it.fast4x.rimusic.extensions.nextvisualizer.painters.Painter
import it.fast4x.rimusic.extensions.nextvisualizer.utils.VisualizerHelper
import kotlin.math.absoluteValue

class FullWfmAnalog(
    val colorPaint: Int = Color.WHITE,
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPaint;style = Paint.Style.STROKE;strokeWidth = 2f
    },
    //
    var startHz: Int = 0,
    var endHz: Int = 2000,
    //
    var num: Int = 256,
    //
    var ampR: Float = 1f,
    val player: ExoPlayer?
) : Painter() {

    private val path = Path()
    private var skipFrame = false
    private var value: Long = 0
    lateinit var waveform : ByteArray
    private val skip_frames = num*10
    private var frame_val = 0

    // private var fft: DoubleArray = DoubleArray(1){1.0}


    override fun calc(helper: VisualizerHelper) {
        // fft = helper.getFftMagnitudeRange(startHz, endHz)

        /*frame_val = (frame_val++) % skip_frames
        skipFrame = frame_val != 0
        if (skipFrame)
            return*/
        skipFrame = false // for now, I don't know if I can use this

        waveform = helper.getWave()
    }

    @ExperimentalUnsignedTypes
    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        if (skipFrame) return

        val width = canvas.width.toFloat()

        val point = waveform.size / (num + 1)
        // val sliceWidth = width / num
        val durationWidth = (player?.duration ?: 0) / width

        // TODO path should start at currentDurationWidth instead of zero if non-zero

        value = (player?.currentPosition ?: 0).toLong()

        var pointValue = 0f
        for (i in 1..num){
            pointValue += -waveform[point*i].toUByte().toInt()
        }
        pointValue= ((pointValue/num)+128f)
        val currentDurationWidth = (value)/durationWidth
        // path.lineTo(currentDurationWidth, (-waveform[point].toUByte().toInt() + 128f) * ampR)

        path.lineTo(currentDurationWidth, -(pointValue.absoluteValue) * ampR)
        path.lineTo(currentDurationWidth, (pointValue.absoluteValue) * ampR)

        // path.lineTo(currentDurationWidth, (-waveform[point].toUByte().toInt() + 128f) * ampR)
        drawHelper(canvas, "a", 0f, .5f) { canvas.drawPath(path, paint) }
        // path.reset()
    }
}