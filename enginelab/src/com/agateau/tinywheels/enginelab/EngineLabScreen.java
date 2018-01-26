package com.agateau.tinywheels.enginelab;

import com.agateau.tinywheels.sound.SynthEngineSound;
import com.agateau.ui.Menu;
import com.agateau.ui.SliderMenuItem;
import com.agateau.ui.StageScreen;
import com.agateau.ui.anchor.Anchor;
import com.agateau.ui.anchor.AnchorGroup;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Main screen for EngineLab
 */
class EngineLabScreen extends StageScreen {
    private Skin mSkin;
    private SynthEngineSound mEngineSound;
    private SliderMenuItem mSpeedItem;
    private SynthEngineSound.Settings mSettings = new SynthEngineSound.Settings();

    private SliderMenuItem mFreqItem;
    private SliderMenuItem mGainItem;
    private SliderMenuItem mNoiseItem;

    public EngineLabScreen() {
        super(new ScreenViewport());
        setupEngineLab();
        loadSkin();
        setupUi();
    }

    private void loadSkin() {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/uiskin.atlas"));
        mSkin = new Skin(atlas);
        loadFonts();
        mSkin.load(Gdx.files.internal("ui/uiskin.json"));
    }

    private void loadFonts() {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter;
        mSkin.add("default-font", loadFont("fonts/Xolonium-Regular.ttf", 28));
        mSkin.add("title-font", loadFont("fonts/Aero.ttf", 32));

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 12;
        parameter.borderWidth = 0.5f;
        mSkin.add("small-font", loadFont("fonts/Xolonium-Regular.ttf", parameter));

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 28;
        parameter.borderWidth = 0.5f;
        mSkin.add("hud-font", loadFont("fonts/Xolonium-Regular.ttf", parameter));

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 56;
        parameter.characters = "1234567890thsrdneméè";
        parameter.borderWidth = 0.5f;
        mSkin.add("hud-rank-font", loadFont("fonts/Xolonium-Regular.ttf", parameter));
    }

    private BitmapFont loadFont(String name, int size) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        return loadFont(name, parameter);
    }

    private BitmapFont loadFont(String name, FreeTypeFontGenerator.FreeTypeFontParameter parameter) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(name));
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    private void setupUi() {
        AnchorGroup root = new AnchorGroup();
        getStage().addActor(root);
        root.setFillParent(true);

        Menu menu = new Menu(mSkin);
        menu.setLabelColumnWidth(200);
        menu.setDefaultItemWidth(500);

        mSpeedItem = new SliderMenuItem(menu);
        mSpeedItem.setRange(0, 1, 0.01f);
        menu.addItemWithLabel("Speed", mSpeedItem);

        mFreqItem = new SliderMenuItem(menu);
        mFreqItem.setRange(50, 200, 1);
        mFreqItem.setIntValue(mSettings.frequency);
        menu.addItemWithLabel("Freq", mFreqItem);

        mGainItem = new SliderMenuItem(menu);
        mGainItem.setRange(0.5f, 2, 0.01f);
        mGainItem.setFloatValue(mSettings.gain);
        menu.addItemWithLabel("Gain", mGainItem);

        mNoiseItem = new SliderMenuItem(menu);
        mNoiseItem.setRange(0, 1, 0.01f);
        mNoiseItem.setFloatValue(mSettings.noise);
        menu.addItemWithLabel("Noise", mNoiseItem);

        root.addPositionRule(menu, Anchor.CENTER, root, Anchor.CENTER);
    }

    private void setupEngineLab() {
        mEngineSound = new SynthEngineSound();
    }

    @Override
    public void render(float dt) {
        super.render(dt);
        mSettings.frequency = mFreqItem.getIntValue();
        mSettings.gain = mGainItem.getFloatValue();
        mSettings.noise = mNoiseItem.getFloatValue();
        mEngineSound.setSettings(mSettings);
        mEngineSound.play(mSpeedItem.getFloatValue());
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean isBackKeyPressed() {
        return false;
    }
}
