package by.eugene.maven.migrations;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for reading migration and rollback SQL commands from migration files.
 * <p>
 * This class provides methods to read SQL commands from files that define database migrations and rollbacks,
 * separated by delimiters. It also extracts migration versions from the files.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * List<String> migrations = MigrationFileReader.readMigrationsFromFile("migration_v1.sql");
 * List<String> rollbacks = MigrationFileReader.readRollbacksFromFile("rollback_v1.sql");
 * int version = MigrationFileReader.readMigrationVersion("migration_v1.sql");
 * </pre>
 */
@Slf4j
public class MigrationFileReader {
    private static final String MIGRATION_DELIMITER = "--migration--";
    private static final String ROLLBACK_DELIMITER = "--rollback--";

    /**
     * Reads migration SQL commands from a file.
     * <p>
     * This method reads the migration SQL commands defined in a file and splits them by the specified delimiter.
     * It returns a list of SQL commands, each representing a single migration statement.
     * </p>
     *
     * @param filePath the path to the migration file (must be in the classpath)
     * @return a list of SQL migration commands
     * @throws RuntimeException if an error occurs while reading the file or parsing the SQL commands
     */
    public static List<String> readMigrationsFromFile(String filePath) {
        return readSqlFromFile(filePath, MIGRATION_DELIMITER);
    }

    /**
     * Reads rollback SQL commands from a file.
     * <p>
     * This method reads the rollback SQL commands defined in a file and splits them by the specified delimiter.
     * It returns a list of SQL commands, each representing a single rollback statement.
     * </p>
     *
     * @param filePath the path to the rollback file (must be in the classpath)
     * @return a list of SQL rollback commands
     * @throws RuntimeException if an error occurs while reading the file or parsing the SQL commands
     */
    public static List<String> readRollbacksFromFile(String filePath) {
        return readSqlFromFile(filePath, ROLLBACK_DELIMITER);
    }

    /**
     * Reads the migration version from the file.
     * <p>
     * This method reads the first line of the file, extracts the migration version (a number), and returns it.
     * </p>
     *
     * @param filePath the path to the migration file (must be in the classpath)
     * @return the migration version extracted from the first line of the file
     * @throws RuntimeException if the migration version cannot be extracted or if the file is not found
     */
    public static int readMigrationVersion(String filePath) {
        try (InputStream inputStream = MigrationFileReader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException("File %s not found".formatted(filePath));
            }
            String migrationVersionLine = new Scanner(inputStream).nextLine();

            Pattern pattern = Pattern.compile("\\d");
            Matcher matcher = pattern.matcher(migrationVersionLine);
            if(!matcher.find()) {
                throw new RuntimeException("No migration number provided in file: " + filePath);
            }
            return Integer.parseInt(matcher.group());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the SQL content from the file stream, based on the provided delimiter.
     * <p>
     * This method reads the SQL commands from the file stream until the specified delimiter is found,
     * then returns the SQL content before the delimiter.
     * </p>
     *
     * @param fileStream the InputStream of the file being read
     * @param delimiter the delimiter that separates migration or rollback commands
     * @return the SQL content as a string before the delimiter
     */
    public static String getSql(InputStream fileStream, String delimiter) {
        Scanner scanner = new Scanner(fileStream);
        scanner.useDelimiter(delimiter);
        String sql = null;
        if(delimiter.equals(ROLLBACK_DELIMITER)) {
            scanner.next();
            sql = scanner.next();
        } else {
            scanner.next();
            sql = scanner.next()
                    .split(ROLLBACK_DELIMITER)[0];
        }

        return sql;
    }

    private static List<String> readSqlFromFile (String filePath, String delimiter) {
        try (InputStream inputStream = MigrationFileReader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException("File %s not found".formatted(filePath));
            }

            String sql = getSql(inputStream, delimiter);

            List<String> sqlCommands = Arrays.stream(sql.split(";"))
                    .map(String::trim)
                    .filter(command -> !command.isBlank())
                    .toList();

            return sqlCommands;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
