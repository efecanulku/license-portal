package com.avkar.licenseportal.dto;

public class SimplePageParams {
    public static final int PAGE_SIZE = 20;

    private int page = 0;

    /** Faz 6 — serbest metin arama (ad, kod, vergi no vb.). */
    private String q;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    /** Boş veya yalnızca boşluk ise null (sorguda filtre yok). */
    public String normalizedQuery() {
        if (q == null) {
            return null;
        }
        String trimmed = q.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
