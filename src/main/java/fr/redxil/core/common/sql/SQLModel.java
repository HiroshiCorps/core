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
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.sql.utils.SQLColumns;
import fr.redxil.core.common.sql.utils.SQLJoin;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class SQLModel {

    private final String table;

    private final SQLColumns primaryKey;

    private final HashMap<String, Object> columns = new HashMap<>();

    private final SQLJoin SQLJoin;

    private boolean populate = false;

    public SQLModel(String table, SQLColumns primaryKey) {
        this.table = table;
        this.primaryKey = primaryKey;
        this.SQLJoin = null;
    }

    public SQLModel(String table, SQLColumns primaryKey, SQLJoin SQLJoin) {
        this.table = table;
        this.primaryKey = primaryKey;
        this.SQLJoin = SQLJoin;
    }

    public static String toSQL(SQLColumns sqlColumns) {
        return sqlColumns.toSQL();
    }

    public static String toSQL(String table, String columns) {
        return new SQLColumns(table, columns).toSQL();
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

    public boolean containsDataForTable(String table) {
        return !getDataMap(table).isEmpty();
    }

    public HashMap<SQLColumns, Object> getDataMap(String table) {
        return new HashMap<>() {{
            for (Map.Entry<String, Object> value : columns.entrySet()) {
                SQLColumns converted = SQLColumns.fromSQL(value.getKey());
                if (converted == null) {
                    CoreAPI.getInstance().getAPIEnabler().printLog(Level.SEVERE, "Error on convert String to SQLColumns with String: " + value.getKey());
                    continue;
                }
                if (converted.getTable().equalsIgnoreCase(table)) {
                    put(converted, value.getValue());
                }
            }
        }};
    }

    protected void onPopulated() {
    }

    public SQLJoin getJoinData() {
        return SQLJoin;
    }

    public String getString(SQLColumns columnName) {
        return (String) this.get(columnName);
    }

    public void populate(ResultSet resultSet) {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); ++i) {
                this.columns.put(SQLModel.toSQL(meta.getTableName(i), meta.getColumnName(i)), resultSet.getObject(i));
            }
            this.populate = true;
            this.onPopulated();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public Object get(SQLColumns columnName) {
        return columns.get(SQLModel.toSQL(columnName));
    }

    public Integer getInt(SQLColumns columnName) {
        try {
            return Integer.parseInt(this.get(columnName).toString());
        } catch (NumberFormatException error) {
            return null;
        }
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

    public Double getDouble(SQLColumns columnName) {
        try {
            return Double.parseDouble(this.get(columnName).toString());
        } catch (NumberFormatException | NullPointerException error) {
            return null;
        }
    }

    public Long getLong(SQLColumns columnName) {
        try {
            return Long.parseLong(this.get(columnName).toString());
        } catch (NumberFormatException | NullPointerException error) {
            return null;
        }
    }

    public void set(HashMap<SQLColumns, Object> map) {
        Pair<String, Collection<Object>> pair = this.setSQL(map);
        if (pair != null)
            CoreAPI.getInstance().getSQLConnection().ifPresent(sqlConnection -> sqlConnection.asyncExecute(pair.getOne(), pair.getTwo().toArray()));
    }

    public boolean exists() {
        return this.populate;
    }

    public void setSync(HashMap<SQLColumns, Object> map) {
        Pair<String, Collection<Object>> pair = this.setSQL(map);
        if (pair != null)
            CoreAPI.getInstance().getSQLConnection().ifPresent(sqlConnection -> sqlConnection.execute(pair.getOne(), pair.getTwo().toArray()));
    }

    private Pair<String, Collection<Object>> setSQL(Map<SQLColumns, Object> values) {
        for (Map.Entry<SQLColumns, Object> value : values.entrySet()) {
            columns.put(SQLModel.toSQL(value.getKey()), value.getValue());
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
            setterBuilder.append(SQLModel.toSQL(value.getKey())).append(" = ?");
        }
        stringBuilder.append(setterBuilder).append(" WHERE ").append(SQLModel.toSQL(this.primaryKey)).append(" = ?");
        ArrayList<Object> objects = new ArrayList<>(values.values());
        objects.add(this.getInt(this.primaryKey));
        return new Pair<>(stringBuilder.toString(), objects);
    }

    public boolean tablesAccept(SQLColumns sqlColumns) {
        if (this.getTable().equalsIgnoreCase(sqlColumns.getTable()))
            return true;
        SQLJoin SQLJoin = getJoinData();
        if (SQLJoin == null)
            return false;
        return SQLJoin.getColumnsPair().getTwo().getColumns().equalsIgnoreCase(sqlColumns.getTable());
    }

}
