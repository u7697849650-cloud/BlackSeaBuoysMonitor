# Дистанционно управление

1. Публикувайте HTTPS JSON конфигурация.
2. При build задайте bootstrap адреса в `gradle.properties`: `AD_CONFIG_URL=https://example.com/ads/config.json`.
3. Конфигурацията задава `advertisingEnabled`, разрешените формати и `campaignsJsonUrl`.
4. В campaigns JSON добавете или заменете изображението чрез HTTPS `imageUrl` и задайте `backgroundOpacity` между `0.03` и `0.20`.
5. За спиране на кампания задайте `enabled:false` или минала `endsAt`.
6. За аварийно спиране на всичко задайте `advertisingEnabled:false`.

Пример:

```json
{
  "advertisingEnabled": true,
  "compactBannersEnabled": true,
  "sponsoredCardsEnabled": false,
  "backgroundAdsEnabled": false,
  "admobEnabled": false,
  "campaignsJsonUrl": "https://example.com/ads/campaigns.json",
  "defaultBackgroundOpacity": 0.08,
  "minimumBackgroundOpacity": 0.03,
  "maximumBackgroundOpacity": 0.20
}
```

Ако bootstrap URL е празен, невалиден или недостъпен и няма кеш, всички рекламни флагове остават `false`.
