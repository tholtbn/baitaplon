package com.auction.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class SchemaInitializer {
    private final DataSourceProvider dataSourceProvider;

    public SchemaInitializer(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    public void initialize() {
        try (Connection conn = dataSourceProvider.getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = getClass().getClassLoader().getResourceAsStream("schema.sql")) {

            if (is == null) {
                throw new RuntimeException("schema.sql not found in resources");
            }

            String schemaSql = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            stmt.execute(schemaSql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}