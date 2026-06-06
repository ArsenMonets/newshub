#!/bin/bash

# Check if SONAR_TOKEN is present (prevents empty API requests)
if [ -z "$SONAR_TOKEN" ]; then
  echo "❌ Error: SONAR_TOKEN environment variable is not set!"
  exit 1
fi

REPORT_FILE="sonar-report.md"

# 1. Initialize Dynamic Report Table
echo "## 📊 Latest Quality Report from SonarQube Cloud" > "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "| Project | Status | Reliability (Bugs) | Security (Vuln) | Maintainability (Smells) | Coverage |" >> "$REPORT_FILE"
echo "| --- | --- | :---: | :---: | :---: | --- |" >> "$REPORT_FILE"

echo "Waiting for SonarCloud data synchronization (15 sec)..."
sleep 15

# Helper function to map dynamic rating grades to clean emojis
get_rating_letter() {
  case "$1" in
    1.0|1) echo "🟢 **A**" ;;
    2.0|2) echo "🟡 **B**" ;;
    3.0|3) echo "🟠 **C**" ;;
    4.0|4) echo "🔴 **D**" ;;
    5.0|5) echo "💀 **E**" ;;
    *)     echo "⚪ N/A" ;;
  esac
}

# 2. Fetch and Parse Dynamic Metrics for Each Project
for PROJECT in "ArsenMonets_newshub_backend" "ArsenMonets_newshub_frontend"; do

  METRICS=$(curl -s -u "${SONAR_TOKEN}:" "https://sonarcloud.io/api/measures/component?component=${PROJECT}&metricKeys=alert_status,coverage,reliability_rating,security_rating,sqale_rating")
  
  # Safe JSON parsing with jq
  STATUS=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="alert_status") | .value' 2>/dev/null || echo "N/A")
  COVERAGE=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="coverage") | .value' 2>/dev/null || echo "0")
  
  BUG_RATING=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="reliability_rating") | .value' 2>/dev/null || echo "0")
  VULN_RATING=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="security_rating") | .value' 2>/dev/null || echo "0")
  SMELL_RATING=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="sqale_rating") | .value' 2>/dev/null || echo "0")
  
  BUG_LETTER=$(get_rating_letter "$BUG_RATING")
  VULN_LETTER=$(get_rating_letter "$VULN_RATING")
  SMELL_LETTER=$(get_rating_letter "$SMELL_RATING")

  if [ "$STATUS" = "OK" ]; then 
    STATUS_TEXT="✅ PASSED"
  else 
    STATUS_TEXT="❌ FAILED"
  fi

  DISPLAY_NAME=$(echo "$PROJECT" | sed 's/ArsenMonets_//')

  echo "| ${DISPLAY_NAME} | ${STATUS_TEXT} | ${BUG_LETTER} | ${VULN_LETTER} | ${SMELL_LETTER} | 🧪 ${COVERAGE}% |" >> "$REPORT_FILE"
done

# 3. Append Static Badges Section (Including Unit Tests Count Metrics)
cat << 'EOF' >> "$REPORT_FILE"

### ☕ NewsHub Backend Core

| Metric | Badge | Analysis Link |
| --- | --- | --- |
| **Quality Gate** | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ArsenMonets_newshub_backend) | [Project Overview](https://sonarcloud.io/summary/new_code?id=ArsenMonets_newshub_backend) |
| **Code Coverage** | [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_backend&metric=coverage)](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_backend&metric=coverage) | [Coverage Analysis](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_backend&metric=coverage) |
| **Unit Tests Count** | [![Tests](https://img.shields.io/sonar/tests/ArsenMonets_newshub_backend?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_backend&metric=tests) | [Test Suites](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_backend&metric=tests) |
| **Reliability (Bugs)** | [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_backend&metric=bugs)](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_backend&resolved=false&types=BUG) | [Bug Tracking List](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_backend&resolved=false&types=BUG) |
| **Maintainability (Code Smells)** | [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_backend&metric=code_smells)](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_backend&resolved=false&types=CODE_SMELL) | [Code Smells Review](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_backend&resolved=false&types=CODE_SMELL) |

### 🅰️ NewsHub Frontend UI

| Metric | Badge | Analysis Link |
| --- | --- | --- |
| **Quality Gate** | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_frontend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ArsenMonets_newshub_frontend) | [Project Overview](https://sonarcloud.io/summary/new_code?id=ArsenMonets_newshub_frontend) |
| **Code Coverage** | [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_frontend&metric=coverage)](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_frontend&metric=coverage) | [Coverage Analysis](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_frontend&metric=coverage) |
| **Unit Tests Count** | [![Tests](https://img.shields.io/sonar/tests/ArsenMonets_newshub_frontend?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_frontend&metric=tests) | [Test Suites](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_frontend&metric=tests) |
| **Reliability (Bugs)** | [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_frontend&metric=bugs)](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_frontend&resolved=false&types=BUG) | [Bug Tracking List](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_frontend&resolved=false&types=BUG) |
| **Maintainability (Code Smells)** | [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_frontend&metric=code_smells)](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_frontend&resolved=false&types=CODE_SMELL) | [Code Smells Review](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_frontend&resolved=false&types=CODE_SMELL) |
EOF

# 4. Export Report to GitHub Step Summary
if [ -n "$GITHUB_STEP_SUMMARY" ]; then
  cat "$REPORT_FILE" >> "$GITHUB_STEP_SUMMARY"
fi

echo "✅ Quality report successfully generated in $REPORT_FILE and exported to GitHub Step Summary!"