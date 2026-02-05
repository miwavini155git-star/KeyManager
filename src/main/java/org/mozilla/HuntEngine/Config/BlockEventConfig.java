package org.mozilla.HuntEngine.Config;

import java.util.HashMap;
import java.util.Map;

public class BlockEventConfig {
    public int x;
    public int y;
    public int z;
    public Map<String, String> eventScripts;

    public BlockEventConfig(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.eventScripts = new HashMap<>();
    }

    public String getKey() {
        return x + "_" + y + "_" + z;
    }

    public void setEventScript(String eventType, String scriptPath) {
        eventScripts.put(eventType, scriptPath);
    }

    public String getEventScript(String eventType) {
        return eventScripts.getOrDefault(eventType, null);
    }

    public boolean hasEventScript(String eventType) {
        return eventScripts.containsKey(eventType) && !eventScripts.get(eventType).isEmpty();
    }
}
