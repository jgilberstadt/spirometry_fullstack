package com.spirometry.homespirometry.classes;

import java.util.HashMap;
import java.util.Map;

public class Sites {
    public enum Site {
        WashU
    }
    public static Map<Site, String> contactMap;
    //this will eventually be set form data base
    static {
        contactMap = new HashMap<>();
        contactMap.put(Site.WashU, "(314)747-0497");
    }

}
