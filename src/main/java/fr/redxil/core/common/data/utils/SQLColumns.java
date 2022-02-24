/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.utils;

public class SQLColumns {
    final String table, columns;

    public SQLColumns(String table, String columns) {
        this.table = table;
        this.columns = columns;
    }

    public String getColumns() {
        return columns;
    }

    public String getTable() {
        return table;
    }

    public String toSQL() {
        return "`" + getTable() + "`.`" + getColumns() + "`";
    }
}
