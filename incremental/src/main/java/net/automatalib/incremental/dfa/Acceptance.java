/* Copyright (C) 2013-2019 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.incremental.dfa;

import javax.annotation.Nonnull;

/**
 * Tri-state acceptance value.
 *
 * @author Malte Isberner
 */
public enum Acceptance {
    FALSE {
        @Override
        public boolean toBoolean() {
            return false;
        }
    },
    TRUE {
        @Override
        public boolean toBoolean() {
            return true;
        }
    },
    DONT_KNOW {
        @Override
        public boolean toBoolean() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean conflicts(boolean val) {
            return false;
        }
    };

    /**
     * Retrieves the corresponding acceptance value (either {@link #TRUE} or {@link #FALSE}) for a given boolean value.
     *
     * @param val
     *         the boolean value
     *
     * @return the corresponding acceptance value
     */
    @Nonnull
    public static Acceptance fromBoolean(boolean val) {
        return val ? TRUE : FALSE;
    }

    public boolean conflicts(boolean val) {
        return (val != toBoolean());
    }

    public abstract boolean toBoolean();
}
