package com.soulland.book;

import com.soulland.soul.MartialSoul;
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
        pages.add("SELAMAT DATANG DI BENUA DOULUO\n\nKamu telah membangkitkan Martial Soul! Buku ini menjelaskan seluruh sistem: kultivasi, spirit ring, skill, soul beast, sekte, dan jalan menuju Title Douluo.");
        pages.add("0. LAYAR SAMBUTAN\n\nSaat pertama kali masuk dunia, layar sambutan terbuka sendiri. Isinya cara main, angka level dan ring, serta daftar tombol. Tekan H atau ketik /panduan kapan saja untuk membukanya lagi.\n\nKartu Martial Soul (J) memuat potret besarmu, daftar skill lengkap, isi sembilan slot spirit ring, dan tempat memilih sekte.");
        pages.add("1. MARTIAL SOUL (WU HUN)\n\nSaat pertama masuk dunia, martial soul-mu diundi acak seperti takdir saat lahir. Hasil undian permanen dan hanya bisa diubah di mode Creative dengan /martialsoul reroll.\n\nPool undian berisi 1.434 martial soul dari seluruh Benua Douluo: bentuk dasar, bentuk evolusi, bentuk tahun ring, soul gabungan, dan body soul setelah awakening kedua.");
        pages.add("2. MENAMPILKAN MARTIAL SOUL\n\nKlik kanan Kartu Martial Soul (tekan J) untuk membuka kartu soul: potret besar, daftar skill, status ring, dan pilihan sekte.\n\nTekan K untuk memunculkan martial soul sungguhan yang melayang di belakangmu. Saat kamu memakai skill, martial soul itu ikut maju, jadi seranganmu terlihat punya sumber.");
        pages.add("3. LEVEL SPIRIT POWER\n\nLevel 1-99. Bunuh soul beast untuk mendapat XP kultivasi.\n\nSetiap 10 level ada GERBANG peringkat: kamu tidak bisa naik level sebelum menyerap spirit ring baru.");
        pages.add("4. PERINGKAT KULTIVASI\n\n1-10 Spirit Scholar\n11-20 Spirit Master\n21-30 Spirit Grandmaster\n31-40 Spirit Elder\n41-50 Spirit Ancestor\n51-60 Spirit King\n61-70 Spirit Emperor\n71-80 Spirit Sage\n81-90 Spirit Douluo\n91-99 Title Douluo");
        pages.add("5. SPIRIT RING\n\nDidapat dari membunuh soul beast:\n- 100 tahun (monster lemah)\n- 1.000 tahun (monster sedang)\n- 10.000 tahun (monster kuat dan boss)\n- 100.000 tahun (Ender Dragon)\n\nKlik kanan ring untuk menyerapnya. Tiap ring memberi +2 HP maksimum dan +0.5 attack damage.");
        pages.add("6. SKILL MARTIAL SOUL\n\nSetiap martial soul punya 5 skill. Skill terbuka bertahap mengikuti jumlah ring:\n- Skill 1: 1 ring (R)\n- Skill 2: 2 ring (G)\n- Skill 3: 3 ring (F)\n- Skill 4: 5 ring (V)\n- Skill 5: 7 ring (Z)\n\nSetiap skill punya bentuk efek sendiri: bola, garis, kerucut, cincin, cakram, pilar, atau badai. Tidak ada dua skill yang terlihat sama.");
        pages.add("7. LEDAKAN SPIRIT RING\n\nTekan X untuk melepaskan energi seluruh spirit ring sekaligus. Damage dan radius ledakan mengikuti jumlah ring yang kamu miliki. Cara paling cepat memakai investasi ring di saat kritis.");
        pages.add("8. ABSORPSI BERANI\n\nTahan Shift lalu klik kanan spirit ring untuk absorpsi berani. Kamu membayar 20% HP dan 40 Spirit Power, dengan peluang 55% ring naik satu tingkat dan skill ring tambahan terbuka. Kalau gagal, ring hancur dan vitalitas turun.\n\nAbsorpsi biasa selalu berhasil, tapi hanya memberi satu skill ring.");
        pages.add("9. LOADOUT SKILL\n\nLima slot tombol (R G F V Z) bisa diisi skill bebas: skill martial soul atau skill spirit ring. Buka kartu soul dengan J, tab SKILLS, lalu klik skill untuk memasangnya. Klik slot untuk mengosongkan dan kembali ke urutan bawaan.\n\nSkill spirit ring terbuka setiap kali kamu menyerap ring, dan satu skill ekstra tiap absorpsi berani yang berhasil.");
        pages.add("10. RANTAI SOUL\n\nSetiap skill yang berhasil dilepas menambah satu mata rantai. Rantai bertahan 4 detik dan setiap mata menambah 8% damage skill berikutnya, maksimal 5 mata (+40%). Jadi urutan skill itu penting: skill area di awal, skill tunggal di akhir.");
        pages.add("11. LAWAN ELEMEN\n\nSetiap soul beast punya elemen yang tertulis pada namanya: Api, Es, Petir, Angin, Air, Tanah, Racun, Tumbuhan, Cahaya, atau Gelap. Elemen martial soul-mu mengalahi elemen tertentu dan kalah dari yang lain: +25% damage kalau menang, -15% kalau kalah.\n\nBawa soul yang elemennya cocok dengan berburu.");
        pages.add("12. BAKAR DARAH\n\nTekan B untuk membakar 25 HP menjadi 30 Spirit Power. Selama 8 detik berikutnya damage skill naik 50%. Dipakai saat Spirit Power habis dan kamu masih harus bertarung. Cooldown 45 detik, dan tidak boleh dipakai di bawah 35 HP.");
        pages.add("13. UPACARA TEROBOSAN\n\nDi setiap gerbang peringkat (10, 20, ... 90) kamu tidak bisa naik hanya dengan XP. Kamu harus menahan C dan bermeditasi 3 detik penuh, dengan memakai 1 Esensi Jiwa. Ritual inilah yang membedakan kultivasi dari sekadar grind.");
        pages.add("14. MEDITASI\n\nTahan C untuk bermeditasi. Diam di tempat: kanal spiritual terisi, XP dan Spirit Power bertambah. Bergerak akan memutus meditasi.");
        pages.add("15. SEKTE\n\nTekan J lalu tab SECT untuk memilih sekte. Sekte memberi bonus pasif yang selalu berlaku: Sekte Tang (+20% XP), Sekte Shrek (+12% damage), Sekte Bintang Luo (+10% kecepatan dan HP), Sekte Tujuh Harta (+20% HP dan penyembuhan).");
        pages.add("16. EVOLUSI MARTIAL SOUL\n\nDengan 6 spirit ring, 8 Esensi Jiwa, dan satu Inti Evolusi, martial soul-mu bisa berevolusi ke bentuk yang lebih kuat. Contoh: Blue Silver Grass menjadi Blue Silver Emperor, Pagoda Tujuh Lantai menjadi Pagoda Sembilan Harta, Harimau Putih menjadi Harimau Putih Mata Evil, dan Phoenix Api Jahat menjadi Fire Phoenix.\n\nBentuk evolusi punya skill sendiri dan potret sendiri.");
        pages.add("17. SOUL BEAST DAN BOSS\n\nMonster adalah soul beast, dan namanya menyebut usianya: 10 Tahun, 100 Tahun, 1.000 Tahun, atau 10.000 Tahun. Boss dunia ini jauh lebih kuat:\n- Elder Guardian (HP 120)\n- Wither (HP 600)\n- Warden (HP 800)\n- ENDER DRAGON (HP 1000), boss akhir");
        pages.add("18. TUJUAN AKHIR\n\nJadilah Title Douluo (level 91+) dengan 9 spirit ring, lalu kalahkan Ender Dragon, Kaisar Soul Beast terakhir. Serap ring 100.000 tahunnya dan namamu akan melegenda di Benua Douluo.");
        pages.add("19. PERINTAH\n\n/soulinfo - status kultivasimu\n/martialsoul info - detail martial soul\n/martialsoul summon - munculkan atau sembunyikan martial soul\n/sect info - lihat sektemu\n/sect choose <nama> - pilih sekte\n/martialsoul reroll - undi ulang (Creative saja)\n/cultivation set <pemain> <level> - admin/OP");
        pages.add("20. MULTIPLAYER\n\nSemua sistem berjalan penuh di server: gacha, level, ring, skill, sekte, dan evolusi setiap pemain tersimpan sendiri. Semua pemain wajib memasang mod yang sama.\n\nMain bareng: Pagoda Tujuh Lantai menyembuhkan, Harimau Putih menghancurkan, Sosis Ajaib menjaga.");

        // Daftar martial soul ikonik, dihasilkan otomatis dari registry agar selalu akurat.
        int shown = 0;
        for (MartialSoul soul : MartialSouls.notable()) {
            if (shown++ >= 24) break;
            StringBuilder sb = new StringBuilder();
            sb.append(soul.name().toUpperCase()).append("\n");
            if (!soul.nameZh().isEmpty()) {
                sb.append("Nama Mandarin: ").append(soul.nameZh()).append("\n");
            }
            sb.append("Pemilik: ").append(soul.owner().isEmpty() ? "-" : soul.owner()).append("\n");
            sb.append("Tipe: ").append(soul.type()).append(" | ").append(soul.rarity())
                    .append(" | Elemen: ").append(soul.element()).append("\n\n");
            sb.append(soul.description()).append("\n\n");
            soul.skills().stream().limit(3).forEach(sk -> sb.append("- ").append(sk.name())
                    .append(" (").append(sk.ringsRequired()).append(" ring): ").append(sk.description()).append("\n"));
            if (soul.hasEvolution()) {
                MartialSoul evolved = MartialSouls.get(soul.evolutionId());
                if (evolved != null) {
                    sb.append("\nBentuk evolusi: ").append(evolved.name());
                }
            }
            pages.add(trimPage(sb.toString()));
        }

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
