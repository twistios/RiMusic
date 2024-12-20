package it.fast4x.rimusic.ui.styling

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

@Immutable
data class AlbumPalette(
    var dominant: Color,
    var vibrant: Color,
    var lightVibrant: Color,
    var darkVibrant: Color,
    var muted: Color,
    var darkMuted: Color,
    var lightMuted: Color,
){

}

    fun dynamicPaletteOf(bitmap: Bitmap, isDark: Boolean): AlbumPalette? {
        val palette = Palette
            .from(bitmap)
            .maximumColorCount(8)
            .generate()

        //Dominant

        val dominanthsl = if (isDark) {
            palette.dominantSwatch ?: Palette
                .from(bitmap)
                .maximumColorCount(8)
                .generate()
                .dominantSwatch
        } else {
            palette.dominantSwatch
        }?.hsl ?: return null

        //Vibrant

        val vibranthsl = if (isDark) {
            palette.vibrantSwatch ?: Palette
                .from(bitmap)
                .maximumColorCount(8)
                .generate()
                .vibrantSwatch
        } else {
            palette.vibrantSwatch
        }?.hsl ?: return null

        //LightVibrant

        val lightvibranthsl = if (isDark) {
            palette.lightVibrantSwatch ?: Palette
                .from(bitmap)
                .maximumColorCount(8)
                .generate()
                .lightVibrantSwatch
        } else {
            palette.lightVibrantSwatch
        }?.hsl ?: return null

        //DarkVibrant

        val darkvibranthsl = if (isDark) {
            palette.darkVibrantSwatch ?: Palette
                .from(bitmap)
                .maximumColorCount(8)
                .generate()
                .darkVibrantSwatch
        } else {
            palette.darkVibrantSwatch
        }?.hsl ?: return null

        //muted

        val mutedhsl = if (isDark) {
            palette.mutedSwatch ?: Palette
                .from(bitmap)
                .maximumColorCount(8)
                .generate()
                .mutedSwatch
        } else {
            palette.mutedSwatch
        }?.hsl ?: return null

        //lightmuted

        val lightmutedhsl = if (isDark) {
            palette.lightMutedSwatch ?: Palette
                .from(bitmap)
                .maximumColorCount(8)
                .generate()
                .lightMutedSwatch
        } else {
            palette.lightMutedSwatch
        }?.hsl ?: return null

        //dark

        val darkmutedhsl = if (isDark) {
            palette.darkMutedSwatch ?: Palette
                .from(bitmap)
                .maximumColorCount(8)
                .generate()
                .darkMutedSwatch
        } else {
            palette.darkMutedSwatch
        }?.hsl ?: return null


        return dynamicPaletteOf(
            dominanthsl,
            vibranthsl,
            lightvibranthsl,
            darkvibranthsl,
            mutedhsl,
            lightmutedhsl,
            darkmutedhsl)
    }

    fun dynamicPaletteOf(
        dominanthsl: FloatArray,
        vibranthsl: FloatArray,
        lightvibranthsl: FloatArray,
        darkvibranthsl: FloatArray,
        mutedhsl: FloatArray,
        lightmutedhsl: FloatArray,
        darkmutedhsl: FloatArray
    ): AlbumPalette {
        return AlbumPalette(
            dominant = Color.hsl(dominanthsl[1],dominanthsl[2],dominanthsl[3]),
            vibrant = Color.hsl(vibranthsl[1],vibranthsl[2],vibranthsl[3]),
            lightVibrant = Color.hsl(lightvibranthsl[1],lightvibranthsl[2],lightvibranthsl[3]),
            darkVibrant = Color.hsl(darkvibranthsl[1],darkvibranthsl[2],darkvibranthsl[3]),
            muted = Color.hsl(mutedhsl[1],mutedhsl[2],mutedhsl[3]),
            darkMuted = Color.hsl(darkmutedhsl[1],darkmutedhsl[2],darkmutedhsl[3]),
            lightMuted = Color.hsl(lightmutedhsl[1],lightmutedhsl[2],lightmutedhsl[3]))
    }