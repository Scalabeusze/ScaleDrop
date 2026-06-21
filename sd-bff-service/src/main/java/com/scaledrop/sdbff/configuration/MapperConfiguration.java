package com.scaledrop.sdbff.configuration;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.ReportingPolicy.ERROR;
import static org.mapstruct.ReportingPolicy.WARN;

import org.mapstruct.MapperConfig;

@MapperConfig(
    unmappedTargetPolicy = ERROR,
    unmappedSourcePolicy = WARN,
    componentModel = "spring",
    injectionStrategy = CONSTRUCTOR)
public class MapperConfiguration {}
