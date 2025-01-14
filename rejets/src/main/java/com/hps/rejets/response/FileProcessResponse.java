package com.hps.rejets.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileProcessResponse {

    private Integer irdCount;
    private Integer insertCount;
    private List<ResultResponse> resultResponses;

}
