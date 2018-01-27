package com.agateau.tinywheels.enginelab;

import com.agateau.tinywheels.sound.SynthEngineSound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * An actor to show a sound
 */
class SoundView extends Actor {
    private SynthEngineSound mSound;
    private ShapeRenderer mRenderer = new ShapeRenderer();

    public SoundView() {
        setHeight(80);
    }

    public void setSound(SynthEngineSound sound) {
        mSound = sound;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        mRenderer.begin(ShapeRenderer.ShapeType.Line);
        mRenderer.setProjectionMatrix(getStage().getCamera().combined);
        mRenderer.setColor(0.5f, 0.5f, 0.5f, 1);

        float y0 = getY() + getHeight() / 2;
        mRenderer.rect(getX(), getY(), getWidth(), getHeight());
        mRenderer.line(getX(), y0, getWidth(), y0);
        float[] buffer = mSound.getWorkBuffer();
        mRenderer.setColor(0.2f, 0.2f, 0.9f, 1);
        float prevX = getX();
        float prevY = y0;
        for (float x = 0; x < getWidth(); ++x) {
            int idx = (int)(x / getWidth() * buffer.length);
            float sample = buffer[idx];
            float y = y0 + sample * getHeight() / 2;
            mRenderer.line(prevX, prevY, getX() + x, y);
            prevX = getX() + x;
            prevY = y;
        }
        mRenderer.end();
    }
}
