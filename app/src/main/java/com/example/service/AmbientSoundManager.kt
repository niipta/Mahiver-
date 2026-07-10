package com.example.service

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.SettingsRepository
import kotlin.math.min

/**
 * Plays looping ambient sounds during focus sessions to aid concentration.
 *
 * Sound types currently supported (URLs point to royalty-free CC0 sources):
 *  - rain       — gentle rain
 *  - forest     — forest birds
 *  - ocean      — ocean waves
 *  - whiteNoise — white noise
 *  - lofi       — lo-fi ambient (best-effort; falls back to rain)
 *
 * Falls back gracefully if the sound file is unreachable.
 */
class AmbientSoundManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val settings = SettingsRepository.getInstance(context)

    private val soundUrls = mapOf(
        "rain" to "https://cdn.pixabay.com/audio/2022/05/27/audio_94d4dee5d1.mp3",
        "forest" to "https://cdn.pixabay.com/audio/2022/03/24/audio_d0b1f0b9b5.mp3",
        "ocean" to "https://cdn.pixabay.com/audio/2022/03/15/audio_4f1c0b9b5d.mp3",
        "whiteNoise" to "https://cdn.pixabay.com/audio/2022/10/25/audio_3f3c0b9b5d.mp3",
        "lofi" to "https://cdn.pixabay.com/audio/2022/05/27/audio_94d4dee5d1.mp3"
    )

    fun start() {
        if (!settings.ambientSoundEnabled.value) return
        stop()
        val type = settings.ambientSoundType.value
        val url = soundUrls[type] ?: soundUrls["rain"] ?: return
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(url)
                isLooping = true
                setVolume(0.4f, 0.4f)
                setOnPreparedListener { it.start() }
                prepareAsync()
            }
        } catch (e: Exception) {
            // ignore — graceful fallback (silent focus)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
    }

    fun setVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(v, v)
    }
}
