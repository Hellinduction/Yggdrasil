package dev.heypr.yggdrasil.misc.papi.impl;

import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.papi.IPlaceholder;

public final class KillsPlaceholder implements IPlaceholder {
    @Override
    public String resolve(final PlayerData playerData) {
        return String.valueOf(playerData.getKills());
    }
}