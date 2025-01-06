package com.hps.rejets.service;


import com.hps.rejets.response.ResultResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecyclingScriptGenerator {

    public String generateRecyclingScript(List<ResultResponse> resultResponses) {
        Map<String, List<String>> groupedByNewIrd = resultResponses.stream()
                .collect(Collectors.groupingBy(ResultResponse::getNewIrd,
                        Collectors.mapping(ResultResponse::getMicrofilm, Collectors.toList())));

        StringBuilder script = new StringBuilder();

        for (Map.Entry<String, List<String>> entry : groupedByNewIrd.entrySet()) {
            String newIrd = entry.getKey();
            List<String> microfilms = entry.getValue();

            script.append("UPDATE transaction_hist_mvt SET settlement_cutoff_outg='X', ")
                    .append("private_data_1='").append(newIrd).append("', ")
                    .append("transaction_fee_rule='WE").append(newIrd).append("' ")
                    .append("WHERE microfilm_ref_number IN (")
                    .append(formatMicrofilms(microfilms))
                    .append(") AND microfilm_ref_seq='1';\n\n\n");
        }

        script.append("COMMIT;\n");
        return script.toString();
    }

    private String formatMicrofilms(List<String> microfilms) {
        return microfilms.stream()
                .map(mf -> "'" + mf + "'")
                .collect(Collectors.joining(",\n ", "\n ", "\n"));
    }
}
