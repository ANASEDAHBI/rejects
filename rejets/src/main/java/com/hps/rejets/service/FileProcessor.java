package com.hps.rejets.service;

import com.hps.rejets.response.FileProcessResponse;
import com.hps.rejets.response.ResultResponse;
import com.hps.rejets.response.SqlAnalyzerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
            boolean containsValidP0158S04 = false; // Indique si une ligne valide avec *P0158 S04 est trouvée
            String oldIrd = "";

            for (int i = 0; i < fileLines.size(); i++) {
                if (fileLines.get(i).contains(value)) {
                    for (int j = i; j < fileLines.size(); j++) {
                        String lineToCheck = fileLines.get(j);

                        // Vérification si la ligne contient P0158 S04 sans astérisque (*)
                        if (lineToCheck.matches(".*\\bP0158\\s+S04\\b.*") && !lineToCheck.contains("*")) {
                            System.out.println("Ligne ignorée car elle contient 'P0158 S04' sans astérisque : " + lineToCheck);
                            containsValidP0158S04 = false;
                            break; // Ignorer cette valeur et passer à la suivante
                        }

                        // Vérification stricte pour *P0158 S04
                        if (lineToCheck.matches(".*\\*P0158\\s+S04\\s+[A-Z]+.*")) {
                            containsValidP0158S04 = true;

                            // Extraire la valeur suivante après *P0158 S04
                            String[] parts = lineToCheck.split("\\s+");
                            for (int k = 0; k < parts.length; k++) {
                                if (k + 1 < parts.length && parts[k].equals("*P0158") && parts[k + 1].equals("S04")) {
                                    if (k + 2 < parts.length) {
                                        oldIrd = parts[k + 2];
                                        System.out.println("Valeur IRD trouvée : " + oldIrd);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                if (containsValidP0158S04) {
                    break; // Sortir si une ligne valide est trouvée
                }
            }

            // Si aucune ligne valide avec *P0158 S04 n'est trouvée, passer à la prochaine valeur
            if (!containsValidP0158S04) {
                System.out.println("La valeur '" + value + "' n'a pas de correspondance valide avec '*P0158 S04'. Passer à la prochaine.");
                continue;
            }

            // Traitement normal si une ligne valide avec *P0158 S04 est trouvée
            for (int i = 0; i < fileLines.size(); i++) {
                if (fileLines.get(i).contains(value) && !foundIRDForCurrentValue) {
                    for (int j = i; j < fileLines.size(); j++) {
                        String lineToCheck = fileLines.get(j);

                        if (lineToCheck.matches(".*\\*P0158\\s+S04\\s+[A-Z]+.*")) {
                            String[] parts = lineToCheck.split("\\s+");

                            for (int k = 0; k < parts.length; k++) {
                                if (k + 1 < parts.length && parts[k].equals("*P0158") && parts[k + 1].equals("S04")) {
                                    if (k + 2 < parts.length) {
                                        String nextValue = parts[k + 2];
                                        System.out.println("Valeur IRD trouvée : " + nextValue);
                                        ResultResponse response = new ResultResponse();
                                        response.setOldIrd(oldIrd);

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
                                                    String newIRD = irdProductRatesService.getNewIRDForProduct(nextValue, productID, "1");
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
            boolean foundIRDForCurrentValue = false; // Indicateur de traitement pour cette valeur
            boolean containsLiteralP0158S04 = false; // Indicateur pour `*P0158 S04`

            // Recherche littérale de `*P0158 S04` pour la valeur actuelle
            for (int i = 0; i < fileLines.size(); i++) {
                if (fileLines.get(i).contains(value)) {
                    for (int j = i; j < fileLines.size(); j++) {
                        String lineToCheck = fileLines.get(j);

                        // Vérification stricte pour `*P0158 S04` avec startsWith()
                        if (lineToCheck.trim().startsWith("*P0158 S04")) {
                            containsLiteralP0158S04 = true;
                            break;
                        }
                    }
                }
                if (containsLiteralP0158S04) {
                    break; // Sortir si trouvé
                }
            }

            // Si `*P0158 S04` n'est pas trouvé, passer à la prochaine valeur
            if (!containsLiteralP0158S04) {
                System.out.println("La valeur '" + value + "' n'a pas de correspondance avec '*P0158 S04'. Passer à la prochaine.");
                continue;
            }

            // Traitement normal si `*P0158 S04` est trouvé
            for (int i = 0; i < fileLines.size(); i++) {
                if (fileLines.get(i).contains(value) && !foundIRDForCurrentValue) {
                    for (int j = i; j < fileLines.size(); j++) {
                        String lineToCheck = fileLines.get(j);

                        if (lineToCheck.trim().startsWith("*P0158 S04")) {
                            String[] parts = lineToCheck.split("\\s+");

                            for (int k = 0; k < parts.length; k++) {
                                if (k + 1 < parts.length && parts[k].equals("*P0158") && parts[k + 1].equals("S04")) {
                                    if (k + 2 < parts.length) {
                                        String nextValue = parts[k + 2];
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
                                                    String newIRD = irdProductRatesService.getNewIRDForProduct(nextValue, productID, "1");
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

     */


    /*

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
            System.out.println("=======================================================================================");
            System.out.println("Microfilm_extrait : "+value);
            boolean foundIRDForCurrentValue = false;

            for (int i = 0; i < fileLines.size(); i++) {
                if (fileLines.get(i).contains(value) && !foundIRDForCurrentValue) {


                    // Logique Standard avec *P0158 S04
                    for (int j = i; j < fileLines.size(); j++) {
                        String lineToCheck = fileLines.get(j);

                        if (lineToCheck.contains("*P0158 S04")) {

                            String[] parts = lineToCheck.split("\\s+");

                            for (int k = 0; k < parts.length; k++) {
                                if (k + 1 < parts.length && parts[k].equals("*P0158") && parts[k + 1].equals("S04")) {
                                    if (k + 2 < parts.length) {
                                        String nextValue = parts[k + 2];
                                        System.out.println("Valeur IRD trouvée : " + nextValue);

                                        ResultResponse response = new ResultResponse();
                                        response.setOldIrd(nextValue);

                                        for (Map.Entry<String, String> entry : sqlAnalyzerResponse.getMaps().entrySet()) {
                                            if (entry.getValue().equals(value)) {
                                                String key = entry.getKey();
                                                response.setMicrofilm(key);
                                                System.out.println("Microfilm_complet : "+key);


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

                                                // **Appel à la méthode pour récupérer le nouvel IRD avec transactionType = "1"**
                                                if (!productID.isEmpty()) {
                                                    String newIRD = irdProductRatesService.getNewIRDForProduct(nextValue, productID, "1");
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

                            if (foundIRDForCurrentValue) {
                                break;
                            }
                        }
                    }

                    // **Traitement Alternatif (sans *P0158 S04)**
                    /*
                    for (int l = i - 1; l >= 0; l--) {
                        String previousLine = fileLines.get(l);
                        if (previousLine.contains("GCMS PRODUCT ID") && previousLine.contains("INVALID FOR BUSINESS SERVICE ARRANGEMENT")) {
                            // Extraire les 7 chiffres après "ARRANGEMENT"
                            String[] parts = previousLine.split("\\s+");
                            for (int m = 0; m < parts.length; m++) {
                                if (parts[m].equals("ARRANGEMENT") && m + 1 < parts.length) {
                                    String bsaValue = parts[m + 1];
                                    if (bsaValue.length() == 7) {
                                        String bsa = bsaValue;
                                        System.out.println("BSA trouvé : " + bsa);

                                        // Déterminer le `transactionType` à partir du premier caractère du BSA
                                        String transactionType = bsa.startsWith("1") ? "1" : bsa.startsWith("2") ? "2" : null;

                                        System.out.println("TransactionType "+transactionType);

                                        if (transactionType != null) {
                                            // Chercher P0158 S04 après cette ligne
                                            for (int n = l; n < fileLines.size(); n++) {
                                                String lineToCheck = fileLines.get(n);
                                                if (lineToCheck.matches(".*P0158\\s+S04\\s+\\S+\\s*")) {
                                                    System.out.println("Officielement on est dans Traitement Alternatif (sans *P0158 S04)**");
                                                    String[] lineParts = lineToCheck.split("\\s+");
                                                    for (int o = 0; o < lineParts.length; o++) {
                                                        if (o + 1 < lineParts.length && lineParts[o].equals("P0158") && lineParts[o + 1].equals("S04")) {
                                                            if (o + 2 < lineParts.length) {
                                                                String nextValue = lineParts[o + 2];
                                                                System.out.println("Valeur IRD trouvée : " + nextValue);

                                                                ResultResponse response = new ResultResponse();
                                                                response.setOldIrd(nextValue);
                                                                for (Map.Entry<String, String> entry : sqlAnalyzerResponse.getMaps().entrySet()) {
                                                                    if (entry.getValue().equals(value)) {
                                                                        String key = entry.getKey();
                                                                        response.setMicrofilm(key);
                                                                        System.out.println("Microfilm_complet : "+key);

                                                                        String productID = "";
                                                                        for (int p = n - 1; p >= 0; p--) {
                                                                            String previousLineProduct = fileLines.get(p);
                                                                            if (previousLineProduct.matches(".*D0063\\s+S02\\s+\\S+\\s*")) {
                                                                                //System.out.println("previousLineProduct correspondante : " + previousLineProduct);

                                                                                String[] partsProduct = previousLineProduct.split("\\s+");
                                                                                for (int q = 0; q < partsProduct.length; q++) {
                                                                                    if (q + 1 < partsProduct.length && partsProduct[q].equals("D0063") && partsProduct[q + 1].equals("S02")) {
                                                                                        if (q + 2 < partsProduct.length) {
                                                                                            productID = partsProduct[q + 2].substring(0, 3);
                                                                                            System.out.println("ProductID trouvé : " + productID);
                                                                                            response.setProductId(productID);
                                                                                        }
                                                                                        break;
                                                                                    }
                                                                                }
                                                                                break;
                                                                            }
                                                                        }

                                                                        // **Appel à la méthode pour récupérer le nouvel IRD avec transactionType**
                                                                        if (!productID.isEmpty()) {
                                                                            String newIRD = irdProductRatesService.getNewIRDForProduct(nextValue, productID, transactionType);
                                                                            if (!newIRD.equals(nextValue)) {
                                                                                System.out.println("IRD mis à jour : " + newIRD);
                                                                                System.out.println("======================================================================");
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

        fileProcessResponse.setIrdCount(irdCount);
        fileProcessResponse.setResultResponses(resultResponse);
        fileProcessResponse.setInsertCount(sqlAnalyzerResponse.getInsertCount());

        return fileProcessResponse;
    }
    */








/*
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
            boolean skipToNextValue = false;

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
                                                    String newIRD = irdProductRatesService.getNewIRDForProduct(nextValue, productID,"1");
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


 */
