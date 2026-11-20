package edu.handong.week12.book;

import edu.handong.week12.DTO.BookRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
    return repository.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "책을 찾을 수 없습니다. ID: " + id));
  }

  public Book create(BookRequest request) {
    Book book = new Book(request.title(), request.author(), request.price());
    return repository.save(book);
  }

  public Book update(Long id, BookRequest request) {
    Book book = findById(id);
    book.update(request.title(), request.author(), request.price());
    return repository.save(book);
  }

  public void delete(Long id) {
    repository.delete(findById(id));
  }
}
