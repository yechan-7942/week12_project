package edu.handong.week12.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @Min(0) int price
) {
}
