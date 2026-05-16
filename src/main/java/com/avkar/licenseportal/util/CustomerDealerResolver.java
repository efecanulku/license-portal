package com.avkar.licenseportal.util;

import com.avkar.licenseportal.entity.Customer;
import com.avkar.licenseportal.entity.Dealer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CustomerDealerResolver {
    private CustomerDealerResolver() {
    }

    /** Kuruma bağlı bayiler; bağ yoksa oluşturan bayi. */
    public static List<Dealer> eligibleDealers(Customer customer) {
        Map<Long, Dealer> byId = new LinkedHashMap<>();
        if (customer.getLinkedDealers() != null) {
            for (Dealer dealer : customer.getLinkedDealers()) {
                if (dealer != null && dealer.getId() != null) {
                    byId.put(dealer.getId(), dealer);
                }
            }
        }
        if (byId.isEmpty() && customer.getCreatedByDealer() != null) {
            Dealer creator = customer.getCreatedByDealer();
            byId.put(creator.getId(), creator);
        }
        List<Dealer> sorted = new ArrayList<>(byId.values());
        sorted.sort(Comparator.comparing(Dealer::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return sorted;
    }
}
