package com.soulland.soul;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.soulland.SoulLandMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry seluruh martial soul beserta skill-nya.
 *
 * <p>Datanya dimuat dari {@code data/soulland/martial_souls.json} yang dibangun oleh
 * {@code tools/build_souls.py} dari sumber-sumber lore resmi series. Tiap soul punya lima
 * skill yang terbuka bertahap mengikuti jumlah spirit ring (1, 2, 3, 5, 7).
 */
public final class MartialSouls {
    private MartialSouls() {}

    private static final String DATA = "/data/soulland/martial_souls.json";
    private static final Map<String, MartialSoul> REGISTRY = new LinkedHashMap<>();

    static {
        load();
    }

    public static MartialSoul get(String id) {
        if (id == null) return null;
        return REGISTRY.get(id);
    }

    public static List<MartialSoul> all() {
        return new ArrayList<>(REGISTRY.values());
    }

    /** Soul yang punya pemilik tercatat di lore, dipakai buku panduan agar tidak terlalu panjang. */
    public static List<MartialSoul> notable() {
        List<MartialSoul> list = new ArrayList<>();
        for (MartialSoul soul : REGISTRY.values()) {
            if (soul.notable()) list.add(soul);
        }
        return list;
    }

    /**
     * Martial soul yang benar-benar dipakai pemain.
     *
     * <p>Bentuk evolusi menggantikan bentuk dasar saat tahap evolusi sudah terbuka, tapi id
     * dasar tetap disimpan supaya takdir awal pemain tidak hilang.
     */
    public static MartialSoul effective(String soulId, int evolutionStage) {
        MartialSoul base = get(soulId);
        if (base == null) return null;
        if (evolutionStage > 0 && base.hasEvolution()) {
            MartialSoul evolved = get(base.evolutionId());
            if (evolved != null) return evolved;
        }
        return base;
    }

    /** Gacha: martial soul diundi acak dengan bobot rarity, seperti takdir saat lahir di cerita. */
    public static MartialSoul roll(RandomSource random) {
        int total = 0;
        for (MartialSoul soul : REGISTRY.values()) total += soul.weight();
        if (total <= 0) {
            return REGISTRY.values().iterator().next();
        }
        int r = random.nextInt(total);
        for (MartialSoul soul : REGISTRY.values()) {
            r -= soul.weight();
            if (r < 0) return soul;
        }
        return REGISTRY.values().iterator().next();
    }

    // ------------------------------------------------------------------ pemuatan data

    private static void load() {
        try (InputStream in = MartialSouls.class.getResourceAsStream(DATA)) {
            if (in == null) {
                SoulLandMod.LOGGER.error("Data martial soul tidak ditemukan: {}", DATA);
                return;
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray souls = root.getAsJsonArray("souls");
                for (JsonElement element : souls) {
                    MartialSoul soul = parseSoul(element.getAsJsonObject());
                    if (soul != null) REGISTRY.put(soul.id(), soul);
                }
            }
            SoulLandMod.LOGGER.info("Memuat {} martial soul dari data lore.", REGISTRY.size());
        } catch (Exception e) {
            SoulLandMod.LOGGER.error("Gagal memuat data martial soul.", e);
        }
    }

    private static MartialSoul parseSoul(JsonObject json) {
        String id = str(json, "id");
        if (id == null || id.isEmpty()) return null;

        List<SkillDef> skills = new ArrayList<>();
        JsonArray skillArray = json.getAsJsonArray("skills");
        if (skillArray != null) {
            for (JsonElement element : skillArray) {
                skills.add(parseSkill(element.getAsJsonObject()));
            }
        }

        return new MartialSoul(
                id,
                str(json, "name"),
                str(json, "nameZh"),
                str(json, "owner"),
                str(json, "affiliation"),
                str(json, "type"),
                str(json, "family"),
                str(json, "rarity"),
                json.get("weight").getAsInt(),
                str(json, "desc"),
                str(json, "element"),
                json.get("color").getAsInt(),
                str(json, "portrait"),
                str(json, "evolvesTo"),
                json.has("notable") && json.get("notable").getAsBoolean(),
                skills);
    }

    private static SkillDef parseSkill(JsonObject json) {
        SkillVisual visual = new SkillVisual(
                shape(str(json, "shape")),
                json.get("color").getAsInt(),
                json.get("color2").getAsInt(),
                json.get("size").getAsFloat(),
                json.get("count").getAsInt(),
                str(json, "sound"),
                json.get("shake").getAsFloat(),
                json.get("flash").getAsInt());

        return new SkillDef(
                str(json, "id"),
                str(json, "name"),
                str(json, "desc"),
                json.get("cost").getAsFloat(),
                json.get("cd").getAsInt(),
                json.get("rings").getAsInt(),
                target(str(json, "target")),
                visual,
                json.get("damage").getAsFloat(),
                json.get("heal").getAsFloat(),
                json.get("radius").getAsFloat(),
                effect(str(json, "effect")),
                json.get("effectTicks").getAsInt(),
                json.get("effectAmp").getAsInt(),
                effect(str(json, "selfEffect")),
                json.get("selfTicks").getAsInt(),
                json.get("selfAmp").getAsInt());
    }

    private static String str(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || element.isJsonNull()) return "";
        return element.getAsString();
    }

    private static FxShape shape(String value) {
        try {
            return FxShape.valueOf(value);
        } catch (IllegalArgumentException e) {
            return FxShape.SPHERE;
        }
    }

    private static SkillTarget target(String value) {
        try {
            return SkillTarget.valueOf(value);
        } catch (IllegalArgumentException e) {
            return SkillTarget.ENEMY_NEAREST;
        }
    }

    private static MobEffect effect(String id) {
        if (id == null || id.isEmpty()) return null;
        return BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(id)).orElse(null);
    }
}
