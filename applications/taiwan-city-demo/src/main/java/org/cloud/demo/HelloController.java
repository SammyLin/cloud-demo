package org.cloud.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/")
    public String hello() {
        String label = System.getenv("APP_INSIDE_LABEL");
        if (label == null || label.isBlank()) {
            label = "(not set)";
        }
        return String.format("""
<!DOCTYPE html>
<html lang=\"en\">
<head>
    <meta charset=\"UTF-8\">
    <title>Azure App Service Demo</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 2em; }
        table { border-collapse: collapse; margin-top: 2em; }
        th, td { border: 1px solid #aaa; padding: 8px 16px; }
        caption { font-weight: bold; margin-bottom: 0.5em; }
    </style>
</head>
<body>
    <h2>Azure App Service - 內建環境變數 Demo</h2>
    <table>
        <caption>Built-in App Service Environment Variable</caption>
        <tr><th>Variable Name</th><th>Value</th></tr>
        <tr><td>APP_INSIDE_LABEL</td><td>%s</td></tr>
    </table>
    <p style=\"margin-top:2em; color:#888;\">此頁面由 Azure App Service Java Spring Boot 應用自動產生。</p>
</body>
</html>
""", label);
    }
}
