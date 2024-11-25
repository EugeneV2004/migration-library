package by.eugene.maven.migrations;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MigrationFileReader {
    private static final String MIGRATION_DELIMITER = "--migration--";
    private static final String ROLLBACK_DELIMITER = "--rollback--";

    public static List<String> readMigrationsFromFile(String filePath) {
        return readSqlFromFile(filePath, MIGRATION_DELIMITER);
    }

    public static List<String> readRollbacksFromFile(String filePath) {
        return readSqlFromFile(filePath, ROLLBACK_DELIMITER);
    }

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
}
