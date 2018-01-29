package com.agateau.tinywheels.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.math.MathUtils;

/**
 * A synthesised engine sound
 */
public class SynthEngineSound {
    private static final int SAMPLE_DURATION_MS = 50;
    private static final int SAMPLING_RATE = 44100;
    private final AudioDevice mDevice;
    private final float[][] mBuffers = new float[2][SAMPLE_DURATION_MS * SAMPLING_RATE / 1000];
    private int mPlayingBufferIdx = 0;
    private long mSampleIdx = 0;
    private boolean mNeedUpdate = false;
    private float mSpeed = 0;

    private Settings mSettings = new Settings();

    private AudioThread mThread = new AudioThread();

    public enum WaveForm {
        SIN,
        POP,
        SAWTOOTH,
        SQUARE,
    }

    class AudioThread extends Thread {
        @Override
        public void run() {
            while (!isCanceled()) {
                float[] buffer = swapBuffers();
                mDevice.writeSamples(buffer, 0, buffer.length);
            }
        }

        synchronized void cancel() {
            mCanceled = true;
        }

        synchronized private boolean isCanceled() {
            return mCanceled;
        }

        boolean mCanceled = false;
    }

    public static class Settings {
        public WaveForm waveForm = WaveForm.POP;
        public int minFrequency = 40;
        public int maxFrequency = 250;
        public float gain = 1f;
        public float modulationStrength = 0f;
        public int modulationFrequency = 20;
        public float echoStrength = 0f;
        public float echoDelay = 0.2f;
        public float lpfBeta = 0.2f;
    }

    public SynthEngineSound() {
        mDevice = Gdx.audio.newAudioDevice(SAMPLING_RATE, /* mono= */true);
    }

    public void dispose() {
        mThread.cancel();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mDevice.dispose();
    }

    public void play(float speed) {
        mSpeed = speed;
        updateBuffer();
        if (!mThread.isAlive()) {
            mThread.start();
        }
    }

    public void setSettings(Settings settings) {
        mSettings = settings;
    }

    /**
     * Swap buffers, returns the new play buffer
     */
    private synchronized float[] swapBuffers() {
        mPlayingBufferIdx = (mPlayingBufferIdx + 1) % 2;
        mNeedUpdate = true;
        return mBuffers[mPlayingBufferIdx];
    }

    public float[] getWorkBuffer() {
        return mBuffers[(mPlayingBufferIdx + 1) % 2];
    }

    private synchronized void updateBuffer() {
        if (!mNeedUpdate) {
            return;
        }
        mNeedUpdate = false;

        float[] buffer = getWorkBuffer();
        if (buffer == null) {
            return;
        }
        for (int idx = 0; idx < buffer.length; ++idx, ++mSampleIdx) {
            float t = (float)(mSampleIdx) / SAMPLING_RATE;
            float frequency = MathUtils.lerp(mSettings.minFrequency, mSettings.maxFrequency, mSpeed);
            float value = 0;
            switch (mSettings.waveForm) {
                case POP:
                    value = generatePop(t, frequency);
                    break;
                case SIN:
                    value = generateSin(t, frequency);
                    break;
                case SQUARE:
                    value = generateSquare(t, frequency);
                    break;
                case SAWTOOTH:
                    value = generateSawTooth(t, frequency);
                    break;
            }

            value *= MathUtils.random(0.6f, 1f);
            //value = distort(value);

            if (value >= 0) {
                value = (float) Math.pow(value, 1 / mSettings.gain);
            } else {
                value = -(float) Math.pow(-value, 1 / mSettings.gain);
            }

            if (mSettings.modulationStrength > 0) {
                float modulation = (1 - 2 * mSettings.modulationStrength) + mSettings.modulationStrength * (float) Math.sin(t * mSettings.modulationFrequency * MathUtils.PI2);
                value *= modulation;
            }

            if (mSettings.echoStrength > 0) {
                value = applyEcho(t, value);
            }
            value = lowPassFilter(value, mSettings.lpfBeta);

            buffer[idx] = MathUtils.clamp(value, -1, 1);
        }
    }

    private float generateSin(float time, float frequency) {
        float period = 1f / frequency;
        time %= period;
        return MathUtils.sin(time * frequency * MathUtils.PI2);
    }

    private float generatePop(float time, float frequency) {
        float period = 1f / frequency;
        time %= period;
        if (time < period / 2) {
            return MathUtils.cos(time * frequency * MathUtils.PI2 / 2);
        } else {
            return 0;
        }
    }

    private float generateSawTooth(float time, float frequency) {
        float period = 1f / frequency;
        time %= period;
        return 1 - (float)Math.pow(time / period, 1 / 3f) - 0.5f;
    }

    private float generateSquare(float time, float frequency) {
        float period = 1f / frequency;
        time %= period;
        return (time < period / 2) ? 1 : -1;
    }

    private float distort(float value) {
        return value + MathUtils.random(-0.001f, 0.001f);
    }

    private float[] mEchoBuffer;
    private float applyEcho(float time, float value) {
        int bufferLength = (int)(mSettings.echoDelay * SAMPLING_RATE);
        if (mEchoBuffer == null || mEchoBuffer.length < bufferLength) {
            mEchoBuffer = new float[bufferLength];
        }
        int currentIdx = (int)(time * SAMPLING_RATE) % mEchoBuffer.length;
        int referenceIdx = (int)((time - mSettings.echoDelay) * SAMPLING_RATE) % mEchoBuffer.length;
        if (referenceIdx < 0) {
            referenceIdx += mEchoBuffer.length;
        }
        mEchoBuffer[currentIdx] = value;
        return value + mEchoBuffer[referenceIdx] * mSettings.echoStrength;
    }

    private float mPreviousValue = 0;
    private float lowPassFilter(float value, float beta) {
        value = mPreviousValue - beta * (mPreviousValue - value);
        mPreviousValue = value;
        return value;
    }
}
