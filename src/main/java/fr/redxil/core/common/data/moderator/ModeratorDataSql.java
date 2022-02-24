/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.moderator;

import fr.redxil.core.common.data.utils.SQLColumns;

public enum ModeratorDataSql {

    MODERATOR_MEMBERID_SQL("moderator", "member_id"),
    MODERATOR_MOD_SQL("moderator", "moderator_mod"),
    MODERATOR_VANISH_SQL("moderator", "moderator_vanish"),
    MODERATOR_CIBLE_SQL("moderator", "moderator_cible");

    final SQLColumns sqlColumns;

    ModeratorDataSql(String table, String columns) {
        this.sqlColumns = new SQLColumns(table, columns);
    }

    public SQLColumns getSQLColumns() {
        return this.sqlColumns;
    }

}
