/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.moderator;

import fr.redxil.api.common.moderators.Actions;
import fr.redxil.core.common.sql.SQLModel;

import java.sql.Timestamp;

public class ModLogsModels extends SQLModel {
    public ModLogsModels() {
        super("mod_logs", "id");
    }

    public ModLogsModels(String author, String data, String reason, Actions action) {
        this();
        this.set("author", author);
        this.set("data", data);
        this.set("reason", reason);
        this.set("action", action.getName());
    }

    public String getAuthor() {
        return this.getString("author");
    }

    public String getData() {
        return this.getString("data");
    }

    public String getReason() {
        return this.getString("reason");
    }

    public Actions getAction() {
        return Actions.getAction(this.getString("action"));
    }

    public long getTime() {
        return ((Timestamp) this.get("time")).getTime();
    }
}
