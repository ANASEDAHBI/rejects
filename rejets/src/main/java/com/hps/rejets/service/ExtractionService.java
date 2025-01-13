package com.hps.rejets.service;

import com.hps.rejets.response.FileProcessResponse;
import com.hps.rejets.response.SqlAnalyzerResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
public class ExtractionService {

    private final SqlInsertAnalyzer sqlInsertAnalyzer;
    private final FileProcessor fileProcessor;

    public ExtractionService(SqlInsertAnalyzer sqlInsertAnalyzer, FileProcessor fileProcessor) {
        this.sqlInsertAnalyzer = sqlInsertAnalyzer;
        this.fileProcessor = fileProcessor;
    }

    public FileProcessResponse extractAndProcess(String sqlFilePath, String textFilePath) throws IOException, ExecutionException {
        SqlAnalyzerResponse sqlAnalyzerResponse = sqlInsertAnalyzer.extractValues(sqlFilePath);
        return  fileProcessor.processFile(textFilePath,sqlAnalyzerResponse);
    }
}