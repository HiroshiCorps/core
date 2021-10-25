/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.player;

import fr.redxil.api.common.player.data.Setting;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.sql.SQLModel;

public class SettingsModel extends SQLModel implements Setting {
    public SettingsModel() {
        super("members_settings", "id");
    }

    public SettingsModel(Long playerID, String settingsName, String settingsValue) {
        this();
        this.set(PlayerDataValue.PLAYER_MEMBERID_SQL.getString(), playerID.intValue());
        this.set("settings_name", settingsName);
        this.set("settings_value", settingsValue);
    }

    @Override
    public Integer getID() {
        return this.getInt("id");
    }

    @Override
    public String getName() {
        return this.getString("settings_name");
    }

    @Override
    public String getValue() {
        return this.getString("settings_value");
    }

    @Override
    public void setValue(String value) {
        this.set("settings_value", value);
    }

}
