package com.shortener.rest.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EncodeRequest {

    @Size(max = 4)
    private String prefix;

    @NotNull
    private String url;

}
