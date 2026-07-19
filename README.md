# Black Sea Buoys Monitor

Android приложение за реални морски измервания, Open-Meteo прогноза и ориентировъчни оценки за морски спортове. Българските инструкции са в [README_BG.md](README_BG.md).

Рекламите са изключени по подразбиране. Дистанционното управление, кампаниите и прозрачността са описани в [REMOTE_CONFIG.md](REMOTE_CONFIG.md) и [ADVERTISING.md](ADVERTISING.md).
# Data sources and swell assessment

Air temperature is forecast data from Open-Meteo Weather API. Sea temperature, waves and swell remain separate Open-Meteo Marine API data. Observation-station values and forecast values retain distinct origins and captions.

The swell-conditions indicator is a configurable product heuristic, not an official warning or safety guarantee. It uses forecast swell height, period and direction and does not establish the presence of a rip current at a specific beach.

## Marine activity profiles

All 12 activities have distinct factory thresholds, required parameters and minimum useful-window durations. Daylight activities use Open-Meteo sunrise +30 minutes through sunset −30 minutes, with a local 07:00–19:00 fallback. Fishing permits day-and-night evaluation by default. These profiles are indicative product defaults, not official safety standards.
