package org.test.railroad.generation;

import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.test.railroad.analysis.GroundScanResult;
import org.test.railroad.analysis.StoneScanResult;
import org.test.railroad.analysis.TerrainAnalyzers;
import org.test.railroad.initialization.RailWorldState;
import org.test.railroad.util.RailLog;

import java.util.Optional;

import static net.minecraft.util.BlockMirror.NONE;
import static net.minecraft.util.math.random.Random.createLocal;
import static org.test.railroad.analysis.TerrainAnalyzers.getHighestGroundAhead;
import static org.test.railroad.generation.BuildSupports.buildGroundUpToTemplate;

public class RailGenerator {
    public static int RAIL_Z = -6;

    public static StructurePlacementData placementData = new StructurePlacementData()
            .setRotation(BlockRotation.NONE)
            .setMirror(NONE)
            .setIgnoreEntities(true)
            .setRandom(createLocal());

    public static void generateSegment(ServerWorld world) {
        if (world.getRegistryKey() != World.OVERWORLD) {
            return;
        }

        RailWorldState state = RailWorldState.get(world);

        int lastSegmentEndX = state.lastSegmentX;
        int lastY = state.lastY;

        if (lastSegmentEndX == 0) {
            lastSegmentEndX = state.spawnX - 1;
            state.isFirstSegment = true;
        }
        int blockX = lastSegmentEndX + 1;

        BlockPos placePos = new BlockPos(blockX, lastY, RAIL_Z);

        GroundDecision dBefore = analyzeGround(world, placePos, state.lastY, state.isFirstSegment, false, false);

        TemplateChoice choice = chooseTemplate(world, placePos, dBefore, state.lastY);

        Identifier templateId = choice.id();
        int finalY = choice.currentYOffset() + choice.lastYOffset();
        BlockPos finalPlacePos = placePos.add(0, finalY, state.isFirstSegment ? -5 : 0);

        StructureTemplateManager manager = world.getStructureTemplateManager();

        Optional<StructureTemplate> optionalTemplate = manager.getTemplate(templateId);
        if (optionalTemplate.isEmpty()) {
            RailLog.d("Шаблон не найден: " + templateId);
            return;
        }
        StructureTemplate template = optionalTemplate.get();
        RailLog.d(" Шаблон загружен: " + templateId + " размер=" + template.getSize());

        world.getChunk(finalPlacePos.getX() >> 4, finalPlacePos.getZ()  >> 4, ChunkStatus.FULL, true);

        StoneScanResult mine = TerrainAnalyzers.hasStoneOnBottomLine(
                world,
                finalPlacePos,
                template
        );
        boolean mineNow = mine.top();
        boolean noRoof = mine.any();

        finalPlacePos = finalPlacePos.add(0, (mineNow) ? -1 : 0, (mineNow) ? -1 : 0);

        GroundDecision d = analyzeGround(world, placePos, state.lastY, state.isFirstSegment, mineNow, noRoof);
        TemplateChoice finalChoice = chooseTemplate(world, placePos, d, state.lastY);

        if (choice.emergency()) {
            placeEmergencySegment(world, placePos, finalChoice, d, lastY);
            return;
        }

        Identifier finalTemplateId = finalChoice.id();
        Optional<StructureTemplate> finalOptionalTemplate = manager.getTemplate(finalTemplateId);
        if (finalOptionalTemplate.isEmpty()) {
            RailLog.d("Шаблон не найден: " + templateId);
            return;
        }
        StructureTemplate finalTemplate = finalOptionalTemplate.get();

        ClearEntities.teleportEntities(world, template, finalPlacePos);


        finalTemplate.place(
                world,
                finalPlacePos,
                finalPlacePos,
                placementData,
                world.getRandom(),
                Block.NOTIFY_LISTENERS | Block.FORCE_STATE
        );

        lastY += choice.lastYOffset();

        lastSegmentEndX = blockX + template.getSize().getX() - 1;

        BuildSupports.placeSupport(world, finalPlacePos, choice, template);

        tryPlaceSideStructure(world, finalPlacePos, d);

        state.lastSegmentX = lastSegmentEndX;
        state.lastY = lastY;
        state.markDirty();

        state.isFirstSegment = false;

        RailLog.d("[SAVE] updated lastSegmentX = " + lastSegmentEndX);
        RailLog.d("Сегмент готов: " + finalPlacePos);
    }

    private static GroundDecision analyzeGround(ServerWorld world, BlockPos pos, int lastY, boolean start, boolean mineNow, boolean noRoof) {
        BlockPos lookahead = pos.add(16, 0, 0);
        GroundScanResult scan = getHighestGroundAhead(world, lookahead, 16);
        RailWorldState state = RailWorldState.get(world);
        int lastSegmentEndX = state.lastSegmentX;

        int highest = scan.highest();
        int targetY = highest + 2;
        int dy = targetY - lastY;

        return new GroundDecision(
                highest,
                targetY,
                dy,
                dy > 0,
                Math.abs(dy),
                start,
                mineNow,
                noRoof,
                lastSegmentEndX,
                scan.water()
        );
    }

    private record GroundDecision(
            int highest,
            int targetY,
            int dy,
            boolean up,
            int abs,
            boolean start,
            boolean mine,
            boolean noRoof,
            int lastSegmentEndX,
            boolean water
    ) {}

    private static TemplateChoice chooseTemplate(ServerWorld world, BlockPos pos, GroundDecision d, int lastY) {

        boolean chance = chancePercent(3, world.random);

        RailLog.d("[LOG] chooseTemplate: start pos=" + pos + " lastSegmentEndX=" + d.lastSegmentEndX + " lastY=" + lastY + " RAIL_Z=" + (RAIL_Z - 2));

        RailLog.d("[LOG] chooseTemplate: highest=" + d.highest + " targetY=" + d.targetY + " lastY=" + lastY);

        RailLog.d("[LOG] chooseTemplate: dy=" + d.dy + " up=" + d.up + " mine" + d.mine + " abs=" + d.abs);

        if (d.start) {
            return new TemplateChoice(new Identifier("org.test.railroad", "rail/startsegment"), -2, 2, false, true, true);
        }

        if (d.dy >= 10 || d.dy == 0) {
            RailLog.d("[LOG] chooseTemplate: choosing d0ver1 (no change or absurdly large positive)");
            return new TemplateChoice(new Identifier("org.test.railroad", "rail/d0ver1" + (d.mine ? (chance ? "gr_mine01" : "gr") : "")), 0, 0, false, true, false);
        }

        if (d.abs >= 7) {
            RailLog.d("[LOG] chooseTemplate: abs>=8 -> emergency (up=" + d.up + ")");
            if (d.up) {
                return new TemplateChoice(new Identifier("org.test.railroad", "rail/em_start_up"), 0, 0, true, true, false);
            } else {
                return new TemplateChoice(new Identifier("org.test.railroad", "rail/em_start_down"), 0, 0, true, false, false);
            }
        }
        String postpostfix = d.noRoof ? "grnr" : "";
        String postfix = d.mine ? "gr" : "";
        String suffix = d.up ? "up" : "down";
        String name = "rail/d" + d.abs + suffix + postfix;

        int currentYOffset = d.up ? -d.abs : 0;
        int lastYOffset = d.up ? d.abs : -d.abs;

        RailLog.d("[LOG] chooseTemplate: chosen template=" + name + " currentYOffset=" + currentYOffset + " lastYOffset=" + lastYOffset);

        return new TemplateChoice(new Identifier("org.test.railroad", name), currentYOffset, lastYOffset, false, d.up, false);
    }

    private static void placeEmergencySegment(ServerWorld world, BlockPos pos, TemplateChoice choice, GroundDecision d, int lastY) {

        int dAbs = d.abs;

        RailLog.d("[LOG] placeEmergencySegment: start pos=" + pos + " choice=" + choice + " lastSegmentEndX=" + d.lastSegmentEndX + " lastY=" + lastY);

        RailLog.d("[LOG] placeEmergencySegment: highest=" + d.highest + " targetY=" + d.targetY + " dy=" + d.dy + " up=" + d.up + " abs=" + d.abs);

        // --- START ---
        RailLog.d("[LOG] placeEmergencySegment: placing START (up=" + d.up + ")");
        placeSub(world, d,d.up ? (d.mine ? "rail/em_start_up_gr" : "rail/em_start_up") : (d.mine ? "rail/em_start_down_gr" : "rail/em_start_down"), d.up ? 0 : -4, d.up ? +4 : -4, choice,true);
        dAbs -= 4;
        RailLog.d("[LOG] placeEmergencySegment: after START lastSegmentEndX=" + d.lastSegmentEndX + " lastY=" + lastY + " abs=" + d.abs);

        // --- MID ---
        while (dAbs > 6) {
            RailLog.d("[LOG] placeEmergencySegment: placing MID (remaining abs=" + d.abs + " up=" + d.up + ")");
            placeSub(world, d,d.up ? (d.mine ? "rail/em_mid_up_gr" : "rail/em_mid_up") : (d.mine ? "rail/em_mid_down_gr" : "rail/em_mid_down"), d.up ? 0 : -4, d.up ? +4 : -4, choice, false);
            dAbs -= 4;
            RailLog.d("[LOG] placeEmergencySegment: after MID lastSegmentEndX=" + d.lastSegmentEndX + " lastY=" + lastY + " abs=" + d.abs);
        }

        // --- END ---
        RailLog.d("[LOG] placeEmergencySegment: placing END (remaining abs=" + dAbs + " up=" + d.up + ")");
        placeSub(world, d, d.up ? (d.mine ? "rail/em_end_up_gr" : "rail/em_end_up") : (d.mine ? "rail/em_end_down_gr" : "rail/em_end_down"), d.up ? 0 : -4, d.up ? +4 : -4, choice, true);
        RailLog.d("[LOG] placeEmergencySegment: finished emergency lastSegmentEndX=" + d.lastSegmentEndX + " lastY=" + lastY);
    }
    private static void placeSub(ServerWorld world, GroundDecision d, String name, int cY, int lY, TemplateChoice choice, boolean supports) {
        RailWorldState state = RailWorldState.get(world);

        int lastSegmentEndX = state.lastSegmentX;
        int lastY = state.lastY;

        RailLog.d("[LOG] placeSub: name=" + name + " cY=" + cY + " lY=" + lY + " lastSegmentEndX(before)=" + lastSegmentEndX + " lastY(before)=" + lastY);

        Identifier id = new Identifier("org.test.railroad", name);

        StructureTemplateManager manager = world.getStructureTemplateManager();
        StructureTemplate tpl = manager.getTemplate(id).orElse(null);
        if (tpl == null) {
            RailLog.d("[LOG] placeSub: template NOT FOUND: " + id);
            return;
        }

        int finalY = lastY + cY;
        BlockPos fp = new BlockPos(lastSegmentEndX + 1, finalY, RAIL_Z);
        BlockPos finalPlacePos = fp.add(0, 0, (d.mine) ? -1 : 0);

        RailLog.d("[LOG] placeSub: placing template " + id + " at fp=" + fp + " tpl.size=" + tpl.getSize());
        tpl.place(world, finalPlacePos, finalPlacePos, placementData, world.getRandom(),
                Block.NOTIFY_LISTENERS | Block.FORCE_STATE);

        lastY += lY;
        lastSegmentEndX += tpl.getSize().getX();

        state.lastSegmentX = lastSegmentEndX;
        state.lastY = lastY;
        state.markDirty();

        if (supports) BuildSupports.placeSupport(world,fp,choice,tpl);

        RailLog.d("[LOG] placeSub: done. lastSegmentEndX(after)=" + lastSegmentEndX + " lastY(after)=" + lastY);
    }
    public static boolean chancePercent(int percent, Random random) {
        return random.nextInt(100) < percent;
    }
    private static void tryPlaceSideStructure(
            ServerWorld world,
            BlockPos railPlacePos, GroundDecision d
    ) {
        int zOffset = 13;
        int minusZOffset = -6;
        int gy = 0;
        int gz = 0;
        Identifier id = null;
        GroundScanResult gLeft = getHighestGroundAhead(world, railPlacePos.add(0, 0, minusZOffset), 32);
        GroundScanResult gRight = getHighestGroundAhead(world, railPlacePos.add(0, 0, zOffset), 32);

        // шанс
        if (!chancePercent(8, world.random)) return;

        if (d.start || gRight.water() || gLeft.water()) return;

        if (d.mine) {
            RailLog.d("[SIDE] not spawning, mine segment");
            return;
        }

        if ((gLeft.highest() - gLeft.lowest()) < 2) {
            gz = railPlacePos.getZ() + minusZOffset;
            gy = gLeft.highest();
            id = new Identifier(
                    "org.test.railroad",
                    "rail/tower"
            );
        } else if ((gRight.highest() - gRight.lowest()) < 2) {
            gz = railPlacePos.getZ() + zOffset;
            gy = gRight.highest();
            id = new Identifier(
                    "org.test.railroad",
                    "rail/train"
            );
        } else {
            return;
        }
                BlockPos placePos = new BlockPos(
                railPlacePos.getX(),
                gy, gz
        );
        StructureTemplateManager manager = world.getStructureTemplateManager();
        StructureTemplate template = manager.getTemplate(id).orElse(null);
        if (template == null) {
            RailLog.d("[SIDE] template not found: " + id);
            return;
        }

        buildGroundUpToTemplate(world, template, placePos);

        RailLog.d("[SIDE] placing " + id + " y=" + gy + " Δy=" + (d.dy));

        template.place(
                world,
                placePos,
                placePos,
                placementData,
                world.getRandom(),
                Block.NOTIFY_LISTENERS | Block.FORCE_STATE
        );
    }
}

// продолжить логирование +
//заменить шаблоны на em gr +
//проверить чо в бочках спавнится +
//доделать сегменты специальные (ещё доделать войд блоки) +
//подкрутить миксин(ну или нет посмотреть) +
//донастроить генерацию +
// РЕШИТЬ ПРОБЛЕМЫ НЕСОСТЫКОВТИ НАЧ СЕГМЕНТА +
//перегенерировать лут в бочках, сделать обязательный и опциональный, реализовать несколькими лут таблицами +
//повысить диапазон миксина +
//сделать чтобы компас не давался когда в стар мир ззаход +
//доделать ступеньки у башни +
//сделать чтобы вместо воды спавнилась зесля под структурами +

//проверить совместимость версий
//убрать ватерлог блооков (опц.)

//проверка локализации
//проверка норм действия в незере