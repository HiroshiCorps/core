/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql;

import fr.redxil.api.common.API;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class SQLModel {

    private final String table;

    private final String primaryKey;

    private final HashMap<String, Object> columns = new HashMap<>();

    private boolean populate = false;

    public SQLModel(String table, String primaryKey) {
        this.table = table;
        this.primaryKey = primaryKey;
    }

    public String getTable() {
        return this.table;
    }

    public String getPrimaryKey() {
        return this.primaryKey;
    }

    public HashMap<String, Object> getColumns() {
        return this.columns;
    }

    public void populate(ResultSet resultSet) {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); ++i) {
                this.columns.put(meta.getColumnName(i), resultSet.getObject(i));
            }
            this.populate = true;
            this.onPopulated();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    protected void onPopulated() {
    }

    public Object get(String columnName) {
        return this.columns.get(columnName);
    }

    public String getString(String columnName) {
        return (String) this.columns.get(columnName);
    }

    public int getInt(String columnName) {
        return Integer.parseInt(this.columns.get(columnName).toString());
    }

    public double getDouble(String columnName) {
        return Double.parseDouble(this.columns.get(columnName).toString());
    }

    public long getLong(String columnName) {
        return Long.parseLong(this.columns.get(columnName).toString());
    }

    public void set(String columnName, Object value) {
        if (this.exists() && columnName.equals(this.primaryKey)) {
            return;
        }
        this.columns.put(columnName, value);
        if (!this.populate) {
            return;
        }
        if (value == null) {
            API.getInstance().getSQLConnection().asyncExecute("UPDATE " + this.table
                    + " SET " + columnName + " = NULL WHERE " + this.primaryKey + " = ?", this.getInt(this.primaryKey));
        } else {
            API.getInstance().getSQLConnection().asyncExecute("UPDATE " + this.table
                    + " SET " + columnName + " = ? WHERE " + this.primaryKey + " = ?", value, this.getInt(this.primaryKey));
        }
    }

    public void set(Map<String, Object> values) {
        if (this.exists() && values.containsKey(this.primaryKey)) {
            return;
        }
        this.columns.putAll(values);
        if (!this.populate) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder("UPDATE ").append(this.table).append(" SET ");
        StringBuilder setterBuilder = new StringBuilder();
        for (Map.Entry<String, Object> value : values.entrySet()) {
            if (!setterBuilder.isEmpty())
                setterBuilder.append(", ");
            setterBuilder.append(value.getKey()).append(" = ?");
        }
        stringBuilder.append(setterBuilder).append(" WHERE ").append(this.primaryKey).append(" = ?");
        API.getInstance().getSQLConnection().asyncExecute(stringBuilder.toString(), values.values(), this.getInt(this.primaryKey));
    }

    public void setSync(String columnName, Object value) {
        if (this.exists() && columnName.equals(this.primaryKey)) {
            return;
        }
        this.columns.put(columnName, value);
        API.getInstance().getPluginEnabler().printLog(Level.INFO, "UPDATE " + this.table
                + " SET " + columnName + " = " + value + " WHERE " + this.primaryKey + " = " + this.getInt(this.primaryKey));
        if (value == null) {
            API.getInstance().getSQLConnection().execute("UPDATE " + this.table
                    + " SET " + columnName + " = NULL WHERE " + this.primaryKey + " = ?", this.getInt(this.primaryKey));
        } else {
            API.getInstance().getSQLConnection().execute("UPDATE " + this.table
                    + " SET " + columnName + " = ? WHERE " + this.primaryKey + " = ?", value, this.getInt(this.primaryKey));
        }
    }

    public void add(String columnName, int add) {
        if (this.exists() && columnName.equals(this.primaryKey)) {
            return;
        }
        if (!this.populate) {
            return;
        }
        this.columns.put(columnName, this.getInt(columnName) + add);
        API.getInstance().getSQLConnection().asyncExecute("UPDATE " + this.table
                + " SET " + columnName + " = " + columnName + " + " + add
                + " WHERE " + this.primaryKey + " = ?", this.getInt(this.primaryKey));
    }

    public void sub(String columnName, int sub) {
        if (this.exists() && columnName.equals(this.primaryKey)) {
            return;
        }
        if (!this.populate) {
            return;
        }
        this.columns.put(columnName, this.getInt(columnName) - sub);
        API.getInstance().getSQLConnection().asyncExecute("UPDATE " + this.table
                + " SET " + columnName + " = " + columnName + " - " + sub
                + " WHERE " + this.primaryKey + " = ?", this.getInt(this.primaryKey));
    }

    public boolean exists() {
        return this.populate;
    }

}
