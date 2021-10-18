/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.money;

import fr.redxil.core.common.sql.SQLModel;

public class MoneyModel extends SQLModel {
    public MoneyModel() {
        super("members_money", "member_id");
    }

    public MoneyModel(int id, int coins, int solde, String name) {
        this();
        this.set("member_id", id);
        this.set("member_name", name);
        this.set("member_solde", solde);
        this.set("member_coins", coins);
    }

    public int getCoins() {
        return this.getInt("member_coins");
    }

    public int getSolde() {
        return this.getInt("member_solde");
    }
}
