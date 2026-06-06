#!/bin/bash

echo "## 📊 Останній звіт якості з SonarQube Cloud" > sonar-report.md
echo "" >> sonar-report.md
echo "| Проєкт | Статус | Надійність (Bugs) | Безпека (Vuln) | Підтримка (Smells) | Покриття |" >> sonar-report.md
echo "| --- | --- | :---: | :---: | :---: | --- |" >> sonar-report.md

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
  METRICS=$(curl -s -u "${SONAR_TOKEN}:" "https://sonarcloud.io/api/measures/component?component=${PROJECT}&metricKeys=alert_status,coverage,reliability_rating,security_rating,sqale_rating")
  
  STATUS=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="alert_status") | .value' || echo "N/A")
  COVERAGE=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="coverage") | .value' || echo "0")
  
  # Витягуємо сирі цифри рейтингів
  BUG_RATING=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="reliability_rating") | .value' || echo "0")
  VULN_RATING=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="security_rating") | .value' || echo "0")
  SMELL_RATING=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="sqale_rating") | .value' || echo "0")
  
  BUG_LETTER=$(get_rating_letter "$BUG_RATING")
  VULN_LETTER=$(get_rating_letter "$VULN_RATING")
  SMELL_LETTER=$(get_rating_letter "$SMELL_RATING")

  if [ "$STATUS" = "OK" ]; then 
    STATUS_TEXT="✅ PASSED"
  else 
    STATUS_TEXT="❌ FAILED"
  fi

  DISPLAY_NAME=$(echo "$PROJECT" | sed 's/ArsenMonets_//')

  echo "| ${DISPLAY_NAME} | ${STATUS_TEXT} | ${BUG_LETTER} | ${VULN_LETTER} | ${SMELL_LETTER} | 🧪 ${COVERAGE}% |" >> sonar-report.md
done

cat sonar-report.md >> $GITHUB_STEP_SUMMARY