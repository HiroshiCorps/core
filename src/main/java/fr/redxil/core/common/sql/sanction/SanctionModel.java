/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.sanction;

import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.sql.SQLModel;

import java.sql.Timestamp;

public class SanctionModel extends SQLModel implements SanctionInfo {
    public SanctionModel() {
        super("sanction", "sanctionID");
    }

    public SanctionModel(Long targetID, Long authorID, SanctionType sanctionType, String reason, long banEnd) {
        this();
        this.set("targetID", targetID.intValue());
        this.set("authorID", authorID.intValue());
        this.set("sanctionType", sanctionType.getID());
        this.set("reason", reason);
        if (banEnd != -1L)
            this.set("endTS", new Timestamp(banEnd));
    }

    public SanctionModel(Long targetID, Long authorID, SanctionType sanctionType, String reason) {
        this();
        this.set("targetID", targetID.intValue());
        this.set("authorID", authorID.intValue());
        this.set("sanctionType", sanctionType.getID());
        this.set("reason", reason);
    }

    @Override
    public Integer getSanctionID() {
        return this.getInt("sanctionID");
    }

    @Override
    public long getTargetID() {
        return Integer.valueOf(this.getInt("targetID")).longValue();
    }

    @Override
    public long getAuthorID() {
        return Integer.valueOf(this.getInt("authorID")).longValue();
    }

    @Override
    public SanctionType getSanctionType() {
        return SanctionType.getSanctionType(this.getInt("sanctionType"));
    }

    @Override
    public String getReason() {
        return this.getString("reason");
    }

    @Override
    public Long getCanceller() {
        Object sancCancel = this.get("cancellerID");
        if (sancCancel == null) return null;
        if (!(sancCancel instanceof Integer)) return null;
        return ((Integer) sancCancel).longValue();
    }

    @Override
    public void setCanceller(long playerID) {
        if (!isCancelled()) this.set("cancellerID", Long.valueOf(playerID).intValue());
    }

    @Override
    public boolean isCancelled() {
        return getCanceller() != null;
    }

    @Override
    public long getSanctionDateTS() {
        return ((Timestamp) this.get("sanctionTS")).getTime();
    }

    @Override
    public long getSanctionEndTS() {
        if (hasSanctionEnd())
            return ((Timestamp) this.get("endTS")).getTime();
        return -1L;
    }

    @Override
    public boolean hasSanctionEnd() {
        return this.get("endTS") != null;
    }

    @Override
    public boolean isEffective() {

        if (isCancelled()) return false;

        if (!hasSanctionEnd()) return true;
        return getSanctionEndTS() > DateUtility.getCurrentTimeStamp();

    }

    @Override
    public TextComponentBuilder getSancMessage() {

        if (getSanctionID() == null) return null;

        TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("§4§lSWAMP MC");
        tcb.appendText("\n§r§cVous avez été " + getSanctionType().getName());
        tcb.appendText("\n\n§r§7Raison: §e" + getReason());
        tcb.appendText("\n§r§7Expiration: §c" + DateUtility.getMessage(getSanctionEndTS()));
        tcb.appendText("\n\n§r§7Sagit-il d'une erreur ? Faites une réclamation");
        tcb.appendNewComponentBuilder("\n§r§bredxil.net/reclam").setOnClickExecCommand("redxil.net/reclam").appendNewComponentBuilder("\n§7ID Sanction: " + getSanctionID());

        return tcb;

    }

}
