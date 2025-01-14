
package com.hps.rejets.controller;

import com.hps.rejets.response.FileProcessResponse;
import com.hps.rejets.response.ResultResponse;
import com.hps.rejets.service.ExtractionService;
import com.hps.rejets.service.RecyclingScriptGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExtractionController {

    private final ExtractionService extractionService;
    private final RecyclingScriptGenerator recyclingScriptGenerator;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";
    private static final String GENERATED_SCRIPTS_DIR = System.getProperty("user.dir") + File.separator + "generated-scripts";

    private static final Logger logger = LoggerFactory.getLogger(ExtractionController.class);

    public ExtractionController(ExtractionService extractionService, RecyclingScriptGenerator recyclingScriptGenerator) {
        this.extractionService = extractionService;
        this.recyclingScriptGenerator = recyclingScriptGenerator;
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("sqlFile") MultipartFile sqlFile,
                                              @RequestParam("textFile") MultipartFile textFile) {

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Enregistrement du fichier SQL
            Path sqlFilePath = Paths.get(UPLOAD_DIR, "uploaded.sql");
            Files.write(sqlFilePath, sqlFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Vérification si le fichier est un PDF
            if (textFile.getOriginalFilename() != null && textFile.getOriginalFilename().endsWith(".pdf")) {
                Path textFilePath = Paths.get(UPLOAD_DIR, "uploaded.txt");
                convertPdfToText(textFile, textFilePath);
            } else {
                Path textFilePath = Paths.get(UPLOAD_DIR, "uploaded.txt");
                Files.write(textFilePath, textFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            return ResponseEntity.ok("Fichiers uploadés avec succès.");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'upload des fichiers : " + e.getMessage());
        }
    }

    /**
     * Convertit un fichier PDF en texte brut et le sauvegarde en fichier .txt.
     */
    private void convertPdfToText(MultipartFile pdfFile, Path outputPath) throws IOException {
        try (InputStream inputStream = pdfFile.getInputStream();
             PDDocument document = PDDocument.load(inputStream);
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            writer.write(text);
        }
    }
/*
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("sqlFile") MultipartFile sqlFile,
                                              @RequestParam("textFile") MultipartFile textFile) {

        logger.info("Fichier SQL reçu : {}", sqlFile.getOriginalFilename());
        logger.info("Fichier TXT reçu : {}", textFile.getOriginalFilename());
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            Path sqlFilePath = Paths.get(UPLOAD_DIR, "uploaded.sql");
            Path textFilePath = Paths.get(UPLOAD_DIR, "uploaded.txt");
            Files.write(sqlFilePath, sqlFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(textFilePath, textFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return ResponseEntity.ok("Fichiers uploadés avec succès.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'upload des fichiers : " + e.getMessage());
        }
    }

 */

    /*
    @GetMapping("/analyze")
    public FileProcessResponse analyzeFiles() throws IOException, ExecutionException {
        String sqlFilePath = Paths.get(UPLOAD_DIR, "uploaded.sql").toString();
        String textFilePath = Paths.get(UPLOAD_DIR, "uploaded.txt").toString();
        return extractionService.extractAndProcess(sqlFilePath, textFilePath);
    }

     */

    @GetMapping("/analyze")
    public ResponseEntity<FileProcessResponse> analyzeFiles() throws IOException {
        String sqlFilePath = Paths.get(UPLOAD_DIR, "uploaded.sql").toString();
        String textFilePath = Paths.get(UPLOAD_DIR, "uploaded.txt").toString();
        FileProcessResponse response = extractionService.extractAndProcess(sqlFilePath, textFilePath);
        return ResponseEntity.ok(response);

    }


    @GetMapping(value = "/recycle", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> recycleScript() throws IOException, ExecutionException {
        String sqlFilePath = Paths.get(UPLOAD_DIR, "uploaded.sql").toString();
        String textFilePath = Paths.get(UPLOAD_DIR, "uploaded.txt").toString();
        FileProcessResponse response = extractionService.extractAndProcess(sqlFilePath, textFilePath);
        List<ResultResponse> resultResponses = response.getResultResponses();
        String sqlScript = recyclingScriptGenerator.generateRecyclingScript(resultResponses);
        String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String fileName = "recycling_script_" + dateSuffix + ".sql";
        Files.createDirectories(Paths.get(GENERATED_SCRIPTS_DIR));
        Path outputPath = Paths.get(GENERATED_SCRIPTS_DIR, fileName);
        Files.write(outputPath, sqlScript.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .body("Le fichier '" + fileName + "' a été généré  \n\n" + sqlScript);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadGeneratedFile() throws IOException {
        String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String fileName = "recycling_script_" + dateSuffix + ".sql";
        Path filePath = Paths.get(GENERATED_SCRIPTS_DIR, fileName);
        if (!Files.exists(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        byte[] fileContent = Files.readAllBytes(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteRecyclingScript() {
        try {
            // Supprimer tous les fichiers dans GENERATED_SCRIPTS_DIR
            File generatedScriptsDir = new File(GENERATED_SCRIPTS_DIR);
            if (generatedScriptsDir.exists() && generatedScriptsDir.isDirectory()) {
                for (File file : generatedScriptsDir.listFiles()) {
                    if (file.isFile()) {
                        Files.delete(file.toPath());
                    }
                }
            }

            // Supprimer tous les fichiers dans UPLOAD_DIR
            File uploadDir = new File(UPLOAD_DIR);
            if (uploadDir.exists() && uploadDir.isDirectory()) {
                for (File file : uploadDir.listFiles()) {
                    if (file.isFile()) {
                        Files.delete(file.toPath());
                    }
                }
            }

            return ResponseEntity.ok("Tous les fichiers ont été supprimés avec succès.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression des fichiers : " + e.getMessage());
        }
    }
}


/*
package com.hps.rejets.controller;

import com.hps.rejets.response.FileProcessResponse;
import com.hps.rejets.response.ResultResponse;
import com.hps.rejets.service.ExtractionService;
import com.hps.rejets.service.RecyclingScriptGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class ExtractionController {

    private final ExtractionService extractionService;
    private final RecyclingScriptGenerator recyclingScriptGenerator;
    public ExtractionController(ExtractionService extractionService, RecyclingScriptGenerator recyclingScriptGenerator) {
        this.extractionService = extractionService;
        this.recyclingScriptGenerator = recyclingScriptGenerator;
    }
    @GetMapping("/analyze")
    public FileProcessResponse analyzeFiles() throws IOException, URISyntaxException {

            String sqlFilePath = findFileWithExtension(".sql");
            String textFilePath = findFileWithExtension(".txt");

            return extractionService.extractAndProcess(sqlFilePath, textFilePath);

    }


    @GetMapping(value = "/recycle", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> recycleScript() throws IOException, URISyntaxException {
        String sqlFilePath = findFileWithExtension(".sql");
        String textFilePath = findFileWithExtension(".txt");

        FileProcessResponse response = extractionService.extractAndProcess(sqlFilePath, textFilePath);
        List<ResultResponse> resultResponses = response.getResultResponses();

        String sqlScript = recyclingScriptGenerator.generateRecyclingScript(resultResponses);

        // 🔹 Générer la date du jour au format ddMMyyyy
        String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        // 🔹 Définir le nom du fichier avec la date
        String fileName = "recycling_script_" + dateSuffix + ".sql";

        // Récupérer l'URL du répertoire des ressources
       // URL resourceUrl = getClass().getClassLoader().getResource("");



        // Vérifier si l'URL est valide
       // if (resourceUrl == null) {
       //     throw new FileNotFoundException("Le répertoire des ressources n'a pas été trouvé.");
       // }

        // 🔹 Convertir l'URL en Path, sans inclure le préfixe 'file:'
      // URI resourceUri = resourceUrl.toURI();
        //Path resourcePath = Paths.get(resourceUri).resolve(fileName); // Résoudre le chemin du fichier dans resources

        // 🔹 Écrire le script dans un fichier
        //Files.write(resourcePath, sqlScript.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        String saveDir = System.getProperty("user.dir") + File.separator + "generated-scripts";
        Path savePath = Paths.get(saveDir);

        if (!Files.exists(savePath)) {
            Files.createDirectories(savePath);
        }

       // String projectDir = System.getProperty("user.dir");
       // Path outputPath = Paths.get(saveDir, fileName);
        Path outputPath = savePath.resolve(fileName);


       // Files.write(outputPath, sqlScript.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Files.write(outputPath, sqlScript.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .body("Le fichier '" + fileName + "' a été généré et enregistré dans : " + outputPath.toString()+"\n\n"+sqlScript);
    }

    @GetMapping("/delete")
    public ResponseEntity<String> deleteRecyclingScript() {
        // 🔹 Générer le nom du fichier du jour
        String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String fileName = "recycling_script_" + dateSuffix + ".sql";
        String saveDir = System.getProperty("user.dir") + File.separator + "generated-scripts";
        Path filePath = Paths.get(saveDir, fileName);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseEntity.ok("Le fichier '" + fileName + "' a été supprimé avec succès.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le fichier '" + fileName + "' n'existe pas.");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du fichier : " + e.getMessage());
        }
    }




    // 🔹 Trouver un fichier avec une extension donnée (.sql ou .txt)
    private String findFileWithExtension(String extension) throws IOException, URISyntaxException {
        // 🔹 Récupérer le chemin des ressources via ClassLoader
        URL resourceUrl = getClass().getClassLoader().getResource("");

        if (resourceUrl == null) {
            throw new IOException("Le dossier resources n'a pas été trouvé.");
        }

        // 🔹 Corriger le chemin pour Windows (éviter les caractères invalides)
        Path resourcesDir = Paths.get(resourceUrl.toURI());

        System.out.println("📂 Chemin du dossier resources : " + resourcesDir.toAbsolutePath());

        try (Stream<Path> paths = Files.walk(resourcesDir)) {
            return paths
                    .filter(Files::isRegularFile) // Garder uniquement les fichiers
                    .map(Path::toString) // Convertir en String
                    .filter(file -> file.endsWith(extension)) // Vérifier l'extension
                    .findFirst() // Retourner le premier trouvé
                    .orElse(null);
        } catch (Exception e) {
            throw new IOException("Erreur lors de la lecture du dossier resources", e);
        }
    }

}

 */