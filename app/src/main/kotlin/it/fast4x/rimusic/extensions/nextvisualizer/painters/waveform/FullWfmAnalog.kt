package it.fast4x.rimusic.extensions.nextvisualizer.painters.waveform

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
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

    private var path = Path()
    private var skipFrame = false
    private var currentPosition: Long = 0
    private lateinit var waveform : ByteArray
    private var offset = 0f
    private var speedScale = 1f
    // private val skip_frames = num*10
    // private var frame_val = 0

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

        val durationWidth = (player?.duration ?: 0) / width

        // TODO path should start at currentDurationWidth instead of zero if non-zero

        currentPosition = (player?.currentPosition ?: 0).toLong()

        var pointValue = 0f
        for (i in 1..num){
            pointValue += -waveform[point*i].toUByte().toInt()
        }
        pointValue = ((pointValue/num)) // average of current frame

        // not actually the width
        val currentDurationWidth = (currentPosition*speedScale)/durationWidth

        path.lineTo(currentDurationWidth + offset, (-pointValue.absoluteValue+128) * ampR * 10)

        val pos = getCurrentVisualizerPosition()[0]
        if (pos > width){
            offset -= width
            path = Path()
            // path.offset(offset, 0f) // offset-method not working
        } else if (pos < 0){
            offset += pos.absoluteValue
            path = Path()
        }

        drawHelper(canvas, "a", 0f, .5f) { canvas.drawPath(path, paint) }
        // path.reset()
    }

    private fun getCurrentVisualizerPosition(): FloatArray {
        val pm = PathMeasure(path, false)

        val pos = floatArrayOf(0f, 0f) //pos will be here

        pm.getPosTan(pm.length, pos, null) //pos from end of path
        return pos
    }
}