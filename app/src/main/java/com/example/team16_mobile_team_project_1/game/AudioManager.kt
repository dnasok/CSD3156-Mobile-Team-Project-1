package com.example.team16_mobile_team_project_1.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.team16_mobile_team_project_1.R

object AudioManager {
    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false

    private val soundMap = mutableMapOf<Sound, Int>()

    enum class Sound {
        HIT, SELECT, SHOOT
    }

    fun initialize(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundMap[Sound.HIT] = soundPool?.load(context, R.raw.sfx_hit, 1) ?: 0
        soundMap[Sound.SELECT] = soundPool?.load(context, R.raw.sfx_select, 1) ?: 0
        soundMap[Sound.SHOOT] = soundPool?.load(context, R.raw.sfx_shoot, 1) ?: 0

        isInitialized = true
    }

    fun playSound(sound: Sound) {
        if (!isInitialized) return
        soundMap[sound]?.let { soundId ->
            if (soundId != 0) {
                soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    fun playMenuMusic(context: Context) {
        if (mediaPlayer?.isPlaying == true) {
            // Avoid restarting if music is already playing
            return
        }
        stopMusic()
        mediaPlayer = MediaPlayer.create(context, R.raw.bgm_menu).apply {
            isLooping = true
            start()
        }
    }

    fun playGameMusic(context: Context) {
        stopMusic()
        mediaPlayer = MediaPlayer.create(context, R.raw.bgm_game).apply {
            isLooping = true
            start()
        }
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        stopMusic()
        isInitialized = false
        soundMap.clear()
    }
}
