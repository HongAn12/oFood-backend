package com.example.demo.Controller;

import org.springframework.web.bind.annotation.GetMapping;

public class DbHealthController {
    private final javax.sql.DataSource ds;

    public DbHealthController(javax.sql.DataSource ds) {
        this.ds = ds;
    }

    @GetMapping("/db-ping")
    public String ping() throws Exception {
        try (var c = ds.getConnection();
             var st = c.createStatement();
             var rs = st.executeQuery("SELECT current_schema()")) {
            rs.next();
            return "DB OK, schema=" + rs.getString(1);
        }
    }
}
