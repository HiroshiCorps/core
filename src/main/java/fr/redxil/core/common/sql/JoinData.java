/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql;

import fr.redxil.api.common.utils.Pair;
import fr.redxil.core.common.data.utils.SQLColumns;

public class JoinData {

    final JoinType joinType;
    final Pair<SQLColumns, SQLColumns> columnsPair;
    boolean nullBoolean = false;

    public JoinData(JoinType joinType, Pair<SQLColumns, SQLColumns> pair) {
        this.joinType = joinType;
        this.columnsPair = pair;
    }

    public boolean setNullBoolean(boolean nullJoin) {
        if (!this.joinType.acceptNULL)
            return false;
        this.nullBoolean = nullJoin;
        return true;
    }

    public boolean isNullBoolean() {
        return nullBoolean;
    }

    public Pair<SQLColumns, SQLColumns> getColumnsPair() {
        return columnsPair;
    }

    public JoinType getJoinType() {
        return this.joinType;
    }

    public String toSQL() {
        StringBuilder sqlBuilder = new StringBuilder(joinType.sql);
        sqlBuilder.append("JOIN ")
                .append(columnsPair.getTwo().getColumns())
                .append(" ")
                .append(columnsPair.getTwo().getColumns())
                .append(" ON ")
                .append(columnsPair.getOne().toSQL())
                .append(" = ")
                .append(columnsPair.getTwo().toSQL());
        if (nullBoolean)
            sqlBuilder.append(" WHERE ").append(columnsPair.getTwo()).append(" IS NULL");
        sqlBuilder.append(" ");
        return sqlBuilder.toString();
    }

    public enum JoinType {
        LEFT("LEFT", true),
        RIGHT("RIGHT", true),
        INNER("INNER", true),
        FULL_OUTER("FULL OUTER", true);

        final String sql;
        final boolean acceptNULL;

        JoinType(String sql, boolean acceptNULL) {
            this.sql = sql;
            this.acceptNULL = acceptNULL;
        }
    }

}
