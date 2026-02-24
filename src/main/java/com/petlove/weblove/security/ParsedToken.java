package com.petlove.weblove.security;

public record ParsedToken(
    long subjectId,
    JwtTokenType tokenType,
    String role
) {
}
