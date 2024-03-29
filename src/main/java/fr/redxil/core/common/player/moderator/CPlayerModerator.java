/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.moderator;

import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.moderator.ModeratorDataRedis;
import fr.redxil.core.common.data.moderator.ModeratorDataSql;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.redis.RedisManager;
import fr.redxil.core.common.sql.SQLModel;
import fr.redxil.core.common.sql.SQLModels;
import fr.redxil.core.common.utils.DataReminder;

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

        if (CoreAPI.getInstance().isOnlineMod()) {
            Optional<ModeratorModel> model = new SQLModels<>(ModeratorModel.class).getOrInsert(null, new HashMap<>() {{
                this.put(ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns(), memberID.intValue());
                this.put(ModeratorDataSql.MODERATOR_MOD_SQL.getSQLColumns(), Boolean.valueOf(false).toString());
                this.put(ModeratorDataSql.MODERATOR_VANISH_SQL.getSQLColumns(), Boolean.valueOf(false).toString());
            }}, "WHERE " + ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID.intValue());

            if (model.isPresent()) {
                setModeratorMod(Boolean.parseBoolean(model.get().getString(ModeratorDataSql.MODERATOR_MOD_SQL.getSQLColumns())));
                setVanish(Boolean.parseBoolean(model.get().getString(ModeratorDataSql.MODERATOR_VANISH_SQL.getSQLColumns())));
                setCible(model.get().getString(ModeratorDataSql.MODERATOR_CIBLE_SQL.getSQLColumns()));
            }

        } else
            CoreAPI.getInstance().getModeratorManager().getMap().put(memberID, this);

        CoreAPI.getInstance().getModeratorManager().getStringToLongModerator().put(name, memberID);
        CoreAPI.getInstance().getModeratorManager().getUUIDToLongModerator().put(uuid.toString(), memberID);
    }

    public void initDataReminder() {
        initUUID();
        initName();
    }

    @Override
    public void disconnectModerator() {
        if (!CoreAPI.getInstance().getAPIEnabler().isVelocity() && CoreAPI.getInstance().isOnlineMod()) return;

        String name = getName();
        UUID uuid = getUUID();

        if (CoreAPI.getInstance().isOnlineMod()) {

            Optional<ModeratorModel> modelOpt = new SQLModels<>(ModeratorModel.class).getFirst("WHERE " + ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID);

            modelOpt.ifPresent(model -> model.set(new HashMap<>() {{
                put(ModeratorDataSql.MODERATOR_MOD_SQL.getSQLColumns(), Boolean.valueOf(isModeratorMod()).toString());
                put(ModeratorDataSql.MODERATOR_VANISH_SQL.getSQLColumns(), Boolean.valueOf(isVanish()).toString());
                put(ModeratorDataSql.MODERATOR_CIBLE_SQL.getSQLColumns(), getCible());
            }}));

            ModeratorDataRedis.clearRedisData(DataType.PLAYER, this.getMemberID());

        } else {
            CoreAPI.getInstance().getModeratorManager().getMap().remove(this.getMemberID());
        }

        CoreAPI.getInstance().getModeratorManager().getLoadedModerator().remove(memberID);
        CoreAPI.getInstance().getModeratorManager().getStringToLongModerator().remove(name);
        CoreAPI.getInstance().getModeratorManager().getUUIDToLongModerator().remove(uuid.toString());
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
        return getCible().isPresent();
    }


    @Override
    public Optional<String> getCible() {
        initCibleReminder();
        return Optional.ofNullable(this.cibleReminder.getData());
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

    public static class ModeratorModel extends SQLModel {

        public ModeratorModel() {
            super("moderator", ModeratorDataSql.MODERATOR_MEMBERID_SQL.getSQLColumns());
        }

    }

}