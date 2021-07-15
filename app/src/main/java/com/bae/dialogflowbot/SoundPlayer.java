package com.bae.dialogflowbot;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;

public class SoundPlayer {
    public static final int DING_DONG = R.raw.sound_dingdong;
    public static final int SUCCESS = R.raw.success;
    public static final int NO_EMOTION = R.raw.noemotion;
    public static final int NEGATIVE = R.raw.negative;
    public static final int POSITIVE = R.raw.positive;

    private static SoundPool soundPool;
    private static HashMap<Integer, Integer> soundPoolMap;

    // sound media initialize
    public static void initSounds(Context context) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();

        soundPoolMap = new HashMap(5);
        soundPoolMap.put(DING_DONG, soundPool.load(context, DING_DONG, 1));
        soundPoolMap.put(SUCCESS, soundPool.load(context, SUCCESS, 2));
        soundPoolMap.put(NEGATIVE, soundPool.load(context, NEGATIVE, 3));
        soundPoolMap.put(POSITIVE, soundPool.load(context, POSITIVE, 4));
        soundPoolMap.put(NO_EMOTION, soundPool.load(context, NO_EMOTION, 5));

    }

    public static void play(int raw_id) {
        if (soundPoolMap.containsKey(raw_id)) {
            soundPool.play(soundPoolMap.get(raw_id), 1, 1, 1, 0, 1f);
        }
    }
}
