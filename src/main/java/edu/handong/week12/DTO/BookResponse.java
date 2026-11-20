package edu.handong.week12.DTO;

import edu.handong.week12.book.Book;

public record BookResponse(
        Long id, String title, String author, int price
) {
    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getPrice());
    }
}
