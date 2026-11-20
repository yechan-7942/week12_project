package edu.handong.week12.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:book-post-test;MODE=MariaDB;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class BookControllerTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired BookRepository repository;

    @Test
    void readsAndUpdatesBookThroughDtosAndDeletesBook() throws Exception {
        Book book = repository.save(new Book("Original", "Author", 10000));
        Long id = book.getId();

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":%d,"title":"Original","author":"Author","price":10000}]
                        """.formatted(id), true));

        mvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"id":%d,"title":"Original","author":"Author","price":10000}
                        """.formatted(id), true));

        mvc.perform(put("/api/books/{id}", id)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"id":99999,"title":"Updated","author":"New Author","price":20000}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"id":%d,"title":"Updated","author":"New Author","price":20000}
                        """.formatted(id), true));

        Book updated = repository.findById(id).orElseThrow();
        assertEquals("Updated", updated.getTitle());
        assertEquals("New Author", updated.getAuthor());
        assertEquals(20000, updated.getPrice());

        mvc.perform(delete("/api/books/{id}", id))
                .andExpect(status().is2xxSuccessful());
        assertFalse(repository.existsById(id));
    }

    @Test
    void createReturns201WithSavedBookAndServerGeneratedId() throws Exception {
        String body = mvc.perform(post("/api/books")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"id":99999,"title":"Clean Code","author":"Martin","price":35000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Martin"))
                .andExpect(jsonPath("$.price").value(35000))
                .andReturn().getResponse().getContentAsString();

        long id = mapper.readTree(body).get("id").asLong();
        assertNotEquals(99999L, id);
        Book saved = repository.findById(id).orElseThrow();
        assertEquals("Clean Code", saved.getTitle());
        assertEquals("Martin", saved.getAuthor());
        assertEquals(35000, saved.getPrice());
    }
}
