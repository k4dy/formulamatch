package com.formulamatch.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DatabaseResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(DatabaseResetScheduler.class);

    private final JdbcTemplate jdbc;

    public DatabaseResetScheduler(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Scheduled(fixedRate = 72 * 60 * 60 * 1000L)
    public void reset() {
        log.info("Starting scheduled database reset");
        jdbc.execute("DELETE FROM product_ingredients WHERE product_id >= 1000000");
        jdbc.execute("DELETE FROM products WHERE id >= 1000000");
        jdbc.execute("DELETE FROM brands WHERE id NOT IN (SELECT DISTINCT brand_id FROM products WHERE brand_id IS NOT NULL)");
        jdbc.execute("TRUNCATE TABLE proposal_ingredients, submission_ingredients, change_proposals, product_submissions, users RESTART IDENTITY");
        log.info("Database reset complete — crowdsourced products and user data cleared");
    }
}
