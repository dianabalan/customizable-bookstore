package service;

import service.books.InventoryService;
import service.books.MyInventoryService;
import service.shopping_cart.ShoppingCartsService;

public class BookStore {

    private InventoryService bookInventory;
    private ShoppingCartsService shoppingCartsService;

    public BookStore(InventoryService inventoryService, ShoppingCartsService shoppingCartsService) {
        this.bookInventory = inventoryService;
        this.shoppingCartsService = shoppingCartsService;
    }

    public InventoryService getBookInventory() {
        return bookInventory;
    }

    public ShoppingCartsService getShoppingCartsService() {
        return shoppingCartsService;
    }


}
