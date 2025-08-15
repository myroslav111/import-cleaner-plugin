package com.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Pattern;

@Mojo(name = "clean-imports", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class ImportCleanerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/java", property = "sourceDirectory")
    private String sourceDirectory;

    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([^;]+);\\s*");

    public void execute() throws MojoExecutionException {
        try (Stream<Path> paths = Files.walk(Paths.get(sourceDirectory))) {
            List<Path> javaFiles = paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            for (Path file : javaFiles) {
                cleanImports(file);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Fehler beim Verarbeiten der Dateien", e);
        }
    }

    private void cleanImports(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        Set<String> usedClasses = lines.stream()
                .filter(line -> !line.startsWith("import"))
                .flatMap(line -> Stream.of(line.split("\\W+")))
                .collect(Collectors.toSet());

        List<String> cleanedLines = lines.stream()
                .filter(line -> {
                    if (!line.startsWith("import")) return true;
                    if (line.contains(".*")) return true; // Behalte Wildcard-Imports
                    java.util.regex.Matcher matcher = IMPORT_PATTERN.matcher(line);
                    if (matcher.find()) {
                        String importedClass = matcher.group(1).substring(matcher.group(1).lastIndexOf('.') + 1);
                        return usedClasses.contains(importedClass);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        Files.write(filePath, cleanedLines);
        getLog().info("Nicht genutzte Importe in " + filePath + " entfernt.");
    }
}







//import org.apache.maven.plugin.AbstractMojo;
//import org.apache.maven.plugin.MojoExecutionException;
//import org.apache.maven.plugin.MojoFailureException;
//import org.apache.maven.plugins.annotations.Mojo;
//import org.apache.maven.plugins.annotations.Parameter;
//
//import java.io.*;
//import java.nio.file.*;
//import java.util.List;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
///**
// * Ein Maven-Plugin, das unnötige import-Anweisungen entfernt.
// */
//@Mojo(name = "clean-imports")
//public class ImportCleanerMojo extends AbstractMojo {
//
//    @Parameter(property = "sourceDirectory", defaultValue = "${project.basedir}/src/main/java")
//    private File sourceDirectory;
//
//    private static final Pattern IMPORT_PATTERN = Pattern.compile("^import\\s+([^;]+);");
//
//    public void execute() throws MojoExecutionException, MojoFailureException {
//        if (!sourceDirectory.exists()) {
//            getLog().warn("Source directory not found: " + sourceDirectory);
//            return;
//        }
//
//        try {
//            Files.walk(sourceDirectory.toPath())
//                    .filter(path -> path.toString().endsWith(".java"))
//                    .forEach(this::cleanImports);
//        } catch (IOException e) {
//            throw new MojoExecutionException("Error processing files", e);
//        }
//    }
//
//    private void cleanImports(Path filePath) {
//        try {
//            List<String> lines = Files.readAllLines(filePath);
//            List<String> cleanedLines = lines.stream()
//                    .filter(line -> !isUnusedImport(line, lines))
//                    .collect(Collectors.toList());
//
//            Files.write(filePath, cleanedLines);
//            getLog().info("Bereinigt: " + filePath);
//        } catch (IOException e) {
//            getLog().error("Fehler beim Verarbeiten der Datei: " + filePath, e);
//        }
//    }
//
//    private boolean isUnusedImport(String importLine, List<String> fileLines) {
//        if (!IMPORT_PATTERN.matcher(importLine).matches()) {
//            return false;
//        }
//
//        String importedClass = importLine.replace("import", "").replace(";", "").trim();
//        String simpleClassName = importedClass.substring(importedClass.lastIndexOf('.') + 1);
//
//        // Prüfen, ob die importierte Klasse im Code verwendet wird
//        return fileLines.stream().noneMatch(line -> line.contains(simpleClassName));
//    }
//}

