import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

// Main Application
public class MusicDiscStoreApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // UI Components
        TabPane tabPane = new TabPane();

        // Tabs for each entity
        tabPane.getTabs().add(new DiscTab().getTab());
        tabPane.getTabs().add(new CustomerTab().getTab());
        tabPane.getTabs().add(new OrderTab().getTab());

        // Set up the stage
        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setTitle("Music Disc Store Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Database connection handler
class DatabaseConnection {
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/store";
    private static final String USER = "postgres";
    private static final String PASSWORD = "87502409";

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                System.out.println("Connected to the database.");
            } catch (SQLException e) {
                System.out.println("Failed to connect to the database.");
                e.printStackTrace();
            }
        }
        return connection;
    }
}

// Entity classes (Disc, Customer, Order)
class Disc {
    private int id;
    private String title;
    private String genre;
    private String releaseDate;
    private int stockQuantity;
    private double price;

    public Disc(int id, String title, String genre, String releaseDate, int stockQuantity, double price) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.stockQuantity = stockQuantity;
        this.price = price;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getReleaseDate() { return releaseDate; }
    public int getStockQuantity() { return stockQuantity; }
    public double getPrice() { return price; }
}

// DiscTab: Handles disc management
class DiscTab {
    public Tab getTab() {
        Tab tab = new Tab("Discs");
        tab.setClosable(false);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        // Buttons for different actions
        Button findButton = new Button("Find Disc");
        Button addButton = new Button("Add Disc");
        Button deleteButton = new Button("Delete Disc");

        // Create a ListView to display search results
        ListView<String> resultList = new ListView<>();

        layout.getChildren().addAll(findButton, addButton, deleteButton, resultList);

        // Add button listeners
        findButton.setOnAction(e -> showFindDiscForm(layout, resultList));
        addButton.setOnAction(e -> showAddDiscForm(layout));
        deleteButton.setOnAction(e -> showDeleteDiscForm(layout));

        tab.setContent(layout);
        return tab;
    }

    private void showFindDiscForm(VBox layout, ListView<String> resultList) {
        layout.getChildren().clear();

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        Button searchButton = new Button("Search Disc");
        searchButton.setOnAction(e -> {
            String title = titleField.getText();
            searchDisc(title, resultList); // Display results in the ListView
        });

        layout.getChildren().addAll(titleField, searchButton, resultList);
    }

    private void showAddDiscForm(VBox layout) {
        layout.getChildren().clear();

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField genreField = new TextField();
        genreField.setPromptText("Genre");
        TextField releaseDateField = new TextField();
        releaseDateField.setPromptText("Release Date");
        TextField stockField = new TextField();
        stockField.setPromptText("Stock Quantity");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        Button addButton = new Button("Add Disc");
        addButton.setOnAction(e -> {
            String title = titleField.getText();
            String genre = genreField.getText();
            String releaseDate = releaseDateField.getText();
            int stock = Integer.parseInt(stockField.getText());
            double price = Double.parseDouble(priceField.getText());
            addDisc(new Disc(0, title, genre, releaseDate, stock, price)); // Add disc to the database
        });

        layout.getChildren().addAll(titleField, genreField, releaseDateField, stockField, priceField, addButton);
    }

    private void showDeleteDiscForm(VBox layout) {
        layout.getChildren().clear();

        TextField idField = new TextField();
        idField.setPromptText("Enter Disc ID to delete");

        Button deleteButton = new Button("Delete Disc");
        deleteButton.setOnAction(e -> {
            int discId = Integer.parseInt(idField.getText());
            deleteDisc(discId);
        });

        layout.getChildren().addAll(idField, deleteButton);
    }

    private void addDisc(Disc disc) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO disc (title, genre, release_date, stock_quantity, price) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, disc.getTitle());
            statement.setString(2, disc.getGenre());
            statement.setString(3, disc.getReleaseDate());
            statement.setInt(4, disc.getStockQuantity());
            statement.setDouble(5, disc.getPrice());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteDisc(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM disc WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchDisc(String title, ListView<String> resultList) {
        String query = "SELECT * FROM disc WHERE title LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, "%" + title + "%");

            ResultSet resultSet = statement.executeQuery();
            resultList.getItems().clear(); // Clear the previous results
            while (resultSet.next()) {
                String discInfo = "Title: " + resultSet.getString("title") +
                        ", Genre: " + resultSet.getString("genre") +
                        ", Release Date: " + resultSet.getString("release_date") +
                        ", Stock: " + resultSet.getInt("stock_quantity") +
                        ", Price: " + resultSet.getDouble("price");
                resultList.getItems().add(discInfo); // Add each result to the ListView
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// CustomerTab: Handles customer management
class CustomerTab {
    public Tab getTab() {
        Tab tab = new Tab("Customers");
        tab.setClosable(false);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button addButton = new Button("Add Customer");
        Button deleteButton = new Button("Delete Customer");

        layout.getChildren().addAll(addButton, deleteButton);

        addButton.setOnAction(e -> showAddCustomerForm(layout));
        deleteButton.setOnAction(e -> showDeleteCustomerForm(layout));

        tab.setContent(layout);
        return tab;
    }

    private void showAddCustomerForm(VBox layout) {
        layout.getChildren().clear();

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        Button addButton = new Button("Add Customer");
        addButton.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            addCustomer(name, email);
        });

        layout.getChildren().addAll(nameField, emailField, addButton);
    }

    private void showDeleteCustomerForm(VBox layout) {
        layout.getChildren().clear();

        TextField idField = new TextField();
        idField.setPromptText("Enter Customer ID to delete");

        Button deleteButton = new Button("Delete Customer");
        deleteButton.setOnAction(e -> {
            int customerId = Integer.parseInt(idField.getText());
            deleteCustomer(customerId);
        });

        layout.getChildren().addAll(idField, deleteButton);
    }

    private void addCustomer(String name, String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO customers (name, email) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteCustomer(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM customers WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// OrderTab: Handles order management
class OrderTab {
    public Tab getTab() {
        Tab tab = new Tab("Orders");
        tab.setClosable(false);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button addButton = new Button("Add Order");
        Button deleteButton = new Button("Delete Order");

        layout.getChildren().addAll(addButton, deleteButton);

        addButton.setOnAction(e -> showAddOrderForm(layout));
        deleteButton.setOnAction(e -> showDeleteOrderForm(layout));

        tab.setContent(layout);
        return tab;
    }

    private void showAddOrderForm(VBox layout) {
        layout.getChildren().clear();

        TextField customerIdField = new TextField();
        customerIdField.setPromptText("Customer ID");
        TextField discIdField = new TextField();
        discIdField.setPromptText("Disc ID");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        Button addButton = new Button("Add Order");
        addButton.setOnAction(e -> {
            int customerId = Integer.parseInt(customerIdField.getText());
            int discId = Integer.parseInt(discIdField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            addOrder(customerId, discId, quantity);
        });

        layout.getChildren().addAll(customerIdField, discIdField, quantityField, addButton);
    }

    private void showDeleteOrderForm(VBox layout) {
        layout.getChildren().clear();

        TextField idField = new TextField();
        idField.setPromptText("Enter Order ID to delete");

        Button deleteButton = new Button("Delete Order");
        deleteButton.setOnAction(e -> {
            int orderId = Integer.parseInt(idField.getText());
            deleteOrder(orderId);
        });

        layout.getChildren().addAll(idField, deleteButton);
    }

    private void addOrder(int customerId, int discId, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO orders (customer_id, disc_id, quantity) VALUES (?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, customerId);
            statement.setInt(2, discId);
            statement.setInt(3, quantity);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteOrder(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM orders WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
