/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data;

public enum IDDataValue {

    PARTY("id/party"),
    GAME("id/game"),
    PLAYERID("id/player"),
    SERVERID("id/server"),
    TEAM("id/team"),
    HOST("id/host");

    public final String location;

    IDDataValue(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

}
