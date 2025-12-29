package org.test.railroad;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import org.test.railroad.config.CServer;
import org.test.railroad.initialization.WorldInit;
import org.test.railroad.queue.TaskQueue;

public class RailRoad implements ModInitializer {

    @Override
    public void onInitialize() {
        MidnightConfig.init("railroad", CServer.class);
        WorldInit.init();
        TaskQueue.init();
        }
    }
