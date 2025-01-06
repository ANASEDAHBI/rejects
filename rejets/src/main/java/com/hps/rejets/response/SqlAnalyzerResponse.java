package com.hps.rejets.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlAnalyzerResponse {

    private Map<String,String> maps;
    private Integer insertCount;


}
