package com.hps.rejets.service;

import com.hps.rejets.response.SqlAnalyzerResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SqlInsertAnalyzer {

    public SqlAnalyzerResponse extractValues(String filePath) {
        SqlAnalyzerResponse sqlAnalyzerResponse = new SqlAnalyzerResponse();

        Map<String,String> maps = new HashMap<>();
        int insertCount = 0; // Initialisation du compteur d'inserts

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Pattern insertPattern = Pattern.compile("INSERT INTO .*?\\((.*?)\\) VALUES \\((.*?)\\);", Pattern.CASE_INSENSITIVE);

            while ((line = reader.readLine()) != null) {
                Matcher matcher = insertPattern.matcher(line);
                while (matcher.find()) {
                    // Chaque fois qu'un insert est trouvé, incrémenter le compteur
                    insertCount++;
                    String valuesPart = matcher.group(2);
                    String[] values = valuesPart.split(",");
                    if (values.length > 0) {
                        String firstValue = values[0].trim().replaceAll("['\"]", "");

                        if (firstValue.length() > 6) {
                            String extractedValue = firstValue.substring(firstValue.length() - 7, firstValue.length() - 1);
                            maps.put(firstValue,extractedValue);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier SQL : " + e.getMessage());
        }

        // Loggez ou retournez le nombre d'inserts trouvés
        System.out.println("Nombre d'inserts trouvés : " + insertCount);
        sqlAnalyzerResponse.setInsertCount(insertCount);
        sqlAnalyzerResponse.setMaps(maps);
        return sqlAnalyzerResponse;
    }
}