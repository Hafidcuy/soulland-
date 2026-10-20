package com.soulland.soul;

/** Menentukan siapa yang terkena efek sebuah skill. */
public enum SkillTarget {
    /** Hanya pemain sendiri. */
    SELF,
    /** Satu musuh terdekat dalam radius. Gagal kalau tidak ada musuh. */
    ENEMY_NEAREST,
    /** Semua musuh dalam radius. */
    ENEMY_AOE,
    /** Musuh dalam koridor lurus di depan pemain. */
    ENEMY_LINE,
    /** Musuh dalam sudut di depan pemain. */
    ENEMY_CONE,
    /** Area di depan pemain: musuh yang berdiri di sana terkena. */
    POINT_AOE,
    /** Sekutu di sekitar, termasuk pemain sendiri. */
    ALLY_AURA
}
