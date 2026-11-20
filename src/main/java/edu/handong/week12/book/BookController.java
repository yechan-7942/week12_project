package edu.handong.week12.book;

import edu.handong.week12.DTO.BookRequest;
import edu.handong.week12.DTO.BookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<Book> list() {
        return service.findAll();
    } // TODO: Pageable

    @GetMapping("/{id}")
    public Book get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@RequestBody BookRequest request) {
        Book saved = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(saved));
    }
    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @RequestBody Book input) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    } // TODO: 204
}
