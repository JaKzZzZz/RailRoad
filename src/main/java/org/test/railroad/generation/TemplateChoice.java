package org.test.railroad.generation;

import net.minecraft.util.Identifier;

public record TemplateChoice (Identifier id, int currentYOffset, int lastYOffset, boolean emergency, boolean up, boolean start) {}