package edu.handong.week12.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookRequest(
        @NotBlank(message = "제목은 필수입니다.") String title,
        @NotBlank(message = "저자는 필수입니다.") String author,
        @Min(value = 0, message = "가격은 0 이상이어야 합니다.") int price
) {
}
