/*
 *   Copyright (C) 2011 Jacek Olszak
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package framework;

public interface Cache {

    /**
     * @param timeout
     *            number of milliseconds
     */
    void put(byte[] key, byte[] value, long timeout);

    /**
     * Put without timeout
     */
    void put(byte[] key, byte[] value);

    byte[] get(byte[] key);

    void remove(byte[] key);

}
