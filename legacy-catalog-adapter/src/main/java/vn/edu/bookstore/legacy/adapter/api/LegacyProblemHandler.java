package vn.edu.bookstore.legacy.adapter.api;

import jakarta.servlet.http.HttpServletRequest;
import vn.edu.bookstore.legacy.adapter.soap.LegacyCatalogException;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Chuyển lỗi → Problem Details (RFC 9457). */
@RestControllerAdvice
public class LegacyProblemHandler {

    // TODO(TH1-7): thêm @ExceptionHandler(LegacyCatalogException.class) ánh xạ:
    //   NOT_FOUND → 404 book-not-found; INVALID_REQUEST → 400 validation-failed;
    //   UPSTREAM_ERROR → 502, UPSTREAM_TIMEOUT → 504, UPSTREAM_UNAVAILABLE → 503 (upstream-unavailable);
    //   thêm thuộc tính "legacyCode" và instance = đường dẫn yêu cầu.

    @ExceptionHandler(LegacyCatalogException.class)
    ProblemDetail onLegacy(LegacyCatalogException e, HttpServletRequest request) {
        ProblemDetail p = switch (e.kind()) {
            case NOT_FOUND -> problem(HttpStatus.NOT_FOUND, ProblemTypes.BOOK_NOT_FOUND, "Khong tim thay sach", e.getMessage());
            case INVALID_REQUEST -> problem(HttpStatus.BAD_REQUEST, ProblemTypes.VALIDATION_FAILED, "Yeu cau khong hop le", e.getMessage());
            case UPSTREAM_ERROR -> problem(HttpStatus.BAD_GATEWAY, ProblemTypes.UPSTREAM_UNAVAILABLE, "He thong khong danh muc ke thua bao loi", e.getMessage());
            case UPSTREAM_TIMEOUT -> problem(HttpStatus.GATEWAY_TIMEOUT, ProblemTypes.UPSTREAM_UNAVAILABLE, "He thong danh muc ke thua phan hoi qua cham", "Het thoi gian cho BookCatalogService");
            case UPSTREAM_UNAVAILABLE -> problem(HttpStatus.SERVICE_UNAVAILABLE, ProblemTypes.UPSTREAM_UNAVAILABLE, "He thong danh muc ke thua khong truy cap duoc", "Khong ket noi duoc BookCatalogService");
        };
        if (e.legacyCode() != null) {
            p.setProperty("legacyCode", e.legacyCode());
        }
        p.setInstance(URI.create(request.getRequestURI()));
        return p;
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail onInvalid(InvalidRequestException e, HttpServletRequest request) {
        ProblemDetail p = problem(HttpStatus.BAD_REQUEST, ProblemTypes.VALIDATION_FAILED,
                "Yêu cầu không hợp lệ", e.getMessage());
        p.setInstance(URI.create(request.getRequestURI()));
        return p;
    }

    private static ProblemDetail problem(HttpStatus status, URI type, String title, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setType(type);
        p.setTitle(title);
        return p;
    }
}
