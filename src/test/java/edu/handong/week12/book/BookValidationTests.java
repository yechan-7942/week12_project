package edu.handong.week12.book;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:book-validation-test;MODE=MariaDB;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class BookValidationTests {
    @Autowired MockMvc mvc;
    @Autowired BookRepository repository;

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"title\":\"\",\"author\":\"Author\",\"price\":100}",
            "{\"title\":\"   \",\"author\":\"Author\",\"price\":100}",
            "{\"author\":\"Author\",\"price\":100}",
            "{\"title\":\"Book\",\"author\":\"\",\"price\":100}",
            "{\"title\":\"Book\",\"author\":\"   \",\"price\":100}",
            "{\"title\":\"Book\",\"price\":100}",
            "{\"title\":\"Book\",\"author\":\"Author\",\"price\":-1}"
    })
    void invalidRequestReturns400WithoutChangingBooks(String json) throws Exception {
        Book book = repository.save(new Book("Original", "Author", 100));
        long count = repository.count();

        mvc.perform(post("/api/books").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/books/{id}", book.getId()).contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        assertEquals(count, repository.count());
        Book unchanged = repository.findById(book.getId()).orElseThrow();
        assertEquals("Original", unchanged.getTitle());
        assertEquals("Author", unchanged.getAuthor());
        assertEquals(100, unchanged.getPrice());
    }

    @Test
    void zeroPriceIsAcceptedForCreateAndUpdate() throws Exception {
        mvc.perform(post("/api/books").contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Free Book\",\"author\":\"Author\",\"price\":0}"))
                .andExpect(status().isCreated());
        Book book = repository.save(new Book("Original", "Author", 100));
        mvc.perform(put("/api/books/{id}", book.getId()).contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Free Book\",\"author\":\"Author\",\"price\":0}"))
                .andExpect(status().isOk());
        assertEquals(0, repository.findById(book.getId()).orElseThrow().getPrice());
    }
}
