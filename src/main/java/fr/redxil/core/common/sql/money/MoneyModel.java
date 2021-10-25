/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.money;

import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.sql.SQLModel;

public class MoneyModel extends SQLModel {
    public MoneyModel() {
        super("members_money", PlayerDataValue.PLAYER_MEMBERID_SQL.getString());
    }

    public MoneyModel(int id, int coins, int solde, String name) {
        this();
        this.set(PlayerDataValue.PLAYER_MEMBERID_SQL.getString(), id);
        this.set(PlayerDataValue.PLAYER_NAME_SQL.getString(), name);
        this.set(PlayerDataValue.PLAYER_SOLDE_SQL.getString(), solde);
        this.set(PlayerDataValue.PLAYER_COINS_SQL.getString(), coins);
    }

    public int getCoins() {
        return this.getInt(PlayerDataValue.PLAYER_COINS_SQL.getString());
    }

    public int getSolde() {
        return this.getInt(PlayerDataValue.PLAYER_SOLDE_SQL.getString());
    }
}
