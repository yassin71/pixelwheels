package com.greenyetilab.tinywheels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.greenyetilab.utils.anchor.Anchor;
import com.greenyetilab.utils.anchor.AnchorGroup;
import com.greenyetilab.utils.log.NLog;

/**
 * Handle joypad input, for desktop mode
 */
public class PadInputHandler extends ControllerAdapter implements GameInputHandler {
    public enum Action {
        LEFT(0),
        RIGHT(1),
        BRAKE(2),
        TRIGGER(3);

        int id;

        Action(int id) {
            this.id = id;
        }
    }

    public static class Factory implements GameInputHandlerFactory {
        @Override
        public String getId() {
            return "pad";
        }

        @Override
        public String getName() {
            return "Joypad";
        }

        @Override
        public String getDescription() {
            return "Pad to drive, button to trigger bonuses.";
        }

        @Override
        public GameInputHandler create() {
            return new KeyboardInputHandler();
        }

    }

    private final BonusIndicator mBonusIndicator = new BonusIndicator();
    private GameInput mInput = new GameInput();
    private Controller mController;

    public PadInputHandler() {
        for (Controller controller : Controllers.getControllers()) {
            NLog.i("controller %s", controller.getName());
            mController = controller;
            Controllers.addListener(this);
            break;
        }
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        NLog.i("buttonCode=%d", buttonCode);
        return true;
    }

    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        NLog.i("povCode=%d value=%s", povCode, value);
        return true;
    }

    public void setActionKey(Action action, int key) {
        mKeyForAction[action.id] = key;
    }

    @Override
    public BonusIndicator getBonusIndicator() {
        return mBonusIndicator;
    }

    @Override
    public GameInput getGameInput() {
        PovDirection direction = mController.getPov(0);
        mInput.direction = 0;
        /*
        mInput.braking = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        mInput.accelerating = Gdx.input.isKeyPressed(Input.Keys.UP);
        */
        mInput.braking = isKeyPressed(Action.BRAKE) || direction == PovDirection.south;
        mInput.accelerating = !mInput.braking; //Gdx.input.isKeyPressed(Input.Keys.UP);
        if (isKeyPressed(Action.LEFT) || direction == PovDirection.west) {
            mInput.direction = 1;
        } else if (isKeyPressed(Action.RIGHT) || direction == PovDirection.east) {
            mInput.direction = -1;
        }
        mInput.triggeringBonus = isKeyPressed(Action.TRIGGER);

        return mInput;
    }

    @Override
    public void createHud(Assets assets, HudBridge hudBridge) {
        AnchorGroup group = new AnchorGroup();
        group.setFillParent(true);
        hudBridge.getStage().addActor(group);
        group.addPositionRule(mBonusIndicator, Anchor.TOP_LEFT, group, Anchor.TOP_LEFT, 16, -48);
    }

    private boolean isKeyPressed(Action action) {
        return Gdx.input.isKeyPressed(mKeyForAction[action.id]);
    }
}
