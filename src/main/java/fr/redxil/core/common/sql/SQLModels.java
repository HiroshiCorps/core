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
import fr.redxil.core.common.sql.utils.SQLColumns;
import fr.redxil.core.common.sql.utils.SQLJoin;
import fr.redxil.core.common.utils.TripletData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class SQLModels<T extends SQLModel> {

    private final Class<T> method;
    private final Logger logs;

    public SQLModels(Class<T> method) {
        this.method = method;
        this.logs = Logger.getLogger("SQLModels<" + method.getSimpleName() + ">");
    }

    public T get(int primaryKey) {
        T model = generateInstance();
        this.get(model, primaryKey);
        return model;
    }

    public void get(T model, int primaryKey) {
        if (model == null)
            return;

        StringBuilder query = new StringBuilder("SELECT * FROM " + model.getTable()
                + " WHERE " + model.getPrimaryKey() + " = " + primaryKey);

        SQLJoin SQLJoin = model.getJoinData();
        if (SQLJoin != null)
            query.append(" ").append(SQLJoin.toSQL());

        API.getInstance().getSQLConnection().query(query.toString(), resultSet -> {
            try {
                if (resultSet.first()) {
                    model.populate(resultSet);
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
                this.logs.severe("Error SQL get() = " + exception.getMessage());
            }
        });
    }

    public T getFirst(String query, Object... vars) {
        List<T> results = this.get(query, vars);
        return results.size() > 0 ? results.get(0) : null;
    }

    public List<T> get(String query, Object... vars) {
        ArrayList<T> results = new ArrayList<>();
        try {
            T model = generateInstance();
            if (model == null)
                return null;

            StringBuilder query2 = new StringBuilder("SELECT * FROM " + model.getTable());

            if (query != null)
                query2.append(" ").append(query);

            SQLJoin SQLJoin = model.getJoinData();
            if (SQLJoin != null)
                query2.append(" ").append(SQLJoin.toSQL());

            API.getInstance().getSQLConnection().query(query2.toString(),
                    resultSet -> {
                        try {
                            while (resultSet.next()) {
                                T newModel = generateInstance();
                                assert newModel != null;
                                newModel.populate(resultSet);
                                results.add(newModel);
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }, vars
            );
        } catch (Exception exception) {
            exception.printStackTrace();
            this.logs.severe("Error SQL get() #2 = " + exception.getMessage());
        }
        return results;
    }

    public List<T> all() {
        return this.get(null);
    }

    public T getOrInsert(int primaryKey) {
        return this.getOrInsert(new HashMap<>(), primaryKey);
    }

    public T getOrInsert(HashMap<SQLColumns, Object> defaultValues, int primaryKey) {
        T model = generateInstance();
        if (model == null)
            return null;
        this.getOrInsert(model, defaultValues, primaryKey);
        return model;
    }

    public void getOrInsert(T model, int primaryKey) {
        this.getOrInsert(model, null, primaryKey);
    }

    public void getOrInsert(T model, HashMap<SQLColumns, Object> defaultValues, int primaryKey) {
        try {
            this.get(model, primaryKey);
            if (!model.exists()) {
                model.set(model.getPrimaryKey(), primaryKey);
                if (defaultValues != null) {
                    model.set(defaultValues);
                }
                this.insert(model);
                this.get(model, primaryKey);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            this.logs.severe("Error SQL getOrInsert() #2 = " + exception.getMessage());
        }
    }

    public T getOrInsert(HashMap<SQLColumns, Object> defaultValues, String query, Object... vars) {
        try {
            T foundRow = this.getFirst(query, vars);
            if (foundRow != null) {
                return foundRow;
            }
            T model = generateInstance();
            if (model == null)
                return null;
            if (defaultValues != null) {
                model.set(defaultValues);
            }
            this.insert(model);
            return this.getOrInsert(defaultValues, query, vars);
        } catch (Exception exception) {
            exception.printStackTrace();
            this.logs.severe("Error SQL getOrInsert() #3 = " + exception.getMessage());
        }
        return null;
    }

    public void delete(String query, Object... vars) {
        T model = generateInstance();
        if (model == null)
            return;
        String queryString = "DELETE FROM " + model.getTable() + " " + query;
        this.logs.info(queryString);
        API.getInstance().getSQLConnection().execute(queryString, vars);
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

    public void insert(T model) {

        String query;
        SQLJoin SQLJoin = model.getJoinData();

        TripletData<String, String, Collection<Object>> listNecString = listCreator(model, model.getTable());
        Collection<Object> objectList = listNecString.getThird();

        if (SQLJoin == null || !model.containsDataForTable(SQLJoin.getColumnsPair().getTwo().getTable())) {

            query = "INSERT INTO " + model.getTable() + "(" + listNecString.getFirst() + ") VALUES (" + listNecString.getSecond() + ")";

        } else {

            TripletData<String, String, Collection<Object>> listNecString2 = listCreator(model, SQLJoin.getColumnsPair().getTwo().getTable());
            objectList.addAll(listNecString2.getThird());
            query = "BEGIN TRANSACTION" +
                    "DECLARE @DataID int;" +
                    "INSERT INTO " + model.getTable() + "(" + listNecString.getFirst() + ") VALUES (" + listNecString.getSecond() + ");" +
                    "SELECT @DataID = scope_identity();" +
                    "INSERT INTO " + SQLJoin.getColumnsPair().getTwo().getTable() + "(" + SQLJoin.getColumnsPair().getTwo().getColumns() + ", " + listNecString2.getFirst() + ") VALUES (@DataID, " + listNecString2.getSecond() + ");" +
                    "COMMIT";

        }

        this.logs.info(query);

        API.getInstance().getSQLConnection().execute(query, objectList.toArray());

    }

    public T generateInstance() {
        try {
            Constructor<T> constructors = this.method.getDeclaredConstructor();
            return constructors.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
