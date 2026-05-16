package com.avkar.licenseportal.util;

import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ReportChartHelper {
    private ReportChartHelper() {
    }

    public static <T> void addBarChart(Model model, List<T> rows, Function<T, String> labelFn, Function<T, Long> valueFn) {
        if (rows == null || rows.isEmpty()) {
            model.addAttribute("showChart", false);
            return;
        }
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (T row : rows) {
            labels.add(labelFn.apply(row));
            values.add(valueFn.apply(row));
        }
        model.addAttribute("showChart", true);
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartValues", values);
    }
}
