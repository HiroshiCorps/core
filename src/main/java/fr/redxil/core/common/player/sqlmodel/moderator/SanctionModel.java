/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.sqlmodel.moderator;

import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.data.SanctionDataSql;
import fr.redxil.core.common.sql.SQLModel;

import java.sql.Timestamp;

public class SanctionModel extends SQLModel implements SanctionInfo {
    public SanctionModel() {
        super("sanction", SanctionDataSql.SANCTION_ID.getSQLColumns());
    }

    public SanctionModel(Long targetID, Long authorID, SanctionType sanctionType, String reason, long banEnd) {
        this();
        this.set(SanctionDataSql.SANCTION_TARGET.getSQLColumns(), targetID.intValue());
        this.set(SanctionDataSql.SANCTION_AUTHOR.getSQLColumns(), authorID.intValue());
        this.set(SanctionDataSql.SANCTION_TYPE.getSQLColumns(), sanctionType.getID());
        this.set(SanctionDataSql.SANCTION_REASON.getSQLColumns(), reason);
        if (banEnd != -1L)
            this.set(SanctionDataSql.SANCTION_END.getSQLColumns(), new Timestamp(banEnd));
    }

    public SanctionModel(Long targetID, Long authorID, SanctionType sanctionType, String reason) {
        this();
        this.set(SanctionDataSql.SANCTION_TARGET.getSQLColumns(), targetID.intValue());
        this.set(SanctionDataSql.SANCTION_AUTHOR.getSQLColumns(), authorID.intValue());
        this.set(SanctionDataSql.SANCTION_TYPE.getSQLColumns(), sanctionType.getID());
        this.set(SanctionDataSql.SANCTION_REASON.getSQLColumns(), reason);
    }

    @Override
    public Integer getSanctionID() {
        return this.getInt(SanctionDataSql.SANCTION_ID.getSQLColumns());
    }

    @Override
    public long getTargetID() {
        return Integer.valueOf(this.getInt(SanctionDataSql.SANCTION_TARGET.getSQLColumns())).longValue();
    }

    @Override
    public long getAuthorID() {
        return Integer.valueOf(this.getInt(SanctionDataSql.SANCTION_AUTHOR.getSQLColumns())).longValue();
    }

    @Override
    public SanctionType getSanctionType() {
        return SanctionType.getSanctionType(this.getInt(SanctionDataSql.SANCTION_TYPE.getSQLColumns()));
    }

    @Override
    public String getReason() {
        return this.getString(SanctionDataSql.SANCTION_REASON.getSQLColumns());
    }

    @Override
    public Long getCanceller() {
        Object sancCancel = this.get(SanctionDataSql.SANCTION_CANCELLER.getSQLColumns());
        if (sancCancel == null) return null;
        if (!(sancCancel instanceof Integer)) return null;
        return ((Integer) sancCancel).longValue();
    }

    @Override
    public void setCanceller(long playerID) {
        if (!isCancelled())
            this.set(SanctionDataSql.SANCTION_CANCELLER.getSQLColumns(), Long.valueOf(playerID).intValue());
    }

    @Override
    public boolean isCancelled() {
        return getCanceller() != null;
    }

    @Override
    public long getSanctionDateTS() {
        return ((Timestamp) this.get(SanctionDataSql.SANCTION_DATE.getSQLColumns())).getTime();
    }

    @Override
    public long getSanctionEndTS() {
        if (hasSanctionEnd())
            return ((Timestamp) this.get(SanctionDataSql.SANCTION_END.getSQLColumns())).getTime();
        return -1L;
    }

    @Override
    public boolean hasSanctionEnd() {
        return this.get(SanctionDataSql.SANCTION_END.getSQLColumns()) != null;
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

        TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("§4§lSERVER MC");
        tcb.appendText("\n§r§cVous avez été " + getSanctionType().getName());
        tcb.appendText("\n\n§r§7Raison: §e" + getReason());
        tcb.appendText("\n§r§7Expiration: §c" + DateUtility.getMessage(getSanctionEndTS()));
        tcb.appendText("\n\n§r§7Sagit-il d'une erreur ? Faites une réclamation");
        tcb.appendNewComponentBuilder("\n§r§bredxil.net/reclam").setOnClickExecCommand("redxil.net/reclam").appendNewComponentBuilder("\n§7ID Sanction: " + getSanctionID());

        return tcb;

    }

}
