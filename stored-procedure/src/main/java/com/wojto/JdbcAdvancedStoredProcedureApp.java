package com.wojto;

import com.wojto.utils.DBUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.*;

@SpringBootApplication
public class JdbcAdvancedStoredProcedureApp {

    private static final String USERNAME = "jgmpuser";
    private static final String PASSWORD = "password1234";
    private static final String CONN_STRING = "jdbc:mysql://localhost:3306/spring_jgmp";

    public static void main(String[] args) throws SQLException {

        SpringApplication.run(JdbcAdvancedStoredProcedureApp.class, args);

        ResultSet resultSet = null;

        try( Connection connection = DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            System.out.println("Connected to HSQL DB");

            statement.execute("DROP PROCEDURE IF EXISTS GetAverageMoneySpentPerCustomer");
            statement.execute("DROP PROCEDURE IF EXISTS GetTopCustomers");
            statement.execute("DROP PROCEDURE IF EXISTS GetTopMonths");

            System.out.println("Procedures after dropping:");
            resultSet = statement.executeQuery("SHOW PROCEDURE STATUS WHERE db = 'spring_jgmp';");
            DBUtils.printResultSet(resultSet);

            String sqlProcedure;

            sqlProcedure = """
                    CREATE PROCEDURE GetAverageMoneySpentPerCustomer()
                    BEGIN
                    SELECT AVG(e.ticketPrice) as tickets_bought FROM users AS u
                    	JOIN tickets AS t ON t.userId=u.id
                        JOIN events AS e ON e.id=t.eventId
                        ORDER BY tickets_bought DESC
                        ;
                    END""";
            statement.execute(sqlProcedure);

            sqlProcedure = """
                    CREATE PROCEDURE GetTopCustomers(IN LimitUsers INT)
                    BEGIN
                    SELECT u.name, COUNT(t.id), SUM(e.ticketPrice) as tickets_bought FROM users AS u
                    	JOIN tickets AS t ON t.userId=u.id
                        JOIN events AS e ON e.id=t.eventId
                        GROUP BY u.name
                        ORDER BY tickets_bought DESC
                        LIMIT LimitUsers
                        ;
                    END""";
            statement.execute(sqlProcedure);

            sqlProcedure = """
                    CREATE PROCEDURE GetTopMonths(IN LimitMonths INT)
                    BEGIN
                    SELECT MONTH(e.date), SUM(e.ticketPrice) as tickets_bought FROM users AS u
                    	JOIN tickets AS t ON t.userId=u.id
                        JOIN events AS e ON e.id=t.eventId
                        GROUP BY MONTH(e.date)
                        ORDER BY tickets_bought DESC
                        LIMIT LimitMonths
                        ;
                    END""";
            statement.execute(sqlProcedure);

            System.out.println("Procedures after creating:");
            resultSet = statement.executeQuery("SHOW PROCEDURE STATUS WHERE db = 'spring_jgmp';");
            DBUtils.printResultSet(resultSet);

            System.out.println("Calling Stored Procedures.");

            resultSet = statement.executeQuery("CALL GetAverageMoneySpentPerCustomer()");
            DBUtils.printResultSet(resultSet);

            String preparedStatementString = "CALL GetTopCustomers(?)";
            PreparedStatement preparedStatement1 = connection.prepareStatement(preparedStatementString);
            preparedStatement1.setInt(1, 10);
            resultSet = preparedStatement1.executeQuery();
            DBUtils.printResultSet(resultSet);

            preparedStatementString = "CALL GetTopMonths(?)";
            PreparedStatement preparedStatement2 = connection.prepareStatement(preparedStatementString);
            preparedStatement2.setInt(1, 3);
            resultSet = preparedStatement2.executeQuery();
            DBUtils.printResultSet(resultSet);

        } finally {
//            resultSet.close();
        }

        System.out.println("Closing application");


    }
}