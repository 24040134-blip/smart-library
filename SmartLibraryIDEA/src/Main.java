import java.util.Scanner;
import java.util.Stack;

interface LibraryADT {
    void addBook(int isbn, String title, String author);

    void borrowBook(int isbn);

    void viewLatestHistory();

    void searchBook(int isbn);
}

class Book {
    private final int isbn;
    private final String title;
    private final String author;

    public Book(int isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public int getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "[ISBN: " + isbn + "] " + title + " by " + author;
    }
}

class BookBST {
    private BookNode root;

    public boolean insert(int isbn, String title, String author) {
        if (root == null) {
            root = new BookNode(new Book(isbn, title, author));
            return true;
        }

        return insert(root, new Book(isbn, title, author));
    }

    private boolean insert(BookNode current, Book book) {
        if (book.getIsbn() == current.book.getIsbn()) {
            return false;
        }

        if (book.getIsbn() < current.book.getIsbn()) {
            if (current.left == null) {
                current.left = new BookNode(book);
                return true;
            }
            return insert(current.left, book);
        }

        if (current.right == null) {
            current.right = new BookNode(book);
            return true;
        }
        return insert(current.right, book);
    }

    public Book search(int isbn) {
        return search(root, isbn);
    }

    private Book search(BookNode current, int isbn) {
        if (current == null) {
            return null;
        }

        if (isbn == current.book.getIsbn()) {
            return current.book;
        }

        if (isbn < current.book.getIsbn()) {
            return search(current.left, isbn);
        }

        return search(current.right, isbn);
    }

    public Book remove(int isbn) {
        Book found = search(isbn);
        if (found == null) {
            return null;
        }

        root = remove(root, isbn);
        return found;
    }

    private BookNode remove(BookNode current, int isbn) {
        if (current == null) {
            return null;
        }

        if (isbn < current.book.getIsbn()) {
            current.left = remove(current.left, isbn);
            return current;
        }

        if (isbn > current.book.getIsbn()) {
            current.right = remove(current.right, isbn);
            return current;
        }

        if (current.left == null) {
            return current.right;
        }

        if (current.right == null) {
            return current.left;
        }

        BookNode successor = findSmallest(current.right);
        current.book = successor.book;
        current.right = remove(current.right, successor.book.getIsbn());
        return current;
    }

    private BookNode findSmallest(BookNode current) {
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    private static class BookNode {
        private Book book;
        private BookNode left;
        private BookNode right;

        private BookNode(Book book) {
            this.book = book;
        }
    }
}

class BorrowStack {
    private final Stack<Book> stack = new Stack<>();

    public void push(Book book) {
        stack.push(book);
    }

    public void show() {
        if (stack.isEmpty()) {
            System.out.println("History is empty.");
            return;
        }

        System.out.println("Borrowing History (Most Recent First):");
        int position = 1;
        for (int i = stack.size() - 1; i >= 0; i--) {
            System.out.println(position + ". " + stack.get(i));
            position++;
        }
    }
}

class SmartLibrary implements LibraryADT {
    private final BookBST catalogue = new BookBST();
    private final BorrowStack history = new BorrowStack();

    @Override
    public void addBook(int isbn, String title, String author) {
        boolean added = catalogue.insert(isbn, title, author);
        if (added) {
            System.out.println("Book added: " + title);
        } else {
            System.out.println("A book with this ISBN already exists.");
        }
    }

    @Override
    public void searchBook(int isbn) {
        Book book = catalogue.search(isbn);
        if (book == null) {
            System.out.println("No book found with ISBN " + isbn + ".");
            return;
        }

        System.out.println("Found: " + book);
    }

    @Override
    public void borrowBook(int isbn) {
        Book book = catalogue.remove(isbn);
        if (book == null) {
            System.out.println("Book not in catalogue.");
            return;
        }

        history.push(book);
        System.out.println("Borrowed: " + book.getTitle());
    }

    @Override
    public void viewLatestHistory() {
        history.show();
    }

    public void runMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readPositiveInt(scanner, "Choice: ");

            if (choice == 5) {
                running = false;
            } else {
                handleChoice(choice, scanner);
            }
        }

        System.out.println("Goodbye.");
        scanner.close();
    }

    private void printMenu() {
        System.out.println();
        System.out.println("--- SmartLibrary Menu ---");
        System.out.println("1. Add Book");
        System.out.println("2. Search (BST)");
        System.out.println("3. Borrow (Stack)");
        System.out.println("4. History");
        System.out.println("5. Exit");
    }

    private void handleChoice(int choice, Scanner scanner) {
        switch (choice) {
            case 1:
                int isbn = readPositiveInt(scanner, "Enter ISBN: ");
                String title = readRequiredText(scanner, "Enter Title: ");
                String author = readRequiredText(scanner, "Enter Author: ");
                addBook(isbn, title, author);
                break;
            case 2:
                int searchIsbn = readPositiveInt(scanner, "Enter ISBN to search: ");
                searchBook(searchIsbn);
                break;
            case 3:
                int borrowIsbn = readPositiveInt(scanner, "Enter ISBN to borrow: ");
                borrowBook(borrowIsbn);
                break;
            case 4:
                viewLatestHistory();
                break;
            default:
                System.out.println("Invalid option. Please choose 1-5.");
                break;
        }
    }

    private int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Please enter a number.");
                continue;
            }

            try {
                int value = Integer.parseInt(input);
                if (value <= 0) {
                    System.out.println("Please enter a positive number.");
                    continue;
                }
                return value;
            } catch (NumberFormatException error) {
                System.out.println("Invalid number. Please enter digits only.");
            }
        }
    }

    private String readRequiredText(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("This field cannot be empty.");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        new SmartLibrary().runMenu();
    }
}
