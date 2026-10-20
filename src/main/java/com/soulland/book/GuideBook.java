package com.soulland.book;

import com.soulland.CultivationRanks;
import com.soulland.lang.SoulLang;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Buku panduan lengkap dunia Soul Land, diberikan otomatis kepada setiap pemain baru.
 *
 * <p>Halaman ditulis dalam bahasa yang dipilih pemain, jadi server membangkitkan
 * buku dengan bahasa itu. Halaman yang isinya berubah (tangga peringkat dan
 * daftar martial soul) dibangkitkan dari registry supaya selalu akurat.
 */
public final class GuideBook {
    private GuideBook() {}

    /** Halaman statis, urut sama dengan {@link #STATIC_PAGES}. */
    private static final String[] STATIC_PAGES = {
            "book.soulland.page.welcome",
            "book.soulland.page.martial_soul",
            "book.soulland.page.level",
            "book.soulland.page.rings",
            "book.soulland.page.foundation",
            "book.soulland.page.skills",
            "book.soulland.page.beasts",
            "book.soulland.page.alchemy",
            "book.soulland.page.commands",
            "book.soulland.page.multiplayer",
            "book.soulland.page.closing"
    };

    /** Buku panduan untuk pemain dengan bahasa tertentu. */
    public static ItemStack create(String language) {
        String requested = SoulLang.normalize(language);
        final String code = requested.isEmpty() ? SoulLang.DEFAULT : requested;

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        var tag = book.getOrCreateTag();
        tag.putString("title", SoulLang.translate(code, "book.soulland.title", "Panduan Dunia Soul Land"));
        tag.putString("author", SoulLang.translate(code, "book.soulland.author", "Soul Land Team"));
        tag.putInt("generation", 0);

        List<String> pages = new ArrayList<>();
        for (String key : STATIC_PAGES) {
            String text = SoulLang.translate(code, key, "");
            if (!text.isEmpty()) pages.add(text);
        }
        pages.add(rankLadder(code));
        pages.add(rankGoal(code));

        // Daftar martial soul ikonik, dihasilkan otomatis dari registry agar selalu akurat.
        int shown = 0;
        for (MartialSoul soul : MartialSouls.notable()) {
            if (shown++ >= 24) break;
            StringBuilder sb = new StringBuilder();
            sb.append(SoulLang.text(code, "soul.soulland." + soul.id() + ".name", soul.name()).getString().toUpperCase())
                    .append("\n");
            if (!soul.nameZh().isEmpty()) {
                sb.append(SoulLang.translate(code, "book.soulland.label.mandarin", "Nama Mandarin: %s", soul.nameZh()))
                        .append("\n");
            }
            sb.append(SoulLang.translate(code, "book.soulland.label.owner", "Pemilik: %s",
                    soul.owner().isEmpty() ? "-" : soul.owner())).append("\n");
            sb.append(SoulLang.translate(code, "book.soulland.label.traits", "Tipe: %s | %s | Elemen: %s",
                    SoulLang.translate(code, "soul.soulland.type." + com.soulland.soul.SoulCategories.typeSlug(soul.type()),
                            soul.type()),
                    SoulLang.translate(code, "soul.soulland.rarity." + soul.rarity().toLowerCase(java.util.Locale.ROOT),
                            soul.rarity()),
                    soul.element())).append("\n\n");
            sb.append(SoulLang.translate(code, "soul.soulland." + soul.id() + ".description", soul.description()))
                    .append("\n\n");
            soul.skills().stream().limit(3).forEach(sk -> sb.append("- ")
                    .append(SoulLang.translate(code, "skill.soulland." + sk.id() + ".name", sk.name()))
                    .append(" (").append(sk.ringsRequired()).append(" ring): ")
                    .append(SoulLang.translate(code, "skill.soulland." + sk.id() + ".description", sk.description()))
                    .append("\n"));
            if (soul.hasEvolution()) {
                MartialSoul evolved = MartialSouls.get(soul.evolutionId());
                if (evolved != null) {
                    sb.append("\n").append(SoulLang.translate(code, "book.soulland.label.evolution",
                            "Bentuk evolusi: %s",
                            SoulLang.translate(code, "soul.soulland." + evolved.id() + ".name", evolved.name())));
                }
            }
            pages.add(trimPage(sb.toString()));
        }

        ListTag list = new ListTag();
        for (String page : pages) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(page))));
        }
        tag.put("pages", list);
        tag.put("filtered_pages", new ListTag());
        return book;
    }

    /** Buku panduan dengan bahasa bawaan mod. */
    public static ItemStack create() {
        return create(SoulLang.DEFAULT);
    }

    /** Halaman tangga peringkat, dibangkitkan dari data rank yang aktif. */
    private static String rankLadder(String language) {
        StringBuilder sb = new StringBuilder();
        sb.append(SoulLang.translate(language, "book.soulland.page.ranks.title", "TANGGA PERINGKAT KULTIVASI"))
                .append("\n\n");
        for (int index = 0; index < CultivationRanks.rankCount(); index++) {
            int end = CultivationRanks.bandEnd(index);
            int start = index == 0 ? 1 : CultivationRanks.bandEnd(index - 1) + 1;
            sb.append(start).append('-').append(end).append(' ')
                    .append(SoulLang.rankName(start, language).getString())
                    .append("  (").append(CultivationRanks.ringsRequiredAtGate(end)).append(" ring)\n");
        }
        sb.append("\n").append(SoulLang.translate(language, "book.soulland.page.ranks.note",
                "Setiap batas peringkat adalah gerbang: kamu harus menyerap ring baru dan bermeditasi penuh sebelum naik."));
        return sb.toString();
    }

    /** Halaman tujuan akhir, menyebut ambang dewa dan tribulasi. */
    private static String rankGoal(String language) {
        return SoulLang.translate(language, "book.soulland.page.ranks.goal",
                "TUJUAN AKHIR\n\nTangga ini berakhir di Dewa, mulai level %s. Mencapainya memunculkan tanda "
                        + "tribulasi: langit bergemuruh dan petir menyambar. Sistem tribulasi penuh menyusul "
                        + "di v1.6, jadi v1.2B hanya menandai ambangnya.\n\n"
                        + "Serap ring emas 1.000.000 tahun, kalahkan Ender Dragon, dan namamu "
                        + "akan melegenda di Benua Douluo.",
                CultivationRanks.GOD_THRESHOLD);
    }

    private static String trimPage(String text) {
        return text.length() <= 800 ? text : text.substring(0, 797) + "...";
    }
}
