/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot.vanish;

import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.moderators.ModeratorManager;
import fr.redxil.api.spigot.utils.ActionBar;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.ModeratorDataValue;
import fr.redxil.core.spigot.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class VanishGestion {

    final CorePlugin corePlugin;
    final HashMap<String, TimerTask> map = new HashMap<>();

    public VanishGestion(CorePlugin corePlugin) {
        this.corePlugin = corePlugin;
    }

    public void setVanish(APIPlayerModerator mod, boolean b) {

        Player modPlayer = Bukkit.getPlayer(mod.getName());
        if (modPlayer == null) return;

        CoreAPI.get().getRedisManager().setRedisString(ModeratorDataValue.MODERATOR_VANISH_REDIS.getString(mod), Boolean.valueOf(b).toString());

        modPlayer.setGameMode(GameMode.SURVIVAL);

        ModeratorManager modManager = CoreAPI.get().getModeratorManager();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!modManager.isModerator(player.getUniqueId())) {
                if (b) player.hidePlayer(corePlugin, modPlayer);
                else player.showPlayer(corePlugin, modPlayer);
            }
        }

        if (b) {

            if (map.containsKey(mod.getName())) return;

            AtomicInteger lastMSG = new AtomicInteger(0);

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (modPlayer.isOnline()) {

                        String message;

                        if (!mod.isModeratorMod()) {

                            switch (lastMSG.get()) {

                                case 0: {
                                    message = "§cVous êtes actuellement invisible !";
                                    break;
                                }

                                case 1: {
                                    message = "Mod moderation: §4§lOFF";
                                    break;
                                }

                                default: {
                                    lastMSG.set(0);
                                    message = "§cVous êtes actuellement invisible !";
                                    break;
                                }

                            }

                        } else {

                            String targetName = mod.getCible();
                            if (targetName != null) {

                                APIPlayer target = CoreAPI.get().getPlayerManager().getPlayer(targetName);

                                if (target != null) {

                                    Player player = Bukkit.getPlayer(target.getUUID());

                                    if (player != null) {

                                        switch (lastMSG.get()) {

                                            case 0: {
                                                message = "§cVous êtes actuellement invisible !";
                                                break;
                                            }

                                            case 1: {
                                                message = "Cible: §a§l" + targetName;
                                                break;
                                            }

                                            case 2: {
                                                double healPercent = (100 * player.getHealth()) / 20;
                                                message = "HP: §a§l" + healPercent;
                                                break;
                                            }

                                            case 3: {
                                                message = "Distance: §a§l" + calculateDiff(modPlayer.getLocation(), player.getLocation());
                                                break;
                                            }

                                            case 4: {
                                                String freezeString = "§c§lNON";
                                                if (target.isFreeze()) freezeString = "§a§lOUI";
                                                message = "Gelé: " + freezeString;
                                                break;
                                            }

                                            default: {
                                                lastMSG.set(0);
                                                message = "§cVous êtes actuellement invisible !";
                                                break;
                                            }

                                        }

                                    } else {
                                        switch (lastMSG.get()) {

                                            case 0: {
                                                message = "§cVous êtes actuellement invisible !";
                                                break;
                                            }

                                            case 1: {
                                                message = "Cible: §a§l" + targetName;
                                                break;
                                            }

                                            case 2: {
                                                message = "Serveur: §a§l" + target.getServer().getServerName();
                                                break;
                                            }

                                            case 3: {
                                                String freezeString = "§c§lNON";
                                                if (target.isFreeze()) freezeString = "§a§lOUI";
                                                message = "Gelé: " + freezeString;
                                                break;
                                            }

                                            default: {
                                                lastMSG.set(0);
                                                message = "§cVous êtes actuellement invisible !";
                                                break;
                                            }

                                        }

                                    }

                                } else {

                                    switch (lastMSG.get()) {

                                        case 0: {
                                            message = "§cVous êtes actuellement invisible !";
                                            break;
                                        }

                                        case 1: {
                                            message = "Cible: §a§l" + targetName;
                                            break;
                                        }

                                        case 2: {
                                            String msg2 = "§c§lDéconnecté";
                                            APIOfflinePlayer offTarget = CoreAPI.get().getPlayerManager().getOfflinePlayer(targetName);
                                            if (offTarget.isBan())
                                                msg2 = "§4§lBANNIS";
                                            message = "Etat: " + msg2;
                                            break;
                                        }

                                        default: {
                                            lastMSG.set(0);
                                            message = "§cVous êtes actuellement invisible !";
                                            break;
                                        }

                                    }

                                }

                            } else {

                                switch (lastMSG.get()) {

                                    case 0: {
                                        message = "§cVous êtes actuellement invisible !";
                                        break;
                                    }

                                    case 1: {
                                        message = "Aucune Cible !";
                                        break;
                                    }

                                    default: {
                                        lastMSG.set(0);
                                        message = "§cVous êtes actuellement invisible !";
                                        break;
                                    }

                                }

                            }
                            lastMSG.set(lastMSG.get() + 1);

                        }

                        ActionBar.sendActionBar(modPlayer, message);

                    } else {
                        map.remove(mod.getName());
                        cancel();
                    }
                }
            };

            new Timer().schedule(timerTask, 0L, 5000L);
            map.put(mod.getName(), timerTask);
        } else {

            if (!map.containsKey(mod.getName())) return;
            map.remove(mod.getName()).cancel();

        }

    }

    public void applyVanish(Player p) {
        if (CoreAPI.get().getModeratorManager().isModerator(p.getUniqueId())) return;

        Collection<Long> mods = CoreAPI.get().getModeratorManager().getLoadedModerator();

        if (mods.isEmpty()) return;

        for (Long mod : mods) {
            APIPlayerModerator moderator = CoreAPI.get().getModeratorManager().getModerator(mod);
            if (moderator.isVanish()) {
                Player modPlayer = Bukkit.getPlayer(moderator.getUUID());
                if (modPlayer != null) p.hidePlayer(corePlugin, modPlayer);
            }
        }
    }

    public void playerDisconnect(Player p) {
        if (!map.containsKey(p.getName())) return;
        map.remove(p.getName()).cancel();
    }

    private double calculateDiff(Location loc1, Location loc2) {
        double diff = 0d;

        double diffX = loc2.getX() - loc1.getX();
        double diffY = loc2.getY() - loc1.getY();
        double diffZ = loc2.getZ() - loc1.getZ();

        double squareHigh = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

        diff += Math.sqrt(Math.pow(squareHigh, 2) + Math.pow(diffZ, 2));

        return diff;
    }

}
