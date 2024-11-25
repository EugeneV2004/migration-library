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
        log.info("Reading migration commands from file: {}", filePath);
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
        log.info("Reading rollback commands from file: {}", filePath);
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
        log.info("Extracting migration version from file: {}", filePath);
        try (InputStream inputStream = MigrationFileReader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                log.error("File not found: {}", filePath);
                throw new IOException("File %s not found".formatted(filePath));
            }
            String firstLine = new Scanner(inputStream).nextLine().trim();
            log.debug("First line of the file: {}", firstLine);

            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(firstLine);

            if (!matcher.find()) {
                log.error("Migration version not found in file: {}", filePath);
                throw new RuntimeException("No migration number provided in file: " + filePath);
            }

            int version = Integer.parseInt(matcher.group());
            log.info("Migration version extracted: {}", version);
            return version;

        } catch (IOException e) {
            log.error("Error reading file: {}", filePath, e);
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
        log.debug("Parsing SQL content with delimiter: {}", delimiter);
        Scanner scanner = new Scanner(fileStream).useDelimiter(delimiter);

        String sql = null;
        if (delimiter.equals(ROLLBACK_DELIMITER)) {
            if (scanner.hasNext()) scanner.next(); // Skip migration part
            sql = scanner.hasNext() ? scanner.next() : "";
        } else {
            if (scanner.hasNext()) sql = scanner.next(); // Get migration part
        }

        log.debug("SQL content extracted before splitting by rollback: {}", sql);
        return sql;
    }

    private static List<String> readSqlFromFile(String filePath, String delimiter) {
        try (InputStream inputStream = MigrationFileReader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                log.error("File not found: {}", filePath);
                throw new IOException("File %s not found".formatted(filePath));
            }

            String sql = getSql(inputStream, delimiter);
            log.debug("Raw SQL read from file: {}", sql);

            List<String> sqlCommands = Arrays.stream(sql.split(";"))
                    .map(String::trim)
                    .filter(command -> !command.isBlank())
                    .toList();

            log.info("Parsed {} SQL commands from file: {}", sqlCommands.size(), filePath);
            return sqlCommands;

        } catch (IOException e) {
            log.error("Error reading or parsing file: {}", filePath, e);
            throw new RuntimeException(e);
        }
    }
}