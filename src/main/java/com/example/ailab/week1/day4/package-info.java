/**
 * Day 4 — {@code @Transactional} is a proxy, not a compiler feature.
 *
 * <p>{@link com.example.ailab.week1.day4.AccountService} has no interface, so
 * Spring subclasses it with CGLIB (Boot's default is
 * {@code spring.aop.proxy-target-class=true}, which uses CGLIB even when an
 * interface exists). A JDK dynamic proxy would be used only for an interface
 * bean when {@code proxy-target-class} is false. Either way, only calls that
 * enter through the proxy are intercepted. {@code this.withdraw()} does not.
 */
package com.example.ailab.week1.day4;
