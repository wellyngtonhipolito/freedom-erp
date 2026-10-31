package org.freedom.infra.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Fábrica e Gerenciador do Pool de Conexões de Alta Performance HikariCP.
 * Substitui integralmente o TopLink/freedom-pool legado.
 */
public final class DatabaseConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionPool.class);
    private static volatile HikariDataSource dataSource;

    private DatabaseConnectionPool() {
        // Utilitário estático
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (dataSource == null) {
                    dataSource = initDataSource();
                }
            }
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static HikariDataSource initDataSource() {
        log.info("Inicializando Pool de Conexões HikariCP para PostgreSQL 16...");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("freedom.db.url", "jdbc:postgresql://localhost:5432/freedom_erp"));
        config.setUsername(System.getProperty("freedom.db.user", "freedom_user"));
        config.setPassword(System.getProperty("freedom.db.password", "freedom_secure_password"));

        // Configurações recomendadas para alta performance e estabilidade
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300_000); // 5 minutos
        config.setConnectionTimeout(20_000); // 20 segundos
        config.setMaxLifetime(1_800_000); // 30 minutos
        config.setPoolName("FreedomERP-HikariPool");

        // Otimizações do Driver PostgreSQL
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            log.info("Encerrando Pool de Conexões HikariCP...");
            dataSource.close();
        }
    }
}

