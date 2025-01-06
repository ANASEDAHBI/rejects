package com.hps.rejets.service;

import com.hps.rejets.response.FileProcessResponse;
import com.hps.rejets.response.ResultResponse;
import com.hps.rejets.response.SqlAnalyzerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileProcessor {

    private final IRDProductRatesService irdProductRatesService;



    public FileProcessResponse processFile(String filePath, SqlAnalyzerResponse sqlAnalyzerResponse) throws IOException {
        FileProcessResponse fileProcessResponse = new FileProcessResponse();
        List<String> extractedIRDs = new ArrayList<>();
        int irdCount = 0;
        List<ResultResponse> resultResponse = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        List<String> fileLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            fileLines.add(line);
        }

        for (String value : sqlAnalyzerResponse.getMaps().values()) {
            boolean foundIRDForCurrentValue = false;

            for (int i = 0; i < fileLines.size(); i++) {
                if (fileLines.get(i).contains(value) && !foundIRDForCurrentValue) {
                    for (int j = i; j < fileLines.size(); j++) {
                        String lineToCheck = fileLines.get(j);

                        if (lineToCheck.contains("*P0158 S04")) {
                            String[] parts = lineToCheck.split("\\s+");

                            for (int k = 0; k < parts.length; k++) {
                                if (k + 1 < parts.length && parts[k].equals("*P0158") && parts[k + 1].equals("S04")) {
                                    if (k + 2 < parts.length) {
                                        String nextValue = parts[k + 2];
                                        if (nextValue.length() == 2) {
                                            System.out.println("Valeur IRD trouvée : " + nextValue);
                                            ResultResponse response = new ResultResponse();
                                            response.setOldIrd(nextValue);

                                            for (Map.Entry<String, String> entry : sqlAnalyzerResponse.getMaps().entrySet()) {
                                                if (entry.getValue().equals(value)) {
                                                    String key = entry.getKey();
                                                    response.setMicrofilm(key);

                                                    String productID = "";
                                                    for (int l = j - 1; l >= 0; l--) {
                                                        String previousLine = fileLines.get(l);
                                                        if (previousLine.contains("D0063 S02")) {
                                                            String[] partsPrevious = previousLine.split("\\s+");
                                                            for (int m = 0; m < partsPrevious.length; m++) {
                                                                if (m + 1 < partsPrevious.length && partsPrevious[m].equals("D0063") && partsPrevious[m + 1].equals("S02")) {
                                                                    if (m + 2 < partsPrevious.length) {
                                                                        productID = partsPrevious[m + 2].substring(0, 3);
                                                                        System.out.println("ProductID trouvé : " + productID);
                                                                        response.setProductId(productID);
                                                                    }
                                                                    break;
                                                                }
                                                            }
                                                            break;
                                                        }
                                                    }

                                                    // **Appel à la méthode pour récupérer le nouvel IRD**
                                                    if (!productID.isEmpty()) {
                                                        String newIRD = irdProductRatesService.getNewIRDForProduct(nextValue, productID);
                                                        if (!newIRD.equals(nextValue)) {
                                                            System.out.println("IRD mis à jour : " + newIRD);
                                                            response.setNewIrd(newIRD);
                                                        }
                                                    }

                                                    resultResponse.add(response);
                                                    break;
                                                }
                                            }

                                            extractedIRDs.add(nextValue);
                                            irdCount++;
                                            foundIRDForCurrentValue = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (foundIRDForCurrentValue) {
                                break;
                            }
                        }
                    }

                    if (foundIRDForCurrentValue) {
                        break;
                    }
                }
            }
        }

        fileProcessResponse.setIrdCount(irdCount);
        fileProcessResponse.setResultResponses(resultResponse);
        fileProcessResponse.setInsertCount(sqlAnalyzerResponse.getInsertCount());

        return fileProcessResponse;
    }
}


/*
@Service
public class FileProcessor {

    public FileProcessResponse processFile(String filePath, SqlAnalyzerResponse sqlAnalyzerResponse) throws IOException {
        FileProcessResponse fileProcessResponse = new FileProcessResponse();
        List<String> extractedIRDs = new ArrayList<>(); // Liste pour stocker les valeurs IRD extraites
        int irdCount = 0; // Compteur pour le nombre de valeurs IRD traitées
        List<ResultResponse> resultResponse = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        List<String> fileLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            fileLines.add(line);
        }

        // Parcours des valeurs extraites depuis le fichier SQL
        for (String value : sqlAnalyzerResponse.getMaps().values()) {
            boolean foundIRDForCurrentValue = false;

            // Parcours du fichier pour chaque valeur extraite
            for (int i = 0; i < fileLines.size(); i++) {
                // Recherche de la valeur extraite dans la ligne
                if (fileLines.get(i).contains(value) && !foundIRDForCurrentValue) {
                    // Une fois la valeur trouvée, rechercher "*P0158 S04" dans les lignes suivantes
                    for (int j = i; j < fileLines.size(); j++) {
                        String lineToCheck = fileLines.get(j);

                        // Si on trouve "*P0158 S04" dans la ligne
                        if (lineToCheck.contains("*P0158 S04")) {
                            // Diviser la ligne en parties pour isoler les valeurs après les espaces
                            String[] parts = lineToCheck.split("\\s+");

                            // Chercher la valeur qui suit "*P0158 S04"
                            for (int k = 0; k < parts.length; k++) {
                                // Vérifier si la chaîne est exactement "*P0158 S04"
                                if (k + 1 < parts.length && parts[k].equals("*P0158") && parts[k + 1].equals("S04")) {
                                    // Extraire la valeur qui suit "S04"
                                    if (k + 2 < parts.length) {
                                        // Vérifier si la valeur a exactement 2 caractères
                                        String nextValue = parts[k + 2];
                                        if (nextValue.length() == 2) {
                                            // Loggez la valeur IRD extraite pour le débogage
                                            System.out.println("Valeur IRD trouvée : " + nextValue);
                                            ResultResponse response = new ResultResponse();
                                            response.setIrd(nextValue);

                                            // Recherche du microfilm
                                            for (Map.Entry<String, String> entry : sqlAnalyzerResponse.getMaps().entrySet()) {
                                                if (entry.getValue().equals(value)) {
                                                    String key = entry.getKey();
                                                    System.out.println("Clé trouvée pour la valeur '" + value + "' : " + key);
                                                    response.setMicrofilm(key);

                                                    // Recherche de "D0063 S02" avant l'IRD et extraction du productID
                                                    for (int l = j - 1; l >= 0; l--) {  // On parcourt les lignes avant l'IRD
                                                        String previousLine = fileLines.get(l);
                                                        if (previousLine.contains("D0063 S02")) {
                                                            // Trouver la position de "D0063 S02"
                                                            String[] partsPrevious = previousLine.split("\\s+");
                                                            for (int m = 0; m < partsPrevious.length; m++) {
                                                                // Vérifier si la chaîne est exactement "D0063 S02"
                                                                if (m + 1 < partsPrevious.length && partsPrevious[m].equals("D0063") && partsPrevious[m + 1].equals("S02")) {
                                                                    // Extraire la valeur après "S02" (prenant les 3 caractères)
                                                                    if (m + 2 < partsPrevious.length) {
                                                                        String productID = partsPrevious[m + 2];
                                                                        if (productID.length() >= 3) {
                                                                            productID = productID.substring(0, 3); // Prendre seulement les 3 premiers caractères
                                                                            System.out.println("ProductID trouvé : " + productID);
                                                                            response.setProductId(productID);
                                                                        }
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            break;  // Sortir dès que "D0063 S02" est trouvé
                                                        }
                                                    }

                                                    resultResponse.add(response); // Ajouter à la liste des résultats
                                                    break;  // Arrêter dès qu'on a trouvé le microfilm et le productID
                                                }
                                            }
                                            extractedIRDs.add(nextValue); // Ajouter la valeur IRD trouvée dans la liste
                                            irdCount++; // Incrémenter le compteur pour chaque IRD trouvé
                                            foundIRDForCurrentValue = true; // Marquer qu'on a trouvé l'IRD pour la valeur actuelle
                                            break; // Passer à la prochaine valeur extraite
                                        }
                                    }
                                }
                            }

                            // Si on a déjà trouvé l'IRD pour cette valeur, on quitte la boucle
                            if (foundIRDForCurrentValue) {
                                break;
                            }
                        }
                    }

                    // Si on a déjà trouvé l'IRD pour cette valeur, on quitte la boucle
                    if (foundIRDForCurrentValue) {
                        break;
                    }
                }
            }

            // Afficher le nombre total de valeurs IRD traitées
            System.out.println("Nombre de valeurs IRD traitées : " + irdCount);

            fileProcessResponse.setIrdCount(irdCount);
            fileProcessResponse.setResultResponses(resultResponse);
            fileProcessResponse.setInsertCount(sqlAnalyzerResponse.getInsertCount());
        }

        return fileProcessResponse;
    }


}

 */
