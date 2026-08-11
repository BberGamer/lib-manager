package model;

import java.time.LocalDateTime;

public class BookAuthor {
    private int bookId;
    private int authorId;
    private String role; // PRIMARY, CO_AUTHOR
    private LocalDateTime createdAt;

    private Book book;
    private Author author;

    public BookAuthor() {}

    public BookAuthor(int bookId, int authorId, String role) {
        this.bookId = bookId;
        this.authorId = authorId;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "BookAuthor{" +
                "bookId=" + bookId +
                ", authorId=" + authorId +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
