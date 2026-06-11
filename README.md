# Smart Library Project

This Java console program follows the PDF requirements:

- Uses a Binary Search Tree (BST) to store books by ISBN.
- Uses recursive BST search for finding books.
- Uses a Stack to show borrowing history in LIFO order.
- Uses `LibraryADT` as the public interface for the library system.
- Keeps BST and Stack internals private for information hiding.
- Reads 200 real books from `SmartLibraryIDEA/books.txt`.
- Handles invalid menu choices, non-integer book numbers, empty titles/authors, missing books, and books that are already borrowed.

## Project Location

The IntelliJ IDEA project is in:

```text
SmartLibraryIDEA
```

Main source file:

```text
SmartLibraryIDEA/src/Main.java
```

Book data file:

```text
SmartLibraryIDEA/books.txt
```

Each book uses this format:

```text
001|To Kill a Mockingbird|Harper Lee|AVAILABLE
```

## Run in IntelliJ IDEA

1. Open the `SmartLibraryIDEA` folder in IntelliJ IDEA.
2. Open `src/Main.java`.
3. Choose the run configuration `Run Smart Library`.
4. Click the green Run button.

## Run from Terminal

```powershell
cd SmartLibraryIDEA
javac -d out src\Main.java
java -cp out Main
```

## Menu

```text
1. Add Book
2. Search / View Books
3. Borrow Book
4. History
5. Exit
```

- `Search / View Books` reads `books.txt` and displays all books.
- `Add Book` asks only for title and author, then automatically adds the next book number.
- `Borrow Book` updates the selected book in `books.txt` from `AVAILABLE` to `BORROWED` and pushes it to the history stack.
