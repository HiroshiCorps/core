/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.api.common.utils.TextUtils;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class InfoCmd implements Command {

    public void execute(CommandSource sender, String @NonNull [] args) {
        if (!(sender instanceof Player)) return;

        APIPlayerModerator APIPlayerModerator = CoreAPI.get().getModeratorManager().getModerator(((Player) sender).getUniqueId());

        if (APIPlayerModerator == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(((Player) sender).getUniqueId());
            return;
        }

        if (args.length == 0) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Erreur de syntaxe, utilisez : /info (Joueur) (Non obligatoire: ban/mute/kick/warn)").setColor(Color.RED).sendTo(((Player) sender).getUniqueId());
            return;
        }

        APIOfflinePlayer apiPlayerTarget = CoreAPI.get().getPlayerManager().getOfflinePlayer(args[0]);
        if (apiPlayerTarget == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("La target ne s'est jamais connecté.").setColor(Color.RED)
                    .sendTo(((Player) sender).getUniqueId());
            return;
        }

        if (args.length == 1) {

            TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("§m                    \n");
            tcb.appendNewComponentBuilder("§7→ §rPseudo§7・" + apiPlayerTarget.getName() + "§r\n");

            String connectedMsg = "§c✘", server = null;
            if (apiPlayerTarget.isConnected()) {
                connectedMsg = "§a✓";
                server = CoreAPI.get().getPlayerManager().getPlayer(apiPlayerTarget.getMemberId()).getServer().getServerName();
            }

            tcb.appendNewComponentBuilder("§7→ §rConnecté§7・" + connectedMsg + "§r\n");

            if (CoreAPI.get().getNickGestion().hasNick(apiPlayerTarget)) {
                tcb.appendNewComponentBuilder("§7→ §rNick§7・§a" + CoreAPI.get().getNickGestion().getNickData(apiPlayerTarget).getName() + "§r\n");
            }

            tcb.appendNewComponentBuilder("§7→ §rRank§7・" + apiPlayerTarget.getRank().getRankName() + "§r\n");

            if (server != null)
                tcb.appendNewComponentBuilder("§7→ §rServeur§7・§a" + server + "§r\n");

            String ip = Color.RED + "Déconnecté";
            if (apiPlayerTarget instanceof APIPlayer)
                ip = String.valueOf(CoreAPI.get().getRedisManager().getRedissonClient().getList("ip/" + ((APIPlayer) apiPlayerTarget).getIpInfo().getIp()).size() - 1);

            tcb.appendNewComponentBuilder("§7→ §rComptes sur la même ip§7・§c" + ip + "§r\n");

            String mute = "§c✘";
            if (apiPlayerTarget.isMute())
                mute = "§a✓";

            String ban = "§c✘";
            if (apiPlayerTarget.isBan())
                ban = "§a✓";

            tcb.appendNewComponentBuilder("§7→ §rEtat§7・Banni: " + ban + " §7Mute: " + mute + "§r\n");
            tcb.appendNewComponentBuilder("§m                    \n");

            tcb.sendTo(((Player) sender).getUniqueId());

            return;

        }

        SanctionType st = SanctionType.getSanctionType(args[1]);
        if (st != null)
            printSanction(apiPlayerTarget.getSanction(st), (Player) sender, apiPlayerTarget.getName());
        else
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION") + "Erreur de syntaxe, utilisez : /info (Joueur) (Non obligatoire: ban/mute/kick/warn)").sendTo(((Player) sender).getUniqueId());

    }

    public void printSanction(List<SanctionInfo> sanctionModels, Player asker, String target) {

        if (!sanctionModels.isEmpty()) {

            TextComponentBuilder.createTextComponent("§4 APIPlayer: " + target + " Sanctions: " + sanctionModels.size()).sendTo(asker.getUniqueId());

            for (int i = sanctionModels.size() - 1; i >= 0; i--) {

                SanctionInfo sanction = sanctionModels.get(i);

                TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("\nSanction n°§r§6" + (sanctionModels.size() - i) + ":");
                tcb.appendNewComponentBuilder("\n§r     §7Sanction ID: §d" + sanction.getSanctionID());
                tcb.appendNewComponentBuilder("\n§r     §7Par: §d" + CoreAPI.get().getPlayerManager().getOfflinePlayer(sanction.getAuthorID()).getName());
                tcb.appendNewComponentBuilder("\n§r     §7Le: §d" + DateUtility.getMessage(sanction.getSanctionDateTS()));
                tcb.appendNewComponentBuilder("\n§r     §7Jusqu'au: §d" + DateUtility.getMessage(sanction.getSanctionEndTS()));
                tcb.appendNewComponentBuilder("\n§r     §7Pour: §d" + sanction.getReason());

                String cancelledString = "§aPas cancel";
                Long longID = sanction.getCanceller();
                if (longID != null)
                    cancelledString = CoreAPI.get().getPlayerManager().getOfflinePlayer(sanction.getCanceller()).getName();

                tcb.appendNewComponentBuilder("\n§r     §7Cancelled: §d" + cancelledString);

                tcb.sendTo(asker.getUniqueId());

            }

            TextComponentBuilder.createTextComponent("§4 APIPlayer: " + target + " Sanctions: " + sanctionModels.size()).sendTo(asker.getUniqueId());
        } else
            TextComponentBuilder.createTextComponent("§4Aucune sanction listée").sendTo(asker.getUniqueId());

    }

}