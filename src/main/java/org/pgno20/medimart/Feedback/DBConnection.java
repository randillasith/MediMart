package org.pgno20.medimart.Feedback;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {

        try {
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (input != null) {
                    props.load(input);
                }
            } catch (Exception ex) {
                System.out.println("Could not load application.properties: " + ex.getMessage());
            }

            String url = props.getProperty("spring.datasource.url", "jdbc:mysql://localhost:3307/medimart_db");
            String user = props.getProperty("spring.datasource.username", "root");
            String password = props.getProperty("spring.datasource.password", "Lcafe@66981");

            Connection con = DriverManager.getConnection(url, user, password);

            System.out.println("Database Connected!");

            // Auto-create table
            try (java.sql.Statement stmt = con.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS feedback (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "customer_name VARCHAR(255), " +
                        "message TEXT, " +
                        "rating INT)");
            } catch (Exception ex) {
                System.out.println("Error initializing feedback table: " + ex.getMessage());
            }

            return con;

        } catch (Exception e) {

            System.out.println("Database connection failed: " + e);
        }

        return null;
    }
}
