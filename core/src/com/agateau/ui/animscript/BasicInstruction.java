/*
 * Copyright 2019 Aurélien Gâteau <mail@agateau.com>
 *
 * This file is part of Pixel Wheels.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.agateau.ui.animscript;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.agateau.ui.animscript.AnimScript.Context;
import com.badlogic.gdx.scenes.scene2d.Action;

class BasicInstruction implements Instruction {
    Object mObject;
    Method mMethod;
    Argument[] mArgs;

    public BasicInstruction(Object object, Method method, Argument[] args) {
        mObject = object;
        mMethod = method;
        mArgs = args;
    }

    /* (non-Javadoc)
     * @see com.agateau.ui.animscript.Instruction#run(com.agateau.burgerparty.utils.AnimScript.Context)
     */
    @Override
    public Action run(Context context) {
        Object[] objectArgs = new Object[mArgs.length];
        for (int idx=0; idx < mArgs.length; ++idx) {
            Argument arg = mArgs[idx];
            objectArgs[idx] = arg.computeValue(context);
        }
        try {
            return (Action)mMethod.invoke(mObject, objectArgs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}