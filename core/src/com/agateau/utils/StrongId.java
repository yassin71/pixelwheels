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
package com.agateau.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class StrongId {
    public final String value;

    public static class Registry<T extends StrongId> {
        private final HashMap<String, T> mMap = new HashMap<String, T>();
        // Keep a list so that getAll returns the ids in insertion order
        private final List<T> mList = new ArrayList<T>();

        public void clear() {
            mMap.clear();
            mList.clear();
        }

        public void register(T id) {
            if (mMap.containsKey(id.value)) {
                throw new RuntimeException("ID (" + id.value + ") has already been registered");
            }
            mMap.put(id.value, id);
            mList.add(id);
        }

        public T getOrNull(String value) {
            return mMap.get(value);
        }

        public T get(String value) {
            T id = getOrNull(value);
            if (id == null) {
                throw new RuntimeException("Unknown ID (" + value + ") in " + getClass().getSimpleName());
            }
            return id;
        }

        public Collection<T> getAll() {
            return mList;
        }
    }

    protected StrongId(String value) {
        this.value = value;
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + value;
    }
}
