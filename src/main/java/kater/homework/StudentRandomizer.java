package kater.homework;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class StudentRandomizer {

    private static final int MIN_GRADE = 2;
    private static final int MAX_GRADE = 5;
    private static final String HELP_COMMAND = "/h";
    private static final String RANDOM_COMMAND = "/r";
    private static final String LIST_COMMAND = "/l";

    private static final Map<String, String> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put(HELP_COMMAND, "Show help menu");
        COMMANDS.put(RANDOM_COMMAND, "Choose random student and ask question");
        COMMANDS.put(LIST_COMMAND, "Show list of students with grades");
    }

    private final Connection connection;
    private final List<String> students;
    private final Map<String, Integer> grades;

    public StudentRandomizer(Connection connection) {
        this.connection = connection;
        students = new ArrayList<>();
        grades = new HashMap<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT name FROM students");
            while (resultSet.next()) {
                String student = resultSet.getString("name");
                students.add(student);
                grades.put(student, null);
            }
        } catch (SQLException e) {
            System.err.println("Error getting students from database: " + e.getMessage());
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Student Randomizer! Type '/h' for help.");
        while (true) {
            String command = scanner.nextLine();
            switch (command) {
                case HELP_COMMAND -> showHelp();
                case RANDOM_COMMAND -> chooseRandomStudent();
                case LIST_COMMAND -> showList();
                default -> System.out.println("Unknown command. Type '/h' for help.");
            }
        }
    }

    private void showHelp() {
        System.out.println("Commands:");
        for (Map.Entry<String, String> entry : COMMANDS.entrySet()) {
            System.out.printf("%s - %s%n", entry.getKey(), entry.getValue());
        }
    }

    private void chooseRandomStudent() {
        String student = getRandomStudent();
        System.out.printf("Student %s is chosen!%n", student);
        System.out.println("What is the answer to your question?");
        int grade = getGrade();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO grades (student_name, grade) VALUES (?, ?)")) {
            statement.setString(1, student);
            statement.setInt(2, grade);
            statement.executeUpdate();
            grades.put(student, grade);
        } catch (SQLException e) {
            System.err.println("Error inserting grade into database: " + e.getMessage());
        }
    }

    private String getRandomStudent() {
        Random random = new Random();
        int index = random.nextInt(students.size());
        return students.get(index);
    }

    private int getGrade() {
        Scanner scanner = new Scanner(System.in);
        int grade;
        do {
            System.out.printf("Enter grade (%d-%d):%n", MIN_GRADE, MAX_GRADE);
            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Try again.");
                scanner.next();
            }
            grade = scanner.nextInt();
        } while (grade < MIN_GRADE || grade > MAX_GRADE);
        return grade;
    }

    private void showList() {
        System.out.println("List of students with grades:");
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT student_name, grade FROM grades");
            while (resultSet.next()) {
                String student = resultSet.getString("student_name");
                Integer grade = resultSet.getInt("grade");
                grades.put(student, grade);
            }
            for (String student : students) {
                Integer grade = grades.get(student);
                System.out.printf("%s - %s%n", student, grade != null ? grade : "Not graded");
            }
        } catch (SQLException e) {
            System.err.println("Error getting grades from database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // TODO(kateR): change data for PGSQL database
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "admin1";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            StudentRandomizer randomizer = new StudentRandomizer(connection);
            randomizer.run();
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
    }
}