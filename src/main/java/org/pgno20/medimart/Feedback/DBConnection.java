package org.pgno20.medimart.Feedback;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static Connection con;

    public static Connection getConnection() {

        try {

            if (con == null || con.isClosed()) {

                Class.forName("com.mysql.cj.jdbc.Driver");

                con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/medical_store",
                        "root",
                        ""
                );
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return con;
    }
}