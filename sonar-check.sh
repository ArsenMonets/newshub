#!/bin/bash

# Перевірка наявності токена (опціонально, але рятує від пустих запитів)
if [ -z "$SONAR_TOKEN" ]; then
  echo "❌ Помилка: Змінна SONAR_TOKEN не задана!"
  exit 1
fi

REPORT_FILE="sonar-report.md"

# 1. Ініціалізація динамічного звіту
echo "## 📊 Останній звіт якості з SonarQube Cloud" > "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "| Проєкт | Статус | Надійність (Bugs) | Безпека (Vuln) | Підтримка (Smells) | Покриття |" >> "$REPORT_FILE"
echo "| --- | --- | :---: | :---: | :---: | --- |" >> "$REPORT_FILE"

echo "Очікування оновлення даних у хмарі (15 сек)..."
sleep 15

# Функція конвертації оцінок у наочні емодзі
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

# 2. Збір метрик по проєктах
for PROJECT in "ArsenMonets_newshub_backend" "ArsenMonets_newshub_frontend"; do

  METRICS=$(curl -s -u "${SONAR_TOKEN}:" "https://sonarcloud.io/api/measures/component?component=${PROJECT}&metricKeys=alert_status,coverage,reliability_rating,security_rating,sqale_rating")
  
  # Безпечний парсинг за допомогою jq
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

# 3. Додавання статичної секції з бейджами (використовуємо heredoc `cat << 'EOF'`)
cat << 'EOF' >> "$REPORT_FILE"

---

## 📈 CI/CD Pipeline & SonarCloud Quality Metrics

Every push to the `main` branch and each Pull Request is automatically tested and analyzed in the cloud. Below are the live code quality metrics updated in real-time directly from **SonarQube Cloud**.

### ☕ NewsHub Backend Core

| Metric | Badge | Analysis Link |
| --- | --- | --- |
| **Quality Gate** | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ArsenMonets_newshub_backend) | [Project Overview](https://sonarcloud.io/summary/new_code?id=ArsenMonets_newshub_backend) |
| **Code Coverage** | [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_backend&metric=coverage)](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_backend&metric=coverage) | [Coverage Analysis](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_backend&metric=coverage) |
| **Reliability (Bugs)** | [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_backend&metric=bugs)](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_backend&resolved=false&types=BUG) | [Bug Tracking List](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_backend&resolved=false&types=BUG) |
| **Maintainability (Code Smells)** | [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_backend&metric=code_smells)](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_backend&resolved=false&types=CODE_SMELL) | [Code Smells Review](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_backend&resolved=false&types=CODE_SMELL) |

### 🅰️ NewsHub Frontend UI

| Metric | Badge | Analysis Link |
| --- | --- | --- |
| **Quality Gate** | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_frontend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ArsenMonets_newshub_frontend) | [Project Overview](https://sonarcloud.io/summary/new_code?id=ArsenMonets_newshub_frontend) |
| **Code Coverage** | [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_frontend&metric=coverage)](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_frontend&metric=coverage) | [Coverage Analysis](https://sonarcloud.io/component_measures?id=ArsenMonets_newshub_frontend&metric=coverage) |
| **Reliability (Bugs)** | [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_frontend&metric=bugs)](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_frontend&resolved=false&types=BUG) | [Bug Tracking List](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_frontend&resolved=false&types=BUG) |
| **Maintainability (Code Smells)** | [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ArsenMonets_newshub_frontend&metric=code_smells)](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_frontend&resolved=false&types=CODE_SMELL) | [Code Smells Review](https://sonarcloud.io/project/issues?id=ArsenMonets_newshub_frontend&resolved=false&types=CODE_SMELL) |

> ℹ️ Clicking on any badge or link redirects you directly to the corresponding section of the project dashboard on SonarCloud for deep-dive log investigation and code line tracing.
EOF

# 4. Вивід у GitHub Step Summary (для відображення в інтерфейсі CI/CD)
if [ -n "$GITHUB_STEP_SUMMARY" ]; then
  cat "$REPORT_FILE" >> "$GITHUB_STEP_SUMMARY"
fi

echo "✅ Звіт успішно згенеровано у $REPORT_FILE та відправлено у GitHub Summary!"