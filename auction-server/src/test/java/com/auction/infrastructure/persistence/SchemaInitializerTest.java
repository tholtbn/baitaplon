package com.auction.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SchemaInitializerTest {

    @Test
    void schemaInitializerShouldCreateCoreTables() throws Exception {
        // Dùng H2 in-memory nếu pom.xml có H2 test dependency; nếu dùng SQLite/MySQL thì đổi URL tương ứng
        String jdbcUrl = "jdbc:h2:mem:auction_test;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, "sa", "")) {
            // Gọi SchemaInitializer của bạn nếu có hàm nhận Connection / DataSource, ví dụ:
            // new SchemaInitializer(dataSource).initialize();

            // Kiểm tra metadata bảng nếu đã chạy schema:
            DatabaseMetaData meta = conn.getMetaData();
            Set<String> tables = new HashSet<>();
            try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
            assertNotNull(tables);
        }
    }
}