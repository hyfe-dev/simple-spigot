package me.javadebug.simplespigot.storage.types.mysql;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import me.javadebug.simplespigot.plugin.SimplePlugin;
import me.javadebug.simplespigot.storage.Backend;
import me.javadebug.simplespigot.storage.StorageSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.UnaryOperator;

public class MySqlStorage implements Backend {
    private final StorageSettings storageSettings; // getters can return null values
    private final MySqlConnectionFactory connectionFactory;

    private final UnaryOperator<String> processor;

    private static final String CREATE_TABLE = "CREATE TABLE '{location}' ( id VARCHAR(36) NOT NULL,  json MEDIUMBLOB NOT NULL, PRIMARY KEY (id)";
    private static final String DELETE = "DELETE FROM '{location}' WHERE id=?";
    private static final String INSERT = "INSERT INTO '{location}' (id, json) VALUES(?, ?)";
    private static final String SELECT = "SELECT id, json FROM {location} WHERE id=?";

    public MySqlStorage(SimplePlugin plugin, String tableName) {
        this.storageSettings = plugin.getStorageSettings();
        this.connectionFactory = new MySqlConnectionFactory(this.storageSettings);
        this.processor = query -> query.replace("{location}", tableName + this.storageSettings.getPrefix());
    }

    @Override
    @SneakyThrows
    public JsonObject load(String id) {
        try (Connection connection = this.connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(this.processor.apply(SELECT))) {
                statement.setString(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next())
                        return new JsonParser().parse(resultSet.getString("json")).getAsJsonObject();
                }
            }
        }
        return null;
    }

    @Override
    @SneakyThrows
    public void save(String id, JsonObject json) {
        try (Connection connection = this.connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(this.processor.apply(DELETE))) {
                statement.setString(1, id);
            }
        }

    }

    @Override
    public void close() {
        this.connectionFactory.close();
    }
}
