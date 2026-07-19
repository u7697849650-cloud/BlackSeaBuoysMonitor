# Black Sea Buoys Monitor — Compliance, Licensing and Release Audit

**Audit date:** 18 July 2026  
**Scope:** Android project source, resources, resolved release dependencies, generated release configuration and available APK/AAB artifacts.  
**Method:** Static source/configuration inspection, Gradle `releaseRuntimeClasspath` inventory, secret-pattern scan, artifact/config inspection, and review of current official provider/Google Play documentation.  
**Important:** This is a technical compliance audit, not legal advice. Where ownership or licence terms could not be proved from an authoritative source, the finding is explicitly marked **REQUIRES MANUAL LEGAL REVIEW**.

## Executive conclusion

# NOT READY FOR GOOGLE PLAY

The project builds and targets API 35, but it must not be treated as commercially cleared or Play-ready yet.

### Publication blockers

| ID | Severity | Finding | Blocks Play publication | Blocks commercial use |
|---|---|---|---|---|
| DS-01 | **Critical** | Meteo Varna data is obtained by HTML scraping over HTTP. No public data licence, redistribution permission, commercial-use permission, rate limit, or API terms were found. **REQUIRES MANUAL LEGAL REVIEW.** | Yes | Yes |
| DS-02 | **Critical** | The application calls the free Open-Meteo endpoints (`api.open-meteo.com` and `marine-api.open-meteo.com`). Open-Meteo currently describes the free hosted API as non-commercial and requires a paid customer endpoint/API key for commercial use. | If monetized | Yes |
| GP-01 | **Critical** | No app-specific Privacy Policy exists in the repository or in-app UI. Google Play requires a privacy policy for every app and a Data Safety declaration. | Yes | No |
| REL-01 | **Critical** | The produced release APK is explicitly `app-release-unsigned.apk`; no release signing configuration is present. | Yes | No |
| OSS-01 | **High** | No `LICENSE`, `NOTICE`, third-party licence inventory, or open-source acknowledgements are shipped. Apache-2.0 dependencies require preservation of licence/notice material where applicable. | Likely review/release blocker | Medium |
| ATTR-01 | **High** | Open-Meteo is named in UI, but the required CC BY 4.0 attribution is incomplete: no licence link, underlying data-provider attribution, or modification notice. | Potentially | Yes |
| SEC-01 | **High** | Measurements are fetched via cleartext HTTP from `mm.meteo-varna.net`; a domain exception explicitly enables cleartext traffic. Integrity and confidentiality are not protected. | Potentially | No |
| ADS-01 | **High** | Remote advertising can be enabled without a complete privacy/consent flow, publisher allow-list, content moderation contract, or documented Data Safety treatment. Remote images/actions disclose the user IP to campaign hosts. | Yes if enabled | Yes |

## 1. Data sources audit

### 1.1 Meteo Varna marine measurements

| Field | Result |
|---|---|
| Name | Meteo Varna marine monitoring page |
| URL | `http://mm.meteo-varna.net/en/?lang=Bg` |
| Data | Station name/time, sea temperature, significant/max wave height, wave period/direction, wind speed/direction |
| Type | Measurements/observations |
| Access method | **HTML scraping** with Jsoup; table headers and cells are parsed from the page |
| Cached | Yes. Observations are stored in Room (`buoys.db`) and exposed as cached data when refresh fails |
| Modified | Yes. HTML is parsed, normalized into internal models, deduplicated, stored, graphed, and used in derived sports scores |
| Transport | Cleartext HTTP, allowed only for this domain in `network_security_config.xml` |
| Licence | **Not found — REQUIRES MANUAL LEGAL REVIEW** |
| Terms of Service | No terms governing this measurement page/data were identified |
| Attribution | Source/station is shown in parts of the UI, but there is no formal attribution/licence screen |
| Commercial use | **Unknown — REQUIRES MANUAL LEGAL REVIEW** |
| Redistribution/cache permission | **Unknown — REQUIRES MANUAL LEGAL REVIEW** |
| Rate limit | Not published/found |
| Permission needed | Obtain written permission or a documented API/data licence before release, especially before monetization |

Evidence: `Providers.kt` defines the HTTP URL and user agent; `MarineHtmlParser.kt` calls `Jsoup.parse(...).select("table")`; `Database.kt` persists observations; `network_security_config.xml` permits cleartext only for the host.

**Risk:** Website HTML is not a stable or licensed API. Scraping may violate unpublished site rules, database rights, copyright/contract terms, operational expectations, or robots/rate limits. HTTP also allows tampering with safety-relevant values.

**Required action:** Contact the operator and obtain written permission covering automated access, caching, transformation, redistribution, attribution, commercial/advertising use, request frequency, availability and termination. Prefer a documented HTTPS JSON/API feed. Until then, do not commercially publish this source.

### 1.2 Open-Meteo Weather API

| Field | Result |
|---|---|
| Endpoint | `https://api.open-meteo.com/v1/forecast` |
| Data | Air/apparent temperature, wind, gusts, precipitation, thunderstorm probability, UV, sunrise/sunset and daily/hourly forecast data |
| Type | Model forecast |
| Cached | Atmospheric response is held in application state; no Room table was found for atmospheric data |
| Modified | Yes. Values are selected by time, summarized, graphed and used in sports scoring |
| Licence | API response/data described by Open-Meteo as **CC BY 4.0**; hosted free endpoint is described as **non-commercial** |
| Attribution | Required under CC BY 4.0; current UI naming alone is incomplete |
| Commercial use | Public/free hosted endpoint: **not allowed for commercial use under current published pricing/terms**. Commercial plan/customer endpoint required |
| Limits | Published free-tier limits: 600/minute, 5,000/hour, 10,000/day, 300,000/month; no uptime guarantee |
| Permission | Commercial subscription/API key or self-hosted/legal alternative before monetized release |

Official references: [Open-Meteo pricing and commercial licence](https://open-meteo.com/en/pricing), [Open-Meteo service overview](https://open-meteo.com/), [Open-Meteo OpenAPI licence/terms metadata](https://github.com/open-meteo/open-meteo/blob/main/openapi.yml).

### 1.3 Open-Meteo Marine API

| Field | Result |
|---|---|
| Endpoint | `https://marine-api.open-meteo.com/v1/marine` |
| Data | Wave height/direction/period, wind waves, swell, sea-surface temperature |
| Type | Marine model forecast |
| Cached | Yes. Marine forecast points are stored in Room and old entries are pruned |
| Modified | Yes. Aggregated into 24-hour/3-day/7-day cards, graphs and sports scores |
| Licence/terms | Same hosted-service/commercial and CC BY 4.0 concerns as above |
| Limits | Same published hosted API limits; actual call accounting may increase with variables/range |
| Permission | Commercial plan/customer endpoint or other documented commercial arrangement before monetization |

### 1.4 Remote advertising JSON and media

| Field | Result |
|---|---|
| Configuration URL | Build-time `AD_CONFIG_URL`; currently blank in release configuration |
| Campaign endpoint | Remotely supplied HTTPS JSON URL |
| Data | Ad configuration, campaign text, sponsor, image/action URLs, schedule, placements and targeting by station/sport |
| Cached | Yes, raw config/campaign JSON in SharedPreferences |
| Validation | HTTPS and no URL credentials; no hostname allow-list, signature, certificate pinning or content provenance validation |
| Licence | Depends on each future campaign asset/contract — **REQUIRES MANUAL LEGAL REVIEW** |

No RSS or XML source was found. No WebView was found. Retrofit is used for JSON REST calls; OkHttp is used directly for HTML and ad JSON.

## 2. Dependency licence audit

The release dependency tree was resolved with `:app:dependencies --configuration releaseRuntimeClasspath`. The table groups AndroidX/Compose modules that share the same licence. “Commercial” means the open-source licence generally permits commercial distribution when its conditions are followed.

| Dependency/group | Resolved/direct version | Licence | Commercial | Copyleft risk | Required handling |
|---|---:|---|---|---|---|
| Kotlin stdlib | 2.1.0 resolved | Apache-2.0 | Yes | No | Include licence/notice |
| Kotlinx Coroutines | 1.8.1 resolved | Apache-2.0 | Yes | No | Include licence/notice |
| Kotlinx Serialization | 1.8.0 | Apache-2.0 | Yes | No | Include licence/notice |
| AndroidX Core/Activity/Lifecycle/Navigation/DataStore/WorkManager/Room/SQLite and transitives | versions per Gradle tree | Apache-2.0 | Yes | No | Include licence/NOTICE material where supplied |
| Jetpack Compose UI/Foundation/Runtime/Material 3/Material Icons | BOM 2025.02.00; Compose 1.7.8 resolved | Apache-2.0 | Yes | No | Include licence/notice; Material Icons are Apache-2.0 |
| Retrofit | 2.11.0 | Apache-2.0 | Yes | No | Include licence/notice |
| OkHttp | 4.12.0 | Apache-2.0 | Yes | No | Include licence/notice |
| Okio | 3.9.0 resolved | Apache-2.0 | Yes | No | Include licence/notice |
| Coil / Coil Compose | 2.7.0 | Apache-2.0 | Yes | No | Include licence/notice |
| Jsoup | 1.18.3 | MIT | Yes | No | Preserve copyright/licence text |
| Accompanist DrawablePainter | 0.32.0 transitive | Apache-2.0 | Yes | No | Include licence/notice |
| JetBrains annotations | 23.0.0 resolved | Apache-2.0 | Yes | No | Include licence/notice |
| Guava `listenablefuture` stub | 1.0 transitive | Apache-2.0 | Yes | No | Include licence/notice |
| JUnit (test only) | 4.13.2 | EPL-1.0 | Yes | Weak copyleft, test-only/not shipped | Keep test tooling notices; not in runtime artifact |
| AndroidX test/JUnit extension (test only) | 1.2.1 | Apache-2.0 | Yes | No | Test-only |
| KSP/Android Gradle/Kotlin plugins | build tooling | Predominantly Apache-2.0; not runtime dependencies | Yes | No runtime inclusion | Keep build-tool licence records |

No Gson or Moshi dependency is present. No GPL, LGPL, MPL or Creative Commons **code library** was found in the declared runtime dependency set. Open-Meteo data is CC BY 4.0; its server source licence (AGPLv3) does not automatically apply to this app merely because the app calls the hosted API.

**Finding OSS-01:** `packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"` removes common embedded licence files from packaged resources while the project provides no replacement acknowledgements. This does not itself prove infringement, but creates a compliance gap. Generate a verified third-party notices file from the final resolved graph and make it available in the app/distribution.

## 3. Icons, fonts and images

### Icons

- Material icons (`Icons.Default.*`) come from `material-icons-extended` and are Apache-2.0.
- Sport pictograms and onboarding graphics are Unicode emoji rendered by the device font. They are not bundled image files, but appearance and glyph artwork vary by platform/vendor. Avoid claiming ownership of the rendered glyph artwork and test accessibility/localization.
- No SVG files were found.

### Fonts

- The theme uses Android generic `sans`; Compose uses platform/default typography.
- No Google Fonts download, `.ttf`, `.otf`, Roboto or Noto font file is bundled in the project.
- Platform fonts are supplied by Android and are not redistributed by this APK.

### Images

Only one bundled raster asset was found: `app/src/main/res/drawable-nodpi/app_icon.png` (also launcher/round icon). No splash illustration, background image or additional drawable was found.

**IMG-01 — High:** The repository contains no author/source, prompt record, assignment, stock licence, model terms or provenance metadata for `app_icon.png`. Its legal status cannot be proven from the repository. **REQUIRES MANUAL LEGAL REVIEW.** Retain creation records and confirm rights to all depicted elements and any generated-image service terms before publication.

Remote advertisement images are not bundled but are displayed by Coil. Each campaign requires a contract/licence confirming display, modification/cropping, territory, duration, audience and trademark rights.

## 4. API keys and secrets

Static scans found:

- No API keys, passwords, OAuth credentials, Firebase configuration, Google service key, AdMob app/unit ID, bearer token or credential-bearing URL.
- `AD_CONFIG_URL` is blank in the generated release BuildConfig.
- `PROVIDER_MODE=live` in `gradle.properties` and generated release BuildConfig.
- Open-Meteo public endpoints currently use no key. That is not proof of commercial permission.
- `local.properties` contains only the local Android SDK location and is not a service secret; it should remain uncommitted.

Do not place a future Open-Meteo commercial API key or ad-management secret directly in BuildConfig/source. A key embedded in a client APK is extractable. Use provider-supported mobile-key restrictions or a controlled backend/proxy.

## 5. Security audit

| Area | Status | Severity / recommendation |
|---|---|---|
| HTTP | Meteo Varna uses cleartext HTTP with a scoped exception | **High.** Susceptible to interception/modification. Obtain HTTPS/API endpoint; do not certificate-pin an HTTP service |
| HTTPS | Open-Meteo and accepted ad URLs require HTTPS | Good baseline |
| Certificate pinning | None | Low/optional for public APIs; pinning adds rotation risk. More important for a future authenticated backend |
| Network security config | Base cleartext disabled, one exact-domain exception | Better than global cleartext, but source remains insecure |
| WebView | None | No WebView risk found |
| FileProvider | None | No FileProvider exposure found |
| Permissions | `INTERNET`, `POST_NOTIFICATIONS` | Notification permission appears unused and should be removed or justified before submission |
| Exported components | Only launcher `MainActivity`, correctly exported for MAIN/LAUNCHER | Acceptable; no services/receivers/providers declared by app manifest |
| Backup | `android:allowBackup="true"`; no backup/data-extraction rules | **Medium.** Room observations/favourites and DataStore/ad preferences can be backed up. Define explicit backup policy or disable backup |
| Debuggable | Controlled by normal release build type; no explicit forced debug flag | Acceptable |
| Logging | No production `Log.*`/credential logging found | Acceptable |
| Hardcoded credentials | None found | Acceptable |
| Remote ad config integrity | HTTPS only, but unsigned JSON and arbitrary HTTPS hosts | **High if enabled.** Add trusted host allow-list/signature and operational content review |
| Database migration | `fallbackToDestructiveMigration()` | Data-loss risk, not a direct compliance breach; document retention/deletion behavior |
| Remote image privacy | Coil loads campaign images from arbitrary accepted HTTPS hosts | Host receives IP, request metadata; disclose and restrict hosts |

## 6. Google Play policy and release readiness

Official policy references: [Data Safety requirements](https://support.google.com/googleplay/android-developer/answer/10787469), [privacy policy/app review requirements](https://support.google.com/googleplay/android-developer/answer/9859455), [Ads policy](https://support.google.com/googleplay/android-developer/answer/9857753), [target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878), [content rating/target audience](https://support.google.com/googleplay/android-developer/answer/9859655).

| Requirement | Status |
|---|---|
| Privacy Policy URL and in-app privacy text/link | **Missing — blocker** |
| Terms of Use | Missing; strongly recommended for safety/weather/ads |
| Data Safety form | Not present in repository; must be completed in Play Console even for no-data apps |
| Ads declaration | Must declare “contains ads” if remote advertising can be enabled |
| Target audience/children | Not declared. If children are included, Families requirements and certified ad handling apply |
| Content rating | Not completed/evidenced |
| Sensitive permissions | No location permission. Notification permission declared but apparently unused |
| Location | Destination coordinates are bundled/provider inputs; device location is not requested |
| Analytics/crash reporting | No implementation found |
| Ad ID/tracking | No Google Ads SDK and no `AD_ID` permission found |
| Notifications | Permission exists, but no notification implementation/channel found |
| Foreground service | None |
| Background work | Periodic WorkManager network refresh, minimum 15 minutes |
| Network use | Measurement refresh can repeat and Open-Meteo refresh occurs on startup/station change; must remain within provider limits |
| Target SDK | API 35. Acceptable before 31 August 2026; from that date new apps/updates must target API 36 under the currently published schedule |
| Versioning | `versionCode=1`, `versionName=1.0.0`; acceptable for first upload only |
| App Bundle | Release AAB exists |
| Signing | No release signing config; release APK is unsigned — blocker |
| Store listing assets | No evidence of feature graphic, phone screenshots, short/full descriptions, support email/site |

## 7. GDPR and privacy assessment

### Data handled locally

- selected station;
- favourite stations and favourite sports;
- language/theme/refresh and sports criteria;
- onboarding state and advertising preferences/dismissals;
- cached public measurements and forecast data.

These preferences are not inherently sensitive but can be personal data when associated with a device/person. Backup is enabled.

### Data transmitted/observable externally

- The app does not request device GPS location and does not read device identifiers or Advertising ID.
- Requests necessarily expose the device public IP, timestamp, TLS/HTTP metadata and app user agent to Meteo Varna, Open-Meteo and any future ad/media/action host.
- Destination coordinates represent selected public forecast locations, not measured device location.
- No analytics/crash reporting is implemented.
- The advertising analytics interface is currently no-op, but remote ad serving and external action links still create disclosure/consent obligations.

**GDPR conclusion:** A privacy policy is required for Play and prudent/likely required under transparency obligations. It must name the controller/developer and contact, purposes/legal bases, processors/recipients (Open-Meteo, Meteo Varna, campaign/media hosts), international transfers where applicable, retention, backup, user rights, deletion/contact procedure, security, children/age position and change history. If personalized ads, tracking, analytics or identifiers are later enabled, implement an appropriate consent management flow before collection; do not rely on the current simple ad toggle as a GDPR/ePrivacy consent mechanism.

## 8. Disclaimer audit

Present text states that ratings are indicative, are not a safety guarantee, and users should check local/official warnings. This is helpful but incomplete and inconsistent across screens/languages.

Add a durable Terms/Safety section stating clearly:

1. observations and forecasts may be delayed, cached, incomplete or wrong;
2. data and derived sports scores are informational only;
3. the app does not replace official meteorological, maritime, lifeguard, harbour-master or emergency warnings;
4. no score guarantees safe conditions;
5. users must assess local conditions, ability, equipment and legal restrictions and remain responsible for decisions;
6. emergency and official restrictions always take priority;
7. provider availability is not guaranteed.

Legal counsel should review the final wording; disclaimers do not cure unlicensed data use or negligence.

## 9. Attribution and proposed Sources screen

Current UI displays source names in several forecast/measurement cards, but there is no complete Sources/Licences screen.

Required proposed entries:

- **Open-Meteo Weather and Marine** — clickable provider link; “Weather/marine model data”; CC BY 4.0 link; required credit to Open-Meteo and underlying model providers; indication that the app selects, summarizes and derives scores from the data; last update/model time.
- **Meteo Varna marine stations** — measurement page link, operator/copyright owner, exact permission/licence and required wording **only after written clarification**.
- **Open-source software** — generated acknowledgements with library, version, copyright, licence text/link and NOTICE content.
- **App icon** — creator/provenance/licence if external or generated under third-party terms.

Do not claim that Meteo Varna data is “open data” without written evidence.

## 10. Advertising and monetization

Advertising defaults are remotely disabled and background ads require a user setting, which reduces current exposure. However, the binary contains the capability and all future campaigns are part of the app for Play policy purposes.

Before enabling:

- complete the Play “contains ads” declaration;
- publish privacy/retention/recipient disclosures and Data Safety answers;
- determine target audience and exclude children unless the full Families regime is implemented;
- use only rights-cleared, age/content-rating-appropriate campaigns;
- contractually prohibit malicious/deceptive links and tracking pixels;
- restrict config/image/action hosts, sign remote config, define incident revocation;
- label every placement clearly as sponsored/advertising;
- never obscure warnings/navigation or imitate system/UI controls;
- obtain valid consent where required for cookies/device storage, tracking, profiling or personalized ads;
- acquire a commercial Open-Meteo plan before monetization. The presence of advertising is commercial use.

No AdMob SDK is currently included despite an `admobEnabled` model flag. If added later, reassess SDK Data Safety, consent, AD_ID permission, child-directed treatment and dependencies.

## 11. Open-source compliance deliverables

Before release create and verify:

1. project `LICENSE` identifying the app code licence/proprietary status;
2. `THIRD_PARTY_NOTICES`/`NOTICE` generated from the final release dependency graph;
3. full required Apache-2.0, MIT and other applicable licence texts;
4. an in-app “Open-source licences” view or packaged acknowledgements accessible to users;
5. a repeatable dependency/SBOM and licence scan in CI;
6. provenance/licence records for every bundled and remotely displayed asset.

Do not blindly copy the table in this audit into legal notices; verify each final resolved artifact/POM and any supplied NOTICE file at release time.

## 12. Play Store release checklist

- [ ] Written Meteo Varna permission/licence obtained, or source removed/replaced.
- [ ] Open-Meteo commercial subscription/customer endpoint configured for a monetized app.
- [ ] Complete CC BY 4.0 attribution and modification notice added.
- [ ] Privacy Policy hosted on a stable public HTTPS URL and linked inside app.
- [ ] Terms of Use/Safety disclaimer reviewed and linked inside app.
- [ ] Data Safety form completed consistently with binary and remote services.
- [ ] Ads, target audience, content rating and app-access declarations completed.
- [ ] Support email and developer/privacy contact published.
- [ ] Feature graphic, screenshots, app descriptions and icon ownership evidence prepared.
- [ ] Third-party licences/NOTICE shipped.
- [ ] Release keystore/signing and protected CI credentials configured; signed AAB verified.
- [ ] Unused `POST_NOTIFICATIONS` permission removed or feature/disclosure implemented.
- [ ] Backup/data extraction policy and retention/deletion behavior documented.
- [ ] HTTP measurement transport eliminated or risk formally accepted only after provider agreement.
- [ ] API call budgets monitored; refresh behavior reviewed against provider contracts.
- [ ] Target API rechecked immediately before upload (API 36 required for new apps/updates from 31 August 2026 under current policy).
- [ ] Play pre-launch report, automated tests, accessibility and device testing completed.

## 13. Prioritized remediation plan

### Critical — before any public/commercial release

1. Obtain Meteo Varna written rights or replace the scraped source.
2. Buy/configure commercial Open-Meteo access for any ad-supported/paid/commercial distribution.
3. Publish and integrate Privacy Policy; complete Data Safety.
4. Configure secure release signing and produce a signed AAB.

### High — before Play submission

5. Add complete attribution and third-party notices.
6. Resolve icon provenance.
7. Finalize Terms/Safety disclaimer.
8. Secure or disable remote advertising pending consent/content governance.
9. Replace HTTP data delivery with a documented HTTPS source.

### Medium/Low

10. Remove/justify notification permission; define backup rules.
11. Add SBOM/licence/security scanning and dependency update process.
12. Prepare listing assets, support contacts, content rating and target-audience declarations.

## Final assessment

**Legal/data-source status:** NOT CLEARED.  
**Open-source dependency status:** Technically compatible with commercial distribution, but notices are incomplete.  
**Security status:** Material cleartext-source and remote-ad governance risks remain.  
**Google Play status:** NOT READY.  
**Commercial-use status:** NOT READY; current Open-Meteo endpoint terms and unknown Meteo Varna rights are blockers.

Re-run this audit against the exact signed release commit and final Play Console declarations after remediation. Provider terms and Play policies can change; archive dated copies of the terms and written permissions relied upon for release.
