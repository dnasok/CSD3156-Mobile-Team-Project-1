package com.example.team16_mobile_team_project_1.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.team16_mobile_team_project_1.R

/**
 * Manages all audio playback for the game, including sound effects and background music.
 * This object handles the loading, playing, and releasing of audio resources.
 */
object AudioManager {
    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false
    private val soundMap = mutableMapOf<Sound, Int>()
    private var isMusicPausedByLifecycle = false

    /**
     * Enum class representing the different sound effects in the game.
     */
    enum class Sound {
        HIT, SELECT, SHOOT, COIN
    }

    /**
     * Initializes the AudioManager, setting up the SoundPool and loading sound effects.
     * This must be called before any other functions in this object.
     *
     * @param context The application context.
     */
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
        soundMap[Sound.COIN] = soundPool?.load(context, R.raw.sfx_coin, 1) ?: 0
        isInitialized = true
    }

    /**
     * Plays a sound effect.
     *
     * @param sound The sound effect to play.
     */
    fun playSound(sound: Sound) {
        if (!isInitialized) return
        soundMap[sound]?.let { soundId ->
            if (soundId != 0) {
                soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    /**
     * Plays the menu background music.
     *
     * @param context The application context.
     */
    fun playMenuMusic(context: Context) {
        if (isMusicPausedByLifecycle) return
        stopMusic()
        mediaPlayer = MediaPlayer.create(context, R.raw.bgm_menu).apply {
            isLooping = true
            start()
        }
    }

    /**
     * Plays the in-game background music.
     *
     * @param context The application context.
     */
    fun playGameMusic(context: Context) {
        if (isMusicPausedByLifecycle) return
        stopMusic()
        mediaPlayer = MediaPlayer.create(context, R.raw.bgm_game).apply {
            isLooping = true
            start()
        }
    }

    /**
     * Stops the currently playing background music.
     */
    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Pauses the music due to an Activity lifecycle event (e.g., app is minimized).
     */
    fun pauseMusicForLifecycle() {
        isMusicPausedByLifecycle = true
        stopMusic()
    }

    /**
     * Resumes the music after an Activity lifecycle event (e.g., app is brought to the foreground).
     *
     * @param context The application context.
     * @param gameState The current state of the game.
     */
    fun resumeMusicForLifecycle(context: Context, gameState: GameState) {
        isMusicPausedByLifecycle = false
        when (gameState) {
            is GameState.Running, is GameState.Countdown -> playGameMusic(context)
            else -> playMenuMusic(context)
        }
    }

    /**
     * Releases all audio resources held by the AudioManager.
     * This should be called when the game is being destroyed.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        stopMusic()
        isInitialized = false
        soundMap.clear()
    }
}
