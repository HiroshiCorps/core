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
import fr.redxil.api.common.API;
import fr.redxil.api.common.Callback;
import fr.redxil.api.common.sql.SQLConnection;
import fr.redxil.api.common.utils.Scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSQLConnection implements SQLConnection {

    private final Logger logs = Logger.getLogger(CSQLConnection.class.getName());
    private HikariDataSource pool = null;

    @Override
    public void connect(IpInfo ipInfo, String database, String username, String password) {
        try {
            Class.forName("org.mariadb.jdbc.MariaDbDataSource");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        HikariConfig config = new HikariConfig();
/*        config.setJdbcUrl("jdbc:mariadb://" + this.host + ":" + this.port + "/" + this.database
                //        + "?verifyServerCertificate=false"
                //        + "&useSSL=false"
                //        + "&serverTimezone=UTC"
                //        + "&characterEncoding=UTF-8"
                //        + "&jdbcCompliantTruncation=false"
                //+ "&allowMultiQueries=true"
        );
*/

        config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        config.addDataSourceProperty("url", "jdbc:mariadb://" + ipInfo.getIp() + ":" + ipInfo.getPort() + "/" + database);
        config.addDataSourceProperty("user", username);
        config.addDataSourceProperty("password", password);

        // + "?user="+ this.username + "&password=" + this.password
        // + "?verifyServerCertificate=false" + "&useSSL=false"

        //config.addDataSourceProperty("cachePrepStmts", "true");
        //config.addDataSourceProperty("prepStmtCacheSize", "250");
        //config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        //config.addDataSourceProperty("useLocalSessionState", true);
        //config.addDataSourceProperty("rewriteBatchedStatements", true);
        //config.addDataSourceProperty("cacheResultSetMetadata", true);
        //config.addDataSourceProperty("cacheServerConfiguration", true);
        //config.addDataSourceProperty("elideSetAutoCommits", true);
        //config.addDataSourceProperty("maintainTimeStats", false);

        // Avoid maxLifeTime disconnection
        config.setMinimumIdle(0);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(35000);
        config.setMaxLifetime(45000);

        this.pool = new HikariDataSource(config);
    }

    @Override
    public boolean isConnected() {
        return this.pool != null && this.pool.isRunning();
    }

    @Override
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

    @Override
    public PreparedStatement prepareStatement(Connection conn, String query, Object... vars) {
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            API.getInstance().getPluginEnabler().printLog(Level.INFO, "Preparing statement for query: " + query);
            int num = Math.toIntExact(query.chars().filter(ch -> ch == '?').count());
            if (num == vars.length) {
                int i = 0;
                for (Object obj : vars) {
                    i++;
                    ps.setObject(i, obj);
                    API.getInstance().getPluginEnabler().printLog(Level.INFO, "Set object: " + i + " object: " + obj);
                }
            } else {
                API.getInstance().getPluginEnabler().printLog(Level.SEVERE, "Problem with argument: Waited argument: " + num + " Gived: " + vars.length);
                return null;
            }
            return ps;

        } catch (SQLException exception) {
            this.logs.severe("MySQL error: " + exception.getMessage());
        }

        return null;
    }

    @Override
    public void asyncQuery(final String query, final Callback<fr.redxil.api.common.sql.SQLRowSet> callback, final Object... vars) {
        Scheduler.runTask(() -> {
            try (Connection conn = this.pool.getConnection()) {
                try (PreparedStatement ps = this.prepareStatement(conn, query, vars)) {
                    if (ps == null) return;
                    try (ResultSet rs = ps.executeQuery()) {
                        CSQLRowSet CSQLRowSet = new CSQLRowSet(rs);
                        this.closeRessources(rs, ps);
                        if (callback != null) {
                            callback.run(CSQLRowSet);
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


    @Override
    public CSQLRowSet query(final String query, final Object... vars) {
        try (Connection conn = this.pool.getConnection()) {
            try (PreparedStatement ps = this.prepareStatement(conn, query, vars)) {
                if (ps == null) return null;
                try (ResultSet rs = ps.executeQuery()) {
                    CSQLRowSet CSQLRowSet = new CSQLRowSet(rs);
                    this.closeRessources(rs, ps);
                    return CSQLRowSet;
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

    @Override
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

    @Override
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

    @Override
    public void asyncExecute(final String query, final Object... vars) {
        this.asyncExecuteCallback(query, null, vars);
    }

    @Override
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
