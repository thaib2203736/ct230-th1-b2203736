package vn.edu.bookstore.catalog.api;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.edu.bookstore.catalog.api.generated.BooksApi;
import vn.edu.bookstore.catalog.api.model.Book;
import vn.edu.bookstore.catalog.api.model.BookCreateRequest;
import vn.edu.bookstore.catalog.api.model.BookPage;
import vn.edu.bookstore.catalog.api.model.BookUpdateRequest;
import vn.edu.bookstore.catalog.domain.BookEntity;
import vn.edu.bookstore.catalog.service.BookService;

/** Hiện thực interface BooksApi sinh từ openapi.yaml (API-first). */
@RestController
public class BookController implements BooksApi {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<BookPage> listBooks(String q, Integer page, Integer size, String sort) {
        //   đổi sort "title,asc" | "price,desc" | "publishedYear,asc" thành Sort
        //   (price → thuộc tính priceAmount), tạo PageRequest.of(page, size, sort),
        //   gọi service.list(q, pageable) và trả BookMapper.toPage(...)
        return ResponseEntity.ok(BookMapper.toPage(service.list(q, PageRequest.of(page, size, toSort(sort)))));
    }

    static Sort toSort(String sort) {
        String[] parts = sort.split(",");
        String property = switch (parts[0]) {
            case "price" -> "priceAmount";
            case "publishedYear" -> "publishedYear";
            default -> "title";
        };
        Sort.Direction dir = parts.length > 1 && parts[1].equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, property).and(Sort.by("isbn"));
    }

    @Override
    public ResponseEntity<Book> getBook(String isbn, String ifNoneMatch) {
        BookEntity e = service.get(isbn);
        // TODO(TH2-5): tính ETag = BookMapper.etag(e); nếu If-None-Match khớp → 304 Not Modified;
        //   ngược lại trả 200 kèm header ETag
        return ResponseEntity.ok(BookMapper.toApi(e));
    }

    @Override
    public ResponseEntity<Book> createBook(BookCreateRequest request) {
        BookEntity e = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{isbn}").buildAndExpand(e.getIsbn()).toUri();
        return ResponseEntity.created(location).eTag(BookMapper.etag(e)).body(BookMapper.toApi(e));
    }

    @Override
    public ResponseEntity<Book> updateBook(String isbn, BookUpdateRequest request, String ifMatch) {
        // TODO(TH2-5): thiếu If-Match → PreconditionRequiredException (428);
        //   đọc số phiên bản trong If-Match ("3" → 3), gọi service.update(...), trả 200 kèm ETag mới
        throw new UnsupportedOperationException("TODO(TH2-5)");
    }

    @Override
    public ResponseEntity<Void> deleteBook(String isbn) {
        service.delete(isbn);
        return ResponseEntity.noContent().build();
    }
}
