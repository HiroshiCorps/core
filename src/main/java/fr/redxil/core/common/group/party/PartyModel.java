/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.group.party;

import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.sql.SQLModel;

public class PartyModel extends SQLModel {

    public PartyModel() {
        super("members_party", "party_id");
    }

    public PartyModel(String data, APIPlayer owner) {
        this();
        this.set("party_data", data);
        this.set("owner_id", owner.getMemberId());
        this.set("owner_name", owner.getName());
    }

    public String getOwnerName() {
        return this.getString("owner_name");
    }

    public String getData() {
        return this.getString("party_data");
    }

    public int getOwnerId() {
        return this.getInt("owner_id");
    }
}
