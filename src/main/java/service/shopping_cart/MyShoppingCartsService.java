package service.shopping_cart;

import service.exceptions.InexistentItemException;
import service.exceptions.InvalidQuantityException;
import model.ShoppingCart;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyShoppingCartsService implements ShoppingCartsService {

    //clientId,shoppingCart
    private Map<String, ShoppingCart> shoppingCarts = new LinkedHashMap<>();

    public void addToCart(String clientId, String isbn) {
        if (!this.shoppingCarts.containsKey(clientId)) {
            this.shoppingCarts.put(clientId, new ShoppingCart());
        }

        ShoppingCart shoppingCart = this.shoppingCarts.get(clientId);
        shoppingCart.add(isbn);
    }


    public void removeFromCart(String clientId, String isbn) throws InexistentItemException {
        this.shoppingCarts.get(clientId).remove(isbn);
    }

    //ADDED IN NEW VERSION
    @Override
    public int getQuantity(String clientId, String isbn) {
        return this.shoppingCarts.get(clientId).getItems().get(isbn);
    }

    public Map<String, Integer> getItemsMap(String clientId) {
        return this.shoppingCarts.get(clientId).getItems();
    }

    public void updateQuantity(String clientId, String isbn, int quantity) throws InvalidQuantityException, InexistentItemException {
        this.shoppingCarts.get(clientId).updateQuantity(isbn, quantity);
    }

    @Override
    public void deleteShoppingCart(String clientId) {
        this.shoppingCarts.remove(clientId);
    }
}
