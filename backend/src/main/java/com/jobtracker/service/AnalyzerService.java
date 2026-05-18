package com.jobtracker.service;

import com.jobtracker.dto.request.AnalyzeRequest;
import com.jobtracker.dto.response.AnalysisResult;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resume & JD Analyzer using weighted TF-IDF with semantic skill clustering.
 * No external APIs needed — runs entirely in-process.
 *
 * Algorithm:
 * 1. Extract candidate keywords from JD using stop-word filtered tokenization
 * 2. Build TF score for each keyword based on JD frequency
 * 3. Match against resume text using semantic clusters (aliases)
 * 4. Weighted match % = (sum of matched TF scores) / (sum of all JD keyword TF scores)
 * 5. Identify missing skills and generate targeted recommendations
 */
@Service
public class AnalyzerService {

    // Tech skill semantic clusters: canonical name -> list of aliases
    private static final Map<String, List<String>> SKILL_CLUSTERS = new LinkedHashMap<>();

    // Weightings for different skill categories
    private static final Map<String, Double> CATEGORY_WEIGHTS = new HashMap<>();

    static {
        // Programming Languages
        SKILL_CLUSTERS.put("java", List.of("java", "core java", "java8", "java11", "java17", "jdk", "jvm"));
        SKILL_CLUSTERS.put("python", List.of("python", "python3", "py"));
        SKILL_CLUSTERS.put("javascript", List.of("javascript", "js", "ecmascript", "es6", "es2015", "es2020"));
        SKILL_CLUSTERS.put("typescript", List.of("typescript", "ts"));
        SKILL_CLUSTERS.put("go", List.of("golang", "go"));
        SKILL_CLUSTERS.put("rust", List.of("rust", "rustlang"));
        SKILL_CLUSTERS.put("c++", List.of("c++", "cpp", "c plus plus"));
        SKILL_CLUSTERS.put("c#", List.of("c#", "csharp", "c sharp", ".net", "dotnet"));
        SKILL_CLUSTERS.put("kotlin", List.of("kotlin"));
        SKILL_CLUSTERS.put("scala", List.of("scala"));
        SKILL_CLUSTERS.put("ruby", List.of("ruby", "ruby on rails", "rails"));
        SKILL_CLUSTERS.put("php", List.of("php", "laravel", "symfony"));

        // Web Frameworks
        SKILL_CLUSTERS.put("spring", List.of("spring", "spring boot", "springboot", "spring framework", "spring mvc", "spring security", "spring data"));
        SKILL_CLUSTERS.put("react", List.of("react", "reactjs", "react.js", "react js", "react native"));
        SKILL_CLUSTERS.put("angular", List.of("angular", "angularjs", "angular.js"));
        SKILL_CLUSTERS.put("vue", List.of("vue", "vuejs", "vue.js"));
        SKILL_CLUSTERS.put("node", List.of("node", "nodejs", "node.js", "express", "expressjs"));
        SKILL_CLUSTERS.put("django", List.of("django", "drf", "django rest framework"));
        SKILL_CLUSTERS.put("fastapi", List.of("fastapi", "fast api"));
        SKILL_CLUSTERS.put("flask", List.of("flask"));
        SKILL_CLUSTERS.put("nextjs", List.of("next.js", "nextjs", "next js"));

        // Databases
        SKILL_CLUSTERS.put("postgresql", List.of("postgresql", "postgres", "psql", "pg"));
        SKILL_CLUSTERS.put("mysql", List.of("mysql", "mariadb"));
        SKILL_CLUSTERS.put("mongodb", List.of("mongodb", "mongo", "mongoose"));
        SKILL_CLUSTERS.put("redis", List.of("redis", "redis cache", "elasticache"));
        SKILL_CLUSTERS.put("elasticsearch", List.of("elasticsearch", "elastic", "opensearch"));
        SKILL_CLUSTERS.put("cassandra", List.of("cassandra", "apache cassandra"));
        SKILL_CLUSTERS.put("oracle", List.of("oracle", "oracle db", "oracle database"));
        SKILL_CLUSTERS.put("sql", List.of("sql", "t-sql", "pl/sql", "plsql", "relational database", "rdbms"));
        SKILL_CLUSTERS.put("dynamodb", List.of("dynamodb", "dynamo"));

        // Cloud & DevOps
        SKILL_CLUSTERS.put("aws", List.of("aws", "amazon web services", "ec2", "s3", "lambda", "ecs", "eks", "cloudformation", "rds", "sqs", "sns"));
        SKILL_CLUSTERS.put("gcp", List.of("gcp", "google cloud", "google cloud platform", "bigquery", "gke", "cloud run"));
        SKILL_CLUSTERS.put("azure", List.of("azure", "microsoft azure", "aks", "azure functions"));
        SKILL_CLUSTERS.put("docker", List.of("docker", "dockerfile", "container", "containerization"));
        SKILL_CLUSTERS.put("kubernetes", List.of("kubernetes", "k8s", "helm", "kubectl"));
        SKILL_CLUSTERS.put("terraform", List.of("terraform", "iac", "infrastructure as code"));
        SKILL_CLUSTERS.put("ci/cd", List.of("ci/cd", "cicd", "jenkins", "github actions", "gitlab ci", "circle ci", "travis ci", "bamboo", "continuous integration", "continuous deployment"));
        SKILL_CLUSTERS.put("ansible", List.of("ansible", "configuration management"));

        // Message Queues
        SKILL_CLUSTERS.put("kafka", List.of("kafka", "apache kafka", "event streaming", "message queue", "rabbitmq", "activemq", "messaging"));

        // Architecture
        SKILL_CLUSTERS.put("microservices", List.of("microservices", "micro-services", "service-oriented", "soa", "distributed systems", "distributed architecture"));
        SKILL_CLUSTERS.put("rest api", List.of("rest", "restful", "rest api", "http api", "web services", "api design"));
        SKILL_CLUSTERS.put("graphql", List.of("graphql", "graph ql"));
        SKILL_CLUSTERS.put("grpc", List.of("grpc", "protobuf", "protocol buffers"));

        // Testing
        SKILL_CLUSTERS.put("unit testing", List.of("unit testing", "unit test", "junit", "pytest", "jest", "mocha", "chai", "testing"));
        SKILL_CLUSTERS.put("tdd", List.of("tdd", "test driven development", "bdd", "behavior driven"));
        SKILL_CLUSTERS.put("selenium", List.of("selenium", "cypress", "playwright", "e2e testing", "end-to-end"));

        // Data & ML
        SKILL_CLUSTERS.put("machine learning", List.of("machine learning", "ml", "deep learning", "neural network", "ai", "artificial intelligence"));
        SKILL_CLUSTERS.put("data science", List.of("data science", "data analysis", "data analytics", "pandas", "numpy", "scipy", "matplotlib"));
        SKILL_CLUSTERS.put("spark", List.of("spark", "apache spark", "pyspark", "big data"));

        // Security
        SKILL_CLUSTERS.put("security", List.of("security", "oauth", "oauth2", "jwt", "authentication", "authorization", "ssl", "tls", "encryption"));

        // Soft skills & processes
        SKILL_CLUSTERS.put("agile", List.of("agile", "scrum", "kanban", "sprint", "jira", "confluence", "project management"));
        SKILL_CLUSTERS.put("git", List.of("git", "github", "gitlab", "bitbucket", "version control", "vcs"));
        SKILL_CLUSTERS.put("linux", List.of("linux", "unix", "bash", "shell scripting", "bash scripting", "shell"));

        // Category weights: more impactful skills = higher weight
        CATEGORY_WEIGHTS.put("java", 1.5);
        CATEGORY_WEIGHTS.put("python", 1.5);
        CATEGORY_WEIGHTS.put("javascript", 1.5);
        CATEGORY_WEIGHTS.put("typescript", 1.3);
        CATEGORY_WEIGHTS.put("spring", 1.4);
        CATEGORY_WEIGHTS.put("react", 1.4);
        CATEGORY_WEIGHTS.put("microservices", 1.3);
        CATEGORY_WEIGHTS.put("aws", 1.3);
        CATEGORY_WEIGHTS.put("docker", 1.2);
        CATEGORY_WEIGHTS.put("kubernetes", 1.2);
        CATEGORY_WEIGHTS.put("postgresql", 1.1);
        CATEGORY_WEIGHTS.put("kafka", 1.2);
        CATEGORY_WEIGHTS.put("rest api", 1.1);
    }

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "is", "are", "was", "were", "will", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "not", "from", "by", "as",
            "we", "you", "your", "our", "their", "they", "he", "she", "it", "this",
            "that", "these", "those", "all", "any", "both", "each", "few", "more",
            "most", "must", "can", "could", "would", "should", "may", "might",
            "work", "team", "experience", "knowledge", "understanding", "ability",
            "skills", "skill", "strong", "excellent", "good", "great", "proven",
            "looking", "seeking", "join", "candidate", "position", "role", "years",
            "year", "plus", "least", "preferred", "required", "responsibilities",
            "qualifications", "requirements", "opportunity", "company"
    ));

    public AnalysisResult analyze(AnalyzeRequest request) {
        String jdLower = request.getJobDescription().toLowerCase();
        String resumeLower = request.getResumeText().toLowerCase();

        // Step 1: Extract weighted skill scores from JD
        Map<String, Double> jdSkillScores = extractSkillScores(jdLower);

        if (jdSkillScores.isEmpty()) {
            // Fallback: use raw keyword frequency matching
            return performRawKeywordAnalysis(jdLower, resumeLower);
        }

        // Step 2: Match against resume
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        double totalScore = 0.0;
        double matchedScore = 0.0;

        for (Map.Entry<String, Double> entry : jdSkillScores.entrySet()) {
            String skill = entry.getKey();
            double score = entry.getValue();
            totalScore += score;

            if (skillPresentInText(skill, resumeLower)) {
                matchedSkills.add(formatSkillName(skill));
                matchedScore += score;
            } else {
                missingSkills.add(formatSkillName(skill));
            }
        }

        double matchPercentage = totalScore > 0 ? Math.min((matchedScore / totalScore) * 100.0, 100.0) : 0.0;
        matchPercentage = Math.round(matchPercentage * 10.0) / 10.0;

        // Step 3: Generate recommendations
        List<String> recommendations = generateRecommendations(
                missingSkills, matchPercentage, matchedSkills.size(), jdSkillScores.size()
        );

        String overallFeedback = generateOverallFeedback(matchPercentage, missingSkills.size());

        return AnalysisResult.builder()
                .matchPercentage(matchPercentage)
                .matchedSkills(matchedSkills)
                .missingSkills(missingSkills)
                .recommendations(recommendations)
                .totalJobKeywords(jdSkillScores.size())
                .matchedKeywords(matchedSkills.size())
                .overallFeedback(overallFeedback)
                .build();
    }

    public String extractTextFromPdf(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse PDF document", e);
        }
    }

    private Map<String, Double> extractSkillScores(String jd) {
        Map<String, Double> scores = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> cluster : SKILL_CLUSTERS.entrySet()) {
            String canonicalSkill = cluster.getKey();
            List<String> aliases = cluster.getValue();

            double termFrequency = 0.0;
            for (String alias : aliases) {
                termFrequency += countOccurrences(jd, alias);
            }

            if (termFrequency > 0) {
                double weight = CATEGORY_WEIGHTS.getOrDefault(canonicalSkill, 1.0);
                // TF score = term frequency * category weight, capped at 3.0
                double tfScore = Math.min(termFrequency, 3.0) * weight;
                scores.put(canonicalSkill, tfScore);
            }
        }

        return scores;
    }

    private boolean skillPresentInText(String skill, String text) {
        List<String> aliases = SKILL_CLUSTERS.getOrDefault(skill, List.of(skill));
        for (String alias : aliases) {
            if (containsSkill(text, alias)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSkill(String text, String skill) {
        // Use word boundary matching for single words, substring for multi-word phrases
        if (skill.contains(" ")) {
            return text.contains(skill);
        }
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b");
        return pattern.matcher(text).find();
    }

    private long countOccurrences(String text, String term) {
        if (term.isEmpty()) return 0;
        long count = 0;
        int idx = 0;
        while ((idx = text.indexOf(term, idx)) != -1) {
            count++;
            idx += term.length();
        }
        return count;
    }

    private List<String> generateRecommendations(
            List<String> missingSkills, double matchPercentage, int matched, int total) {
        List<String> recs = new ArrayList<>();

        if (matchPercentage < 40) {
            recs.add("Your profile has significant skill gaps for this role. Consider building foundational skills first.");
        } else if (matchPercentage < 60) {
            recs.add("You match several key requirements but there are important gaps to address before applying.");
        } else if (matchPercentage < 80) {
            recs.add("Good alignment with the JD! Addressing the missing skills could make you a strong candidate.");
        } else {
            recs.add("Excellent match! Highlight your key matching skills prominently in your resume summary.");
        }

        if (!missingSkills.isEmpty()) {
            int showCount = Math.min(missingSkills.size(), 4);
            for (int i = 0; i < showCount; i++) {
                String skill = missingSkills.get(i);
                recs.add(String.format("Add '%s' to your profile — build a portfolio project or complete a certification.", skill));
            }
        }

        if (matched > 0) {
            recs.add("Quantify your experience with matched skills using metrics (e.g., 'Reduced API latency by 40% using Spring Boot caching').");
        }

        recs.add("Tailor your resume summary section to mirror the exact language used in this job description.");

        if (missingSkills.size() > 4) {
            recs.add(String.format("Consider %d additional missing skills after addressing the priority ones above.", missingSkills.size() - 4));
        }

        return recs;
    }

    private String generateOverallFeedback(double matchPercentage, int missingCount) {
        if (matchPercentage >= 80) return "Strong Match — You are a competitive candidate for this role.";
        if (matchPercentage >= 60) return "Good Match — With minor skill additions, you'd be a strong fit.";
        if (matchPercentage >= 40) return "Moderate Match — Significant upskilling recommended before applying.";
        return "Weak Match — This role requires substantial skills you haven't listed yet.";
    }

    private String formatSkillName(String skill) {
        // Title-case canonical skill name
        String[] words = skill.split("\\s+");
        return Arrays.stream(words)
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    /**
     * Fallback: raw keyword frequency analysis when no skill clusters match.
     */
    private AnalysisResult performRawKeywordAnalysis(String jd, String resume) {
        String[] jdWords = jd.split("[\\s,;.!?()\\[\\]{}\"']+");
        Map<String, Long> jdFreq = Arrays.stream(jdWords)
                .map(String::toLowerCase)
                .filter(w -> w.length() > 3 && !STOP_WORDS.contains(w))
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String keyword : jdFreq.keySet()) {
            if (containsSkill(resume, keyword)) {
                matched.add(keyword);
            } else {
                missing.add(keyword);
            }
        }

        int total = matched.size() + missing.size();
        double pct = total > 0 ? Math.round((matched.size() * 100.0 / total) * 10.0) / 10.0 : 0.0;

        return AnalysisResult.builder()
                .matchPercentage(pct)
                .matchedSkills(matched.stream().limit(20).collect(Collectors.toList()))
                .missingSkills(missing.stream().limit(20).collect(Collectors.toList()))
                .recommendations(List.of(
                        "Tailor your resume to include keywords from the job description.",
                        "Quantify your experience with metrics.",
                        "Align your profile summary with the role requirements."
                ))
                .totalJobKeywords(total)
                .matchedKeywords(matched.size())
                .overallFeedback(pct >= 60 ? "Good Match" : pct >= 40 ? "Moderate Match" : "Weak Match")
                .build();
    }
}
