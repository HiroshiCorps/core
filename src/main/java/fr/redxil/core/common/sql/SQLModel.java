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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class SQLModel {

    private final String table;

    private final SQLColumns primaryKey;

    private final HashMap<SQLColumns, Object> columns = new HashMap<>();

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
        return this.columns;
    }

    public HashMap<SQLColumns, Object> getDataMap(String table) {
        return new HashMap<>() {{
            for (Map.Entry<SQLColumns, Object> value : columns.entrySet())
                if (value.getKey().getTable().equalsIgnoreCase(table))
                    put(value.getKey(), value.getValue());
        }};
    }

    public boolean containsDataForTable(String table) {
        for (Map.Entry<SQLColumns, Object> value : columns.entrySet())
            if (value.getKey().getTable().equalsIgnoreCase(table))
                return true;
        return false;
    }

    public void populate(ResultSet resultSet) {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); ++i) {
                this.columns.put(new SQLColumns(meta.getTableName(i), meta.getColumnName(i)), resultSet.getObject(i));
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
        return this.columns.get(columnName);
    }

    public String getString(SQLColumns columnName) {
        return (String) this.columns.get(columnName);
    }

    public int getInt(SQLColumns columnName) {
        return Integer.parseInt(this.columns.get(columnName).toString());
    }

    public double getDouble(SQLColumns columnName) {
        return Double.parseDouble(this.columns.get(columnName).toString());
    }

    public long getLong(SQLColumns columnName) {
        return Long.parseLong(this.columns.get(columnName).toString());
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
        assert pair != null;
        API.getInstance().getSQLConnection().asyncExecute(pair.getOne(), pair.getTwo());
    }

    public void setSync(HashMap<SQLColumns, Object> map) {
        Pair<String, Collection<Object>> pair = this.setSQL(map);
        assert pair != null;
        API.getInstance().getSQLConnection().execute(pair.getOne(), pair.getTwo());
    }


    private Pair<String, Collection<Object>> setSQL(Map<SQLColumns, Object> values) {
        columns.putAll(values);
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
        stringBuilder.append(setterBuilder).append(" WHERE ").append(this.primaryKey).append(" = ?");
        Collection<Object> objects = values.values();
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
