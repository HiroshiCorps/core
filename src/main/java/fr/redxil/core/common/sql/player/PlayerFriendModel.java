/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.player;

import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.sql.SQLModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerFriendModel extends SQLModel {

    public PlayerFriendModel() {
        super("friend", PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null));
    }

    public List<String> getFriendList() {
        return listFromString(getString(PlayerDataValue.PLAYER_FRIENDLIST_SQL.getString(null)));
    }

    public void setFriendList(List<String> friendList) {
        set(PlayerDataValue.PLAYER_FRIENDLIST_SQL.getString(null), listToString(friendList));
    }

    public List<String> getBlackList() {
        return listFromString(getString(PlayerDataValue.PLAYER_BLACKLIST_SQL.getString(null)));
    }

    public void setBlackList(List<String> friendList) {
        set(PlayerDataValue.PLAYER_BLACKLIST_SQL.getString(null), listToString(friendList));
    }

    public List<String> getSendedList() {
        return listFromString(getString(PlayerDataValue.PLAYER_FRIENDSENDEDLIST_SQL.getString(null)));
    }

    public void setSendedList(List<String> friendList) {
        set(PlayerDataValue.PLAYER_FRIENDSENDEDLIST_SQL.getString(null), listToString(friendList));
    }

    public List<String> getReceivedList() {
        return listFromString(getString(PlayerDataValue.PLAYER_FRIENDRECEIVEDLIST_SQL.getString(null)));
    }

    public void setReceivedList(List<String> friendList) {
        set(PlayerDataValue.PLAYER_FRIENDRECEIVEDLIST_SQL.getString(null), listToString(friendList));
    }

    public String listToString(List<String> list) {

        if (list == null) return null;

        StringBuilder sb = null;
        for (String name : list) {
            if (sb != null) sb.append(";");
            else sb = new StringBuilder();
            sb.append(name);
        }

        if (sb != null)
            return sb.toString();
        return null;

    }

    public List<String> listFromString(String sList) {

        if (sList == null) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(sList.split(";")));

    }

}
