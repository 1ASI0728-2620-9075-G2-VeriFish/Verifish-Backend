package io.github.rafaviv.yakubackend.shared.infrastructure.persistence;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class SchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        makeNullableIfExists("thresholds", "min_ph");
        makeNullableIfExists("thresholds", "max_ph");
    }

    private void makeNullableIfExists(String table, String column) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_name=? AND column_name=? AND is_nullable='NO'",
                Integer.class, table, column);
            if (count != null && count > 0) {
                jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN " + column + " DROP NOT NULL");
            }
        } catch (Exception ignored) {
        }
    }
}
