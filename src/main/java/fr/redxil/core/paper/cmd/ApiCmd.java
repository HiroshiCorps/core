/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.cmd;

import fr.redxil.api.common.API;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.data.ServerDataValue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redisson.api.RedissonClient;

import java.util.concurrent.atomic.AtomicLong;

public class ApiCmd extends Command {

    protected ApiCmd() {
        super("api");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;

        RedissonClient redis = API.getInstance().getRedisManager().getRedissonClient();

        long time = System.currentTimeMillis();
        AtomicLong newTime = new AtomicLong();

        API.getInstance().getSQLConnection().query("SELECT * FROM `members`", resultSet -> newTime.set(System.currentTimeMillis()));

        long response = newTime.get() - time;

        TextComponentBuilder.createTextComponent(
                "\n§7§m                  §6 [§e SERVER Api §6] §7§m                  \n " +
                        "\n§r§eApi reponse time§7: §b" + response + " ms" +
                        "\n§r§eUser cache size§7: §b" + API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString()).size() +
                        "\n§r§eServers cache size§7: §b" + redis.getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).size() +
                        "\n \n§r§7§m                                                              \n"
        ).sendTo(((Player) commandSender).getUniqueId());
        return true;
    }
}
