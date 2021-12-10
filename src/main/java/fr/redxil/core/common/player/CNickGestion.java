/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player;

import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.nick.NickData;
import fr.redxil.api.common.player.nick.NickGestion;
import fr.redxil.api.common.player.rank.Rank;
import org.redisson.api.RedissonClient;

public class CNickGestion implements NickGestion {

    @Override
    public boolean setNick(APIOfflinePlayer apiPlayer, NickData nickData) {

        /// Empecher l'utilisation du pseudo d'un joueur
        if (isIllegalName(nickData.getName())) return false;

        if (API.getInstance().getPlayerManager().getOfflinePlayer(nickData.getName()) != null)
            return false;

        if (!apiPlayer.hasPermission(nickData.getRank().getRankPower()))
            return false;

        if (isNickName(nickData.getName())) {

            APIOfflinePlayer ofs = getAPIOfflinePlayer(nickData.getName());
            if (ofs.isConnected())
                return false;
            else removeNick(ofs);

        }

        removeNick(apiPlayer);

        RedissonClient redis = API.getInstance().getRedisManager().getRedissonClient();
        redis.getMapCache("nick/nickToPlayerList").put(nickData.getName(), apiPlayer.getMemberId());
        redis.getMapCache("nick/playerToNickList").put(apiPlayer.getMemberId(), nickData.getName());
        redis.getMapCache("nick/Rank").put(apiPlayer.getMemberId(), nickData.getRank().getRankPower());

        if (apiPlayer instanceof APIPlayer)
            nickUpdate((APIPlayer) apiPlayer);

        return true;

    }

    @Override
    public boolean removeNick(APIOfflinePlayer apiPlayer) {

        NickData nickData = getNickData(apiPlayer);
        if (nickData == null) return false;

        long playerID = apiPlayer.getMemberId();

        RedissonClient redis = API.getInstance().getRedisManager().getRedissonClient();
        redis.getMapCache("nick/nickToPlayerList").remove(nickData.getName());
        redis.getMapCache("nick/playerToNickList").remove(playerID);
        redis.getMapCache("nick/Rank").remove(playerID);

        if (apiPlayer instanceof APIPlayer)
            nickUpdate((APIPlayer) apiPlayer);

        return true;

    }

    @Override
    public boolean isNickName(String s) {
        return API.getInstance().getRedisManager().getRedissonClient().getMapCache("nick/nickToPlayerList").containsKey(s);
    }

    /// Part: nick -> APIPlayer
    @Override
    public APIOfflinePlayer getAPIOfflinePlayer(String s) {
        Long realID = getRealID(s);
        if (realID == null) return null;
        return API.getInstance().getPlayerManager().getOfflinePlayer(realID);
    }

    @Override
    public APIPlayer getAPIPlayer(String s) {
        APIOfflinePlayer apiOfflinePlayer = getAPIOfflinePlayer(s);
        if (apiOfflinePlayer != null)
            if (apiOfflinePlayer instanceof APIPlayer)
                return (APIPlayer) apiOfflinePlayer;
        return null;
    }

    @Override
    public Long getRealID(String s) {
        if (!isNickName(s)) return null;
        return (long) API.getInstance().getRedisManager().getRedissonClient().getMapCache("nick/nickToPlayerList").get(s);
    }

    /// Part: APIPlayer -> nick

    @Override
    public NickData getNickData(APIOfflinePlayer osp) {
        if (!hasNick(osp))
            return new NickData(osp.getName(), osp.getRank());
        return new NickData((String) API.getInstance().getRedisManager().getRedissonClient().getMapCache("nick/playerToNickList").get(osp.getMemberId()), Rank.getRank((long) API.getInstance().getRedisManager().getRedissonClient().getMapCache("nick/Rank").get(osp.getMemberId())));
    }

    @Override
    public boolean hasNick(APIOfflinePlayer apiPlayer) {
        return API.getInstance().getRedisManager().getRedissonClient().getMapCache("nick/playerToNickList").containsKey(apiPlayer.getMemberId());
    }

    public void nickUpdate(APIPlayer apiPlayer) {

        RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "nickChange", apiPlayer.getUUID().toString());

    }

    @Override
    public boolean isIllegalName(String s) {
        if (s.length() < 3 || s.length() > 16) return true;

        return !(s.matches("[0-9a-zA-Z_]+"));
    }

}
