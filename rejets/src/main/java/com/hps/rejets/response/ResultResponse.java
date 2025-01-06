package com.hps.rejets.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultResponse {

    private String oldIrd;
    private String newIrd;
    private String microfilm;
    private String productId;
}
