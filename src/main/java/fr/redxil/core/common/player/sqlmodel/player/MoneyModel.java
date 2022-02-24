/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.sqlmodel.player;

import fr.redxil.core.common.data.money.MoneyDataSql;
import fr.redxil.core.common.sql.SQLModel;

public class MoneyModel extends SQLModel {
    public MoneyModel() {
        super("money", MoneyDataSql.PLAYER_MEMBERID_SQL.getSQLColumns());
    }

    public int getCoins() {
        return this.getInt(MoneyDataSql.PLAYER_COINS_SQL.getSQLColumns());
    }

    public int getSolde() {
        return this.getInt(MoneyDataSql.PLAYER_SOLDE_SQL.getSQLColumns());
    }

}
