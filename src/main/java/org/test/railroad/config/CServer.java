package org.test.railroad.config;

import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.util.Identifier;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CServer extends MidnightConfig {

    public static final String DEBUG = "debug";

    // ----- Debug Flag -----
    @Entry(category = DEBUG, name = "Enable Debug Logs")
    @Comment(category = DEBUG, name = "If true, internal rail generation logs will be printed")
    public static boolean enableDebugLogs = false;

    // ----- Example usage comments -----
    @Comment(category = DEBUG, name = "You can put any extra info or instructions here", centered = false)
    public static Comment debugInfo;

}