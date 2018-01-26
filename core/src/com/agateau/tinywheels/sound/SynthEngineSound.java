package com.agateau.tinywheels.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.math.MathUtils;

/**
 * A synthesised engine sound
 */
public class SynthEngineSound {
    private static final int SAMPLE_DURATION_MS = 200;
    private static final int SAMPLING_RATE = 44100;
    private final AudioDevice mDevice;
    private float[][] mBuffers = new float[2][SAMPLE_DURATION_MS * SAMPLING_RATE / 1000];
    private int mPlayingBufferIdx = 0;
    private long mSampleIdx = 0;
    private boolean mNeedUpdate = false;

    private Settings mSettings;

    private Thread mThread = new Thread() {
        @Override
        public void run() {
            while (!interrupted()) {
                float[] buffer = getPlayBuffer();
                mDevice.writeSamples(buffer, 0, buffer.length);
            }
        }
    };

    public static class Settings {
        public int frequency = 100;
        public float noise = 0.5f;
        public float gain = 1.5f;
    }

    public SynthEngineSound() {
        mDevice = Gdx.audio.newAudioDevice(SAMPLING_RATE, /* mono= */true);
    }

    public void play(float speed) {
        updateBuffer();
        // TODO: take speed into account
        if (!mThread.isAlive()) {
            mThread.start();
        }
    }

    public void setSettings(Settings settings) {
        mSettings = settings;
    }

    private synchronized float[] getPlayBuffer() {
        mPlayingBufferIdx = (mPlayingBufferIdx + 1) % 2;
        mNeedUpdate = true;
        return mBuffers[mPlayingBufferIdx];
    }

    private synchronized float[] getWorkBuffer() {
        if (!mNeedUpdate) {
            return null;
        }
        mNeedUpdate = false;
        return mBuffers[(mPlayingBufferIdx + 1) % 2];
    }

    private synchronized void updateBuffer() {
        float[] buffer = getWorkBuffer();
        if (buffer == null) {
            return;
        }
        for (int idx = 0; idx < buffer.length; ++idx, ++mSampleIdx) {
            float t = (float)(mSampleIdx) / SAMPLING_RATE;
            float value = 0;
            for (int i = 0; i < 4; ++i) {
                value += generateCylinder(t, i);
            }

            value = (float)Math.pow(value, mSettings.gain);
            buffer[idx] = MathUtils.clamp(value, -1, 1);
        }
    }

    private float generateCylinder(float time, int idx) {
        float period = 1f / mSettings.frequency;
        time %= period;
        float popStart = idx * period / 4;
        float popDuration = period / 2;
        if (time >= popStart && time < popStart + popDuration) {
            float popTime = time - popStart;
            float value = MathUtils.sin(popTime * mSettings.frequency * MathUtils.PI2);
            value += MathUtils.random(-mSettings.noise, mSettings.noise);
            return value;
        }
        return 0;
    }
}
