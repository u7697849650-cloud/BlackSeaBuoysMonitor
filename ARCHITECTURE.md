# Архитектура

Compose UI → ViewModel/StateFlow → Repository → заменяем `MarineDataProvider` → HTML или mock източник. Repository записва валидните наблюдения в Room и при мрежова грешка връща кеша с ясно обозначение. DataStore пази избора и настройките. WorkManager изпълнява ограничено, устойчиво фоново обновяване.

HTML parser-ът намира колоните по нормализирани заглавия, приема точка и запетая, празни клетки и разместени колони и връща ясна структурна грешка.

Прогнозният модул е отделен: Compose → `MarineForecastRepository` → `MarineForecastProvider` → Retrofit/Open-Meteo. Координатите са в `marine_destinations.json`. Прогнозата се пази в собствена Room таблица с уникален ключ `destinationId + forecastAt` и никога не попълва липсващи измервания.

`domain/activity` съдържа независим от Compose evaluation engine, модели, консервативни правила и алгоритъм за сливане на последователни подходящи часове. `ui/activities` визуализира само готовите оценки и предупреждения.

Advertising е отделен слой: domain модели и selector → JSON provider/cache → manager/StateFlow → ненатрапчиви Compose компоненти. Всички remote flags са `false` по подразбиране и рекламна грешка не влияе на морските repositories.
