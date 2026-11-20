package edu.handong.week12.book;

import edu.handong.week12.DTO.BookRequest;
import edu.handong.week12.DTO.BookResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public Page<BookResponse> list(Pageable pageable) {
        return service.findAll(pageable).map(BookResponse::from);
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
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
            String message = exception.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .sorted()
                    .collect(Collectors.joining(" / "));
            return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException exception) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("INVALID_JSON", "요청 JSON의 형식과 필드 타입을 확인해 주세요."));
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(ResponseStatusException exception) {
            return ResponseEntity.status(exception.getStatusCode())
                    .body(new ErrorResponse("BOOK_NOT_FOUND", exception.getReason()));
        }
    }
}
