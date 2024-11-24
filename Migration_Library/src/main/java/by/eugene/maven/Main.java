package by.eugene.maven;

import by.eugene.maven.config.PropertiesUtils;

public class Main {
    public static void main(String[] args) {
        PropertiesUtils properties = new PropertiesUtils("application.properties");
        MigrationTool migrationTool = new MigrationTool(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password"),
                properties.getProperty("migration.directory")
        );

        migrationTool.run();
    }
}
