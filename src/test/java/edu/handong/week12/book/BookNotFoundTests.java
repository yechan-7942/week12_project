package edu.handong.week12.book;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:book-not-found-test;MODE=MariaDB;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class BookNotFoundTests {
    @Autowired MockMvc mvc;
    @Autowired BookRepository repository;

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void missingBookReturns404WithoutChangingExistingBook(String method) throws Exception {
        Book existing = repository.save(new Book("Original", "Author", 100));
        long count = repository.count();
        assertFalse(repository.existsById(99999L));

        mvc.perform(request(HttpMethod.valueOf(method), "/api/books/99999")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"author\":\"New Author\",\"price\":200}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("책을 찾을 수 없습니다. ID: 99999"));

        assertEquals(count, repository.count());
        Book unchanged = repository.findById(existing.getId()).orElseThrow();
        assertEquals("Original", unchanged.getTitle());
        assertEquals("Author", unchanged.getAuthor());
        assertEquals(100, unchanged.getPrice());
    }
}
