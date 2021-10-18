/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot.cmd;

import fr.redxil.api.common.data.PlayerDataValue;
import fr.redxil.api.common.data.ServerDataValue;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.spigot.command.CommandBuilder;
import fr.redxil.api.spigot.command.CommandInfo;
import fr.redxil.core.common.CoreAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RedissonClient;

import java.util.concurrent.atomic.AtomicLong;

@CommandInfo(
        name = "api",
        permission = 500
)
public class ApiCmd extends CommandBuilder {

    public ApiCmd(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) return;

        RedissonClient redis = CoreAPI.get().getRedisManager().getRedissonClient();

        long time = System.currentTimeMillis();
        AtomicLong newTime = new AtomicLong();

        CoreAPI.get().getSQLConnection().query("SELECT * FROM `members`", resultSet -> newTime.set(System.currentTimeMillis()));

        long response = newTime.get() - time;

        TextComponentBuilder.createTextComponent(
                "\n§7§m                  §6 [§e SERVER Api §6] §7§m                  \n " +
                        "\n§r§eApi reponse time§7: §b" + response + " ms" +
                        "\n§r§eUser cache size§7: §b" + CoreAPI.get().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_NUMBER.getString(null)) +
                        "\n§r§eServers cache size§7: §b" + redis.getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).size() +
                        "\n \n§r§7§m                                                              \n"
        ).sendTo(((Player) commandSender).getUniqueId());

    }

}
