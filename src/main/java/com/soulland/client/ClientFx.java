package com.soulland.client;

/**
 * State visual sisi client: guncangan kamera dan kilat layar.
 *
 * <p>Server mengirim angkanya, client decides kapan harus habis. Semua nilai menurun
 * sendiri supaya tidak ada state yang menggantung.
 */
public final class ClientFx {
    private ClientFx() {}

    private static float shake;
    private static float shakeDecay;
    private static int flashColor;
    private static float flashAlpha;
    private static float flashDecay;

    public static void receive(float shakeAmount, int color, int alpha, int durationTicks) {
        if (shakeAmount > shake) {
            shake = shakeAmount;
            shakeDecay = Math.max(0.001f, shakeAmount / Math.max(1, durationTicks));
        }
        if (alpha > flashAlpha) {
            flashColor = color;
            flashAlpha = alpha / 255f;
            flashDecay = Math.max(0.001f, (alpha / 255f) / Math.max(1, durationTicks));
        }
    }

    public static void tick() {
        if (shake > 0f) {
            shake = Math.max(0f, shake - shakeDecay);
        }
        if (flashAlpha > 0f) {
            flashAlpha = Math.max(0f, flashAlpha - flashDecay);
        }
    }

    /** Besarnya guncangan kamera saat ini, 0 berarti diam. */
    public static float shake() {
        return shake;
    }

    public static int flashColor() {
        return flashColor;
    }

    public static float flashAlpha() {
        return flashAlpha;
    }
}
