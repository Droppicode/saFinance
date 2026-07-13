package com.safinance.core.domain;

/**
 * Visitor pattern to dispatch actions based on the user's concrete type
 * without exposing the Domain layer to the View or Infra layers.
 */
public interface UserVisitor<T> {
    T visitAdmin(AdminUser admin);
    T visitRegular(RegularUser regular);
}
