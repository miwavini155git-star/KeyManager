package ru.iglo.hunt.managers;

/**
 * Менеджер для управления состоянием прыжка
 */
public class JumpManager {
    private static boolean jumpEnabled = true;

    public static boolean isJumpEnabled() {
        return jumpEnabled;
    }

    public static void setJumpEnabled(boolean enabled) {
        jumpEnabled = enabled;
    }

    public static void toggleJump() {
        jumpEnabled = !jumpEnabled;
    }
}
