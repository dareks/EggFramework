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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used on production
 */
public class ActionsCache {

    public ConcurrentHashMap<String, ConcurrentHashMap<String, Action>> actions = new ConcurrentHashMap<String, ConcurrentHashMap<String, Action>>();

    // whole method is synchronized because we don't care about performance of it - it is only called in the first
    // minutes after application has been started
    public synchronized void put(String clazz, String action, Action actionInstance) {
        if (actions.get(clazz) == null) {
            actions.put(clazz, new ConcurrentHashMap<String, Action>());
        }
        actions.get(clazz).put(action, actionInstance);
    }

    public Action get(String clazz, String action) {
        Map<String, Action> map = actions.get(clazz);
        if (map == null) {
            return null;
        } else {
            return map.get(action);
        }
    }

}
