/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data;

import fr.redxil.core.common.sql.utils.SQLColumns;

public enum SettingsDataSql {

    SETTINGS_ID("members_settings", "id"),
    SETTINGS_MEMBERID("members_settings", "member_id"),
    SETTINGS_NAME("members_settings", "settings_name"),
    SETTINGS_VALUE("members_settings", "settings_value");

    final SQLColumns sqlColumns;

    SettingsDataSql(String table, String columns) {
        this.sqlColumns = new SQLColumns(table, columns);
    }

    public SQLColumns getSQLColumns() {
        return this.sqlColumns;
    }
}
