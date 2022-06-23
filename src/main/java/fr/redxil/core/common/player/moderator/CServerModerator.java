package fr.redxil.core.common.player.moderator;

import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.redis.RedisManager;

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

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(getMemberID());

        if (apiPlayer.isEmpty())
            return;

        List<SanctionInfo> sanctionInfos = apiOfflinePlayer.getSanction(sanctionType);

        if (!sanctionInfos.isEmpty()) {

            String start = "§4 APIPlayer: " + apiOfflinePlayer.getName() + " Sanctions: " + sanctionInfos.size();
            apiPlayer.get().sendMessage(start);

            for (int i = sanctionInfos.size() - 1; i >= 0; i--) {

                SanctionInfo sanction = sanctionInfos.get(i);

                StringBuilder message = new StringBuilder("\nSanction n°§r§6" + (sanctionInfos.size() - i) + ":");
                message.append("\n§r     §7Sanction ID: §d").append(sanction.getSanctionID());
                message.append("\n§r     §7Par: §d").append(sanction.getAuthorID());
                message.append("\n§r     §7Le: §d").append(DateUtility.getMessage(sanction.getSanctionDateTS()));
                message.append("\n§r     §7Jusqu'au: §d").append(DateUtility.getMessage(sanction.getSanctionEndTS().orElse(null)));
                message.append("\n§r     §7Pour: §d").append(sanction.getReason());

                String cancelledString = "§aPas cancel";
                Optional<Long> longID = sanction.getCanceller();
                if (longID.isPresent())
                    cancelledString = longID.get().toString();

                message.append("\n§r     §7Cancelled: §d").append(cancelledString);

                apiPlayer.get().sendMessage(message.toString());

            }

            apiPlayer.get().sendMessage("§4 APIPlayer: " + apiOfflinePlayer.getName() + " Sanctions: " + sanctionInfos.size());
        } else
            apiPlayer.get().sendMessage("§4Aucune sanction listée");

    }

    @Override
    public void printInfo(APIOfflinePlayer apiOfflinePlayer) {

        Optional<APIPlayer> moderator = CoreAPI.getInstance().getPlayerManager().getPlayer(getMemberID());
        if (moderator.isEmpty())
            return;

        String message = "§m                    \n";

        if (apiOfflinePlayer instanceof APIPlayer && ((APIPlayer) apiOfflinePlayer).isNick()) {
            message += "§7→ §rPseudo§7・" + ((APIPlayer) apiOfflinePlayer).getRealName() + "§r\n";
            message += "§7→ §rNick§7・§a" + apiOfflinePlayer.getName() + "§r\n";
        } else {
            message += "§7→ §rPseudo§7・" + apiOfflinePlayer.getName() + "§r\n";
        }

        String connectedMsg = "§c✘", server = null;
        if (apiOfflinePlayer.isConnected()) {
            connectedMsg = "§a✓";

            Optional<APIPlayer> player = CoreAPI.getInstance().getPlayerManager().getPlayer(apiOfflinePlayer.getMemberID());
            if (player.isPresent())
                server = player.get().getServerID().toString();
        }

        message += "§7→ §rConnecté§7・" + connectedMsg + "§r\n";

        message += "§7→ §rRank§7・" + apiOfflinePlayer.getRank().getRankName() + "§r\n";

        if (server != null)
            message += "§7→ §rServeur§7・§a" + server + "§r\n";

        String ip = "Déconnecté";
        if (apiOfflinePlayer instanceof APIPlayer) {
            Optional<RedisManager> redis = CoreAPI.getInstance().getRedisManager();
            ip = redis.map(redisManager -> String.valueOf(redisManager.getRedissonClient().getList("ip/" + apiOfflinePlayer.getIP().getIp()).size() - 1)).orElse("Error: Redis disconnected");
        }

        message += "§7→ §rComptes sur la même ip§7・§c" + ip + "§r\n";

        String mute = "§c✘";
        if (apiOfflinePlayer.isMute())
            mute = "§a✓";

        String ban = "§c✘";
        if (apiOfflinePlayer.isBan())
            ban = "§a✓";

        message += "§7→ §rEtat§7・Banni: " + ban + " §7Mute: " + mute + "§r\n";
        message += "§m                    \n";

        moderator.get().sendMessage(message);

    }

}
