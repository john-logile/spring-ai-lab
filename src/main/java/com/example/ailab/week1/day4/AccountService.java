package com.example.ailab.week1.day4;

import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * The self-invocation trap, and the fix, side by side.
 *
 * <p>{@code transferDirect} calls {@code this.withdraw()}. That call never
 * enters the proxy, so {@code REQUIRES_NEW} is ignored and withdraw joins
 * the outer transaction (same name, same connection).
 *
 * <p>{@code transferThroughProxy} calls {@code self.withdraw()}. {@code self}
 * is the proxy, injected with {@code @Lazy} so this is not the Day 1
 * constructor cycle. The proxy suspends the outer transaction and starts
 * a new one.
 */
@Service
public class AccountService {

    private final AccountService self;
    private final DataSource dataSource;

    public AccountService(@Lazy AccountService self, DataSource dataSource) {
        this.self = self;
        this.dataSource = dataSource;
    }

    @Transactional
    public TxReport transferDirect() {
        TxView outer = view("transferDirect");
        TxView inner = withdraw("direct");
        return new TxReport(outer, inner);
    }

    @Transactional
    public TxReport transferThroughProxy() {
        TxView outer = view("transferThroughProxy");
        TxView inner = self.withdraw("proxy");
        return new TxReport(outer, inner);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TxView withdraw(String via) {
        return view("withdraw via " + via);
    }

    private TxView view(String label) {
        var status = TransactionAspectSupport.currentTransactionStatus();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        return new TxView(
                label,
                status.isNewTransaction(),
                status.getTransactionName(),
                System.identityHashCode(connection));
    }
}
