/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

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
import fr.redxil.core.common.data.moderator.ModeratorDataRedis;
import fr.redxil.core.common.data.moderator.ModeratorDataSql;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.sql.SQLModel;
import fr.redxil.core.common.sql.SQLModels;
import org.redisson.api.RList;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public record CPlayerModerator(long memberID) implements APIPlayerModerator {

    static APIPlayerModerator initModerator(APIPlayer apiPlayer) {

        Long memberID = apiPlayer.getMemberID();

        if (!API.getInstance().getModeratorManager().isModerator(memberID)) return null;

        ModeratorModel model = new SQLModels<>(ModeratorModel.class).getOrInsert(new HashMap<>() {{
            this.put(ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns(), memberID.intValue());
            this.put(ModeratorDataSql.MODERATOR_MOD_SQL.getSQLColumns(), Boolean.valueOf(false).toString());
            this.put(ModeratorDataSql.MODERATOR_VANISH_SQL.getSQLColumns(), Boolean.valueOf(false).toString());
        }}, "WHERE " + ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID.intValue());

        RedisManager rm = API.getInstance().getRedisManager();

        rm.setRedisString(ModeratorDataRedis.MODERATOR_NAME_REDIS.getString(memberID), apiPlayer.getName());
        rm.setRedisString(ModeratorDataRedis.MODERATOR_MOD_REDIS.getString(memberID), model.getString(ModeratorDataSql.MODERATOR_MOD_SQL.getSQLColumns()));
        rm.setRedisString(ModeratorDataRedis.MODERATOR_VANISH_REDIS.getString(memberID), model.getString(ModeratorDataSql.MODERATOR_VANISH_SQL.getSQLColumns()));
        rm.setRedisString(ModeratorDataRedis.MODERATOR_CIBLE_REDIS.getString(memberID), model.getString(ModeratorDataSql.MODERATOR_CIBLE_SQL.getSQLColumns()));
        rm.setRedisString(ModeratorDataRedis.MODERATOR_UUID_REDIS.getString(memberID), apiPlayer.getUUID().toString());

        RList<Long> idList = rm.getRedisList(ModeratorDataRedis.LIST_MODERATOR.getString());
        if (!idList.contains(memberID))
            idList.add(memberID);

        return new CPlayerModerator(memberID);

    }

    @Override
    public void disconnectModerator() {
        if (!API.getInstance().isVelocity()) return;

        long memberID = this.memberID;

        ModeratorModel model = new SQLModels<>(ModeratorModel.class).getFirst("WHERE " + ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns() + " = ?", memberID);

        model.set(new HashMap<>() {{
            put(ModeratorDataSql.MODERATOR_MOD_SQL.getSQLColumns(), Boolean.valueOf(isModeratorMod()).toString());
            put(ModeratorDataSql.MODERATOR_VANISH_SQL.getSQLColumns(), Boolean.valueOf(isVanish()).toString());
            put(ModeratorDataSql.MODERATOR_CIBLE_SQL.getSQLColumns(), getCible());
        }});

        ModeratorDataRedis.clearRedisData(DataType.PLAYER, this.getMemberID());

        API.getInstance().getRedisManager().getRedisList(ModeratorDataRedis.LIST_MODERATOR.getString()).remove(memberID);
    }


    @Override
    public boolean isConnected() {
        return getAPIPlayer() != null;
    }

    @Override
    public boolean isModeratorMod() {
        String bool = API.getInstance().getRedisManager().getRedisString(ModeratorDataRedis.MODERATOR_MOD_REDIS.getString(this));
        if (bool == null)
            return false;
        else
            return Boolean.parseBoolean(bool);
    }

    @Override
    public boolean isVanish() {
        String bool = API.getInstance().getRedisManager().getRedisString(ModeratorDataRedis.MODERATOR_VANISH_REDIS.getString(this));
        if (bool == null)
            return false;
        else
            return Boolean.parseBoolean(bool);
    }

    @Override
    public boolean hasCible() {
        return getCible() != null;
    }


    @Override
    public String getCible() {
        return API.getInstance().getRedisManager().getRedisString(ModeratorDataRedis.MODERATOR_CIBLE_REDIS.getString(this));
    }

    @Override
    public void setCible(String s) {
        API.getInstance().getRedisManager().setRedisString(ModeratorDataRedis.MODERATOR_CIBLE_REDIS.getString(this), s);
    }

    @Override
    public APIPlayer getAPIPlayer() {
        return API.getInstance().getPlayerManager().getPlayer(getMemberID());
    }

    /**
     * @return This function return the MemberID of the moderator
     */

    @Override
    public long getMemberID() {
        return memberID;
    }

    @Override
    public String getName() {
        return API.getInstance().getRedisManager().getRedisString(ModeratorDataRedis.MODERATOR_NAME_REDIS.getString(this));
    }

    /**
     * @return This function return the current UUID of the moderator
     */

    @Override
    public UUID getUUID() {
        String uuid = API.getInstance().getRedisManager().getRedisString(ModeratorDataRedis.MODERATOR_UUID_REDIS.getString(this));
        if (uuid == null) return null;
        return UUID.fromString(uuid);
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
                tcb.appendNewComponentBuilder("\n§r     §7Par: §d" + sanction.getAuthorID());
                tcb.appendNewComponentBuilder("\n§r     §7Le: §d" + DateUtility.getMessage(sanction.getSanctionDateTS()));
                tcb.appendNewComponentBuilder("\n§r     §7Jusqu'au: §d" + DateUtility.getMessage(sanction.getSanctionEndTS()));
                tcb.appendNewComponentBuilder("\n§r     §7Pour: §d" + sanction.getReason());

                String cancelledString = "§aPas cancel";
                Long longID = sanction.getCanceller();
                if (longID != null)
                    cancelledString = longID.toString();

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

        if (apiOfflinePlayer instanceof APIPlayer && ((APIPlayer) apiOfflinePlayer).isNick()) {
            tcb.appendNewComponentBuilder("§7→ §rPseudo§7・" + ((APIPlayer) apiOfflinePlayer).getRealName() + "§r\n");
            tcb.appendNewComponentBuilder("§7→ §rNick§7・§a" + apiOfflinePlayer.getName() + "§r\n");
        } else {
            tcb.appendNewComponentBuilder("§7→ §rPseudo§7・" + apiOfflinePlayer.getName() + "§r\n");
        }

        String connectedMsg = "§c✘", server = null;
        if (apiOfflinePlayer.isConnected()) {
            connectedMsg = "§a✓";
            server = API.getInstance().getPlayerManager().getPlayer(apiOfflinePlayer.getMemberID()).getServer().getServerName();
        }

        tcb.appendNewComponentBuilder("§7→ §rConnecté§7・" + connectedMsg + "§r\n");

        tcb.appendNewComponentBuilder("§7→ §rRank§7・" + apiOfflinePlayer.getRank().getRankName() + "§r\n");

        if (server != null)
            tcb.appendNewComponentBuilder("§7→ §rServeur§7・§a" + server + "§r\n");

        String ip = "Déconnecté";
        if (apiOfflinePlayer instanceof APIPlayer)
            ip = String.valueOf(API.getInstance().getRedisManager().getRedissonClient().getList("ip/" + apiOfflinePlayer.getIP().getIp()).size() - 1);

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

    }

    public static class ModeratorModel extends SQLModel {

        public ModeratorModel() {
            super("moderator", ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns());
        }

    }

}