package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/shopping-cart";
        String user = "root";
        String password = "Kevscomputer090602!";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
