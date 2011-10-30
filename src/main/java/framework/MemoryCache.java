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

import java.util.Arrays;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weigher;

/**
 * Simple in-memory cache with limited size. When the cache is full last recently used element is removed. Fantastic
 * ConcurrentLinkedHashMap project is used underneath :)
 */
public class MemoryCache implements Cache {

    private ConcurrentLinkedHashMap<Key, Value> map;

    /**
     * @param maxSize
     *            In bytes.
     */
    public MemoryCache(int maxSize) {
        map = new ConcurrentLinkedHashMap.Builder<Key, Value>().maximumWeightedCapacity(maxSize).weigher(new Weigher<Value>() {
            public int weightOf(Value value) {
                return value.value.length + value.key.key.length + 22; // TODO is this correct? (maybe add cost of
                                                                       // having references inside
                                                                       // ConcurrentLinkedHashMap
            }
        }).build();
    }

    public void put(byte[] key, byte[] value, long timeout) {
        Key keyObj = new Key(key);
        map.put(keyObj, new Value(value, keyObj, timeout));
    }

    public void put(byte[] key, byte[] value) {
        Key keyObj = new Key(key);
        map.put(keyObj, new Value(value, keyObj));
    }

    public byte[] get(byte[] key) {
        long currentTime = System.currentTimeMillis();
        Key keyObj = new Key(key);
        Value value = map.get(keyObj);
        if (value != null) {
            if (currentTime > value.expires) {
                map.remove(keyObj, value);
                return get(key);
            } else {
                return value.value;
            }
        }
        return null;
    }

    public void remove(byte[] key) {
        Key keyObj = new Key(key);
        map.remove(keyObj);
    }

    private final class Key {
        public final byte[] key;
        private final int hashCode;

        public Key(byte[] key) {
            this.key = key;
            hashCode = Arrays.hashCode(key);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                return obj == this ? true : Arrays.equals(((Key) obj).key, key);
            }
            return false;
        }

    }

    private final class Value {
        public final Key key;
        public final byte[] value;
        public final long expires;

        public Value(byte[] value, Key key, long timeout) {
            this.value = value;
            this.key = key;
            this.expires = System.currentTimeMillis() + timeout;
        }

        public Value(byte[] value, Key key) {
            this.value = value;
            this.key = key;
            this.expires = Long.MAX_VALUE;
        }

    }

}
