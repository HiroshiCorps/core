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
        assert model != null;

        StringBuilder query = new StringBuilder("SELECT * FROM " + model.getTable()
                + " WHERE " + model.getPrimaryKey() + " = " + primaryKey);

        JoinData joinData = model.getJoinData();
        if (joinData != null)
            query.append(" ").append(joinData.toSQL());

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
            assert model != null;

            StringBuilder query2 = new StringBuilder("SELECT * FROM " + model.getTable());

            if (query != null)
                query2.append(" ").append(query);

            JoinData joinData = model.getJoinData();
            if (joinData != null)
                query2.append(" ").append(joinData.toSQL());

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
        assert model != null;
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
            assert model != null;
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
        assert model != null;
        String queryString = "DELETE FROM " + model.getTable() + " " + query;
        this.logs.info(queryString);
        API.getInstance().getSQLConnection().execute(queryString, vars);
    }

    private String listCreator(Collection<Object> collection, String dataCloser) {

        StringBuilder tableListStr = new StringBuilder();

        for (Object entry : collection) {

            if (tableListStr.length() > 0) {
                tableListStr.append(", ");
            }

            if (entry == null) {
                tableListStr.append("NULL");
            } else {
                String tmp = entry.toString().replaceAll("'", "''");
                tableListStr.append(dataCloser).append(tmp).append(dataCloser);
            }

        }

        return tableListStr.toString();

    }

    private Pair<String, String> listCreator(T model, String table) {

        HashMap<SQLColumns, Object> dataList = new HashMap<>(model.getDataMap(table));

        if (model.get(model.getPrimaryKey()) != null)
            dataList.put(model.getPrimaryKey(), model.get(model.getPrimaryKey()));

        ArrayList<Object> columns = new ArrayList<>() {{
            for (SQLColumns columns1 : dataList.keySet()) {
                add(columns1.toSQL());
            }
        }};

        return new Pair<>(listCreator(columns, null), listCreator(dataList.values(), "'"));

    }

    public void insert(T model) {

        Pair<String, String> listNecString = listCreator(model, model.getTable());

        String query = "INSERT INTO " + model.getTable() + "(" + listNecString.getOne() + ") VALUES (" + listNecString.getTwo() + ")";

        this.logs.info(query);

        API.getInstance().getSQLConnection().execute(query);

    }

    public T generateInstance() {
        try {
            Constructor<T> constructors = this.method.getDeclaredConstructor();
            return constructors.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
