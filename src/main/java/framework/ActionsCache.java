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
