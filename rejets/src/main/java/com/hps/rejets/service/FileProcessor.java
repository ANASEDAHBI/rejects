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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class FileProcessor {

    private final IRDProductRatesService irdProductRatesService;

     /*
    private final IRDProductRatesService irdProductRatesService;

    // Cache pour les résultats des appels externes afin de réduire les répétitions
    private final Map<String, String> irCache = new HashMap<>();

    public FileProcessResponse processFile(String filePath, SqlAnalyzerResponse sqlAnalyzerResponse) throws IOException, ExecutionException {
        FileProcessResponse fileProcessResponse = new FileProcessResponse();
        int irdCount = 0;
        List<ResultResponse> resultResponses = new ArrayList<>();

        // Lecture du fichier en mémoire
        List<String> fileLines = readFile(filePath);

        // Prétraitement du fichier dans une structure optimisée (Map)
        Map<String, List<String>> fileLineMap = new HashMap<>();
        for (int i = 0; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            fileLineMap.put(line, Arrays.asList(line.split("\\s+")));
        }

        // Executor service pour la parallélisation du traitement des lignes
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<ResultResponse>> futures = new ArrayList<>();

        // Parcourir les valeurs extraites du fichier SQL et paralléliser le traitement
        for (Map.Entry<String, String> entry : sqlAnalyzerResponse.getMaps().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            futures.add(executor.submit(() -> processLine(fileLines, fileLineMap, key, value)));
        }

        // Collecter les résultats des threads
        for (Future<ResultResponse> future : futures) {
            try {
                ResultResponse response = future.get();
                if (response != null) {
                    resultResponses.add(response);
                    irdCount++;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown(); // Shutdown de l'executor

        // Compléter la réponse finale
        fileProcessResponse.setIrdCount(irdCount);
        fileProcessResponse.setResultResponses(resultResponses);
        fileProcessResponse.setInsertCount(sqlAnalyzerResponse.getInsertCount());

        return fileProcessResponse;
    }

    // Méthode pour lire le fichier
    private List<String> readFile(String filePath) throws IOException {
        List<String> fileLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileLines.add(line);
            }
        }
        return fileLines;
    }

    // Méthode pour traiter chaque ligne et ajouter la logique de recherche
    private ResultResponse processLine(List<String> fileLines, Map<String, List<String>> fileLineMap, String key, String value) {
        boolean foundIRD = false;
        ResultResponse response = null;

        for (int i = 0; i < fileLines.size() && !foundIRD; i++) {
            String line = fileLines.get(i);
            if (line.contains(value)) {
                for (int j = i; j < fileLines.size() && !foundIRD; j++) {
                    String lineToCheck = fileLines.get(j);
                    if (lineToCheck.contains("*P0158 S04")) {
                        String[] parts = lineToCheck.split("\\s+");

                        for (int k = 0; k < parts.length - 2; k++) {
                            if (parts[k].equals("*P0158") && parts[k + 1].equals("S04")) {
                                String oldIRD = parts[k + 2];
                                if (oldIRD.length() == 2) {
                                    response = new ResultResponse();
                                    response.setOldIrd(oldIRD);
                                    response.setMicrofilm(key);

                                    // Recherche de "D0063 S02" en arrière
                                    String productID = findProductID(fileLines, j);
                                    if (!productID.isEmpty()) {
                                        String newIRD = getNewIRDFromCache(oldIRD, productID);
                                        if (!newIRD.equals(oldIRD)) {
                                            response.setNewIrd(newIRD);
                                        }
                                    }

                                    foundIRD = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return response;
    }

    // Méthode pour trouver le ProductID
    private String findProductID(List<String> fileLines, int startIndex) {
        for (int l = startIndex - 1; l >= 0; l--) {
            String previousLine = fileLines.get(l);
            if (previousLine.contains("D0063 S02")) {
                String[] partsPrevious = previousLine.split("\\s+");
                if (partsPrevious.length > 2 && partsPrevious[0].equals("D0063") && partsPrevious[1].equals("S02")) {
                    return partsPrevious[2].substring(0, 3);
                }
            }
        }
        return "";
    }

    // Méthode pour récupérer le nouvel IRD depuis le cache ou le service
    private String getNewIRDFromCache(String oldIRD, String productID) {
        String key = oldIRD + productID;
        if (irCache.containsKey(key)) {
            return irCache.get(key);
        } else {
            String newIRD = irdProductRatesService.getNewIRDForProduct(oldIRD, productID);
            irCache.put(key, newIRD);
            return newIRD;
        }
    }
}

      */

    /*

    public FileProcessResponse processFile(String filePath, SqlAnalyzerResponse sqlAnalyzerResponse) throws IOException {
        FileProcessResponse fileProcessResponse = new FileProcessResponse();
        int irdCount = 0;
        List<ResultResponse> resultResponses = new ArrayList<>();

        // Lecture du fichier en mémoire
        List<String> fileLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileLines.add(line);
            }
        }

        // Recherche des occurrences de "*P0158 S04" en priorité
        for (Map.Entry<String, String> entry : sqlAnalyzerResponse.getMaps().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            boolean foundIRD = false;

            for (int i = 0; i < fileLines.size() && !foundIRD; i++) {
                if (fileLines.get(i).contains("*P0158 S04")) {
                    String[] parts = fileLines.get(i).split("\\s+");
                    if (parts.length > 2 && parts[0].equals("*P0158") && parts[1].equals("S04")) {
                        String oldIRD = parts[2];
                        if (oldIRD.length() == 2) {
                            ResultResponse response = new ResultResponse();
                            response.setOldIrd(oldIRD);
                            response.setMicrofilm(key);

                            // Recherche de "D0063 S02" en arrière
                            String productID = "";
                            for (int j = i - 1; j >= 0; j--) {
                                if (fileLines.get(j).contains("D0063 S02")) {
                                    String[] partsPrevious = fileLines.get(j).split("\\s+");
                                    if (partsPrevious.length > 2 && partsPrevious[0].equals("D0063") && partsPrevious[1].equals("S02")) {
                                        productID = partsPrevious[2].substring(0, 3);
                                        response.setProductId(productID);
                                    }
                                    break;
                                }
                            }

                            // Mise à jour de l'IRD
                            if (!productID.isEmpty()) {
                                String newIRD = irdProductRatesService.getNewIRDForProduct(oldIRD, productID);
                                if (!newIRD.equals(oldIRD)) {
                                    response.setNewIrd(newIRD);
                                }
                            }

                            resultResponses.add(response);
                            irdCount++;
                            foundIRD = true;
                            System.out.println("RESPONSE : "+response);
                        }
                    }
                }
            }

            // Si aucune occurrence de "*P0158 S04" n'est trouvée, chercher "GCMS PRODUCT ID MPL INVALID FOR BUSINESS SERVICE ARRANGEMENT"
            if (!foundIRD) {
                String businessServiceArrangement = "";
                for (int i = 0; i < fileLines.size(); i++) {
                    if (fileLines.get(i).matches(".*GCMS PRODUCT ID.*INVALID FOR BUSINESS SERVICE ARRANGEMENT.*")) {
                        String numbersOnly = fileLines.get(i).replaceAll(".*INVALID FOR BUSINESS SERVICE ARRANGEMENT\\s*", "").replaceAll("\\D", "");
                        if (numbersOnly.length() >= 7) {
                            businessServiceArrangement = numbersOnly.substring(0, 7);
                        }
                        break;
                    }
                }

                // Recherche de "P0158 S04" sans "*" après l'occurrence précédente
                for (int i = 0; i < fileLines.size() && businessServiceArrangement.isEmpty(); i++) {
                    if (fileLines.get(i).contains("P0158 S04")) {
                        String[] parts = fileLines.get(i).split("\\s+");
                        if (parts.length > 2 && parts[0].equals("P0158") && parts[1].equals("S04")) {
                            String oldIRD = parts[2];
                            if (oldIRD.length() == 2) {
                                ResultResponse response = new ResultResponse();
                                response.setOldIrd(oldIRD);
                                response.setMicrofilm(key);

                                // Recherche de "D0063 S02" en arrière
                                String productID = "";
                                for (int j = i - 1; j >= 0; j--) {
                                    if (fileLines.get(j).contains("D0063 S02")) {
                                        String[] partsPrevious = fileLines.get(j).split("\\s+");
                                        if (partsPrevious.length > 2 && partsPrevious[0].equals("D0063") && partsPrevious[1].equals("S02")) {
                                            productID = partsPrevious[2].substring(0, 3);
                                            response.setProductId(productID);
                                        }
                                        break;
                                    }
                                }

                                // Mise à jour de l'IRD
                                if (!productID.isEmpty()) {
                                    String newIRD = irdProductRatesService.getNewIRDForProduct(oldIRD, productID);
                                    if (!newIRD.equals(oldIRD)) {
                                        response.setNewIrd(newIRD);
                                    }
                                }

                                resultResponses.add(response);
                                irdCount++;
                                System.out.println("RESPONSE where not *P0158 S04: "+response);
                            }
                        }
                    }
                }
            }
        }

        fileProcessResponse.setIrdCount(irdCount);
        fileProcessResponse.setResultResponses(resultResponses);
        fileProcessResponse.setInsertCount(sqlAnalyzerResponse.getInsertCount());
        return fileProcessResponse;
    }



     */

///////////////////////////////////////////////////////////////////////////////
/*
    public FileProcessResponse processFile(String filePath, SqlAnalyzerResponse sqlAnalyzerResponse) throws IOException {
        FileProcessResponse fileProcessResponse = new FileProcessResponse();
        int irdCount = 0;
        List<ResultResponse> resultResponses = new ArrayList<>();

        // Lecture du fichier en mémoire
        List<String> fileLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileLines.add(line);
            }
        }

        // Parcourir les valeurs extraites du fichier SQL
        for (Map.Entry<String, String> entry : sqlAnalyzerResponse.getMaps().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            boolean foundIRD = false;

            for (int i = 0; i < fileLines.size() && !foundIRD; i++) {
                if (fileLines.get(i).contains(value)) {
                    for (int j = i; j < fileLines.size() && !foundIRD; j++) {
                        String lineToCheck = fileLines.get(j);
                        if (lineToCheck.contains("*P0158 S04")) {
                            String[] parts = lineToCheck.split("\\s+");

                            for (int k = 0; k < parts.length - 2; k++) {
                                if (parts[k].equals("*P0158") && parts[k + 1].equals("S04")) {
                                    String oldIRD = parts[k + 2];
                                    if (oldIRD.length() == 2) {
                                        System.out.println("IRD trouvé: " + oldIRD);
                                        ResultResponse response = new ResultResponse();
                                        response.setOldIrd(oldIRD);
                                        response.setMicrofilm(key);

                                        // Recherche de "D0063 S02" en arrière
                                        String productID = "";
                                        for (int l = j - 1; l >= 0; l--) {
                                            String previousLine = fileLines.get(l);
                                            if (previousLine.contains("D0063 S02")) {
                                                String[] partsPrevious = previousLine.split("\\s+");
                                                for (int m = 0; m < partsPrevious.length - 2; m++) {
                                                    if (partsPrevious[m].equals("D0063") && partsPrevious[m + 1].equals("S02")) {
                                                        productID = partsPrevious[m + 2].substring(0, 3);
                                                        response.setProductId(productID);
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }

                                        // Récupération du nouvel IRD
                                        if (!productID.isEmpty()) {
                                            String newIRD = irdProductRatesService.getNewIRDForProduct(oldIRD, productID);
                                            if (!newIRD.equals(oldIRD)) {
                                                System.out.println("IRD mis à jour: " + newIRD);
                                                response.setNewIrd(newIRD);
                                            }
                                        }

                                        resultResponses.add(response);
                                        irdCount++;
                                        foundIRD = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            String businessServiceArrangement = "";
                            for (int k = 0; i < fileLines.size(); k++) {
                                if (fileLines.get(i).matches(".*GCMS PRODUCT ID.*INVALID FOR BUSINESS SERVICE ARRANGEMENT.*")) {
                                    String numbersOnly = fileLines.get(k).replaceAll(".*INVALID FOR BUSINESS SERVICE ARRANGEMENT\\s*", "").replaceAll("\\D", "");
                                    if (numbersOnly.length() >= 7) {
                                        businessServiceArrangement = numbersOnly.substring(0, 7);
                                    }
                                    break;
                                }
                            }
                            for (int f = 0; i < fileLines.size(); f++) {
                                if (fileLines.get(f).contains("P0158 S04")) {
                                    String[] parts = fileLines.get(f).split("\\s+");
                                    if (parts.length > 2 && parts[0].equals("P0158") && parts[1].equals("S04")) {
                                        String oldIRD = parts[2];
                                        if (oldIRD.length() == 2) {
                                            ResultResponse response = new ResultResponse();
                                            response.setOldIrd(oldIRD);
                                            response.setMicrofilm(key);

                                            // Recherche de "D0063 S02" en arrière
                                            String productID = "";
                                            for (int l = i - 1; l >= 0; l--) {
                                                if (fileLines.get(l).contains("D0063 S02")) {
                                                    String[] partsPrevious = fileLines.get(l).split("\\s+");
                                                    if (partsPrevious.length > 2 && partsPrevious[0].equals("D0063") && partsPrevious[1].equals("S02")) {
                                                        productID = partsPrevious[2].substring(0, 3);
                                                        response.setProductId(productID);
                                                    }
                                                    break;
                                                }
                                            }

                                            // Mise à jour de l'IRD
                                            if (!productID.isEmpty()) {
                                                String newIRD = irdProductRatesService.getNewIRDForProduct(oldIRD, productID);
                                                if (!newIRD.equals(oldIRD)) {
                                                    response.setNewIrd(newIRD);
                                                }
                                            }

                                            resultResponses.add(response);
                                            irdCount++;
                                            System.out.println("RESPONSE where not *P0158 S04: " + response);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        fileProcessResponse.setIrdCount(irdCount);
        fileProcessResponse.setResultResponses(resultResponses);
        fileProcessResponse.setInsertCount(sqlAnalyzerResponse.getInsertCount());

        return fileProcessResponse;

    }
}

 */

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
                    boolean foundIRDInCurrentBlock = false;

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

                            if (foundIRDForCurrentValue) {
                                break;
                            }
                        }
                    }

                    if (!foundIRDForCurrentValue) {
                        // Logique alternative si *P0158 S04 n'est pas trouvé
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

                                            // Chercher P0158 S04 après cette ligne
                                            for (int n = l; n < fileLines.size(); n++) {
                                                String lineToCheck = fileLines.get(n);
                                                if (lineToCheck.matches(".*P0158\\s+S04\\s+\\S+\\s*")) {
                                                    System.out.println("UUPPI WE FOUND P0158  S04");
                                                    String[] lineParts = lineToCheck.split("\\s+");
                                                    System.out.println("la ligne checker "+lineToCheck);
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

                                                                        String productID = "";
                                                                        for (int p = n - 1; p >= 0; p--) {
                                                                            String previousLineProduct = fileLines.get(p);
                                                                            //System.out.println("previousLineProduct : "+previousLineProduct);

                                                                            if (previousLineProduct.matches(".*D0063\\s+S02\\s+\\S+\\s*")) {
                                                                                System.out.println("previousLineProduct  correspondante : "+previousLineProduct);

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


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

 */

/////////////////////////////////
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
