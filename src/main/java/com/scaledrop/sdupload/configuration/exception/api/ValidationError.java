package com.scaledrop.sdupload.configuration.exception.api;

public record ValidationError(
    String field,
    String error
) {

}
