package com.example.ailab.week1.day4;

/**
 * What one transactional method observed while it was running.
 *
 * @param newTransaction {@code true} when this method's proxy started the transaction
 * @param transactionName Spring's name for the transaction, usually the method that started it
 * @param connectionId identity of the JDBC connection bound to that transaction
 */
public record TxView(String label, boolean newTransaction, String transactionName, int connectionId) {
}
