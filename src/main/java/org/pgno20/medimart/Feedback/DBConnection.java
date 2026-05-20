package org.pgno20.medimart.Feedback;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {

        try {

            String url = "jdbc:mysql://localhost:3306/medical_store";
            String user = "root";
            String password = "";

            Connection con = DriverManager.getConnection(url, user, password);

            System.out.println("Database Connected!");

            return con;

        } catch (Exception e) {

            System.out.println(e);
        }

        return null;
    }
}
