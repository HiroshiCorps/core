/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql;


import fr.redxil.core.common.sql.result.ResultSetElement;
import fr.redxil.core.common.sql.result.ResultSetMetaData;
import fr.redxil.core.common.sql.result.ResultSetRow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class SQLRowSet {

    private final HashMap<Integer, ResultSetRow> rows = new HashMap<>();
    private int index = -1;
    private int size;
    private ResultSetMetaData metadata;

    public SQLRowSet(ResultSet req) {
        try {
            java.sql.ResultSetMetaData meta = req.getMetaData();
            int columnCount = meta.getColumnCount();
            String tableName = meta.getTableName(1);
            int indexRows = 0;
            HashMap<Integer, String> columnsName = new HashMap<>();

            while (req.next()) {
                HashMap<String, ResultSetElement> columns = new HashMap<>();
                for (int column = 1; column <= columnCount; ++column) {
                    if (indexRows == 0) {
                        columnsName.put(column, meta.getColumnName(column));
                    }
                    columns.put(meta.getColumnName(column),
                            new ResultSetElement(req.getObject(column), meta.isSigned(column)));
                }
                ResultSetRow row = new ResultSetRow(columns);
                this.rows.put(indexRows++, row);
            }
            if (indexRows == 0) {
                this.index = -42;
            }
            this.size = indexRows;
            this.metadata = new ResultSetMetaData(tableName, columnCount, columnsName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean first() {
        if (this.index == -42) {
            return false;
        }
        this.index = 0;
        return true;
    }


    public boolean last() {
        if (this.index == -42) {
            return false;
        }
        this.index = this.size - 1;
        return true;
    }


    public int getRow() {
        return this.index + 1;
    }


    public boolean beforeFirst() {
        if (this.index == -42) {
            return false;
        }
        this.index = -1;
        return true;
    }


    public boolean next() {
        this.index++;
        return this.rows.containsKey(this.index);
    }


    public HashMap<String, ResultSetElement> getColumns() {
        return this.rows.containsKey(this.index) ? this.rows.get(this.index).getColumns() : null;
    }


    public HashMap<String, Object> getColumnsObjects() {
        HashMap<String, Object> objects = new HashMap<>();
        for (Map.Entry<String, ResultSetElement> entry : this.rows.get(this.index).getColumns().entrySet()) {
            objects.put(entry.getKey(), entry.getValue().getValue());
        }
        return objects;
    }


    public Object getObject(String columnName) {
        if (this.isInvalidColumn(columnName, false)) {
            return null;
        }
        return this.rows.get(this.index).getColumns().get(columnName);
    }


    public String getString(String columnName) {
        if (this.isInvalidColumn(columnName, true)) {
            return null;
        }
        return this.rows.get(this.index).getColumns().get(columnName).getValue().toString();
    }


    public Timestamp getTimestamp(String columnName) {
        if (this.isInvalidColumn(columnName, true)) {
            return null;
        }
        try {
            return (Timestamp) this.rows.get(this.index).getColumns().get(columnName).getValue();
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            return null;
        }
    }


    public int getInt(String columnName) {
        if (this.isInvalidColumn(columnName, true)) {
            return -1;
        }
        try {
            return Integer.parseInt(this.rows.get(this.index).getColumns().get(columnName).getValue().toString());
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            return -1;
        }
    }


    public long getLong(String columnName) {
        if (this.isInvalidColumn(columnName, true)) {
            return -1L;
        }
        try {
            return Long.parseLong(this.rows.get(this.index).getColumns().get(columnName).getValue().toString());
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            return -1L;
        }
    }


    public double getDouble(String columnName) {
        if (this.isInvalidColumn(columnName, true)) {
            return -1;
        }
        try {
            return Double.parseDouble(this.rows.get(this.index).getColumns().get(columnName).getValue().toString());
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            return -1L;
        }
    }


    public byte getByte(String columnName) {
        if (this.isInvalidColumn(columnName, true)) {
            return (byte) -1;
        }
        try {
            return Byte.parseByte(this.rows.get(this.index).getColumns().get(columnName).getValue().toString());
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            return (byte) -1;
        }
    }


    public byte[] getBytes(String columnName) {
        if (this.isInvalidColumn(columnName, true)) {
            return new byte[0];
        }
        return (byte[]) this.rows.get(this.index).getColumns().get(columnName).getValue();
    }


    public boolean isSigned(String columnName) {
        if (this.isInvalidColumn(columnName, false)) {
            return false;
        }
        return this.rows.get(this.index).getColumns().get(columnName).isSigned();
    }


    public boolean isInvalidColumn(String columnName, boolean checkNotNull) {
        if (!this.rows.containsKey(this.index)) {
            return true;
        }
        if (!this.rows.get(this.index).getColumns().containsKey(columnName)) {
            return true;
        }
        return checkNotNull && this.rows.get(this.index).getColumns().get(columnName).getValue() == null;
    }


    public ResultSetMetaData getMetaData() {
        return this.metadata;
    }

}
