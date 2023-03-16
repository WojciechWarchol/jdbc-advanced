package com.wojto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

@SpringBootApplication
public class JdbcAdvancedApplication {
	
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "password";
	private static final String CONN_STRING = "jdbc:hsqldb:jdbc-advanced";

	public static void main(String[] args) throws SQLException {
		
		SpringApplication.run(JdbcAdvancedApplication.class, args);

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		connection = DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
		System.out.println("Connected to HSQL DB");
		statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		statement.executeQuery("DROP TABLE IF EXISTS users;");
		statement.executeQuery("DROP TABLE IF EXISTS friendships;");
		statement.executeQuery("DROP TABLE IF EXISTS posts;");
		statement.executeQuery("DROP TABLE IF EXISTS likes;");

		statement.executeQuery("CREATE TABLE users (\n" +
				"\tid INT,\n" +
				"\tfirst_name VARCHAR(50),\n" +
				"\tlast_name VARCHAR(50),\n" +
				"\ttimestamp DATETIME\n" +
				");");

		statement.executeQuery("CREATE TABLE friendships (\n" +
				"    userId1 INT,\n" +
				"    userId2 INT,\n" +
				"    timestamp DATETIME\n" +
				");");

		statement.executeQuery("CREATE TABLE posts (\n" +
				"\tid INT,\n" +
				"\tuserId INT,\n" +
				"\ttext VARCHAR(500),\n" +
				"\ttimestamp DATETIME\n" +
				");");

		statement.executeQuery("CREATE TABLE likes (\n" +
				"    postId INT,\n" +
				"    userId INT,\n" +
				"    timestamp DATETIME\n" +
				");");
		System.out.println("Created all databases");

		try {
			List<String> userInsertStatements = Files.readAllLines(Paths.get("social-network/src/main/resources/users.sql"));
			for (String command : userInsertStatements) {
				statement.executeQuery(command);
			}
			System.out.println("Finished filling USERS database");

			List<String> friendshipInsertStatements = Files.readAllLines(Paths.get("social-network/src/main/resources/friendships.sql"));
			for (String command : friendshipInsertStatements) {
				statement.executeQuery(command);
			}
			System.out.println("Finished filling FRIENDSHIPS database");

			List<String> postInsertStatements = Files.readAllLines(Paths.get("social-network/src/main/resources/posts.sql"));
			for (String command : postInsertStatements) {
				statement.executeQuery(command);
			}
			System.out.println("Finished filling POSTS database");

			List<String> likeInsertStatements = Files.readAllLines(Paths.get("social-network/src/main/resources/likes.sql"));
			for (String command : likeInsertStatements) {
				statement.executeQuery(command);
			}
			System.out.println("Finished filling LIKES database");
		} catch (IOException e) {
			e.printStackTrace();
		}

		resultSet = statement.executeQuery("SELECT u.id, u.first_name, u.last_name, SUM(friends) AS friends FROM ( \n" +
				"\t\tSELECT u.id, u.first_name, u.last_name, COUNT(f.userId1) AS friends FROM users AS u \n" +
				"\t\t\tINNER JOIN friendships AS f ON u.id=f.userId1 \n" +
				"\t\t\tGROUP BY u.id, u.first_name, u.last_name \n" +
				"\tUNION\n" +
				"\t\tSELECT u.id, u.first_name, u.last_name, COUNT(f.userId2) AS friends FROM users AS u \n" +
				"\t\t\tINNER JOIN friendships AS f ON u.id=f.userId2 \n" +
				"\t\t\tGROUP BY u.id, u.first_name, u.last_name ) AS u\n" +
				"\tJOIN\n" +
				"\t\t( SELECT p.userId AS userId, COUNT(l.userId) AS totalLikes FROM posts AS p\n" +
				"\t\tJOIN \n" +
				"\t\t\tlikes AS l ON p.id=l.postid\n" +
				"\t\tWHERE l.timestamp >= '2025-03-01' AND l.timestamp <= '2025-03-31'\n" +
				"\t\tGROUP BY p.userId\n" +
				"\t\tHAVING COUNT(l.userId) > 100 ) AS pl ON u.id=pl.userId\n" +
				"\tGROUP BY u.id, u.first_name, u.last_name\t\t\n" +
				"\tHAVING SUM(friends) > 100;");

		System.out.println("Found users with more than 100 friends, " +
				"and that had received more than 100 likes under their posts in March 2025:");

		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnNumber = rsmd.getColumnCount();
		while(resultSet.next()) {
			System.out.println(resultSet.getString(2) + " " + resultSet.getString(3));
		}

	}

}
