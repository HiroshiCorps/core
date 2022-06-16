package fr.redxil.core.common.player.moderator;

import fr.redxil.api.common.API;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CServerModerator implements APIPlayerModerator {

    @Override
    public void disconnectModerator() {

    }

    @Override
    public Optional<String> getCible() {
        return Optional.empty();
    }

    @Override
    public void setCible(String arg0) {

    }

    @Override
    public long getMemberID() {
        return CoreAPI.getInstance().getPlayerManager().getServerPlayer().getMemberID();
    }

    @Override
    public String getName() {
        return CoreAPI.getInstance().getPlayerManager().getServerPlayer().getName();
    }

    @Override
    public UUID getUUID() {
        return CoreAPI.getInstance().getPlayerManager().getServerPlayer().getUUID();
    }

    @Override
    public boolean hasCible() {
        return false;
    }

    @Override
    public boolean isModeratorMod() {
        return false;
    }

    @Override
    public void setModeratorMod(boolean arg0) {

    }

    @Override
    public boolean isVanish() {
        return false;
    }

    @Override
    public void setVanish(boolean arg0) {

    }

    @Override
    public void printSanction(APIOfflinePlayer apiOfflinePlayer, SanctionType sanctionType) {

        Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(getMemberID());

        if (apiPlayer.isEmpty())
            return;

        List<SanctionInfo> sanctionInfos = apiOfflinePlayer.getSanction(sanctionType);

        if (!sanctionInfos.isEmpty()) {

            TextComponentBuilder.createTextComponent("§4 APIPlayer: " + apiOfflinePlayer.getName() + " Sanctions: " + sanctionInfos.size()).sendTo(apiPlayer.get());

            for (int i = sanctionInfos.size() - 1; i >= 0; i--) {

                SanctionInfo sanction = sanctionInfos.get(i);

                TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("\nSanction n°§r§6" + (sanctionInfos.size() - i) + ":");
                tcb.appendNewComponentBuilder("\n§r     §7Sanction ID: §d" + sanction.getSanctionID());
                tcb.appendNewComponentBuilder("\n§r     §7Par: §d" + sanction.getAuthorID());
                tcb.appendNewComponentBuilder("\n§r     §7Le: §d" + DateUtility.getMessage(sanction.getSanctionDateTS()));
                tcb.appendNewComponentBuilder("\n§r     §7Jusqu'au: §d" + DateUtility.getMessage(sanction.getSanctionEndTS()));
                tcb.appendNewComponentBuilder("\n§r     §7Pour: §d" + sanction.getReason());

                String cancelledString = "§aPas cancel";
                Long longID = sanction.getCanceller();
                if (longID != null)
                    cancelledString = longID.toString();

                tcb.appendNewComponentBuilder("\n§r     §7Cancelled: §d" + cancelledString);

                tcb.sendTo(apiPlayer.get());

            }

            TextComponentBuilder.createTextComponent("§4 APIPlayer: " + apiOfflinePlayer.getName() + " Sanctions: " + sanctionInfos.size()).sendTo(apiPlayer.get());
        } else
            TextComponentBuilder.createTextComponent("§4Aucune sanction listée").sendTo(apiPlayer.get());

    }

    @Override
    public void printInfo(APIOfflinePlayer apiOfflinePlayer) {

        TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("§m                    \n");

        if (apiOfflinePlayer instanceof APIPlayer && ((APIPlayer) apiOfflinePlayer).isNick()) {
            tcb.appendNewComponentBuilder("§7→ §rPseudo§7・" + ((APIPlayer) apiOfflinePlayer).getRealName() + "§r\n");
            tcb.appendNewComponentBuilder("§7→ §rNick§7・§a" + apiOfflinePlayer.getName() + "§r\n");
        } else {
            tcb.appendNewComponentBuilder("§7→ §rPseudo§7・" + apiOfflinePlayer.getName() + "§r\n");
        }

        String connectedMsg = "§c✘", server = null;
        if (apiOfflinePlayer.isConnected()) {
            connectedMsg = "§a✓";

            Optional<APIPlayer> player = API.getInstance().getPlayerManager().getPlayer(apiOfflinePlayer.getMemberID());
            if (player.isPresent())
                server = player.get().getServerID().toString();
        }

        tcb.appendNewComponentBuilder("§7→ §rConnecté§7・" + connectedMsg + "§r\n");

        tcb.appendNewComponentBuilder("§7→ §rRank§7・" + apiOfflinePlayer.getRank().getRankName() + "§r\n");

        if (server != null)
            tcb.appendNewComponentBuilder("§7→ §rServeur§7・§a" + server + "§r\n");

        String ip = "Déconnecté";
        if (apiOfflinePlayer instanceof APIPlayer) {
            Optional<RedisManager> redis = API.getInstance().getRedisManager();
            ip = redis.map(redisManager -> String.valueOf(redisManager.getRedissonClient().getList("ip/" + apiOfflinePlayer.getIP().getIp()).size() - 1)).orElse("Error: Redis disconnected");
        }

        tcb.appendNewComponentBuilder("§7→ §rComptes sur la même ip§7・§c" + ip + "§r\n");

        String mute = "§c✘";
        if (apiOfflinePlayer.isMute())
            mute = "§a✓";

        String ban = "§c✘";
        if (apiOfflinePlayer.isBan())
            ban = "§a✓";

        tcb.appendNewComponentBuilder("§7→ §rEtat§7・Banni: " + ban + " §7Mute: " + mute + "§r\n");
        tcb.appendNewComponentBuilder("§m                    \n");

        tcb.sendToID(getMemberID());

    }

}
