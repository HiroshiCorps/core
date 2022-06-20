/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
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
import java.util.Optional;

public class SanctionModel extends SQLModel implements SanctionInfo {
    public SanctionModel() {
        super("sanction", SanctionDataSql.SANCTION_ID.getSQLColumns());
    }

    public SanctionModel(Long targetID, Long authorID, SanctionType sanctionType, String reason, Timestamp end) {
        this();
        this.set(SanctionDataSql.SANCTION_TARGET.getSQLColumns(), targetID.intValue());
        this.set(SanctionDataSql.SANCTION_AUTHOR.getSQLColumns(), authorID.intValue());
        this.set(SanctionDataSql.SANCTION_TYPE.getSQLColumns(), sanctionType.getID());
        this.set(SanctionDataSql.SANCTION_REASON.getSQLColumns(), reason);
        this.set(SanctionDataSql.SANCTION_END.getSQLColumns(), end);
    }

    public SanctionModel(Long targetID, Long authorID, SanctionType sanctionType, String reason) {
        this();
        this.set(SanctionDataSql.SANCTION_TARGET.getSQLColumns(), targetID.intValue());
        this.set(SanctionDataSql.SANCTION_AUTHOR.getSQLColumns(), authorID.intValue());
        this.set(SanctionDataSql.SANCTION_TYPE.getSQLColumns(), sanctionType.getID());
        this.set(SanctionDataSql.SANCTION_REASON.getSQLColumns(), reason);
    }

    @Override
    public Optional<Long> getSanctionID() {
        Integer sanctionID = this.getInt(SanctionDataSql.SANCTION_ID.getSQLColumns());
        if (sanctionID != null)
            return Optional.of(sanctionID.longValue());
        return Optional.empty();
    }

    @Override
    public Long getTargetID() {
        return this.getInt(SanctionDataSql.SANCTION_TARGET.getSQLColumns()).longValue();
    }

    @Override
    public Long getAuthorID() {
        return this.getInt(SanctionDataSql.SANCTION_AUTHOR.getSQLColumns()).longValue();
    }

    @Override
    public SanctionType getSanctionType() {
        return SanctionType.getSanctionType(this.getInt(SanctionDataSql.SANCTION_TYPE.getSQLColumns())).orElse(SanctionType.KICK);
    }

    @Override
    public String getReason() {
        return this.getString(SanctionDataSql.SANCTION_REASON.getSQLColumns());
    }

    @Override
    public Optional<Long> getCanceller() {
        Integer sancCancel = this.getInt(SanctionDataSql.SANCTION_CANCELLER.getSQLColumns());
        if (sancCancel == null)
            return Optional.empty();
        return Optional.of(sancCancel.longValue());
    }

    @Override
    public void setCanceller(long playerID) {
        if (!isCancelled())
            this.set(SanctionDataSql.SANCTION_CANCELLER.getSQLColumns(), Long.valueOf(playerID).intValue());
    }

    @Override
    public boolean isCancelled() {
        return getCanceller().isPresent();
    }

    @Override
    public Timestamp getSanctionDateTS() {
        return (Timestamp) this.get(SanctionDataSql.SANCTION_DATE.getSQLColumns());
    }

    @Override
    public Optional<Timestamp> getSanctionEndTS() {
        return Optional.ofNullable((Timestamp) this.get(SanctionDataSql.SANCTION_END.getSQLColumns()));
    }

    @Override
    public boolean hasSanctionEnd() {
        return getSanctionEndTS().isPresent();
    }

    @Override
    public boolean isEffective() {

        if (isCancelled()) return false;

        Optional<Timestamp> timestamp = getSanctionEndTS();
        if (timestamp.isEmpty())
            return false;
        return timestamp.get().after(DateUtility.getCurrentTimeStamp());

    }

    @Override
    public TextComponentBuilder getSancMessage() {

        TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("§4§lSERVER MC");
        tcb.appendText("\n§r§cVous avez été " + getSanctionType().getName());
        tcb.appendText("\n\n§r§7Raison: §e" + getReason());
        tcb.appendText("\n§r§7Expiration: §c" + DateUtility.getMessage(getSanctionEndTS().orElse(null)));
        tcb.appendText("\n\n§r§7Sagit-il d'une erreur ? Faites une réclamation");
        TextComponentBuilder tcb2 = tcb.appendNewComponentBuilder("\n§r§bredxil.net/reclam").setOnClickExecCommand("redxil.net/reclam");
        getSanctionID().ifPresent(aLong -> tcb2.appendNewComponentBuilder("\n§7ID Sanction: " + aLong));

        return tcb;

    }

}
