
package com.hps.rejets.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IRDProductRatesService {
    // Structure pour stocker les données de taux pour chaque IRD avec transactionType
    private Map<String, Map<String, Map<String, Set<String>>>> irdToRates;

    // Constructeur
    public IRDProductRatesService() {
        irdToRates = new HashMap<>();
        initializeData();
    }

    // Initialiser les données pour tous les IRD
    private void initializeData() {
        // Ajouter les données avec transactionType = '1'
        addRateWithTransactionType("YA", "Interregional Consumer Rate I, Digital Commerce, Core", "1",
                new HashSet<>(Arrays.asList("MCC", "MCG", "MCS", "MPL", "ACS", "MDG", "MDJ", "MDO", "MDP", "MDS",
                        "MHA", "MHB", "MHH", "MIP", "MPA", "MPF", "MPG", "MPM", "MPN", "MPO",
                        "MPR", "MPT", "MPV", "MPX", "MPY", "MRH", "MPD", "MRG", "SUR", "MGP",
                        "MIU", "MLB", "GCS", "MRJ", "MRO", "SAS", "TCC", "TCG", "TCS", "MKA",
                        "MPJ", "MPP")));

        addRateWithTransactionType("YB", "Interregional Consumer Rate I, Digital Commerce, Premium", "1",
                new HashSet<>(Arrays.asList("MCW", "MDH", "MPE", "MTP", "MWE", "MPL", "GCP", "MCT", "MGS", "MET",
                        "MRD", "SAP", "TPL", "MKE", "MKB")));

        addRateWithTransactionType("YC", "Interregional Consumer Rate I, Digital Commerce, Super Premium", "1",
                new HashSet<>(Arrays.asList("MWE", "MSP", "MBK", "MDW", "MCW", "MNW", "TCW", "TNW", "TWB", "WBE",
                        "MBW", "MKF", "MKG", "MKH", "MWP", "MKC", "MKD", "WPD")));

        addRateWithTransactionType("YD", "Interregional Consumer Rate II, Card Present, Core", "1",
                new HashSet<>(Arrays.asList("ACS", "MDG", "MDJ", "MDO", "MDP", "MDS", "MHA", "MHB", "MHH", "MIP",
                        "MPA", "MPF", "MPG", "MPM", "MPN", "MPO", "MPR", "MPT", "MPV", "MPX",
                        "MPY", "MRH", "MCC", "MCG", "MCS", "MPL", "MPD", "MRG", "SUR", "MGP",
                        "MIU", "MLB", "GCS", "MRJ", "MRO", "SAS", "TCC", "TCG", "TCS", "MPJ",
                        "MKA", "MPP")));

        addRateWithTransactionType("YE", "Interregional Consumer Rate II, Card Present, Premium", "1",
                new HashSet<>(Arrays.asList("MCW", "MDH", "MPE", "MTP", "MWE", "MPL", "MDP", "MRH", "GCP", "MCT",
                        "MGS", "MET", "MRD", "SAP", "TPL", "MKE", "MKB")));

        addRateWithTransactionType("YF", "Interregional Consumer Rate II, Card Present, Super Premium", "1",
                new HashSet<>(Arrays.asList("MWE", "MSP", "MBK", "MDW", "MCW", "MNW", "TCW", "TNW", "TWB", "WBE",
                        "MBW", "MDH", "MKF", "MKG", "MKH", "MWP", "MKC", "MKD", "WPD")));

        addRateWithTransactionType("YG", "Interregional Consumer Rate III, Base, Core", "1",
                new HashSet<>(Arrays.asList("MCC", "MCG", "MCS", "MPL", "ACS", "MDG", "MDJ", "MDO", "MDP", "MDS",
                        "MHA", "MHB", "MHH", "MIP", "MPA", "MPF", "MPG", "MPM", "MPN", "MPO",
                        "MPR", "MPT", "MPV", "MPX", "MPY", "MRH", "MPD", "MRG", "SUR", "MGP",
                        "MIU", "MLB", "GCS", "MRJ", "MRO", "SAS", "TCC", "TCG", "TCS", "MPJ",
                        "MKA", "MPP")));

        addRateWithTransactionType("YH", "Interregional Consumer Rate III, Base, Premium", "1",
                new HashSet<>(Arrays.asList("MCW", "MDH", "MPE", "MTP", "MWE", "MPL", "MDP", "MRH", "GCP", "MCT",
                        "MGS", "MET", "MRD", "SAP", "TPL", "MKE", "MKB")));

        addRateWithTransactionType("YI", "Interregional Consumer Rate III, Base, Super Premium", "1",
                new HashSet<>(Arrays.asList("MWE", "MSP", "MBK", "MDW", "MCW", "MNW", "TCW", "TNW", "TWB", "WBE",
                        "MBW", "MDH", "MKF", "MKG", "MKH", "MWP", "MKC", "MKD", "WPD")));

        // Ajouter les données avec transactionType = '2'
        addRateWithTransactionType("YA", "Intraregional Consumer Rate I, Digital Commerce, Core", "2",
                new HashSet<>(Arrays.asList("MCC", "MCS", "MIU", "MRG", "SUR", "ACS", "MDS", "MIP", "MPG", "MPP")));

        addRateWithTransactionType("YB", "Intraregional Consumer Rate I, Digital Commerce, Premium", "2",
                new HashSet<>(Arrays.asList("MCT", "MET")));

        addRateWithTransactionType("YC", "Intraregional Consumer Rate I, Digital Commerce, Super Premium", "2",
                new HashSet<>(Arrays.asList("MCW", "MWP", "MDH", "MKC", "WPD")));

        addRateWithTransactionType("YD", "Intraregional Consumer Rate II, Card Present, Core", "2",
                new HashSet<>(Arrays.asList("MCC", "MCS", "MIU", "MRG", "SUR", "ACS", "MDS", "MIP", "MPG", "MPP")));

        addRateWithTransactionType("YE", "Intraregional Consumer Rate II, Card Present, Premium", "2",
                new HashSet<>(Arrays.asList("MCT", "MET")));

        addRateWithTransactionType("YF", "Intraregional Consumer Rate II, Card Present, Super Premium", "2",
                new HashSet<>(Arrays.asList("MCW", "MKF", "MWP", "MDH", "MKC", "WPD")));

        addRateWithTransactionType("YG", "Intraregional Consumer Rate III, Base, Core", "2",
                new HashSet<>(Arrays.asList("MCC", "MCS", "MIU", "MRG", "SUR", "ACS", "MDS", "MIP", "MPG", "MPP")));

        addRateWithTransactionType("YH", "Intraregional Consumer Rate III, Base, Premium", "2",
                new HashSet<>(Arrays.asList("MCT", "MET")));

        addRateWithTransactionType("YI", "Intraregional Consumer Rate III, Base, Super Premium", "2",
                new HashSet<>(Arrays.asList("MCW", "MKF", "MWP", "MDH", "MKC", "WPD")));
    }


    // Méthode pour ajouter des données avec un transactionType
    public void addRateWithTransactionType(String ird, String rateType, String transactionType, Set<String> productIDs) {
        irdToRates
                .computeIfAbsent(ird, k -> new HashMap<>())
                .computeIfAbsent(rateType, k -> new HashMap<>())
                .put(transactionType, productIDs);
    }

    // Obtenir les produits pour un IRD, un type de rate, et un transactionType
    public Set<String> getProductIDsForRateAndTransactionType(String ird, String rateType, String transactionType) {
        return irdToRates.getOrDefault(ird, Collections.emptyMap())
                .getOrDefault(rateType, Collections.emptyMap())
                .getOrDefault(transactionType, Collections.emptySet());
    }

    // Vérifie si un IRD existe dans la carte
    public boolean containsIRD(String ird) {
        return irdToRates.containsKey(ird);
    }

    // Afficher toutes les données pour un IRD (utile pour des tests ou débogage)
    public void printRatesForIRD(String ird) {
        Map<String, Map<String, Set<String>>> rates = irdToRates.get(ird);
        if (rates != null) {
            for (Map.Entry<String, Map<String, Set<String>>> rateEntry : rates.entrySet()) {
                String rateType = rateEntry.getKey();
                System.out.println("Rate Type: " + rateType);

                Map<String, Set<String>> transactionTypes = rateEntry.getValue();
                for (Map.Entry<String, Set<String>> transactionEntry : transactionTypes.entrySet()) {
                    String transactionType = transactionEntry.getKey();
                    Set<String> products = transactionEntry.getValue();

                    System.out.println("  Transaction Type: " + transactionType);
                    System.out.println("  Products: " + products);
                }
            }
        } else {
            System.out.println("IRD " + ird + " not found.");
        }
    }

    public String getNewIRDForProduct(String ird, String productId, String transactionType) {
        // Définir les catégories selon transactionType
        String[] rate3Categories;

        if ("1".equals(transactionType)) {
            rate3Categories = new String[]{
                    "Interregional Consumer Rate III, Base, Core",
                    "Interregional Consumer Rate III, Base, Premium",
                    "Interregional Consumer Rate III, Base, Super Premium"
            };
        } else  {
            rate3Categories = new String[]{
                    "Intraregional Consumer Rate III, Base, Core",
                    "Intraregional Consumer Rate III, Base, Premium",
                    "Intraregional Consumer Rate III, Base, Super Premium"
            };
        }

        boolean productFound = false;

        // Parcourir les catégories définies pour vérifier la présence du produit
        for (String rateCategory : rate3Categories) {
            for (Map.Entry<String, Map<String, Map<String, Set<String>>>> entry : irdToRates.entrySet()) {
                String currentIRD = entry.getKey();
                Map<String, Map<String, Set<String>>> rates = entry.getValue();

                // Vérifier si le type de rate et le transactionType existent
                if (rates.containsKey(rateCategory)) {
                    Map<String, Set<String>> transactionTypeMap = rates.get(rateCategory);
                    if (transactionTypeMap.containsKey(transactionType)) {
                        Set<String> products = transactionTypeMap.get(transactionType);
                        if (products.contains(productId)) {
                            productFound = true;
                            // Retourne l'IRD s'il est différent de celui initial
                            if (!currentIRD.equals(ird)) {
                                System.out.println("Product Found "+productId+" With  new IRD "+currentIRD);
                                return currentIRD;
                            }else {
                                System.out.println("Product Found "+productId+" But IRD "+ird+" n'est pas différent de celui initial");
                            }
                        } else {
                            productFound = false;
                            System.out.println("Product Not Found: " + productId + " sa valeur "+productFound);
                        }
                    }
                }
            }
        }

        // Si aucun produit n'est trouvé, retourner "BB"
        return productFound ? ird : "BB";
    }

    /*
    public String getNewIRDForProduct(String ird, String productId, String transactionType) {
        // Définition des familles de Rate III
        String[] rate3Categories = {
                "Interregional Consumer Rate III, Base, Core",
                "Interregional Consumer Rate III, Base, Premium",
                "Interregional Consumer Rate III, Base, Super Premium",
                "Intraregional Consumer Rate III, Base, Core",
                "Intraregional Consumer Rate III, Base, Premium",
                "Intraregional Consumer Rate III, Base, Super Premium"

        };

        boolean productFound = false;

        // Parcourir les IRD pour vérifier dans Rate III
        for (String rateCategory : rate3Categories) {
            for (Map.Entry<String, Map<String, Map<String, Set<String>>>> entry : irdToRates.entrySet()) {
                String currentIRD = entry.getKey();
                Map<String, Map<String, Set<String>>> rates = entry.getValue();

                // Vérifier si le type de rate et le transactionType existent
                if (rates.containsKey(rateCategory)) {
                    Map<String, Set<String>> transactionTypeMap = rates.get(rateCategory);
                    if (transactionTypeMap.containsKey(transactionType)) {
                        Set<String> products = transactionTypeMap.get(transactionType);
                        if (products.contains(productId)) {
                            productFound = true;
                            System.out.println("Product Found"+productId);
                            // Retourne l'IRD s'il est différent de celui initial
                            if (!currentIRD.equals(ird)) {
                                return currentIRD;
                            }
                        }else {
                            System.out.println("Product Not Found : "+productId);
                        }
                    }
                }
            }
        }


        // Si aucun produit n'est trouvé, retourner "BB"
        return productFound ? ird : "BB";
    }

     */

}


/*
package com.hps.rejets.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IRDProductRatesService {
    // Structure pour stocker les données de taux pour chaque IRD
    private Map<String, Map<String, Set<String>>> irdToRates;

    // Constructeur
    public IRDProductRatesService() {
        irdToRates = new HashMap<>();
        initializeData();
    }

    // Initialiser les données pour tous les IRD
    private void initializeData() {
        // Ajouter les données pour chaque IRD avec leur type de rate et les produits associés

        // IRD YA
        Set<String> yaDigitalCommerceCore = new HashSet<>(Arrays.asList(
                "MCC", "MCG", "MCS", "MPL", "ACS", "MDG", "MDJ", "MDO", "MDP", "MDS",
                "MHA", "MHB", "MHH", "MIP", "MPA", "MPF", "MPG", "MPM", "MPN", "MPO",
                "MPR", "MPT", "MPV", "MPX", "MPY", "MRH", "MPD", "MRG", "SUR", "MGP",
                "MIU", "MLB", "GCS", "MRJ", "MRO", "SAS", "TCC", "TCG", "TCS", "MKA",
                "MPJ", "MPP"));
        addRateForIRD("YA", "Interregional Consumer Rate I, Digital Commerce, Core", yaDigitalCommerceCore);

        // IRD YB
        Set<String> ybDigitalCommercePremium = new HashSet<>(Arrays.asList(
                "MCW", "MDH", "MPE", "MTP", "MWE", "MPL", "GCP", "MCT", "MGS", "MET",
                "MRD", "SAP", "TPL", "MKE", "MKB"));
        addRateForIRD("YB", "Interregional Consumer Rate I, Digital Commerce, Premium", ybDigitalCommercePremium);

        // IRD YC
        Set<String> ycDigitalCommerceSuperPremium = new HashSet<>(Arrays.asList(
                "MWE", "MSP", "MBK", "MDW", "MCW", "MNW", "TCW", "TNW", "TWB", "WBE",
                "MBW", "MKF", "MKG", "MKH", "MWP", "MKC", "MKD", "WPD"));
        addRateForIRD("YC", "Interregional Consumer Rate I, Digital Commerce, Super Premium", ycDigitalCommerceSuperPremium);

        // IRD YD
        Set<String> ydCardPresentCore = new HashSet<>(Arrays.asList(
                "ACS", "MDG", "MDJ", "MDO", "MDP", "MDS", "MHA", "MHB", "MHH", "MIP",
                "MPA", "MPF", "MPG", "MPM", "MPN", "MPO", "MPR", "MPT", "MPV", "MPX",
                "MPY", "MRH", "MCC", "MCG", "MCS", "MPL", "MPD", "MRG", "SUR", "MGP",
                "MIU", "MLB", "GCS", "MRJ", "MRO", "SAS", "TCC", "TCG", "TCS", "MPJ",
                "MKA", "MPP"));
        addRateForIRD("YD", "Interregional Consumer Rate II, Card Present, Core", ydCardPresentCore);

        // IRD YE
        Set<String> yeCardPresentPremium = new HashSet<>(Arrays.asList(
                "MCW", "MDH", "MPE", "MTP", "MWE", "MPL", "MDP", "MRH", "GCP", "MCT",
                "MGS", "MET", "MRD", "SAP", "TPL", "MKE", "MKB"));
        addRateForIRD("YE", "Interregional Consumer Rate II, Card Present, Premium", yeCardPresentPremium);

        // IRD YF
        Set<String> yfCardPresentSuperPremium = new HashSet<>(Arrays.asList(
                "MWE", "MSP", "MBK", "MDW", "MCW", "MNW", "TCW", "TNW", "TWB", "WBE",
                "MBW", "MDH", "MKF", "MKG", "MKH", "MWP", "MKC", "MKD", "WPD"));
        addRateForIRD("YF", "Interregional Consumer Rate II, Card Present, Super Premium", yfCardPresentSuperPremium);

        // IRD YG
        Set<String> ygBaseCore = new HashSet<>(Arrays.asList(
                "MCC", "MCG", "MCS", "MPL", "ACS", "MDG", "MDJ", "MDO", "MDP", "MDS",
                "MHA", "MHB", "MHH", "MIP", "MPA", "MPF", "MPG", "MPM", "MPN", "MPO",
                "MPR", "MPT", "MPV", "MPX", "MPY", "MRH", "MPD", "MRG", "SUR", "MGP",
                "MIU", "MLB", "GCS", "MRJ", "MRO", "SAS", "TCC", "TCG", "TCS", "MPJ",
                "MKA", "MPP"));
        addRateForIRD("YG", "Interregional Consumer Rate III, Base, Core", ygBaseCore);

        // IRD YH
        Set<String> yhBasePremium = new HashSet<>(Arrays.asList(
                "MCW", "MDH", "MPE", "MTP", "MWE", "MPL", "MDP", "MRH", "GCP", "MCT",
                "MGS", "MET", "MRD", "SAP", "TPL", "MKE", "MKB"));
        addRateForIRD("YH", "Interregional Consumer Rate III, Base, Premium", yhBasePremium);

        // IRD YI
        Set<String> yiBaseSuperPremium = new HashSet<>(Arrays.asList(
                "MWE", "MSP", "MBK", "MDW", "MCW", "MNW", "TCW", "TNW", "TWB", "WBE",
                "MBW", "MDH", "MKF", "MKG", "MKH", "MWP", "MKC", "MKD", "WPD"));
        addRateForIRD("YI", "Interregional Consumer Rate III, Base, Super Premium", yiBaseSuperPremium);


    }

    // Ajouter les produits à un IRD pour chaque type de rate
    public void addRateForIRD(String ird, String rateType, Set<String> productIDs) {
        irdToRates.computeIfAbsent(ird, k -> new HashMap<>()).put(rateType, productIDs);
    }

    // Obtenir les produits pour un IRD et un type de rate spécifique
    public Set<String> getProductIDsForRate(String ird, String rateType) {
        return irdToRates.getOrDefault(ird, Collections.emptyMap()).getOrDefault(rateType, Collections.emptySet());
    }

    // Vérifie si un IRD existe dans la carte
    public boolean containsIRD(String ird) {
        return irdToRates.containsKey(ird);
    }

    // Afficher toutes les données pour un IRD (utile pour des tests ou débogage)
    public void printRatesForIRD(String ird) {
        Map<String, Set<String>> rates = irdToRates.get(ird);
        if (rates != null) {
            for (Map.Entry<String, Set<String>> entry : rates.entrySet()) {
                System.out.println("Rate Type: " + entry.getKey());
                System.out.println("ProductsID: " + entry.getValue());
            }
        } else {
            System.out.println("IRD " + ird + " not found.");
        }
    }

    public String getNewIRDForProduct(String ird, String productId) {
        // Définition des familles de Rate 3
        String[] rate3Categories = {
                "Interregional Consumer Rate III, Base, Core",
                "Interregional Consumer Rate III, Base, Premium",
                "Interregional Consumer Rate III, Base, Super Premium"
        };

        boolean productFound = false;

        // Vérifier chaque catégorie du Rate III dans l'ordre
        for (String rateCategory : rate3Categories) {
            for (Map.Entry<String, Map<String, Set<String>>> entry : irdToRates.entrySet()) {
                String currentIRD = entry.getKey();
                Map<String, Set<String>> rates = entry.getValue();

                if (rates.containsKey(rateCategory) && rates.get(rateCategory).contains(productId)) {
                    productFound = true; // On a trouvé le productId dans rate3
                    // Si l'IRD trouvé est différent de celui du résultat original, on le retourne
                    if (!currentIRD.equals(ird)) {
                        return currentIRD;
                    }
                    break; // On sort de la boucle des IRDs dès qu'on trouve un match
                }
            }
        }

        // Si aucun produit n'est trouvé dans Rate 3, on retourne "BB"
        return productFound ? ird : "BB";
    }


}

 */
