/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.result;

import java.util.HashMap;

public class ResultSetMetaData {

    private final String tableName;

    private final int columnCount;

    private final HashMap<Integer, String> columnNames;

    public ResultSetMetaData(String tableName, int columnCount, HashMap<Integer, String> columnsNames) {
        this.tableName = tableName;
        this.columnCount = columnCount;
        this.columnNames = columnsNames;
    }

    public String getColumnName(int columnindex) {
        return this.columnNames.get(columnindex);
    }

    public String getTableName() {
        return tableName;
    }

    public int getColumnCount() {
        return columnCount;
    }

}
