/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.sqlmodel.player;

import fr.redxil.api.common.player.data.Setting;
import fr.redxil.core.common.data.SettingsDataSql;
import fr.redxil.core.common.sql.SQLModel;

public class SettingsModel extends SQLModel implements Setting {
    public SettingsModel() {
        super("members_settings", SettingsDataSql.SETTINGS_ID.getSQLColumns());
    }

    public SettingsModel(Long playerID, String settingsName, String settingsValue) {
        this();
        this.set(SettingsDataSql.SETTINGS_MEMBERID.getSQLColumns(), playerID.intValue());
        this.set(SettingsDataSql.SETTINGS_NAME.getSQLColumns(), settingsName);
        this.set(SettingsDataSql.SETTINGS_VALUE.getSQLColumns(), settingsValue);
    }

    @Override
    public Integer getID() {
        return this.getInt(SettingsDataSql.SETTINGS_ID.getSQLColumns());
    }

    @Override
    public String getName() {
        return this.getString(SettingsDataSql.SETTINGS_NAME.getSQLColumns());
    }

    @Override
    public String getValue() {
        return this.getString(SettingsDataSql.SETTINGS_VALUE.getSQLColumns());
    }

    @Override
    public void setValue(String value) {
        this.set(SettingsDataSql.SETTINGS_VALUE.getSQLColumns(), value);
    }

}
