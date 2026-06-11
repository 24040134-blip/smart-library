# Smart Library Project

This Java console program follows the PDF requirements:

- Uses a Binary Search Tree (BST) to store books by ISBN.
- Uses recursive BST search for finding books.
- Uses a Stack to show borrowing history in LIFO order.
- Uses `LibraryADT` as the public interface for the library system.
- Keeps BST and Stack internals private for information hiding.
- Handles invalid menu choices, non-integer ISBNs, duplicate ISBNs, empty titles/authors, missing search results, and borrowing books that are not available.

## Run

```powershell
javac Main.java
java Main
```

## Menu

```text
1. Add Book
2. Search (BST)
3. Borrow (Stack)
4. History
5. Exit
```

Borrowing a book removes it from the catalogue and pushes it onto the borrowing history stack.
