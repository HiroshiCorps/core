/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql;

import fr.redxil.api.common.API;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class SQLModels<T extends SQLModel> {

    private final Class<T> method;
    private final Logger logs;

    public SQLModels(Class<T> method) {
        this.method = method;
        this.logs = Logger.getLogger("SQLModels<" + method.getSimpleName() + ">");
    }

    public T get(int primaryKey) {
        try {
            T model = this.method.newInstance();
            this.get(model, primaryKey);
            return model;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void get(T model, int primaryKey) {
        assert model != null;
        API.get().getSQLConnection().query("SELECT * FROM " + model.getTable()
                + " WHERE " + model.getPrimaryKey() + " = " + primaryKey, resultSet -> {
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
            T model = this.method.newInstance();

            API.get().getSQLConnection().query("SELECT * FROM " + model.getTable() + (query != null ? " " + query : ""),
                    resultSet -> {
                        try {
                            while (resultSet.next()) {
                                T newModel = this.method.newInstance();
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

    public T getOrInsert(HashMap<String, Object> defaultValues, int primaryKey) {
        try {
            T model = this.method.newInstance();
            this.getOrInsert(model, defaultValues, primaryKey);
            return model;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            this.logs.severe("Error SQL getOrInsert() = " + e.getMessage());
        }
        return null;
    }

    public void getOrInsert(T model, int primaryKey) {
        this.getOrInsert(model, null, primaryKey);
    }

    public void getOrInsert(T model, HashMap<String, Object> defaultValues, int primaryKey) {
        try {
            this.get(model, primaryKey);
            if (!model.exists()) {
                model.set(model.getPrimaryKey(), primaryKey);
                if (defaultValues != null) {
                    for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                        model.set(entry.getKey(), entry.getValue());
                    }
                }
                this.insert(model);
                this.get(model, primaryKey);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            this.logs.severe("Error SQL getOrInsert() #2 = " + exception.getMessage());
        }
    }

    public T getOrInsert(HashMap<String, Object> defaultValues, String query, Object... vars) {
        try {
            T foundRow = this.getFirst(query, vars);
            if (foundRow != null) {
                return foundRow;
            }
            T model = this.method.newInstance();
            if (defaultValues != null) {
                for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                    model.set(entry.getKey(), entry.getValue());
                }
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
        try {
            T model = this.method.newInstance();

            String queryString = "DELETE FROM " + model.getTable() + " " + query;
            this.logs.info(queryString);
            API.get().getSQLConnection().execute(queryString, vars);
        } catch (Exception ignored) {
        }
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

    private List<String> listCreator(T model) {

        HashMap<String, Object> dataList = new HashMap<>(model.getColumns());

        if (model.get(model.getPrimaryKey()) != null)
            dataList.put(model.getPrimaryKey(), model.get(model.getPrimaryKey()));

        return Arrays.asList(listCreator(new ArrayList<>(dataList.keySet()), "`"), listCreator(dataList.values(), "'"));

    }

    public void insert(T model) {

        List<String> listNecString = listCreator(model);

        String query = "INSERT INTO " + model.getTable() + "(" + listNecString.get(0) + ") VALUES (" + listNecString.get(1) + ")";

        this.logs.info(query);

        API.get().getSQLConnection().execute(query);

    }

}
