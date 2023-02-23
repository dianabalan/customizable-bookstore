package service.shopping_cart;

import service.exceptions.InexistentItemException;
import service.exceptions.InvalidQuantityException;

import java.util.Map;

public interface ShoppingCartsService {

    void addToCart(String clientId, String book);

    void removeFromCart(String clientId, String isbn) throws InexistentItemException;

    int getQuantity(String clientId, String isbn);

    Map<String, Integer> getItemsMap(String clientId);

    void updateQuantity(String clientId, String isbn, int quantity) throws InvalidQuantityException, InexistentItemException;

    void deleteShoppingCart(String clientId);
}
