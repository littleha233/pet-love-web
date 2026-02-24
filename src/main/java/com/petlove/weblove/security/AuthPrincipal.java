package com.petlove.weblove.security;

public record AuthPrincipal(
    long id,
    AuthPrincipalType type,
    String role
) {
}
