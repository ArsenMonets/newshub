#!/bin/bash

echo "## 📊 Останній звіт якості з SonarQube Cloud" > sonar-report.md
echo "" >> sonar-report.md
echo "| Проєкт | Статус Quality Gate | Баги | Вразливості | Code Smells | Покриття коду |" >> sonar-report.md
echo "| --- | --- | --- | --- | --- | --- |" >> sonar-report.md

echo "Очікування оновлення даних у хмарі..."
sleep 10

for PROJECT in "ArsenMonets_newshub_backend" "ArsenMonets_newshub_frontend"; do
  METRICS=$(curl -s -u "${SONAR_TOKEN}:" "https://sonarcloud.io/api/measures/component?component=${PROJECT}&metricKeys=alert_status,bugs,vulnerabilities,code_smells,coverage")
  
  STATUS=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="alert_status") | .value' || echo "N/A")
  BUGS=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="bugs") | .value' || echo "0")
  VULN=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="vulnerabilities") | .value' || echo "0")
  SMELLS=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="code_smells") | .value' || echo "0")
  COVERAGE=$(echo "$METRICS" | jq -r '.component.measures[] | select(.metric=="coverage") | .value' || echo "0")
  
  if [ "$STATUS" = "OK" ]; then STATUS_TEXT="🟢 PASSED"; else STATUS_TEXT="🔴 FAILED"; fi

  echo "| ${PROJECT} | ${STATUS_TEXT} | 🐛 ${BUGS} | 🛡️ ${VULN} | ⚠️ ${SMELLS} | 🧪 ${COVERAGE}% |" >> sonar-report.md
done

# Магія GitHub Actions: виводимо звіт прямо на головний екран запуску
cat sonar-report.md >> $GITHUB_STEP_SUMMARY