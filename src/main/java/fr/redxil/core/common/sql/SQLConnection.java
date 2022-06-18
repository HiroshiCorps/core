/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.utils.Callback;
import fr.redxil.api.common.utils.Scheduler;
import fr.redxil.core.common.CoreAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLConnection {

    private final Logger logs = Logger.getLogger(SQLConnection.class.getName());
    private HikariDataSource pool = null;


    public void connect(IpInfo ipInfo, String database, String username, String password) {
        try {
            Class.forName("org.mariadb.jdbc.MariaDbDataSource");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        HikariConfig config = new HikariConfig();

        config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        config.addDataSourceProperty("url", "jdbc:mariadb://" + ipInfo.getIp() + ":" + ipInfo.getPort() + "/" + database);
        config.addDataSourceProperty("user", username);
        config.addDataSourceProperty("password", password);

        // Avoid maxLifeTime disconnection
        config.setMinimumIdle(0);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(35000);
        config.setMaxLifetime(45000);

        this.pool = new HikariDataSource(config);
    }


    public boolean isConnected() {
        return this.pool != null && this.pool.isRunning();
    }


    public void closeConnection() {
        this.pool.close();
        this.pool = null;
    }

    private void closeRessources(ResultSet rs, PreparedStatement st) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public PreparedStatement prepareStatement(Connection conn, String query, Object... vars) {
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            CoreAPI.getInstance().getAPIEnabler().printLog(Level.INFO, "Preparing statement for query: " + query);
            int num = Math.toIntExact(query.chars().filter(ch -> ch == '?').count());
            if (num == vars.length) {
                int i = 0;
                for (Object obj : vars) {
                    i++;
                    ps.setObject(i, obj);
                    CoreAPI.getInstance().getAPIEnabler().printLog(Level.INFO, "Set object: " + i + " object: " + obj);
                }
            } else {
                CoreAPI.getInstance().getAPIEnabler().printLog(Level.SEVERE, "Problem with argument: Waited argument: " + num + " Gived: " + vars.length);
                return null;
            }
            return ps;

        } catch (SQLException exception) {
            this.logs.severe("MySQL error: " + exception.getMessage());
        }

        return null;
    }


    public void asyncQuery(final String query, final Callback<SQLRowSet> callback, final Object... vars) {
        Scheduler.runTask(() -> {
            try (Connection conn = this.pool.getConnection()) {
                try (PreparedStatement ps = this.prepareStatement(conn, query, vars)) {
                    if (ps == null) return;
                    try (ResultSet rs = ps.executeQuery()) {
                        SQLRowSet SQLRowSet = new SQLRowSet(rs);
                        this.closeRessources(rs, ps);
                        if (callback != null) {
                            callback.run(SQLRowSet);
                        }
                    }
                } catch (SQLException e) {
                    this.logs.severe("MySQL error: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (SQLException exception) {
                this.logs.severe("Error when getting pool connection !");
                exception.printStackTrace();
            }
        });
    }


    public SQLRowSet query(final String query, final Object... vars) {
        try (Connection conn = this.pool.getConnection()) {
            try (PreparedStatement ps = this.prepareStatement(conn, query, vars)) {
                if (ps == null) return null;
                try (ResultSet rs = ps.executeQuery()) {
                    SQLRowSet SQLRowSet = new SQLRowSet(rs);
                    this.closeRessources(rs, ps);
                    return SQLRowSet;
                }
            } catch (SQLException e) {
                this.logs.severe("MySQL error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException exception) {
            this.logs.severe("Error when getting pool connection !");
            exception.printStackTrace();
        }
        return null;
    }


    public void query(final String query, final Callback<ResultSet> callback, final Object... vars) {
        try (Connection conn = this.pool.getConnection()) {
            try (PreparedStatement ps = this.prepareStatement(conn, query, vars)) {
                if (ps == null) return;
                try (ResultSet rs = ps.executeQuery()) {
                    callback.run(rs);
                    this.closeRessources(rs, ps);
                }
            } catch (SQLException e) {
                this.logs.severe("MySQL error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException exception) {
            this.logs.severe("Error when getting pool connection !");
            exception.printStackTrace();
        }
    }


    public void asyncExecuteCallback(final String query, final Callback<Integer> callback, final Object... vars) {
        Scheduler.runTask(() -> {
            try (Connection conn = this.pool.getConnection()) {
                try (PreparedStatement ps = this.prepareStatement(conn, query, vars)) {
                    if (ps == null) return;
                    ps.execute();
                    this.closeRessources(null, ps);
                    if (callback != null) {
                        callback.run(-1);
                    }
                } catch (SQLException exception) {
                    if (exception.getErrorCode() == 1060) {
                        return;
                    }
                    this.logs.severe("MySQL error: " + exception.getMessage());
                    exception.printStackTrace();
                }
            } catch (SQLException exception) {
                this.logs.severe("Error when getting pool connection !");
                exception.printStackTrace();
            }
        });
    }


    public void asyncExecute(final String query, final Object... vars) {
        this.asyncExecuteCallback(query, null, vars);
    }


    public ResultSet execute(final String query, final Object... vars) {
        try (Connection conn = this.pool.getConnection()) {
            try (PreparedStatement ps = this.prepareStatement(conn, query, vars)) {
                if (ps == null) return null;
                ps.execute();
                return ps.getResultSet();
            } catch (SQLException exception) {
                this.logs.severe("MySQL error: " + exception.getMessage());
                exception.printStackTrace();
            }
        } catch (SQLException exception) {
            this.logs.severe("Error when getting pool connection !");
            exception.printStackTrace();
        }
        return null;
    }

}
