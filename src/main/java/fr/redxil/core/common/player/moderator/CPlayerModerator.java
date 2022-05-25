/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.moderator;

import fr.redxil.api.common.API;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.moderator.ModeratorDataRedis;
import fr.redxil.core.common.data.moderator.ModeratorDataSql;
import fr.redxil.core.common.data.utils.DataReminder;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.sql.SQLModel;
import fr.redxil.core.common.sql.SQLModels;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CPlayerModerator implements APIPlayerModerator {

    /*
     * fully done offline purpose
     */
    private final long memberID;
    private DataReminder<String> uuidReminder = null;
    private DataReminder<String> nameReminder = null;
    private DataReminder<String> modReminder = null;
    private DataReminder<String> vanishReminder = null;
    private DataReminder<String> cibleReminder = null;


    public CPlayerModerator(long memberID) {
        this.memberID = memberID;
    }

    public CPlayerModerator(Long memberID, UUID uuid, String name) {
        this.memberID = memberID;
        initDataReminder();

        uuidReminder.setData(uuid.toString());
        nameReminder.setData(name);

        if (API.getInstance().isOnlineMod()) {

            ModeratorModel model = new SQLModels<>(ModeratorModel.class).getOrInsert(new HashMap<>() {{
                this.put(ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns(), memberID.intValue());
                this.put(ModeratorDataSql.MODERATOR_MOD_SQL.getSQLColumns(), Boolean.valueOf(false).toString());
                this.put(ModeratorDataSql.MODERATOR_VANISH_SQL.getSQLColumns(), Boolean.valueOf(false).toString());
            }}, "WHERE " + ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID.intValue());

            setModeratorMod(Boolean.parseBoolean(model.getString(ModeratorDataSql.MODERATOR_MOD_SQL.getSQLColumns())));
            setVanish(Boolean.parseBoolean(model.getString(ModeratorDataSql.MODERATOR_VANISH_SQL.getSQLColumns())));
            setCible(model.getString(ModeratorDataSql.MODERATOR_CIBLE_SQL.getSQLColumns()));

        } else
            CoreAPI.getInstance().getModeratorManager().getMap().put(memberID, this);

        API.getInstance().getModeratorManager().getLoadedModerator().add(memberID);
    }

    public void initDataReminder() {
        initUUID();
        initName();
    }

    @Override
    public void disconnectModerator() {
        if (!API.getInstance().isVelocity() && API.getInstance().isOnlineMod()) return;

        if (API.getInstance().isOnlineMod()) {

            ModeratorModel model = new SQLModels<>(ModeratorModel.class).getFirst("WHERE " + ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID);

            model.set(new HashMap<>() {{
                put(ModeratorDataSql.MODERATOR_MOD_SQL.getSQLColumns(), Boolean.valueOf(isModeratorMod()).toString());
                put(ModeratorDataSql.MODERATOR_VANISH_SQL.getSQLColumns(), Boolean.valueOf(isVanish()).toString());
                put(ModeratorDataSql.MODERATOR_CIBLE_SQL.getSQLColumns(), getCible());
            }});

            ModeratorDataRedis.clearRedisData(DataType.PLAYER, this.getMemberID());

        } else {
            CoreAPI.getInstance().getModeratorManager().getMap().remove(this.getMemberID());
        }

        API.getInstance().getModeratorManager().getLoadedModerator().remove(memberID);
    }

    public void initUUID() {
        if (this.uuidReminder == null)
            this.uuidReminder = DataReminder.generateReminder(ModeratorDataRedis.MODERATOR_UUID_REDIS.getString(memberID), "none");
    }

    @Override
    public UUID getUUID() {
        initUUID();
        return UUID.fromString(this.uuidReminder.getData());
    }

    public void initName() {
        if (this.nameReminder == null)
            this.nameReminder = DataReminder.generateReminder(ModeratorDataRedis.MODERATOR_NAME_REDIS.getString(memberID), "none");
    }

    @Override
    public String getName() {
        initName();
        return this.nameReminder.getData();
    }

    public void initModReminder() {
        if (this.modReminder == null)
            this.modReminder = DataReminder.generateReminder(ModeratorDataRedis.MODERATOR_MOD_REDIS.getString(memberID), Boolean.FALSE.toString());
    }

    @Override
    public boolean isModeratorMod() {
        initModReminder();
        String bool = this.modReminder.getData();
        if (bool == null)
            return false;
        else
            return Boolean.parseBoolean(bool);
    }

    @Override
    public void setModeratorMod(boolean value) {
        initModReminder();
        this.modReminder.setData(Boolean.valueOf(value).toString());
    }

    public void initVanishReminder() {
        if (this.vanishReminder == null)
            this.vanishReminder = DataReminder.generateReminder(ModeratorDataRedis.MODERATOR_VANISH_REDIS.getString(memberID), Boolean.FALSE.toString());
    }

    @Override
    public boolean isVanish() {
        initVanishReminder();
        String bool = this.vanishReminder.getData();
        if (bool == null)
            return false;
        else
            return Boolean.parseBoolean(bool);
    }

    @Override
    public void setVanish(boolean b) {
        initVanishReminder();
        this.vanishReminder.setData(Boolean.valueOf(b).toString());
    }

    public void initCibleReminder() {
        if (this.cibleReminder == null)
            this.cibleReminder = DataReminder.generateReminder(ModeratorDataRedis.MODERATOR_CIBLE_REDIS.getString(memberID), null);
    }

    @Override
    public boolean hasCible() {
        return getCible() != null;
    }


    @Override
    public String getCible() {
        initCibleReminder();
        return this.cibleReminder.getData();
    }

    @Override
    public void setCible(String s) {
        initCibleReminder();
        this.cibleReminder.setData(s);
    }

    /**
     * @return This function return the MemberID of the moderator
     */

    @Override
    public long getMemberID() {
        return memberID;
    }

    @Override
    public void printSanction(APIOfflinePlayer apiOfflinePlayer, SanctionType sanctionType) {

        List<SanctionInfo> sanctionInfos = apiOfflinePlayer.getSanction(sanctionType);
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(getMemberID());

        if (!sanctionInfos.isEmpty()) {

            TextComponentBuilder.createTextComponent("§4 APIPlayer: " + apiOfflinePlayer.getName() + " Sanctions: " + sanctionInfos.size()).sendTo(apiPlayer);

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

                tcb.sendTo(apiPlayer);

            }

            TextComponentBuilder.createTextComponent("§4 APIPlayer: " + apiOfflinePlayer.getName() + " Sanctions: " + sanctionInfos.size()).sendTo(apiPlayer);
        } else
            TextComponentBuilder.createTextComponent("§4Aucune sanction listée").sendTo(apiPlayer);

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

            Server serverAPI = API.getInstance().getPlayerManager().getPlayer(apiOfflinePlayer.getMemberID()).getServer();
            Optional<String> serverName = serverAPI.getServerName();
            server = serverName.orElseGet(() -> Long.valueOf(serverAPI.getServerID()).toString());
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

        tcb.sendTo(API.getInstance().getPlayerManager().getPlayer(getMemberID()));

    }

    public static class ModeratorModel extends SQLModel {

        public ModeratorModel() {
            super("moderator", ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns());
        }

    }

}