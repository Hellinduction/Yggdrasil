package dev.heypr.yggdrasil.misc.papi.impl;

import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.ColorManager;
import dev.heypr.yggdrasil.misc.papi.IPlaceholder;

public final class LivesColorPlaceholder implements IPlaceholder {
    @Override
    public String resolve(final PlayerData playerData) {
        return ColorManager.getColor(playerData.getLives()).toString();
    }
}