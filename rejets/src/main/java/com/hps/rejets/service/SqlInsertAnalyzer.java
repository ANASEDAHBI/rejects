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
        Map<String, String> maps = new HashMap<>();
        int insertCount = 0;

        Pattern insertPattern = Pattern.compile(
                "INSERT INTO\\s+[^()]+\\((.*?)\\)\\s+VALUES\\s*\\((.*?)\\)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sqlContent = new StringBuilder();
            String line;

            // Lire tout le fichier et stocker son contenu
            while ((line = reader.readLine()) != null) {
                sqlContent.append(line).append(" ");
            }

            Matcher matcher = insertPattern.matcher(sqlContent.toString());


            while (matcher.find()) {
                insertCount++;
                String valuesPart = matcher.group(2);

                // Séparer les valeurs sans casser celles entre apostrophes
                String[] values = valuesPart.split(",(?=(?:[^\']*\'[^\']*\')*[^\']*$)");

                if (values.length > 0) {
                    String firstValue = values[0].trim().replaceAll("['\"]", "");
                    if (firstValue.matches("\\d{20,}")) {
                        String extractedValue = firstValue.substring(firstValue.length() - 7, firstValue.length() - 1);
                        maps.put(firstValue, extractedValue);
                    }
                    /*
                    if (firstValue.length() > 6) {

                    }

                     */

                }
            }



            /*
            while (matcher.find()) {
                insertCount++;
                String valuesPart = matcher.group(2);

                // Diviser les valeurs en tenant compte des guillemets
                String[] values = valuesPart.split(",(?=(?:[^']*'[^']*')*[^']*$)");

                if (values.length > 0) {
                    // Nettoyer la première valeur et extraire uniquement le microfilm
                    String firstValue = values[0].trim().replaceAll("['\"]", ""); // Supprimer les guillemets

                    // Ajouter une condition pour valider que firstValue contient uniquement le microfilm
                    if (firstValue.matches("\\d{20,}")) { // Vérifie que c'est un nombre de 20 chiffres ou plus
                        maps.put(firstValue, firstValue); // Ajouter au map
                    } else {
                        System.out.println("Valeur ignorée (non valide pour un microfilm) : " + firstValue);
                    }
                }
            }

             */


        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier SQL : " + e.getMessage());
        }

        System.out.println("Nombre d'inserts trouvés : " + insertCount);
        System.out.println("Information : " + maps);
        sqlAnalyzerResponse.setInsertCount(insertCount);
        sqlAnalyzerResponse.setMaps(maps);
        return sqlAnalyzerResponse;
    }

    /*
    public SqlAnalyzerResponse extractValues(String filePath) {
        SqlAnalyzerResponse sqlAnalyzerResponse = new SqlAnalyzerResponse();

        Map<String,String> maps = new HashMap<>();
        int insertCount = 0; // Initialisation du compteur d'inserts

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Pattern insertPattern = Pattern.compile(
                    "INSERT INTO\\s+[^()]+\\((.*?)\\)\\s+VALUES\\s*\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
           // Pattern insertPattern = Pattern.compile("INSERT INTO .*?\\((.*?)\\) VALUES \\((.*?)\\);", Pattern.CASE_INSENSITIVE);

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

     */
}
