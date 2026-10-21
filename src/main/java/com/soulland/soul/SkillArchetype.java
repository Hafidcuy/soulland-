package com.soulland.soul;

/**
 * Perilaku dasar sebuah skill martial soul.
 *
 * <p>Arketipe ini yang membuat 132 skill baru bisa tetap realistis tanpa menulis
 * 132 blok {@code case} yang saling mirip. Tiap arketipe punya satu bentuk
 * kerja yang jelas: satu pola damage, satu pola kendali, satu pola penyokong,
 * atau satu pola pergerakan. Yang membedakan skill satu soul dengan skill
 * soul lain adalah angkanya (damage, radius, durasi, amplifier) dan tanda
 * visualnya, bukan nama arketipenya.
 *
 * <p>Aturan main yang dipegang: tidak boleh ada skill terdaftar yang tidak
 * melakukan apa-apa. Karena itu {@link Kind} di bawah dipakai unit test untuk
 * memastikan tiap skill punya angka yang masuk akal untuk jenisnya.
 */
public enum SkillArchetype {

    /** Hantaman ke satu target terdekat. Damage penuh ke satu musuh. */
    STRIKE(Kind.DAMAGE, "hantaman satu target"),

    /** Sapuan maju di depan pemain. Damage ke musuh dalam kerucut. */
    WAVE(Kind.DAMAGE, "sapuan kerucut"),

    /** Ledakan area. Damage ke semua musuh dalam radius. */
    BLAST(Kind.DAMAGE, "ledakan area"),

    /** Hantaman yang memompa vitality: sebagian damage kembali ke penyerang. */
    LIFESTEAL(Kind.DAMAGE, "hantaman yang memompa vitality"),

    /** Racun area: damage kecil plus racun yang menumpuk. */
    VENOM(Kind.DAMAGE, "racun area"),

    /** Pengikatan: memperlambat dan melemahkan tanpa memberi damage. */
    ROOT(Kind.CONTROL, "pengikatan dan pelemahan"),

    /** Dorongan keras: damage ringan, mundur, dan perlambatan. */
    STUN(Kind.CONTROL, "dorongan dan penatahan"),

    /** Rantai: damage beruntun ke beberapa musuh berdekatan. */
    CHAIN(Kind.DAMAGE, "rantai antar musuh"),

    /** Perisai diri: ketahanan dan serapan damage. */
    SHIELD(Kind.SUPPORT, "perisai diri"),

    /** Penguatan diri: kekuatan dan kecepatan. */
    BUFF(Kind.SUPPORT, "penguatan diri"),

    /** Berkah ke sekutu: kekuatan dan ketahanan untuk tim. */
    AURA(Kind.SUPPORT, "berkah untuk sekutu"),

    /** Penyembuhan sekutu di sekitar. */
    MEND(Kind.SUPPORT, "penyembuhan sekutu"),

    /** Penyembuhan diri sendiri. */
    HEAL(Kind.SUPPORT, "penyembuhan diri"),

    /** Lompatan kedipan: pindah posisi ke depan. */
    BLINK(Kind.MOBILITY, "lompatan kedipan"),

    /** Terobosan: melesat sambil menabrak musuh di jalur. */
    DASH(Kind.MOBILITY, "terobosan"),

    /** Penandaan: satu musuh ditandai supaya serangan berikutnya mengena lebih keras. */
    MARK(Kind.CONTROL, "penandaan musuh"),

    /** Pancing: menarik musuh ke arah pemain lalu memperlambatnya. */
    LURE(Kind.CONTROL, "pancing musuh"),

    /** Gangguan: mengikis buff musuh dan melemahkannya. */
    DISRUPT(Kind.CONTROL, "gangguan buff musuh"),

    /** Eksekusi: damage besar ke satu target, lebih besar lagi kalau target lemah. */
    EXECUTE(Kind.DAMAGE, "eksekusi satu target"),

    /** Nova: ledakan besar ke seluruh area. */
    NOVA(Kind.DAMAGE, "nova area luas");

    /** Kelompok perilaku arketipe, dipakai unit test untuk memastikan skill tidak kosong. */
    public enum Kind {
        /** Memberi damage ke musuh. */
        DAMAGE,
        /** Mengubah kondisi musuh tanpa damage besar. */
        CONTROL,
        /** Menguatkan atau menyembuhkan, baik diri sendiri maupun sekutu. */
        SUPPORT,
        /** Memindahkan posisi pemain. */
        MOBILITY
    }

    private final Kind kind;
    private final String summary;

    SkillArchetype(Kind kind, String summary) {
        this.kind = kind;
        this.summary = summary;
    }

    /** Kelompok perilaku arketipe ini. */
    public Kind kind() {
        return kind;
    }

    /** Ringkasan singkat perilaku, dipakai di blueprint dan buku panduan. */
    public String summary() {
        return summary;
    }

    /**
     * True kalau arketipe ini butuh musuh dalam jangkauan. Skill seperti ini
     * gagal dengan pesan jelas, bukan diam-diam tidak terjadi.
     */
    public boolean needsTarget() {
        return switch (this) {
            case STRIKE, LIFESTEAL, MARK, EXECUTE -> true;
            default -> false;
        };
    }
}
