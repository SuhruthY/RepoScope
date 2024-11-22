package com.suhruth.reposcope.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

@Service
public class RepositoryAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryAnalysisService.class);

    private static final String CLONE_DIR = "src/main/resources/clonedRepo";

    private static final AtomicInteger interfaces = new AtomicInteger(0);
    private static final AtomicInteger abstractClasses = new AtomicInteger(0);
    private static final AtomicInteger totalClasses = new AtomicInteger(0);
    private static final AtomicInteger totalMethods = new AtomicInteger(0);
    private static final AtomicInteger methodsWithLoops = new AtomicInteger(0);
    private static final AtomicInteger methodsWithConditionals = new AtomicInteger(0);

    public Map<String, Object> analyzeRepository(String repoUrl) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> classDetails = new ArrayList<>();
        List<Map<String, Object>> methodCalls = new ArrayList<>();
        List<Map<String, Object>> loopsAndConditionals = new ArrayList<>();
        Map<String, Object> repositoryInfo = new HashMap<>();
        Map<String, Object> summary = new HashMap<>();

        repositoryInfo.put("repoName", repoUrl.substring(repoUrl.lastIndexOf('/') + 1));

        try {
            logger.info("Starting repository analysis for URL: {}", repoUrl);
            cloneRepo(repoUrl, CLONE_DIR);
            File repoDir = new File(CLONE_DIR);

            // Repository Analysis
            analyzeFiles(repoDir, classDetails, methodCalls, loopsAndConditionals, repositoryInfo, summary);

            response.put("status", "success");
            response.put("data", Map.of(
                    "repositoryInfo", repositoryInfo,
                    "classDetails", classDetails,
                    "methodCalls", methodCalls,
                    "loopsAndConditionals", loopsAndConditionals,
                    "summary", summary
            ));
        } catch (Exception e) {
            logger.error("Error analyzing repository: {}", repoUrl, e);
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    private void cloneRepo(String repoUrl, String cloneDir) throws IOException {
        File dir = new File(cloneDir);
        if (dir.exists()) {
            logger.info("Deleting existing cloned repository directory.");
            deleteDirectory(dir);
        }

        try {
            logger.info("Cloning repository from URL: {} into directory: {}", repoUrl, cloneDir);
            Git clone = Git.cloneRepository().setURI(repoUrl).setDirectory(dir).call();
            clone.getRepository().close();
            logger.info("Repository cloned successfully.");
        } catch (GitAPIException e) {
            logger.error("Error cloning repository: {}", repoUrl, e);
            throw new IOException("Error cloning repository: " + e.getMessage(), e);
        }
    }

    private void deleteDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                deleteDirectory(subFile);
            }
        }
        if (!file.delete()) {
            logger.warn("Failed to delete file or directory: {}", file.getAbsolutePath());
            throw new IOException("Failed to delete " + file.getAbsolutePath());
        }
    }

    private void analyzeFiles(File repoDir,
                              List<Map<String, Object>> classDetails,
                              List<Map<String, Object>> methodCalls,
                              List<Map<String, Object>> loopsAndConditionals,
                              Map<String, Object> repositoryInfo,
                              Map<String, Object> summary) throws IOException {
        logger.info("Starting analysis of files in directory: {}", repoDir.getAbsolutePath());

        List<File> javaFiles = getJavaFiles(repoDir);
        logger.info("Found {} Java files in the repository.", javaFiles.size());

        for (File file : javaFiles) {
            logger.info("Analyzing Java file: {}", file.getAbsolutePath());

            FileInputStream in = new FileInputStream(file);
            CompilationUnit cu = new JavaParser().parse(in).getResult().orElse(null);

            if (cu != null) {
                logger.debug("Successfully parsed file: {}", file.getName());

                cu.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                        super.visit(n, arg);

                        logger.debug("Found class/interface: {}", n.getNameAsString());

                        Map<String, Object> classDetail = new HashMap<>();
                        classDetail.put("className", n.getNameAsString());
                        classDetail.put("type", 
                                n.isInterface() ? "Interface" : 
                                n.isAbstract() ? "Abstract Class" : "Regular Class");

                        if (n.isInterface()) {
                            interfaces.incrementAndGet();
                            logger.debug("Class is an Interface.");
                        }
                        if (n.isAbstract()) {
                            abstractClasses.incrementAndGet();
                            logger.debug("Class is Abstract.");
                        }

                        List<Map<String, Object>> methods = new ArrayList<>();
                        for (MethodDeclaration method : n.getMethods()) {
                            Map<String, Object> methodDetail = analyzeMethod(method, methodCalls, loopsAndConditionals, summary);
                            methods.add(methodDetail);
                            totalMethods.incrementAndGet();
                            logger.debug("Method found: {}", method.getNameAsString());
                        }

                        classDetail.put("methods", methods);
                        classDetails.add(classDetail);
                        totalClasses.incrementAndGet();
                        logger.debug("Total classes so far: {}", totalClasses.get());
                    }
                }, null);
            } else {
                logger.warn("Failed to parse file: {}", file.getAbsolutePath());
            }
        }

        logger.info("File analysis completed. Total classes: {}, Total methods: {}", totalClasses.get(), totalMethods.get());

        repositoryInfo.put("totalClasses", totalClasses);
        repositoryInfo.put("totalMethods", totalMethods);
        summary.put("abstractClasses", abstractClasses);
        summary.put("interfaces", interfaces);

        logger.info("Repository analysis summary: abstractClasses = {}, interfaces = {}", abstractClasses.get(), interfaces.get());
    }

    private Map<String, Object> analyzeMethod(MethodDeclaration method,
                                              List<Map<String, Object>> methodCalls,
                                              List<Map<String, Object>> loopsAndConditionals,
                                              Map<String, Object> summary) {
        Map<String, Object> methodDetail = new HashMap<>();
        methodDetail.put("name", method.getNameAsString());
        boolean containsLoops = false, containsConditionals = false;

        if (method.getBody().isPresent()) {
            for (Statement stmt : method.getBody().get().getStatements()) {
                if (stmt.isForStmt() || stmt.isWhileStmt()) {
                    containsLoops = true;
                    methodsWithLoops.incrementAndGet();
                    loopsAndConditionals.add(Map.of("method", method.getNameAsString(), "loopType", stmt.toString()));
                }
                if (stmt.isIfStmt()) {
                    containsConditionals = true;
                    methodsWithConditionals.incrementAndGet();
                    loopsAndConditionals.add(Map.of("method", method.getNameAsString(), "conditionalType", stmt.toString()));
                }
                for (MethodCallExpr call : stmt.findAll(MethodCallExpr.class)) {
                    methodCalls.add(Map.of("caller", method.getNameAsString(), "calledMethod", call.getNameAsString()));
                }
            }
        }
        methodDetail.put("containsLoops", containsLoops);
        methodDetail.put("containsConditionals", containsConditionals);
        summary.put("methodWithLoops", methodsWithLoops);
        summary.put("methodWithConditionals", methodsWithConditionals);

        logger.debug("Method analysis for {} - Loops: {}, Conditionals: {}", method.getNameAsString(), containsLoops, containsConditionals);
        return methodDetail;
    }

    private List<File> getJavaFiles(File dir) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaFiles.addAll(getJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }
}
