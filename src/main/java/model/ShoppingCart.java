package model;

import service.exceptions.InexistentItemException;
import service.exceptions.InvalidQuantityException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ShoppingCart {

    private long id;
    //key - isbn book; value -
    private Map<String, Integer> items = new LinkedHashMap<>();

    public ShoppingCart() {
        //assign a unique value to shopping cart id
        id = System.currentTimeMillis();
    }

    public void add(String isbn) {
        if (this.items.containsKey(isbn)) {
            try {
                updateQuantity(isbn, this.items.get(isbn) + 1);
            } catch (InvalidQuantityException | InexistentItemException e) {
                //will never happen here
            }
        } else {
            this.items.put(isbn, 1);
        }
    }

    public void updateQuantity(String isbn, int quantity) throws InvalidQuantityException, InexistentItemException {
        if (exists(isbn)) {
            if (quantity == 0) {
                remove(isbn);
            } else if (quantity < 0) {
                throw new InvalidQuantityException(String.format("Negative quantity for shopping cart item with isbn: %s", isbn));
            } else {
                items.put(isbn, quantity);
            }
        }
    }

    public void remove(String isbn) throws InexistentItemException {
        if (!this.items.containsKey(isbn)) {
            throw new InexistentItemException(String.format("No book with isbn %s in cart", isbn));
        }
        items.remove(isbn);
    }

    public boolean exists(String isbn) {
        return items.get(isbn) == null ? false : true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCart that = (ShoppingCart) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Map<String, Integer> getItems() {
        return items;
    }

}
