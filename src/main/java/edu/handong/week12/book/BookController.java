package edu.handong.week12.book;

import edu.handong.week12.DTO.BookRequest;
import edu.handong.week12.DTO.BookResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<BookResponse> list() {
        return service.findAll().stream().map(BookResponse::from).toList();
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable Long id) {
        return BookResponse.from(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        Book saved = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(saved));
    }

    @PutMapping("/{id}")
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return BookResponse.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record ErrorResponse(String code, String message) {
    }

    @RestControllerAdvice
    public static class ErrorHandler {
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(ResponseStatusException exception) {
            return ResponseEntity.status(exception.getStatusCode())
                    .body(new ErrorResponse("BOOK_NOT_FOUND", exception.getReason()));
        }
    }
}
