package service.shopping_cart;

import database.DbConnection;
import service.exceptions.InexistentItemException;
import service.exceptions.InvalidQuantityException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DbShoppingCartsService implements ShoppingCartsService {

    private Connection connection;

    public DbShoppingCartsService(String url, String user, String password) {
        try {
            this.connection = DbConnection.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Error acquiring connection!");
            System.exit(0);
        }
    }

    @Override
    public void addToCart(String clientId, String isbn) {
        try {
            this.connection.setAutoCommit(false);
            //CHECK IF ITEM ALREADY EXISTS IN CART
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM shopping_cart_items sci JOIN shopping_cart sc ON sci.id_shopping_cart=sc.id WHERE sc.client_id=? AND sci.id_book=?");

            statement.setString(1, clientId);
            statement.setString(2, isbn);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                //IF IT IS NOT IN CART, CHECK IF CLIENT HAS A SHOPPING CART
                PreparedStatement selectStatement = this.connection.prepareStatement("SELECT * FROM shopping_cart WHERE client_id=?");
                selectStatement.setString(1, clientId);
                ResultSet selectRs = selectStatement.executeQuery();

                boolean clientHasSC = selectRs.next();
                long shoppingCartId;
                if (!clientHasSC) {
                    //IF CLIENT DOES NOT HAVE SHOPPING CART, INSERT
                    statement = this.connection.prepareStatement("INSERT INTO shopping_cart (id,client_id) VALUES (?,?)");
                    shoppingCartId = System.currentTimeMillis();
                    statement.setLong(1, shoppingCartId);
                    statement.setString(2, clientId);
                    statement.executeUpdate();

                } else {
                    shoppingCartId = selectRs.getLong("id");
                }

                //INSERT ITEM INTO SHOPPING_CART_ITEMS - WE NEED THE
                statement = this.connection.prepareStatement("INSERT INTO shopping_cart_items (id_book, id_shopping_cart, quantity) VALUES (?,?,1)");
                statement.setString(1, isbn);
                statement.setLong(2, shoppingCartId);

                statement.executeUpdate();
               // throw new SQLException("something weird happened here, and insert in sci did not succeed.");

            } else {//IF ITEM IS IN CART, UPDATE IT'S QUANTITY WITH QUANTITY + 1
                int currentQuantity = resultSet.getInt("quantity");
                statement = this.connection.prepareStatement("UPDATE shopping_cart_items sci JOIN shopping_cart sc ON sc.id=sci.id_shopping_cart SET quantity=? WHERE sc.client_id=? AND sci.id_book=?");
                statement.setInt(1, currentQuantity + 1);
                statement.setString(2, clientId);
                statement.setString(3, isbn);
                statement.executeUpdate();
            }
            this.connection.commit();

        } catch (SQLException e) {
            System.out.println("db error" + e.getMessage());
            try {
                this.connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

        } finally {
            try {
                this.connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public int getQuantity(String clientId, String isbn) {
        try {
            //GET THE ITEM FROM THE SHOPPING CART
            PreparedStatement statement = this.connection.prepareStatement("SELECT sci.quantity FROM shopping_cart_items sci JOIN shopping_cart sc ON sci.id_shopping_cart=sc.id WHERE sci.id_book=? AND sc.client_id=?");
            statement.setString(1, isbn);
            statement.setString(2, clientId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                //item exists in shopping cart
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.out.println("db error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public void removeFromCart(String clientId, String isbn) throws InexistentItemException {
        try {
            //get the shopping cart id first, we need it for the DELETE statement
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM shopping_cart_items sci JOIN shopping_cart sc ON sci.id_shopping_cart=sc.id WHERE sci.id_book=? AND sc.client_id=?");
            statement.setString(1, isbn);
            statement.setString(2, clientId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                //IF ITEM EXISTS
                long shoppingCartId = resultSet.getLong("id_shopping_cart");
                PreparedStatement deleteStmt = this.connection.prepareStatement("DELETE FROM shopping_cart_items WHERE id_shopping_cart=? AND id_book=?");
                deleteStmt.setLong(1, shoppingCartId);
                deleteStmt.setString(2, isbn);
                deleteStmt.executeUpdate();
            } else {
                throw new InexistentItemException(String.format("No item with isbn %s exists in cart"));
            }
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
        }
    }

    @Override
    public void updateQuantity(String clientId, String isbn, int quantity) throws InvalidQuantityException, InexistentItemException {

        try {
            //VALIDATE QUANTITY
            if (quantity < 0) {
                throw new InvalidQuantityException("Negative value for quantity!");
            } else if (quantity == 0) {
                removeFromCart(clientId, isbn);
            }

            //GET THE ITEM
            PreparedStatement statement = this.connection.prepareStatement("SELECT sc.id FROM shopping_cart_items sci JOIN shopping_cart sc WHERE sc.client_id=? AND sci.id_book=?");
            statement.setString(1, clientId);
            statement.setString(2, isbn);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                //IF EXISTS, UPDATE the quantity.
                long shoppingCartId = resultSet.getLong(1);
                PreparedStatement updateStmt = this.connection.prepareStatement("UPDATE shopping_cart_items SET quantity=? WHERE id_shopping_cart=? AND id_book=?");
                updateStmt.setInt(1, quantity);
                updateStmt.setLong(2, shoppingCartId);
                updateStmt.setString(3, isbn);
                updateStmt.executeUpdate();
            } else {
                throw new InexistentItemException(String.format("No item with isbn %s in cart", isbn));
            }

        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
        }
    }

    @Override
    public void deleteShoppingCart(String clientId) {
        try {
            PreparedStatement statement = this.connection.prepareStatement("DELETE FROM SHOPPING_CART WHERE sc.client_id=?");
            statement.setString(1, clientId);
            statement.executeUpdate();
            statement.close();

        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Integer> getItemsMap(String clientId) {
        //isbn - quantity
        Map<String, Integer> itemsMap = new LinkedHashMap<>();

        try {
            PreparedStatement statement = this.connection.prepareStatement("SELECT sci.id_book, sci.quantity FROM shopping_cart_items sci JOIN shopping_cart sc ON sci.id_shopping_cart=sc.id WHERE sc.client_id=?");
            statement.setString(1, clientId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                itemsMap.put(rs.getString(1), rs.getInt(2));
            }
            return itemsMap;

        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
            return null;
        }

    }

}
