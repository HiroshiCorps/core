/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.receiver;

import fr.redline.pms.pm.PMReceiver;
import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.group.team.Team;
import fr.redxil.api.paper.game.GameBuilder;
import fr.redxil.core.common.CoreAPI;
import org.redisson.api.RedissonClient;

import java.util.Optional;

public class PMListen implements PMReceiver {

    public PMListen() {
        CoreAPI.getInstance().getRedisManager().ifPresent(redis -> {
            RedissonClient rc = redis.getRedissonClient();
            RedisPMManager.addRedissonPMListener(rc, "forceSTART", String.class, this);
            RedisPMManager.addRedissonPMListener(rc, "forceSTOPSTART", String.class, this);
            RedisPMManager.addRedissonPMListener(rc, "forceEND", String.class, this);
            RedisPMManager.addRedissonPMListener(rc, "forceWIN", String.class, this);
        });
    }

    @Override
    public void redisPluginMessageReceived(String s, Object o) {
        switch (s) {
            case "forceSTART" -> GameBuilder.getGameBuilder().ifPresent(GameBuilder::forceStart);
            case "forceSTOPSTART" -> GameBuilder.getGameBuilder().ifPresent(GameBuilder::stopStart);
            case "forceEND" ->
                    GameBuilder.getGameBuilder().ifPresent(gameBuilder -> gameBuilder.forceEnd(((String) o).split("<split>")[1]));
            case "forceWIN" -> {
                String[] splitted = ((String) o).split("<split>");
                Optional<Game> game = CoreAPI.getInstance().getGameManager().getGameByServerID(CoreAPI.getInstance().getServerID());
                if (game.isEmpty())
                    return;
                Optional<Team> team = CoreAPI.getInstance().getTeamManager(CoreAPI.getInstance().getServerID()).getTeam(splitted[1]);
                if (team.isEmpty())
                    return;
                GameBuilder.getGameBuilder().ifPresent(gameBuilder -> gameBuilder.forceWin(team.get(), splitted[2]));
            }
        }
    }
}
