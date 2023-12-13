package io.github.lukegrahamlandry.tribes.api;

import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;

import java.util.UUID;

public interface TribesAPI {

    static void registerFakePlayer(UUID id) {
        TribesManager.registerFakePlayer(id);
    }

    static boolean checkFakePlayerBypass(UUID id) {
        return TribesManager.doesPlayerBypassChecks(id);
    }

}
