package com.rahul.realtime.notification.integration;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

class PostgreSqlConnectivityTest {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/realtime_notification";

    private static final String USERNAME =
            "postgres";

    private static final String PASSWORD =
            "root";

    @Test
    void shouldConnectToPostgreSql() throws Exception {

        try (Connection connection =
                     DriverManager.getConnection(
                             URL,
                             USERNAME,
                             PASSWORD
                     )) {

            System.out.println(
                    "JDBC URL: "
                            + connection.getMetaData().getURL()
            );

            System.out.println(
                    "Database: "
                            + connection.getMetaData()
                            .getDatabaseProductName()
            );

            System.out.println(
                    "Database version: "
                            + connection.getMetaData()
                            .getDatabaseProductVersion()
            );

            assertThat(connection.isValid(5))
                    .isTrue();
        }
    }
}