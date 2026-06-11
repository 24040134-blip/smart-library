import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

interface LibraryADT {
    void addBook(String title, String author);

    void searchBooks();

    void borrowBook(int bookId);

    void viewLatestHistory();
}

class Book {
    public static final String AVAILABLE = "AVAILABLE";
    public static final String BORROWED = "BORROWED";

    private final int id;
    private final String title;
    private final String author;
    private String status;

    public Book(int id, String title, String author, String status) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getFormattedId() {
        return String.format("%03d", id);
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return AVAILABLE.equals(status);
    }

    public void markBorrowed() {
        status = BORROWED;
    }

    public String toFileLine() {
        return getFormattedId() + "|" + title + "|" + author + "|" + status;
    }

    @Override
    public String toString() {
        return "[" + getFormattedId() + "] " + title + " by " + author + " (" + status + ")";
    }
}

class BookBST {
    private BookNode root;

    public void clear() {
        root = null;
    }

    public boolean insert(Book book) {
        if (root == null) {
            root = new BookNode(book);
            return true;
        }

        return insert(root, book);
    }

    private boolean insert(BookNode current, Book book) {
        if (book.getId() == current.book.getId()) {
            return false;
        }

        if (book.getId() < current.book.getId()) {
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

    public Book search(int bookId) {
        return search(root, bookId);
    }

    private Book search(BookNode current, int bookId) {
        if (current == null) {
            return null;
        }

        if (bookId == current.book.getId()) {
            return current.book;
        }

        if (bookId < current.book.getId()) {
            return search(current.left, bookId);
        }

        return search(current.right, bookId);
    }

    private static class BookNode {
        private final Book book;
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

class BookFileStorage {
    private final Path filePath;

    public BookFileStorage(Path filePath) {
        this.filePath = filePath;
    }

    public List<Book> loadBooks() throws IOException {
        ensureFileExists();

        List<Book> books = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                books.add(parseBook(line, i + 1));
            }
        }

        return books;
    }

    public void saveBooks(List<Book> books) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Book book : books) {
            lines.add(book.toFileLine());
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    public int nextBookId(List<Book> books) {
        int maxId = 0;
        for (Book book : books) {
            if (book.getId() > maxId) {
                maxId = book.getId();
            }
        }
        return maxId + 1;
    }

    public Book findById(List<Book> books, int bookId) {
        for (Book book : books) {
            if (book.getId() == bookId) {
                return book;
            }
        }
        return null;
    }

    private void ensureFileExists() throws IOException {
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
    }

    private Book parseBook(String line, int lineNumber) throws IOException {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 4) {
            throw new IOException("Invalid books.txt format on line " + lineNumber + ".");
        }

        int id;
        try {
            id = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException error) {
            throw new IOException("Invalid book number on line " + lineNumber + ".");
        }

        String title = parts[1].trim();
        String author = parts[2].trim();
        String status = parts[3].trim().toUpperCase();

        if (title.isEmpty() || author.isEmpty()) {
            throw new IOException("Missing title or author on line " + lineNumber + ".");
        }

        if (!Book.AVAILABLE.equals(status) && !Book.BORROWED.equals(status)) {
            throw new IOException("Invalid status on line " + lineNumber + ".");
        }

        return new Book(id, title, author, status);
    }
}

class SmartLibrary implements LibraryADT {
    private final BookBST catalogue = new BookBST();
    private final BorrowStack history = new BorrowStack();
    private final BookFileStorage storage = new BookFileStorage(locateBooksFile());

    public SmartLibrary() {
        try {
            refreshCatalogue(storage.loadBooks());
        } catch (IOException error) {
            System.out.println("Could not load books.txt: " + error.getMessage());
        }
    }

    @Override
    public void addBook(String title, String author) {
        try {
            List<Book> books = storage.loadBooks();
            int nextId = storage.nextBookId(books);
            Book book = new Book(nextId, cleanForFile(title), cleanForFile(author), Book.AVAILABLE);

            books.add(book);
            storage.saveBooks(books);
            refreshCatalogue(books);

            System.out.println("Book added: " + book);
        } catch (IOException error) {
            System.out.println("Could not add book: " + error.getMessage());
        }
    }

    @Override
    public void searchBooks() {
        try {
            List<Book> books = storage.loadBooks();
            refreshCatalogue(books);

            if (books.isEmpty()) {
                System.out.println("books.txt is empty.");
                return;
            }

            System.out.println("Books from books.txt:");
            System.out.printf("%-6s %-55s %-30s %s%n", "No.", "Title", "Author", "Status");
            System.out.println("----------------------------------------------------------------------------------------------------");
            for (Book book : books) {
                System.out.printf("%-6s %-55s %-30s %s%n",
                        book.getFormattedId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getStatus());
            }
        } catch (IOException error) {
            System.out.println("Could not read books.txt: " + error.getMessage());
        }
    }

    @Override
    public void borrowBook(int bookId) {
        try {
            List<Book> books = storage.loadBooks();
            refreshCatalogue(books);

            Book searched = catalogue.search(bookId);
            if (searched == null) {
                System.out.println("No book found with number " + String.format("%03d", bookId) + ".");
                return;
            }

            Book book = storage.findById(books, bookId);
            if (book == null) {
                System.out.println("No book found with number " + String.format("%03d", bookId) + ".");
                return;
            }

            if (!book.isAvailable()) {
                System.out.println("This book is already borrowed: " + book);
                return;
            }

            book.markBorrowed();
            storage.saveBooks(books);
            refreshCatalogue(books);
            history.push(new Book(book.getId(), book.getTitle(), book.getAuthor(), book.getStatus()));

            System.out.println("Borrowed: " + book);
        } catch (IOException error) {
            System.out.println("Could not borrow book: " + error.getMessage());
        }
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
        System.out.println("2. Search / View Books");
        System.out.println("3. Borrow Book");
        System.out.println("4. History");
        System.out.println("5. Exit");
    }

    private void handleChoice(int choice, Scanner scanner) {
        switch (choice) {
            case 1:
                String title = readRequiredText(scanner, "Enter Title: ");
                String author = readRequiredText(scanner, "Enter Author: ");
                addBook(title, author);
                break;
            case 2:
                searchBooks();
                break;
            case 3:
                int borrowId = readPositiveInt(scanner, "Enter book number to borrow: ");
                borrowBook(borrowId);
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

    private void refreshCatalogue(List<Book> books) {
        catalogue.clear();
        for (Book book : books) {
            catalogue.insert(book);
        }
    }

    private String cleanForFile(String value) {
        return value.replace("|", "-").trim();
    }

    private static Path locateBooksFile() {
        Path localFile = Path.of("books.txt");
        if (Files.exists(localFile)) {
            return localFile;
        }

        Path projectFile = Path.of("SmartLibraryIDEA", "books.txt");
        if (Files.exists(projectFile)) {
            return projectFile;
        }

        return localFile;
    }
}

public class Main {
    public static void main(String[] args) {
        new SmartLibrary().runMenu();
    }
}
