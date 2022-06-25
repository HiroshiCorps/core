/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql;

import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.sql.utils.SQLColumns;
import fr.redxil.core.common.utils.TripletData;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SQLModels<T extends SQLModel> {

    private final Class<T> method;
    private final Logger logs;

    public SQLModels(Class<T> method) {
        this.logs = Logger.getLogger("SQLModels<" + method.getSimpleName() + ">");
        this.method = method;
    }

    public Optional<T> get(int primaryKey) {
        Optional<T> model = SQLModel.generateInstance(method);
        if (model.isEmpty())
            return model;
        if (this.get(model.get(), primaryKey))
            return model;
        else return Optional.empty();
    }

    public boolean get(T model, int primaryKey) {
        if (model == null)
            return false;

        String query = "SELECT * FROM " + model.getTable()
                + " WHERE " + model.getPrimaryKey().toSQL() + " = " + primaryKey;

        Optional<SQLConnection> sqlConnection = CoreAPI.getInstance().getSQLConnection();
        if (sqlConnection.isEmpty())
            return false;
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        sqlConnection.get().query(query, resultSet -> {
            try {
                if (resultSet.first()) {
                    model.populate(resultSet);
                    atomicBoolean.set(true);
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
                this.logs.severe("Error SQL get() = " + exception.getMessage());
            }
        });
        return atomicBoolean.get();
    }

    public Optional<T> getFirst(@Nullable T firstModel, String query, Object... vars) {
        List<T> results = this.get(firstModel, query, vars);
        return results.size() > 0 ? Optional.of(results.get(0)) : Optional.empty();
    }

    public List<T> get(@Nullable T firstModel, @Nullable String query, Object... vars) {
        ArrayList<T> results = new ArrayList<>();
        try {
            final T model = firstModel == null ? SQLModel.generateInstance(method).orElse(null) : firstModel;
            if (model == null)
                return results;

            StringBuilder query2 = new StringBuilder("SELECT * FROM " + model.getTable());

            if (query != null)
                query2.append(" ").append(query);

            CoreAPI.getInstance().getSQLConnection().ifPresent(sql -> sql.query(query2.toString(),
                    resultSet -> {
                        try {
                            while (resultSet.next()) {
                                Optional<T> newModel;
                                if (model.exists())
                                    newModel = SQLModel.generateInstance(method);
                                else newModel = Optional.of(model);

                                if (newModel.isPresent()) {
                                    newModel.get().populate(resultSet);
                                    results.add(newModel.get());
                                }
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }, vars
            ));
        } catch (Exception exception) {
            exception.printStackTrace();
            this.logs.severe("Error SQL get() #2 = " + exception.getMessage());
        }
        return results;
    }

    public List<T> all() {
        return this.get(null, null);
    }

    public Optional<T> getOrInsert(@Nullable T model, @Nullable HashMap<SQLColumns, Object> defaultValues, int primaryKey) {
        try {
            Optional<T> modelOpt = model == null ? SQLModel.generateInstance(method) : Optional.of(model);
            if (modelOpt.isEmpty())
                return modelOpt;

            T modelUse = modelOpt.get();
            this.get(modelUse, primaryKey);
            if (modelUse.exists())
                return Optional.of(modelUse);
            modelUse.set(modelUse.getPrimaryKey(), primaryKey);
            if (defaultValues != null) {
                modelUse.set(defaultValues);
            }
            this.insert(modelUse);
            return Optional.of(modelUse);
        } catch (Exception exception) {
            exception.printStackTrace();
            this.logs.severe("Error SQL getOrInsert() #2 = " + exception.getMessage());
        }
        return Optional.empty();
    }

    public Optional<T> getOrInsert(@Nullable T model, @Nullable HashMap<SQLColumns, Object> defaultValues, String query, Object... vars) {
        try {
            Optional<T> modelOpt = model == null ? SQLModel.generateInstance(method) : Optional.of(model);
            if (modelOpt.isEmpty())
                return modelOpt;

            T modelUse = modelOpt.get();

            this.get(modelUse, query, vars);
            if (modelUse.exists())
                return Optional.of(modelUse);

            this.getFirst(modelUse, query, vars);
            if (modelUse.exists()) {
                return Optional.of(modelUse);
            }
            if (defaultValues != null) {
                modelUse.set(defaultValues);
            }
            this.insert(modelUse);
            return Optional.of(modelUse);
        } catch (Exception exception) {
            exception.printStackTrace();
            this.logs.severe("Error SQL getOrInsert() #3 = " + exception.getMessage());
        }
        return Optional.empty();
    }

    public boolean delete(String query, Object... vars) {
        Optional<T> model = SQLModel.generateInstance(method);
        if (model.isEmpty())
            return false;
        String queryString = "DELETE FROM " + model.get().getTable() + " " + query;
        this.logs.info(queryString);
        Optional<SQLConnection> sqlConnection = CoreAPI.getInstance().getSQLConnection();
        return sqlConnection.map(connection -> connection.execute(queryString, vars).isPresent()).orElse(false);
    }

    private String listCreator(Collection<Object> collection, boolean value) {

        StringBuilder tableListStr = new StringBuilder();

        for (Object entry : collection) {

            if (tableListStr.length() > 0) {
                tableListStr.append(", ");
            }

            if (!value) {
                if (entry == null) {
                    tableListStr.append("NULL");
                } else {
                    String tmp = entry.toString().replaceAll("'", "''");
                    tableListStr.append(tmp);
                }
            } else {
                tableListStr.append("?");
            }

        }

        return tableListStr.toString();

    }

    private TripletData<String, String, Collection<Object>> listCreator(T model, String table) {

        HashMap<SQLColumns, Object> dataList = new HashMap<>(model.getDataMap(table));

        ArrayList<Object> columns = new ArrayList<>() {{
            for (SQLColumns columns1 : dataList.keySet()) {
                add(columns1.getColumns());
            }
        }};

        return new TripletData<>(listCreator(columns, false), listCreator(dataList.values(), true), dataList.values());

    }

    public boolean insert(T model) {

        TripletData<String, String, Collection<Object>> listNecString = listCreator(model, model.getTable());
        Collection<Object> objectList = listNecString.getThird();

        String query = "BEGIN TRANSACTION" +
                "INSERT INTO " + model.getTable() + "(" + listNecString.getFirst() + ") VALUES (" + listNecString.getSecond() + ");" +
                "SELECT SCOPE_IDENTITY() AS [SCOPE_IDENTITY];  " +
                "COMMIT";


        this.logs.info(query);

        Optional<SQLConnection> sqlConnection = CoreAPI.getInstance().getSQLConnection();
        if (sqlConnection.isPresent()) {
            Optional<ResultSet> resultSet = sqlConnection.get().execute(query, objectList.toArray());
            if (resultSet.isPresent()) {
                try {
                    model.getDataMap().put(model.getPrimaryKey().toSQL(), resultSet.get().getObject("SCOPE_IDENTITY"));
                    return true;
                } catch (SQLException e) {
                    return false;
                }
            }
        }
        return false;
    }

}
