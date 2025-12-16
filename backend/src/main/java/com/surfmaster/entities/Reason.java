package com.surfmaster.entities;

public record Reason(
    ReasonType type,
    String description
) {}
