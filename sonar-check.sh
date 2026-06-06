#!/bin/bash

set -e

echo "## 📊 Комбінований звіт якості (SonarQube Cloud)" > sonar-report.md
echo "" >> sonar-report.md

echo "### 🆕 Якість НОВОГО коду (зміни в цьому PR/комміті)" >> sonar-report.md
echo "| Проєкт | Статус | Надійність | Безпека | Підтримка | Покриття змін |" >> sonar-report.md
echo "| --- | --- | :---: | :---: | :---: | --- |" >> sonar-report.md

echo "Очікування оновлення даних у хмарі (Background Tasks)..."
sleep 25

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

cat /dev/null > /tmp/overall_rows.txt

for PROJECT in "ArsenMonets_newshub_backend" "ArsenMonets_newshub_frontend"; do
  METRICS=$(curl -s -u "${SONAR_TOKEN}:" "https://sonarcloud.io/api/measures/component?component=${PROJECT}&metricKeys=alert_status,new_coverage,new_reliability_rating,new_security_rating,new_sqale_rating,coverage,reliability_rating,security_rating,sqale_rating")

  # Конструкція (.value // "1") захищає від пустих значень, якщо метрики немає для нового коду
  STATUS=$(echo "$METRICS" | jq -r '.component.measures[]? | select(.metric=="alert_status") | .value' || echo "N/A")
  
  NEW_COVERAGE=$(echo "$METRICS" | jq -r '(.component.measures[]? | select(.metric=="new_coverage") | .value) // "0"' || echo "0")
  NEW_BUG_RATING=$(echo "$METRICS" | jq -r '(.component.measures[]? | select(.metric=="new_reliability_rating") | .value) // "1"' || echo "1")
  NEW_VULN_RATING=$(echo "$METRICS" | jq -r '(.component.measures[]? | select(.metric=="new_security_rating") | .value) // "1"' || echo "1")
  NEW_SMELL_RATING=$(echo "$METRICS" | jq -r '(.component.measures[]? | select(.metric=="new_sqale_rating") | .value) // "1"' || echo "1")

  TOTAL_COVERAGE=$(echo "$METRICS" | jq -r '(.component.measures[]? | select(.metric=="coverage") | .value) // "0"' || echo "0")
  TOTAL_BUG_RATING=$(echo "$METRICS" | jq -r '(.component.measures[]? | select(.metric=="reliability_rating") | .value) // "1"' || echo "1")
  TOTAL_VULN_RATING=$(echo "$METRICS" | jq -r '(.component.measures[]? | select(.metric=="security_rating") | .value) // "1"' || echo "1")
  TOTAL_SMELL_RATING=$(echo "$METRICS" | jq -r '(.component.measures[]? | select(.metric=="sqale_rating") | .value) // "1"' || echo "1")

  NEW_BUG_L=$(get_rating_letter "$NEW_BUG_RATING")
  NEW_VULN_L=$(get_rating_letter "$NEW_VULN_RATING")
  NEW_SMELL_L=$(get_rating_letter "$NEW_SMELL_RATING")
  
  TOTAL_BUG_L=$(get_rating_letter "$TOTAL_BUG_RATING")
  TOTAL_VULN_L=$(get_rating_letter "$TOTAL_VULN_RATING")
  TOTAL_SMELL_L=$(get_rating_letter "$TOTAL_SMELL_RATING")
  
  if [ "$STATUS" = "OK" ]; then STATUS_TEXT="✅ PASSED"; else STATUS_TEXT="❌ FAILED"; fi
  DISPLAY_NAME=$(echo "$PROJECT" | sed 's/ArsenMonets_//')

  echo "| ${DISPLAY_NAME} | ${STATUS_TEXT} | ${NEW_BUG_L} | ${NEW_VULN_L} | ${NEW_SMELL_L} | 🧪 ${NEW_COVERAGE}% |" >> sonar-report.md

  echo "| ${DISPLAY_NAME} | ${TOTAL_BUG_L} | ${TOTAL_VULN_L} | ${TOTAL_SMELL_L} | 🧪 ${TOTAL_COVERAGE}% |" >> /tmp/overall_rows.txt
done

echo "" >> sonar-report.md
echo "### 🗄️ Загальний стан всього проєкту (Overall Code)" >> sonar-report.md
echo "| Проєкт | Надійність | Безпека | Підтримка | Повне покриття |" >> sonar-report.md
echo "| --- | :---: | :---: | :---: | --- |" >> sonar-report.md
cat /tmp/overall_rows.txt >> sonar-report.md

cat sonar-report.md >> $GITHUB_STEP_SUMMARY