#!/bin/bash

echo "## 📊 Останній звіт якості з SonarQube Cloud" > sonar-report.md
echo "" >> sonar-report.md

# Шапка таблиці
echo "| Проєкт | Статус | Надійність | Безпека | Підтримка | Покриття | Нові Баги | Нова Безпека | Nova Підтримка | Покриття Змін |" >> sonar-report.md
echo "| --- | --- | :---: | :---: | :---: | --- | :---: | :---: | :---: | --- |" >> sonar-report.md

echo "Очікування оновлення даних у хмарі..."
sleep 15

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

for PROJECT in "ArsenMonets_newshub_backend" "ArsenMonets_newshub_frontend"; do
  # Запит до SonarCloud API
  METRICS=$(curl -s -u "${SONAR_TOKEN}:" "https://sonarcloud.io/api/measures/component?component=${PROJECT}&metricKeys=alert_status,coverage,reliability_rating,security_rating,sqale_rating,new_coverage,new_reliability_rating,new_security_rating,new_sqale_rating")
  
  STATUS=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="alert_status") | .value' || echo "N/A")
  
  # Твій оригінальний парсинг загального покриття (без змін)
  COVERAGE=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="coverage") | .value' || echo "0")
  
  # Рейтинги для ВСЬОГО проєкту
  BUG_RATING=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="reliability_rating") | .value' || echo "0")
  VULN_RATING=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="security_rating") | .value' || echo "0")
  SMELL_RATING=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="sqale_rating") | .value' || echo "0")
  
  # Метрики для НОВОГО коду
  NEW_COV=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="new_coverage") | .value' || echo "")
  NEW_BUG_R=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="new_reliability_rating") | .value' || echo "N/A")
  NEW_VULN_R=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="new_security_rating") | .value' || echo "N/A")
  NEW_SMELL_R=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="new_sqale_rating") | .value' || echo "N/A")

  # Конвертуємо рейтинги в літери
  BUG_LETTER=$(get_rating_letter "$BUG_RATING")
  VULN_LETTER=$(get_rating_letter "$VULN_RATING")
  SMELL_LETTER=$(get_rating_letter "$SMELL_RATING")
  
  NEW_BUG_LETTER=$(get_rating_letter "$NEW_BUG_R")
  NEW_VULN_LETTER=$(get_rating_letter "$NEW_VULN_R")
  NEW_SMELL_LETTER=$(get_rating_letter "$NEW_SMELL_R")

  # ПЕРЕВІРКА НА NULL ТІЛЬКИ ДЛЯ НОВОГО ПОКРИТТЯ
  if [ -n "$NEW_COV" ] && [ "$NEW_COV" != "null" ]; then
    NEW_COVERAGE_TEXT="🧪 ${NEW_COV}%"
  else
    NEW_COVERAGE_TEXT="⚪ N/A"
  fi

  # Твоя оригінальна перевірка статусу
  if [ "$STATUS" = "OK" ]; then
    STATUS_TEXT="✅ PASSED"
  elif [ "$STATUS" = "ERROR" ] && { [ -z "$NEW_COV" ] || [ "$NEW_COV" = "null" ]; }; then
    STATUS_TEXT="✅ PASSED (Main)"
  else
    STATUS_TEXT="❌ FAILED"
  fi

  DISPLAY_NAME=$(echo "$PROJECT" | sed 's/ArsenMonets_//')

  # Запис рядка в звіт
  echo "| ${DISPLAY_NAME} | ${STATUS_TEXT} | ${BUG_LETTER} | ${VULN_LETTER} | ${SMELL_LETTER} | 🧪 ${COVERAGE}% | ${NEW_BUG_LETTER} | ${NEW_VULN_LETTER} | ${NEW_SMELL_LETTER} | ${NEW_COVERAGE_TEXT} |" >> sonar-report.md
done

cat sonar-report.md >> $GITHUB_STEP_SUMMARY