/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player.moderator;

import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.ModeratorDataValue;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.sql.SQLModel;
import fr.redxil.core.common.sql.SQLModels;
import org.redisson.api.RList;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CPlayerModerator implements APIPlayerModerator {

    private final long memberID;

    public CPlayerModerator(long memberId) {
        this.memberID = memberId;
        new SQLModels<>(ModeratorModel.class).getFirst("WHERE " + ModeratorDataValue.MODERATOR_MEMBERID_SQL.getString(null) + " = ?", memberId);
    }

    protected static APIPlayerModerator initModerator(APIPlayer apiPlayer) {

        Long memberID = apiPlayer.getMemberId();

        if (!CoreAPI.get().getModeratorManager().isModerator(memberID)) return null;

        ModeratorModel model = new SQLModels<>(ModeratorModel.class).getOrInsert(new HashMap<String, Object>() {{
            this.put(ModeratorDataValue.MODERATOR_MEMBERID_SQL.getString(null, null), memberID);
            this.put(ModeratorDataValue.MODERATOR_MOD_SQL.getString(null, null), Boolean.valueOf(false).toString());
            this.put(ModeratorDataValue.MODERATOR_VANISH_SQL.getString(null, null), Boolean.valueOf(false).toString());
            this.put(ModeratorDataValue.MODERATOR_CIBLE_SQL.getString(null), null);
        }}, "WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        RedisManager rm = CoreAPI.get().getRedisManager();

        rm.setRedisString(ModeratorDataValue.MODERATOR_NAME_REDIS.getString(null, memberID), apiPlayer.getName());
        rm.setRedisString(ModeratorDataValue.MODERATOR_MOD_REDIS.getString(null, memberID), model.getString(ModeratorDataValue.MODERATOR_MOD_SQL.getString(null, null)));
        rm.setRedisString(ModeratorDataValue.MODERATOR_VANISH_REDIS.getString(null, memberID), model.getString(ModeratorDataValue.MODERATOR_VANISH_SQL.getString(null, null)));
        rm.setRedisString(ModeratorDataValue.MODERATOR_CIBLE_REDIS.getString(null, memberID), model.getString(ModeratorDataValue.MODERATOR_CIBLE_SQL.getString(null, null)));
        rm.setRedisString(ModeratorDataValue.MODERATOR_UUID_REDIS.getString(null, memberID), apiPlayer.getUUID().toString());

        RList<Long> idList = rm.getRedisList(ModeratorDataValue.LIST_MODERATOR.getString(null));
        if (!idList.contains(memberID))
            idList.add(memberID);

        return new CPlayerModerator(memberID);

    }

    @Override
    public boolean hasCible() {
        return getCible() != null;
    }

    @Override
    public String getCible() {
        return CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_CIBLE_REDIS.getString(this));
    }

    @Override
    public void setCible(String s) {
        CoreAPI.get().getRedisManager().setRedisString(ModeratorDataValue.MODERATOR_CIBLE_REDIS.getString(this), s);
    }

    @Override
    public boolean isVanish() {
        String bool = CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_VANISH_REDIS.getString(this));
        if (bool == null)
            return false;
        else
            return Boolean.parseBoolean(bool);
    }

    @Override
    public void printSanction(APIOfflinePlayer apiOfflinePlayer, SanctionType sanctionType) {

        List<SanctionInfo> sanctionInfos = apiOfflinePlayer.getSanction(sanctionType);

        if (!sanctionInfos.isEmpty()) {

            TextComponentBuilder.createTextComponent("§4 APIPlayer: " + apiOfflinePlayer.getName() + " Sanctions: " + sanctionInfos.size()).sendTo(this.getUUID());

            for (int i = sanctionInfos.size() - 1; i >= 0; i--) {

                SanctionInfo sanction = sanctionInfos.get(i);

                TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("\nSanction n°§r§6" + (sanctionInfos.size() - i) + ":");
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

                tcb.sendTo(this.getUUID());

            }

            TextComponentBuilder.createTextComponent("§4 APIPlayer: " + apiOfflinePlayer.getName() + " Sanctions: " + sanctionInfos.size()).sendTo(this.getUUID());
        } else
            TextComponentBuilder.createTextComponent("§4Aucune sanction listée").sendTo(this.getUUID());

    }

    @Override
    public void printInfo(APIOfflinePlayer apiOfflinePlayer) {

        TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("§m                    \n");
        tcb.appendNewComponentBuilder("§7→ §rPseudo§7・" + apiOfflinePlayer.getName() + "§r\n");

        String connectedMsg = "§c✘", server = null;
        if (apiOfflinePlayer.isConnected()) {
            connectedMsg = "§a✓";
            server = CoreAPI.get().getPlayerManager().getPlayer(apiOfflinePlayer.getMemberId()).getServer().getServerName();
        }

        tcb.appendNewComponentBuilder("§7→ §rConnecté§7・" + connectedMsg + "§r\n");

        if (CoreAPI.get().getNickGestion().hasNick(apiOfflinePlayer)) {
            tcb.appendNewComponentBuilder("§7→ §rNick§7・§a" + CoreAPI.get().getNickGestion().getNickData(apiOfflinePlayer).getName() + "§r\n");
        }

        tcb.appendNewComponentBuilder("§7→ §rRank§7・" + apiOfflinePlayer.getRank().getRankName() + "§r\n");

        if (server != null)
            tcb.appendNewComponentBuilder("§7→ §rServeur§7・§a" + server + "§r\n");

        String ip = Color.RED + "Déconnecté";
        if (apiOfflinePlayer instanceof APIPlayer)
            ip = String.valueOf(CoreAPI.get().getRedisManager().getRedissonClient().getList("ip/" + ((APIPlayer) apiOfflinePlayer).getIpInfo().getIp()).size() - 1);

        tcb.appendNewComponentBuilder("§7→ §rComptes sur la même ip§7・§c" + ip + "§r\n");

        String mute = "§c✘";
        if (apiOfflinePlayer.isMute())
            mute = "§a✓";

        String ban = "§c✘";
        if (apiOfflinePlayer.isBan())
            ban = "§a✓";

        tcb.appendNewComponentBuilder("§7→ §rEtat§7・Banni: " + ban + " §7Mute: " + mute + "§r\n");
        tcb.appendNewComponentBuilder("§m                    \n");

        tcb.sendTo(this.getUUID());

        return;

    }

    @Override
    public String getName() {
        return CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_NAME_REDIS.getString(this));
    }

    @Override
    public boolean isModeratorMod() {
        String bool = CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_MOD_REDIS.getString(this));
        if (bool == null)
            return false;
        else
            return Boolean.parseBoolean(bool);
    }

    @Override
    public APIPlayer getAPIPlayer() {
        return CoreAPI.get().getPlayerManager().getPlayer(getMemberId());
    }

    /**
     * @return This function return the MemberId of the moderator
     */

    @Override
    public long getMemberId() {
        return memberID;
    }

    @Override
    public void disconnectModerator() {
        if (!CoreAPI.get().isBungee()) return;

        long memberID = this.memberID;

        ModeratorModel model = new SQLModels<>(ModeratorModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        model.set(ModeratorDataValue.MODERATOR_MOD_SQL.getString(null, null), Boolean.valueOf(this.isModeratorMod()).toString());
        model.set(ModeratorDataValue.MODERATOR_VANISH_SQL.getString(null, null), Boolean.valueOf(this.isVanish()).toString());
        model.set(ModeratorDataValue.MODERATOR_CIBLE_SQL.getString(null, null), this.getCible());

        ModeratorDataValue.clearRedisData(DataType.PLAYER, this);

        CoreAPI.get().getRedisManager().getRedisList(ModeratorDataValue.LIST_MODERATOR.getString(null)).remove(memberID);
    }

    @Override
    public boolean isConnected() {
        return getAPIPlayer() != null;
    }

    /**
     * @return This function return the current UUID of the moderator
     */

    @Override
    public UUID getUUID() {
        String uuid = CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_UUID_REDIS.getString(this));
        if (uuid == null) return null;
        return UUID.fromString(uuid);
    }

    public static class ModeratorModel extends SQLModel {

        public ModeratorModel() {
            super("moderator", ModeratorDataValue.MODERATOR_MEMBERID_SQL.getString(null));
        }

    }

}