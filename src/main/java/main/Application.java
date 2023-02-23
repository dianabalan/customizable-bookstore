package main;

import model.Book;
import service.BookStore;
import service.books.DbInventoryService;
import service.books.InventoryService;
import service.books.MyInventoryService;
import service.exceptions.BookAlreadyExistsException;
import service.exceptions.InexistentBookException;
import service.exceptions.InexistentItemException;
import service.exceptions.InsufficientStockException;
import service.exceptions.InvalidQuantityException;
import service.shopping_cart.DbShoppingCartsService;
import service.shopping_cart.MyShoppingCartsService;
import service.shopping_cart.ShoppingCartsService;

import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

public class Application {

    public static void main(String[] args) {

        String storageType = args[0];
        BookStore bookStore = null;

        switch (storageType) {
            case "persistent":
                System.out.println("Initilizing persistent book store.");
                String url = args[1];
                String user = args[2];
                String password = args[3];

                bookStore = new BookStore(new DbInventoryService(url, user, password), new DbShoppingCartsService(url, user, password));
                break;
            case "non-persistent":
                System.out.println("Initilizing non-persistent book store.");
                bookStore = new BookStore(new MyInventoryService(), new MyShoppingCartsService());
                break;
            default:
                System.out.println("Invalid option for storage type");
                System.exit(0);
        }

        Scanner scanner = new Scanner(System.in);
        int option;

        do {
            try {
                System.out.println("*********MAIN MENU*******");
                System.out.println("0. Exit");
                System.out.println("1. Admin");
                System.out.println("2. Client");
                System.out.println("**********************");

                option = Integer.parseInt(scanner.nextLine());

                switch (option) {
                    case 0:
                        System.exit(0);
                    case 1:
                        boolean backToMainMenu;
                        do {
                            displayAdminMenu();
                            backToMainMenu = processAdminOption(scanner, bookStore.getBookInventory());
                        } while (!backToMainMenu);
                        break;
                    case 2:
                        System.out.println("What is your id?");
                        String clientId = scanner.nextLine();
                        do {
                            displayCustomerMenu();
                            backToMainMenu = processClientOption(scanner, bookStore.getBookInventory(), bookStore.getShoppingCartsService(), clientId);
                        } while (!backToMainMenu);
                        break;
                    default:
                        System.out.println("Invalid option. Choose from 0-2");
                }

            } catch (NumberFormatException e) {
                System.out.println("Input numeric value for option!");
            }

        } while (true);

    }

    private static boolean processClientOption(Scanner scanner, InventoryService bookInventory, ShoppingCartsService shoppingCartsService, String clientId) {
        String title;
        String isbn;

        System.out.println("Input option: ");
        int option = Integer.parseInt(scanner.nextLine());

        try {
            switch (option) {
                case 0:
                    return true;
                case 1:
                    //add to shopping cart
                    System.out.println("Input isbn: ");
                    isbn = scanner.nextLine();
                    //this throws exception if does not exist
                    Book book = bookInventory.searchByIsbn(isbn);

                    //see if item exists in cart and get its quantity
                    int quantity = shoppingCartsService.getQuantity(clientId, isbn);
                    if (quantity == 0) {
                        //user is trying to add to cart an item that already exists
                        //check stock for quantity+1
                        book.checkStock(-(quantity + 1));
                    } else {
                        //item does not exist in cart, let's check if there is enough stock for 1 book
                        book.checkStock(-1);
                    }

                    //ALTERNATIVE - TERNARY OPERATOR
                    //book.checkStock(quantity == 0 ? -(quantity + 1) : -1);

                    shoppingCartsService.addToCart(clientId, isbn);

                    System.out.println("Item successfully added!");

                    break;
                case 2:
                    //remove item
                    System.out.println("Which item?");
                    displayShoppingCartItems(bookInventory, shoppingCartsService, clientId);

                    System.out.println("Input isbn for book you wish to delete from cart.");
                    isbn = scanner.nextLine();

                    shoppingCartsService.removeFromCart(clientId, isbn);
                    System.out.println("Item successfully removed!");

                    break;

                case 3:
                    System.out.println("Which item?");
                    displayShoppingCartItems(bookInventory, shoppingCartsService, clientId);

                    System.out.println("Input isbn for book you wish to delete from cart.");
                    isbn = scanner.nextLine();

                    System.out.println("Input new quantity");
                    quantity = Integer.parseInt(scanner.nextLine());

                    //check if we have enough stock
                    book = bookInventory.searchByIsbn(isbn);
                    book.checkStock(-quantity);

                    //update quantity
                    shoppingCartsService.updateQuantity(clientId, isbn, quantity);
                    System.out.println("Item successfully updated!");

                    break;

                case 4:
                    //search by title
                    System.out.println("Input title: ");
                    title = scanner.nextLine();

                    System.out.println(bookInventory.searchByTitle(title));
                    break;
                case 5:
                    //display all
                    bookInventory.displayAll();
                    break;
                case 6:
                    //display items in shopping cart
                    displayShoppingCartItems(bookInventory, shoppingCartsService, clientId);
                    break;

                case 7:
                    //delete cart
                    shoppingCartsService.deleteShoppingCart(clientId);
                    System.out.println("Cart successfully deleted!");
                    break;

            }

        } catch (InexistentBookException e) {
            System.out.println("Warning: " + e.getMessage());
            System.out.println("Choose from: ");
            bookInventory.displayAll();
        } catch (InvalidQuantityException | InsufficientStockException | InexistentItemException e) {
            System.out.println("Warning: " + e.getMessage());
        }

        return false;
    }

    private static void displayShoppingCartItems(InventoryService bookInventory, ShoppingCartsService shoppingCartsService, String clientId) throws InexistentBookException {
        for (Map.Entry<String, Integer> entry : shoppingCartsService.getItemsMap(clientId).entrySet()) {
            System.out.println(bookInventory.searchByIsbn(entry.getKey()) + ", quantity: " + entry.getValue());
        }
    }

    private static boolean processAdminOption(Scanner scanner, InventoryService bookInventory) {

        String title;
        int stock;
        double price;
        try {
            System.out.println("Input option: ");
            int option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 0:
                    return true;
                case 1:
                    //add book
                    System.out.println("Input isbn:");
                    String isbn = scanner.nextLine();

                    System.out.println("Input title: ");
                    title = scanner.nextLine();

                    System.out.println("Input stock:");
                    stock = Integer.parseInt(scanner.nextLine());

                    System.out.println("Input price: ");
                    price = Double.parseDouble(scanner.nextLine());

                    System.out.println("Input authors, separated by comma (,) :");
                    String authors = scanner.nextLine();

                    bookInventory.add(new Book(isbn, title, Collections.emptyList(), price, stock));

                    break;

                case 2:
                    //search by title
                    System.out.println("Input title: ");
                    title = scanner.nextLine();

                    System.out.println("Found: " + bookInventory.searchByTitle(title));
                    break;
                case 3:
                    //remove by title
                    System.out.println("Input title: ");
                    title = scanner.nextLine();

                    bookInventory.remove(title);
                    break;

                case 4:
                    System.out.println("Input title: ");
                    title = scanner.nextLine();
                    System.out.println("Input new stock: ");
                    stock = Integer.parseInt(scanner.nextLine());

                    bookInventory.updateStock(title, stock);
                    break;
                case 5:
                    System.out.println("Input title: ");
                    title = scanner.nextLine();

                    System.out.println("Input price: ");
                    price = Double.parseDouble(scanner.nextLine());

                    bookInventory.updatePrice(title, price);
                    break;
                case 6:
                case 7:
                    bookInventory.displayAll();
                    break;
                default:
                    System.out.println("Invalid option");

            }

        } catch (BookAlreadyExistsException | InsufficientStockException | InexistentBookException e) {
            System.out.println("Warning: " + e.getMessage());
        }
        return false;
    }


    private static void displayAdminMenu() {
        System.out.println("**********ADMIN MENU***********");
        System.out.println("0. Back to MAIN MENU");
        System.out.println("1. Add book");
        System.out.println("2. Search by title");
        System.out.println("3. Remove by title");
        System.out.println("4. Update stock");
        System.out.println("5. Update price");
        System.out.println("6. Display in alphabetical title order");
        System.out.println("7. Display all");
        System.out.println("*************************");
    }

    private static void displayCustomerMenu() {
        System.out.println("**********CUSTOMER MENU***********");

        System.out.println("0. Back to MAIN MENU");
        System.out.println("1. Add to cart");
        System.out.println("2. Remove from cart");
        System.out.println("3. Update item quantity");
        System.out.println("4. Search by title");
        System.out.println("5. Display all");
        System.out.println("6. Show cart");
        System.out.println("7. Delete cart");
        System.out.println("**********************************");
    }
}
