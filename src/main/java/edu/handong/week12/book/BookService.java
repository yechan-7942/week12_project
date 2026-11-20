package edu.handong.week12.book;

import edu.handong.week12.DTO.BookRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {
  private final BookRepository repository;

  public BookService(BookRepository repository) {
    this.repository = repository;
  }

  public List<Book> findAll() {
    return repository.findAll();
  } // TODO STEP 6: Pagination/Sorting

  public Book findById(Long id) {
    return repository.findById(id).orElseThrow();
  } // TODO STEP 5: 404

  public Book create(BookRequest request) {
    Book book = new Book(request.title(), request.author(), request.price());
    return repository.save(book);
  }

  public Book update(Long id, Book input) {
    Book book = findById(id);
    book.update(input.getTitle(), input.getAuthor(), input.getPrice());
    return repository.save(book);
  }

  public void delete(Long id) {
    repository.deleteById(id);
  } // TODO: 404 if absent
}
