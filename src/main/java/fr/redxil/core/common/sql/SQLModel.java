/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql;

import fr.redxil.api.common.API;
import fr.redxil.api.common.utils.Pair;
import fr.redxil.core.common.data.utils.SQLColumns;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class SQLModel {

    private final String table;

    private final SQLColumns primaryKey;

    private final HashMap<String, Object> columns = new HashMap<>();

    private final JoinData joinData;

    private boolean populate = false;

    public SQLModel(String table, SQLColumns primaryKey) {
        this.table = table;
        this.primaryKey = primaryKey;
        this.joinData = null;
    }

    public SQLModel(String table, SQLColumns primaryKey, JoinData joinData) {
        this.table = table;
        this.primaryKey = primaryKey;
        this.joinData = joinData;
    }

    public JoinData getJoinData() {
        return joinData;
    }

    public String getTable() {
        return this.table;
    }

    public SQLColumns getPrimaryKey() {
        return this.primaryKey;
    }

    public HashMap<SQLColumns, Object> getDataMap() {
        return new HashMap<>() {{
            for (Map.Entry<String, Object> value : columns.entrySet())
                put(SQLColumns.fromSQL(value.getKey()), value.getValue());
        }};
    }

    public HashMap<SQLColumns, Object> getDataMap(String table) {
        return new HashMap<>() {{
            for (Map.Entry<String, Object> value : columns.entrySet()) {
                SQLColumns converted = SQLColumns.fromSQL(value.getKey());
                if (converted.getTable().equalsIgnoreCase(table)) {
                    put(converted, value.getValue());
                }
            }
        }};
    }

    public boolean containsDataForTable(String table) {
        return !getDataMap(table).isEmpty();
    }

    public void populate(ResultSet resultSet) {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); ++i) {
                this.columns.put(new SQLColumns(meta.getTableName(i), meta.getColumnName(i)).toSQL(), resultSet.getObject(i));
            }
            this.populate = true;
            this.onPopulated();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    protected void onPopulated() {
    }

    public Object get(SQLColumns columnName) {
        return columns.get(columnName.toSQL());
    }

    public String getString(SQLColumns columnName) {
        return (String) this.get(columnName);
    }

    public int getInt(SQLColumns columnName) {
        return Integer.parseInt(this.get(columnName).toString());
    }

    public double getDouble(SQLColumns columnName) {
        return Double.parseDouble(this.get(columnName).toString());
    }

    public long getLong(SQLColumns columnName) {
        return Long.parseLong(this.get(columnName).toString());
    }


    public void set(SQLColumns columnName, Object value) {
        this.set(new HashMap<>() {{
            put(columnName, value);
        }});
    }

    public void setSync(SQLColumns columnName, Object value) {
        this.setSync(new HashMap<>() {{
            put(columnName, value);
        }});
    }


    public void set(HashMap<SQLColumns, Object> map) {
        Pair<String, Collection<Object>> pair = this.setSQL(map);
        if(pair != null)
        API.getInstance().getSQLConnection().asyncExecute(pair.getOne(), pair.getTwo().toArray());
    }

    public void setSync(HashMap<SQLColumns, Object> map) {
        Pair<String, Collection<Object>> pair = this.setSQL(map);
        if(pair != null)
        API.getInstance().getSQLConnection().execute(pair.getOne(), pair.getTwo().toArray());
    }


    private Pair<String, Collection<Object>> setSQL(Map<SQLColumns, Object> values) {
        for (Map.Entry<SQLColumns, Object> value : values.entrySet()) {
            columns.put(value.getKey().toSQL(), value.getValue());
        }
        if (!this.populate) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder("UPDATE ").append(this.table).append(" SET ");
        if (getJoinData() != null)
            stringBuilder.append(getJoinData().toSQL());
        StringBuilder setterBuilder = new StringBuilder();
        for (Map.Entry<SQLColumns, Object> value : values.entrySet()) {
            if (!tablesAccept(value.getKey()))
                continue;
            if (!setterBuilder.isEmpty())
                setterBuilder.append(", ");
            setterBuilder.append(value.getKey().toSQL()).append(" = ?");
        }
        stringBuilder.append(setterBuilder).append(" WHERE ").append(this.primaryKey.toSQL()).append(" = ?");
        ArrayList<Object> objects = new ArrayList<>(values.values());
        objects.add(this.getInt(this.primaryKey));
        return new Pair<>(stringBuilder.toString(), objects);
    }

    public boolean exists() {
        return this.populate;
    }

    public boolean tablesAccept(SQLColumns sqlColumns) {
        if (this.getTable().equalsIgnoreCase(sqlColumns.getTable()))
            return true;
        JoinData joinData = getJoinData();
        if (joinData == null)
            return false;
        return joinData.getColumnsPair().getTwo().getColumns().equalsIgnoreCase(sqlColumns.getTable());
    }

}
