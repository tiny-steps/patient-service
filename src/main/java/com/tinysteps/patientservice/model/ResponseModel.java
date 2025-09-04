package com.tinysteps.patientservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModel<T> {

    private HttpStatus status;
    private String message;
    private T data;
    private String error;
    private String path;

    @Builder.Default
    private LocalDateTime Instant = LocalDateTime.now();

    // Success response with data
    public static <T> ResponseModel<T> success(T data, String message) {
        return ResponseModel.<T>builder()
                .status(HttpStatus.OK)
                .message(message)
                .data(data)
                .build();
    }

    // Success response without data
    public static <T> ResponseModel<T> success(String message) {
        return ResponseModel.<T>builder()
                .status(HttpStatus.OK)
                .message(message)
                .build();
    }

    // Created response
    public static <T> ResponseModel<T> created(T data, String message) {
        return ResponseModel.<T>builder()
                .status(HttpStatus.CREATED)
                .message(message)
                .data(data)
                .build();
    }

    // Error response
    public static <T> ResponseModel<T> error(HttpStatus status, String message, String error) {
        return ResponseModel.<T>builder()
                .status(status)
                .message(message)
                .error(error)
                .build();
    }

    // Error response with path
    public static <T> ResponseModel<T> error(HttpStatus status, String message, String error, String path) {
        return ResponseModel.<T>builder()
                .status(status)
                .message(message)
                .error(error)
                .path(path)
                .build();
    }
}
