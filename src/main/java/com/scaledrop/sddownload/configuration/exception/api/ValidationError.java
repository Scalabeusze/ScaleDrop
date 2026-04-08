package com.scaledrop.sddownload.configuration.exception.api;

public record ValidationError(
    String field,
    String error
) {

}
