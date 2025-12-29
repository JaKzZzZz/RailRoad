package org.test.railroad.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.test.railroad.config.CServer;

public class RailLog {

    public static final Logger LOG = LogManager.getLogger("RailRoad");

    public static void d(String message) {
        if (CServer.enableDebugLogs) {
            LOG.info("[DEBUG] " + message);
        }
    }
}