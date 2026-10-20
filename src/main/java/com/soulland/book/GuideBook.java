package com.soulland.book;

import com.soulland.soul.MartialSouls;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Buku panduan lengkap dunia Soul Land, diberikan otomatis kepada setiap pemain baru. */
public final class GuideBook {
    private GuideBook() {}

    public static ItemStack create() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        var tag = book.getOrCreateTag();
        tag.putString("title", "Panduan Dunia Soul Land");
        tag.putString("author", "Tang San");
        tag.putInt("generation", 0);

        List<String> pages = new ArrayList<>();
        pages.add("SELAMAT DATANG DI BENUA DOULUO\n\nKamu telah membangkitkan Martial Soul! Buku ini menjelaskan seluruh sistem dunia Soul Land: kultivasi, spirit ring, skill, soul beast, dan jalan menuju Title Douluo.");
        pages.add("1. MARTIAL SOUL (WU HUN)\n\nSaat pertama masuk dunia, Upacara Kebangkitan terbuka dan kamu memilih sendiri martial soul-mu.\nPilihan itu tetap selamanya, HANYA bisa diubah dalam mode Creative dengan perintah /martialsoul reroll.");
        pages.add("2. LEVEL SPIRIT POWER\n\nLevel 1-99. Bunuh soul beast (monster) untuk mendapat XP kultivasi.\nSetiap 10 level ada GERBANG peringkat: kamu tidak bisa naik level sebelum menyerap spirit ring baru.");
        pages.add("3. PERINGKAT KULTIVASI\n\n1-10 Spirit Scholar\n11-20 Spirit Master\n21-30 Grandmaster\n31-40 Spirit Elder\n41-50 Spirit Ancestor\n51-60 Spirit King\n61-70 Spirit Emperor\n71-80 Spirit Sage\n81-90 Spirit Douluo\n91-99 Title Douluo");
        pages.add("4. SPIRIT RING\n\nDidapat dari membunuh soul beast:\n- 100 tahun (monster lemah)\n- 1.000 tahun (monster sedang)\n- 10.000 tahun (monster kuat & boss)\n- 100.000 tahun (Ender Dragon!)\nKlik kanan ring untuk menyerapnya. Ada syarat level minimum.");
        pages.add("5. SKILL MARTIAL SOUL\n\nSetiap martial soul punya 4 skill:\n- Skill 1 terbuka di 1 spirit ring -> tekan [R]\n- Skill 2 terbuka di 3 spirit ring -> tekan [G]\n- Skill 3 terbuka di 5 spirit ring -> tekan [X]\n- Skill 4 terbuka di 7 spirit ring -> tekan [C]\nSkill memakai Spirit Power (energi biru di HUD) dan punya cooldown.");
        pages.add("6. SPIRIT POWER (ENERGI)\n\nEnergi maksimum 100 dan pulih otomatis seiring waktu. Lihat level, peringkat, ring, dan energimu di HUD kiri atas, atau ketik /soulinfo.");
        pages.add("7. SOUL BEAST & BOSS\n\nMonster adalah soul beast. Boss dunia ini JAUH lebih kuat dari biasa:\n- Elder Guardian (HP 120)\n- Wither (HP 600)\n- Warden (HP 800)\n- ENDER DRAGON (HP 1000) - boss akhir! Membunuhnya memberi ring 100.000 tahun.");
        pages.add("8. TUJUAN AKHIR\n\nJadilah Title Douluo (level 91+) dengan 9 spirit ring, lalu kalahkan Ender Dragon, Kaisar Soul Beast terakhir. Serap ring 100.000 tahunnya dan namamu akan melegenda di Benua Douluo.");
        pages.add("9. PERINTAH (COMMAND)\n\n/soulinfo - lihat status kultivasimu\n/martialsoul info - detail martial soul-mu\n/martialsoul reroll - undi ulang (Creative saja)\n/cultivation set <pemain> <level> - admin/OP");
        pages.add("10. MULTIPLAYER\n\nSistem ini berjalan penuh di server: upacara, level, ring, dan skill setiap pemain tersimpan sendiri-sendiri.\nAjak temanmu, bentuk tim (support Pagoda sangat membantu!), dan taklukkan boss bersama.");

        // Daftar martial soul + skill-nya dihasilkan otomatis dari registry agar selalu akurat.
        MartialSouls.all().forEach(soul -> {
            StringBuilder sb = new StringBuilder();
            sb.append(soul.name().toUpperCase()).append("\n");
            sb.append("Pemilik: ").append(soul.owner()).append("\n");
            sb.append("Tipe: ").append(soul.type()).append(" | ").append(soul.rarity()).append("\n\n");
            soul.skills().forEach(sk -> sb.append("- ").append(sk.name()).append(" (").append(sk.ringsRequired()).append(" ring): ").append(sk.description()).append("\n"));
            pages.add(trimPage(sb.toString()));
        });

        pages.add("SELAMAT BERKULTIVASI!\n\n\"Tidak ada martial soul yang sampah, yang ada hanya spirit master yang sampah.\" - Grandmaster Yu Xiaogang\n\nBerlatihlah, berburu bersama tim, dan jangan menyerah. Jalan menuju dewa dimulai dari level 1.");

        ListTag list = new ListTag();
        for (String page : pages) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(page))));
        }
        tag.put("pages", list);
        tag.put("filtered_pages", new ListTag());
        return book;
    }

    private static String trimPage(String text) {
        return text.length() <= 800 ? text : text.substring(0, 797) + "...";
    }
}
