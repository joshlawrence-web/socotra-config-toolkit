package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(type = PluginType.cancellation)
public interface CancellationPlugin {
    public static final Integer VERSION = 1;
    default CancellationPluginResponse cancel(ZenCoverRequest request) {
        return CancellationPluginResponse.builder()
            .retentionCharges(
                RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build())
            .build();
    }

    public static final class CancellationPluginStub implements CancellationPlugin {
    }

    public static record ZenCoverRequest(Policy policy,
            Transaction transaction,
            ZenCoverSegment segment,
            Collection<Charge> charges) implements PluginRequest {

        public Collection<?> structures() { return List.of(policy(), transaction(), segment(), charges()); }

        public static ZenCoverRequest of(ULID transactionLocator, Collection<Charge> charges) {
            DataFetcher dataFetcher = DataFetcherFactory.get();
            Transaction transaction = dataFetcher.getTransaction(transactionLocator);
            if (transaction.transactionState().equals(TransactionState.draft)) {
                throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least");
            }
            ZenCoverSegment segment = dataFetcher.getSegmentByTransaction(transactionLocator);
            Policy policy = dataFetcher.getPolicy(transaction.policyLocator());
            return new ZenCoverRequest(policy, transaction, segment, charges);
        }
    }
}