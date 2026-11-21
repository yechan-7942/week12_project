package edu.handong.week12.book;

import edu.handong.week12.DTO.BookRequest;
import edu.handong.week12.DTO.BookResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Books", description = "도서 등록·조회·수정·삭제 API")
public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "도서 목록 조회", description = "page, size, sort로 페이징·정렬합니다. 예: sort=price,desc")
    public Page<BookResponse> list(@ParameterObject Pageable pageable) {
        return service.findAll(pageable).map(BookResponse::from);
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable Long id) {
        return BookResponse.from(service.findById(id));
    }

    @PostMapping//api/books 요청을 처리한다.
    @Operation(summary = "도서 등록", description = "제목·저자·가격으로 책을 등록하고 서버가 생성한 ID를 반환합니다.") //
    @ApiResponse(responseCode = "201", description = "도서 등록 성공")
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {//요청 JSON을 BookRequest DTO로 받고, @Valid로 제목·저자·가격의 유효성을 검사한다.
        Book saved = service.create(request);// Service에 저장을 요청하고 저장된 Book을 받는다
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(saved));// Book을 응답 DTO로 변환하고, 상태 코드 201과 함께 반환한다.
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
