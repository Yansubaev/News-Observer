# Задачи по коду перед публикацией в Google Play

Дата: **14 сентября 2026**. Основание — `.docs/news_sources_licensing_and_google_play.md`
(дальше «юр. документ»), сверенный с кодом на коммите `c47a0ef`.

Здесь только то, что делается **в репозитории**. Бумажная часть (Play Console,
декларации, closed testing 12×14, письмо в NewsData.io) — в юр. документе,
разделы 6.1–6.9; в конце этого файла есть короткий указатель.

Каждая задача самодостаточна: файлы, что сделать, критерий готовности.
Выполнять можно по одной, в порядке номеров.

---

## 0. Что уже сделано (юр. документ, раздел 1.4, устарел)

Проверено в коде и в собранном `app-release.aab`:

- [x] `HttpLoggingInterceptor` в release — `Level.NONE` (`di/NetworkModule.kt`).
- [x] `android:allowBackup="false"` в манифесте — облачный бэкап Room/DataStore отключён.
- [x] News API отсутствует в release: в dex нет `NewsApiProvider` и `newsapi.org`.
- [x] `IMAGES_ENABLED = false` в release.
- [x] `./gradlew :app:bundleRelease` собирается (но AAB **не подписан**, см. задачу 1).
- [x] Target SDK 37 ≥ требуемых 36.

После выполнения задач ниже — отметить соответствующие пункты в разделе 1.4 и 7
юр. документа.

---

## Блокеры (без них загрузить/пройти ревью нельзя)

### 1. Подпись release-сборки и версия

**Зачем.** Play принимает только подписанный AAB. Сейчас `signingConfig` нет.

**Файлы:** `app/build.gradle.kts`, `local.properties`, `.gitignore`.

**Что сделать:**
1. Создать upload-ключ (Android Studio → *Build → Generate Signed Bundle* или
   `keytool -genkeypair -v -keystore upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload`).
   Хранить **вне репозитория**, сделать резервную копию.
2. Прочитать путь и пароли из `local.properties` / env — тем же способом, каким
   уже читается `NEWS_DATA_API_KEY`, — и завести `signingConfigs { create("release") { ... } }`,
   подключить в `buildTypes.release`.
3. Если свойств нет — не падать на конфигурации (debug-сборка должна работать без ключа).
4. В Play Console включить Play App Signing (по умолчанию) — ключ выше становится upload key.
5. Правило версий: каждая загрузка в Play = `versionCode + 1`. `versionName` — по вкусу.

**Готово, когда:** `./gradlew :app:bundleRelease` даёт AAB, у которого
`jarsigner -verify app-release.aab` проходит; `*.jks` и пароли не в git.

---

### 2. URL контактов и Privacy Policy в ресурсах — ✅ сделано (страница: `docs/index.md`)

**Зачем.** Play требует, чтобы контактный URL и Privacy Policy были доступны
**внутри приложения** и совпадали с Play Console (юр. документ 6.3, 6.5).

**Зависимость:** сначала создать публичную HTTPS-страницу (GitHub Pages) с
контактами, email, способом пожаловаться на материал и политикой. Сам текст
политики — по чек-листу 6.5 юр. документа.

**Файлы:** `res/values/strings.xml`.

**Что сделать:** добавить (все `translatable="false"`):
- `contact_url`, `privacy_policy_url`, `contact_email`, `developer_name`;
- `provider_news_data_terms_url` = `https://newsdata.io/terms-and-conditions`;
- `provider_gdelt_terms_url` = `https://www.gdeltproject.org/about.html#termsofuse`;
- `terms_last_reviewed` — дата последней сверки условий (строка, обновлять руками).

**Готово, когда:** все URL открываются на телефоне и совпадают с тем, что будет
введено в Play Console.

---

### 3. Экран «О приложении и источники» вместо `AboutAlertDialog` — ✅ сделано (`presentation/settings/AboutScreen.kt`)

**Зачем.** Главный пробел по юр. документу (1.4, 6.4). В диалоге не хватает
половины обязательного, и всё не влезет.

**Файлы:** `presentation/settings/SettingsScreen.kt` (`AboutAlertDialog`,
строки ~227 и ~535), `presentation/navigation/Screen.kt`, `AppNavHost.kt`,
`res/values/strings.xml`, `res/values-ru/strings.xml`.

**Что сделать:** новый destination `About` (обычный прокручиваемый `Column`,
по образцу существующих подэкранов настроек). Содержимое:

| Блок | Сейчас | Нужно |
|---|---|---|
| Название, версия | есть | перенести |
| Имя разработчика | нет | `developer_name` |
| Дисклеймер независимости | нет | новая строка, см. ниже |
| Провайдеры с кликабельными ссылками на сайт | только GDELT | для **каждого** id из `availableProviderIds` (URL уже собираются в `providerSiteUrls`) |
| Ссылки на условия провайдеров | нет | `provider_*_terms_url` |
| Атрибуция GDELT со ссылкой | есть | сохранить формулировку «Event and news metadata provided by the GDELT Project — gdeltproject.org» |
| Права на статьи у правообладателей | общими словами | усилить, см. ниже |
| Privacy Policy | строка есть, не используется | ссылка на `privacy_policy_url` |
| Контакты / жалобы на материал | нет | `contact_url` + `mailto:` на `contact_email` |
| Исходный код | строка есть, не используется | ссылка на GitHub, **только если репозиторий публичный**; иначе удалить строку |
| Open-source лицензии | строка есть, не используется | см. задачу 12; до неё — удалить строку |
| Дата проверки условий | нет | `terms_last_reviewed` |

Тексты (EN + RU):
- *«News Observer is an independent news aggregator. It is not affiliated with,
  endorsed by, or sponsored by any of the publishers shown. Articles belong to
  their respective publishers; the app shows only headlines, short descriptions
  and links to the original sources and does not verify their accuracy.»*

Ссылки открывать уже существующим `openUriInBrowser` / `parseArticleUri`
(`presentation/helper/BrowserHelper.kt`). Логотипы СМИ не использовать.

**Готово, когда:** экран доступен из настроек, все ссылки открываются, в RU и
EN нет пропущенных строк; `AboutAlertDialog` удалён.

---

### 4. «Сообщить о проблеме с материалом» на экране статьи — ✅ сделано

**Зачем.** Рабочий канал жалоб (юр. документ 5.4, 6.9, 9.4).

**Файлы:** `presentation/articlepreview/ArticleDetailsScreen.kt`, строки.

**Что сделать:** текстовая кнопка/пункт меню → `Intent.ACTION_SENDTO` с
`mailto:contact_email`, темой «Content report» и телом с `article.originalUrl`
и `article.publisher.name`. Если почтового клиента нет — снэкбар (по образцу
`OpenArticleResult.FailedToOpen`). Рядом, под существующим
`article_details_opens_on_publisher_site`, — короткий дисклеймер независимости
(одна строка, не полный текст из задачи 3).

**Готово, когда:** на устройстве открывается письмо с заполненными полями.

---

### 5. Зафиксировать схему Room и убрать тихую потерю избранного

**Зачем.** В `di/DatabaseModule.kt` стоит `fallbackToDestructiveMigration(true)`,
в `NewsDatabase` — `exportSchema = false`. Пока приложение только у вас, это
удобно. После публикации любое изменение схемы без миграции **молча стирает
избранное у пользователей**. Задача 7 как раз меняет данные — поэтому это
делать до неё.

**Файлы:** `data/local/dao/NewsDatabase.kt`, `di/DatabaseModule.kt`,
`app/build.gradle.kts`.

**Что сделать:**
1. `exportSchema = true`; в `build.gradle.kts` добавить `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`;
   закоммитить `app/schemas/.../14.json` — это baseline первой публичной версии.
2. Удалить `.fallbackToDestructiveMigration(true)`. Версии 1–13 существовали
   только на ваших устройствах: при старой dev-базе приложение упадёт —
   переустановить. Миграции `MIGRATION_6_7 … MIGRATION_13_14` после этого тоже
   можно удалить (публичных пользователей на этих версиях нет).
   Не использовать `fallbackToDestructiveMigrationFrom(..., 13)`: Room бросает
   исключение, если для стартовой версии одновременно есть миграция и
   destructive-fallback (а у 6, 8, 9, 12, 13 миграции есть).
3. Правило в `AGENTS.md`: «изменение схемы = `version + 1` + `Migration` + тест
   в `androidTest`» (образец — `NotificationArticleMigrationTest`).

**Готово, когда:** в репозитории есть `14.json`, сборка проходит, существующие
миграционные тесты зелёные.

---

## Важно, но не блокирует ревью

### 6. Издатель в ежедневном уведомлении — ✅ сделано

**Зачем.** Сейчас в шторке заголовок + описание без источника — самое слабое
место по атрибуции (юр. документ 5.3, 9.2).

**Файлы:** `notification/DailyArticleNotifier.kt`, метод `showArticle`.

**Что сделать:** `.setSubText(article.publisher.name)` в билдере. Одна строка.

**Готово, когда:** в уведомлении виден издатель (проверить кнопкой тестового
уведомления в debug).

---

### 7. Автоочистка кэша старше 30 дней — ✅ сделано (`DeleteStaleArticlesUseCase` при старте приложения)

**Зачем.** Лицензия NewsData.io не подтверждает бессрочное хранение, GDPR
(имена в заголовках), правило свежести Play (юр. документ 5.2, 9.5).

**Как устроено сейчас** (важно для решения):
- при `REFRESH` ленты `ArticleRemoteMediator` уже удаляет старые cross-refs этой
  ленты и сирот — **активно используемые ленты не залипают**;
- залипают ленты, которые больше не открываются (сменили страну/категорию), и их
  статьи: `deleteOrphanedArticles` не трогает статьи, на которые ссылается лента;
- у `RemoteKeyEntity` уже есть `updatedAt` — время последней загрузки ленты.

**Что сделать (без изменения схемы и без новой колонки `fetchedAt`):**
1. В `ArticleFeedCrossRefDao` / `FeedDao` / `RemoteKeyDao` — запросы удаления
   лент, у которых `remote_keys.updated_at < :threshold`.
2. `ArticleRepository.deleteStaleArticles(threshold)` в одной транзакции:
   stale cross-refs → stale feeds → stale remote keys → `deleteOrphanedArticles()`.
   Избранное и `notification_articles` не трогаются (уже защищены в запросе).
3. Вызывать при старте приложения (не из `DailyNewsWorker` — он выходит сразу,
   если уведомления выключены). Порог — 30 дней.
4. Unit-тест use case на фейковом репозитории (по образцу `ClearArticleCacheUseCaseTest`).

**Избранное** хранится бессрочно как явный выбор пользователя — это
зафиксировать в Privacy Policy.

**Готово, когда:** лента с `updatedAt` старше 30 дней и её неизбранные статьи
исчезают после перезапуска; избранное остаётся.

---

### 8. Регрессионный тест изоляции News API — ✅ сделано (`src/testRelease`, включён в `app/build.gradle.kts`)

**Зачем.** Чтобы рефакторинг не вернул News API в release (юр. документ 2).

**Файлы:** новый `app/src/testRelease/java/com/ians/observer/ReleaseIsolationTest.kt`.

**Что сделать:** release-only unit-тест (source set `testRelease` запускается
только в `testReleaseUnitTest`):
- `Class.forName("com.ians.observer.data.remote.newsapi.NewsApiProvider")` бросает `ClassNotFoundException`;
- в `BuildConfig` нет поля `NEWS_API_KEY` (`BuildConfig::class.java.fields`);
- `BuildConfig.IMAGES_ENABLED == false`.

**Готово, когда:** `./gradlew testReleaseUnitTest` зелёный, а если временно
перенести `NewsApiProvider` в `main` — красный.

---

### 9. Запрет cleartext-трафика — ✅ сделано

**Зачем.** Подкрепить «данные шифруются при передаче» в Data Safety. При
`minSdk 24` дефолт не гарантирован.

**Файлы:** `AndroidManifest.xml`: `android:usesCleartextTraffic="false"` на
`<application>`. Отдельный `network_security_config.xml` не нужен.

**Готово, когда:** приложение работает (оба `BASE_URL` уже HTTPS).

---

### 10. R8: минификация и сжатие ресурсов — ✅ сделано, release проверен на эмуляторе (API 37)

**Зачем.** Меньше APK, сложнее разобрать. Ключ NewsData.io это **не спрячет**
(он всё равно в APK) — для portfolio это принятый риск; при утечке ключ
перевыпускается в кабинете NewsData.io.

**Файлы:** `app/build.gradle.kts`, `app/proguard-rules.pro`.

**Что сделать:**
1. `isMinifyEnabled = true`, `isShrinkResources = true` в `release`.
2. Gson-DTO (`data/remote/**/dto`) после обфускации теряют имена полей —
   добавить `-keep class com.ians.observer.data.remote.**.dto.** { *; }` или
   `@SerializedName` на каждое поле. Room, Hilt, Retrofit, WorkManager несут
   свои consumer-правила.
3. Раскомментировать `-keepattributes SourceFile,LineNumberTable` — читаемые
   стектрейсы; `mapping.txt` загружать в Play Console вместе с AAB.

**Готово, когда:** release на устройстве грузит ленту из NewsData.io и GDELT,
работает поиск, избранное, уведомление, открытие статьи. **Именно эта задача
чаще всего ломает release — тестировать руками.**

---

## Уборка и опционально

### 11. Правила бэкапа — ✅ сделано (исключено и из cloud backup, и из device transfer)
`allowBackup="false"` уже стоит, но `backup_rules.xml` и
`data_extraction_rules.xml` — шаблоны с TODO. Ничего не ломают. Для чистоты:
в `data_extraction_rules.xml` явно исключить `database` и `sharedpref`/`file`
в `<device-transfer>` (при `targetSdk ≥ 31` перенос между устройствами идёт
несмотря на `allowBackup=false`). В Data Safety это не «sharing» — можно
отложить.

### 12. Open-source лицензии
Если нужен экран — плагин `com.google.android.gms:oss-licenses-plugin` и готовая
Activity. Для portfolio достаточно ссылки на репозиторий. Не блокер.

### 13. Короче описание на экране статьи (ЕС, ст. 15 CDSM) — ✅ сделано (`shortenSnippet`, также в уведомлении)
Юр. документ 9.2. Показывать `description` целиком только на карточке
(`maxLines = 3` уже есть), на экране деталей — первое предложение / ~200
символов. Делать, если хочется снизить риск; Play этого не требует.

### Не делать сейчас
- blocklist доменов — при первой реальной жалобе;
- удаление `ProviderId.NEWS_API` и строк из `main` — мёртвые, но безвредные, превью их используют;
- поля `imageLicense`/`imageAttribution` — только если включите картинки.

---

## Финальная проверка release-сборки (перед каждой загрузкой)

```bash
./gradlew testDebugUnitTest testReleaseUnitTest
./gradlew :app:bundleRelease
```

На физическом устройстве с **release**-сборкой (через internal testing track
или `bundletool build-apks --connected-device`):

- [ ] лента NewsData.io и GDELT загружается, у каждой статьи видны издатель и дата;
- [ ] «Read on …» открывает оригинал;
- [ ] поиск, избранное, очистка кэша работают;
- [ ] уведомление приходит и содержит издателя;
- [ ] экран «О приложении»: все ссылки открываются, email создаётся;
- [ ] «Сообщить о проблеме» открывает письмо;
- [ ] картинок нет, News API в настройках нет;
- [ ] скриншоты для Store Listing сняты **с этой сборки**.

---

## Не в коде — указатель на юр. документ

| Что | Раздел |
|---|---|
| Публичная страница контактов + Privacy Policy (нужна для задачи 2) | 6.3, 6.5 |
| Письмо в поддержку NewsData.io (кэш, уведомления, сниппеты) | 3 |
| News and Magazine declaration, Data Safety, Content rating, Target audience, Ads | 6.1, 6.6, 6.9 |
| Closed testing 12 тестеров × 14 дней (личный аккаунт после 13.11.2023) | 6.8 |
| Верификация разработчика, EU trader status | 6.8 |
