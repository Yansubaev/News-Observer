# Архитектура нескольких источников новостей

Статус: направление для будущего рефакторинга, код пока не изменён.

Цель документа — зафиксировать подход, при котором NewsData.io добавляется как
дополнительный поставщик новостей, а подключение следующих поставщиков не требует
изменять ViewModel, UI и основную реализацию репозитория.

Юридические и продуктовые ограничения источников описаны отдельно в
[news_sources_licensing_and_google_play.md](news_sources_licensing_and_google_play.md).

## 1. Текущее состояние

Проект уже разделён на `data`, `domain` и `presentation`, использует Room как
локальный кэш и `ArticleRepository` как границу между UI и данными. Это хороший
фундамент.

Основная проблема — data-слой напрямую зависит от конкретного `NewsApi`:

- `ArticleRepositoryImpl` вызывает `NewsApi`;
- `ArticleRemoteMediator` вызывает `NewsApi`;
- `ArticleSearchPagingSource` вызывает `NewsApi`;
- общий Retrofit и OkHttp interceptor настроены только для News API;
- News API DTO преобразуется непосредственно в доменную `Article`;
- пагинация представлена целым числом `page`, которое хранится в статье и Room.

Если добавить `NewsDataApi` непосредственно в эти классы, условия выбора
поставщика начнут повторяться:

```kotlin
when (provider) {
    ProviderId.NEWS_API -> newsApi.getHeadlines(...)
    ProviderId.NEWS_DATA -> newsDataApi.getLatest(...)
    ProviderId.GDELT -> ...
}
```

Такой `when` быстро появится в репозитории, поиске, пагинации и DI. Это признак
того, что изменяемая часть системы — внешний поставщик — не изолирована.

## 2. Архитектурный принцип

Приложению нужен один репозиторий статей, но внутри data-слоя может быть много
адаптеров внешних поставщиков.

```text
UI / ViewModel
      |
      v
ArticleRepository
      |
      v
NewsProviderRegistry / FeedCoordinator
      |
      +------------+----------------+-------------+
      |            |                |             |
      v            v                v             v
 News API      NewsData.io        GDELT       Future API
 Provider       Provider          Provider       Provider
      |            |                |             |
      +------------+----------------+-------------+
                           |
                           v
                          Room
                           |
                           v
                   PagingSource -> UI
```

Это сочетание паттернов Adapter и Strategy:

- Adapter переводит контракт конкретного API во внутренний контракт приложения;
- Strategy позволяет coordinator выбрать одну или несколько реализаций;
- Repository остаётся единой границей, с которой работает presentation-слой.

Интерфейс поставщика является технической деталью data-слоя. Его не обязательно
размещать в `domain`, если use case и ViewModel никогда не работают с ним
напрямую.

## 3. Provider и publisher — разные понятия

Слово `source` сейчас используется в двух смыслах:

- технический поставщик данных: News API, NewsData.io, GDELT;
- оригинальный издатель статьи: Reuters, BBC, TechCrunch и так далее.

Эти понятия необходимо развести:

```kotlin
enum class ProviderId {
    NEWS_API,
    NEWS_DATA,
    GDELT,
}

data class Article(
    val id: String,
    val providerId: ProviderId,
    val providerArticleId: String?,
    val publisherName: String,
    val publisherUrl: String?,
    val author: String?,
    val title: String,
    val description: String?,
    val originalUrl: String,
    val imageUrl: String?,
    val publishedAt: Long,
    val isFavorite: Boolean,
)
```

Пример:

```text
provider       = NewsData.io
publisher      = Reuters
author         = John Smith
originalUrl    = https://reuters.com/...
```

Это разделение требуется не только для кода, но и для корректной атрибуции в UI.

## 4. Нейтральный контракт поставщика

В data-слое следует ввести общий контракт, сформулированный на языке приложения,
а не на языке News API или NewsData.io.

Пример направления, а не окончательная сигнатура:

```kotlin
interface NewsProvider {
    val id: ProviderId
    val capabilities: Set<ProviderCapability>

    suspend fun loadFeed(
        request: FeedRequest,
        pageToken: PageToken?,
    ): ProviderPage

    suspend fun search(
        request: SearchRequest,
        pageToken: PageToken?,
    ): ProviderPage
}
```

Нейтральные параметры запроса:

```kotlin
data class FeedRequest(
    val country: String?,
    val language: String?,
    val category: Category?,
)

data class SearchRequest(
    val query: String,
    val country: String?,
    val language: String?,
    val category: Category?,
)

@JvmInline
value class PageToken(val value: String)

data class ProviderPage(
    val articles: List<RemoteArticle>,
    val nextPageToken: PageToken?,
)
```

`RemoteArticle` — нормализованная модель data-слоя. Она не является DTO
конкретного API и не должна содержать локальные признаки вроде `isFavorite`.

## 5. Почему PageToken, а не Int

News API использует числовые страницы. NewsData.io возвращает строковый
`nextPage`, который передаётся в следующий запрос через параметр `page`.

Общий код не должен предполагать, что следующая страница равна `page + 1`.
`PageToken` должен быть непрозрачным для repository и coordinator:

- `NewsApiProvider` может преобразовать токен `"2"` в число;
- `NewsDataProvider` передаёт строковый cursor без интерпретации;
- будущий provider может использовать timestamp или другой тип cursor.

Каждый адаптер самостоятельно определяет окончание пагинации и формирует
следующий токен.

Актуальную механику NewsData.io перед реализацией нужно повторно сверить с
[документацией пагинации](https://newsdata.io/blog/newsdata-pagination/).

## 6. Ответственность конкретного адаптера

Предлагаемая структура data-слоя:

```text
data/remote/
|-- provider/
|   |-- NewsProvider.kt
|   |-- NewsProviderRegistry.kt
|   `-- model/
|-- newsapi/
|   |-- NewsApi.kt
|   |-- dto/
|   |-- NewsApiMapper.kt
|   `-- NewsApiProvider.kt
`-- newsdata/
    |-- NewsDataApi.kt
    |-- dto/
    |-- NewsDataMapper.kt
    `-- NewsDataProvider.kt
```

Каждый provider владеет только своими особенностями:

- endpoint и параметры запроса;
- DTO и формат ответа;
- авторизация;
- проверка статуса и преобразование ошибок;
- формат даты;
- сопоставление категорий и языков;
- правила пагинации;
- преобразование DTO в `RemoteArticle`.

Например, адаптер NewsData.io знает про `results`, `article_id`, `link`,
`source_id`, `source_name`, `pubDate` и `nextPage`. После маппинга остальные
слои не должны знать названия этих полей.

Документация полей ответа:
[NewsData.io response objects](https://newsdata.io/blog/news-api-response-object/).

## 7. Возможности поставщиков

Не следует создавать общий интерфейс как объединение всех параметров всех API.
Он должен описывать потребности приложения.

Поставщики могут иметь разные возможности:

```kotlin
enum class ProviderCapability {
    TOP_HEADLINES,
    SEARCH,
    COUNTRY_FILTER,
    LANGUAGE_FILTER,
    CATEGORY_FILTER,
}
```

Конкретный адаптер:

- переводит доменную категорию в значение своего API;
- игнорирует необязательный неподдерживаемый фильтр, если это допустимо;
- либо сообщает coordinator, что операция не поддерживается.

Это не позволяет ограничениям одного API определять архитектуру всего
приложения.

## 8. Coordinator нескольких поставщиков

`FeedCoordinator` находится между repository и providers. Он отвечает за:

- получение списка включённых поставщиков;
- вызов каждого подходящего provider;
- частичные ошибки;
- дедупликацию;
- сортировку по `publishedAt`;
- сохранение данных и ключей пагинации в одной Room-транзакции.

При параллельной загрузке отказ одного API не должен отменять успешные запросы
других. Для этого подходит `supervisorScope` с явным сбором результата каждого
provider.

Базовая политика ошибок:

- сохранить и показать данные успешных providers;
- не удалять старые данные упавшего provider;
- сохранить информацию о частичной ошибке;
- позволить повторить запрос только для проблемного provider;
- считать всю загрузку неуспешной только тогда, когда не удалось получить и
  показать никаких данных.

## 9. Room как точка объединения

Несколько `PagingData` сложно корректно объединять в памяти: у них разные
страницы, размеры ответов, ошибки и моменты загрузки.

Предпочтительный поток:

```text
RemoteMediator
    -> providers
    -> normalization and deduplication
    -> Room transaction
    -> one Room PagingSource
    -> UI
```

В целевой схеме стоит рассмотреть три сущности:

```text
ArticleEntity
    Содержимое и метаданные статьи.

FeedArticleCrossRef
    Принадлежность статьи конкретной ленте или поисковому запросу.

RemoteKeyEntity
    Cursor и состояние обновления каждого provider для конкретной ленты.
```

Пример remote key:

```kotlin
data class RemoteKeyEntity(
    val feedKey: String,
    val providerId: ProviderId,
    val nextPageToken: String?,
    val endReached: Boolean,
    val updatedAt: Long,
)
```

`feedKey` должен стабильно описывать запрос:

```text
feed|country=us|language=en|category=technology
search|query=android|country=us|language=en
```

Это решает несколько проблем текущей модели:

- `page` перестаёт быть свойством статьи;
- у каждого provider появляется собственный cursor;
- время синхронизации хранится отдельно для каждого feed/provider;
- одна статья может входить в несколько категорий и результатов поиска;
- обновление одной ленты не удаляет данные другой.

## 10. Идентичность и дедупликация

Одна оригинальная статья может прийти через несколько агрегаторов.

Нужно различать:

- `providerArticleId` — идентификатор внутри внешнего API;
- `providerId + providerArticleId` — техническая уникальность внешней записи;
- `canonicalUrl` — кандидат для определения одной оригинальной статьи;
- локальный `articleId` — стабильный идентификатор приложения.

Практичный первый вариант:

```text
articleId = hash(canonicalUrl)
providerArticleId = ID из внешнего API
providerId = поставщик данных
```

URL желательно нормализовать, аккуратно удаляя только известные tracking-параметры.
Если потребуется хранить сведения о нескольких агрегаторах одной статьи, можно
добавить `ArticleProviderCrossRef`.

Избранное должно быть связано со стабильной идентичностью статьи, а не с
provider page или текущим местом в ленте.

## 11. Dependency Injection и сетевые клиенты

Каждому provider необходим отдельный Retrofit/OkHttp client:

```kotlin
@NewsApiClient
fun provideNewsApiRetrofit(...): Retrofit

@NewsDataClient
fun provideNewsDataRetrofit(...): Retrofit
```

Причины:

- разные base URL;
- разные имена и способы передачи API key;
- разные interceptor;
- разные правила обработки ошибок и rate limits;
- отсутствие риска отправить ключ одного сервиса другому.

Providers можно зарегистрировать через Hilt multibindings:

```kotlin
@IntoMap
@ProviderKey(ProviderId.NEWS_DATA)
fun bindNewsDataProvider(
    provider: NewsDataProvider,
): NewsProvider
```

Coordinator получает реестр:

```kotlin
Map<ProviderId, NewsProvider>
```

После этого добавление нового поставщика не требует менять coordinator: нужна
новая реализация и её DI binding.

## 12. Доступные и включённые providers

В настройках нужно различать два множества:

- `availableProviders` — реализации, присутствующие в конкретной сборке;
- `enabledProviders` — поставщики, выбранные пользователем.

DataStore может предоставлять:

```kotlin
Flow<Set<ProviderId>>
```

UI показывает только доступные реализации и сохраняет выбранные. Следует либо
запрещать отключать последний provider, либо показывать понятное пустое состояние.

Настройки региона, языка и providers желательно предоставить как `Flow`, чтобы
лента перестраивалась при их изменении без пересоздания экрана.

## 13. Build variants

Согласно принятому продуктовому решению, News API должен быть доступен только в
debug, а NewsData.io — в release.

Предпочтительное направление:

- debug binding `NewsApiProvider` находится в `src/debug`;
- в release его binding отсутствует;
- `NewsDataProvider` находится в `main` или соответствующем flavor;
- список доступных источников строится из DI registry;
- скрытие checkbox в UI не считается достаточной защитой;
- release-сборка не должна требовать или содержать ключ News API.

API key, помещённый в `BuildConfig`, можно извлечь из APK. Необходимость backend
proxy и допустимость кэширования определяются условиями каждого поставщика и
рассматриваются отдельно в документе о лицензировании.

## 14. Граница domain-слоя

`ArticleRepository` остаётся интерфейсом, нужным use cases и ViewModel. Его API
можно постепенно выразить через модели запросов:

```kotlin
interface ArticleRepository {
    fun observeFeed(filter: FeedFilter): Flow<PagingData<Article>>
    fun search(filter: SearchFilter): Flow<PagingData<Article>>
    fun observeFavorites(): Flow<List<Article>>
    suspend fun toggleFavorite(articleId: String)
}
```

AndroidX `PagingData` в domain — прагматичный компромисс. Для данного проекта его
можно оставить, если полная платформенная независимость domain не является
целью. Приоритетнее сначала убрать зависимости от конкретного API и сетевой
пагинации из общей модели.

Существующие paging и non-paging методы частично дублируют друг друга. После
стабилизации новой границы стоит определить один основной путь загрузки, чтобы
не поддерживать две реализации одного поведения.

## 15. Тестовая стратегия

### Контракт provider

Для каждого адаптера проверить:

- корректный request;
- успешный response;
- маппинг nullable-полей;
- формат даты;
- API error и HTTP error;
- отсутствие следующей страницы;
- формирование `nextPageToken`;
- mapping категорий, языка и региона.

### Coordinator

Использовать `FakeNewsProvider` и проверить:

- успешное объединение нескольких источников;
- сортировку;
- дедупликацию;
- частичный отказ;
- полный отказ;
- отключённый provider;
- сохранение независимых cursor.

### Room и Paging

Проверить:

- независимые ключи разных feed/provider;
- `REFRESH` не удаляет избранное;
- ошибка одного provider не удаляет его старый кэш;
- статья может входить в несколько лент;
- переключение категории, региона и языка создаёт правильный `feedKey`.

### Архитектурный smoke test

Создать тестовый третий provider. Если для его подключения приходится менять
ViewModel, repository или mediator, граница выбрана недостаточно хорошо.

## 16. Порядок миграции

Не следует одновременно добавлять NewsData.io и переписывать весь data-слой.
Сначала существующий News API нужно провести через новую границу, сохранив
текущее поведение.

1. Переименовать технический `Source` в `ProviderId` и отделить его от publisher.
2. Ввести `NewsProvider`, `RemoteArticle`, `ProviderPage` и `PageToken`.
3. Обернуть существующий `NewsApi` в `NewsApiProvider`.
4. Перевести repository и paging на новый контракт.
5. Убедиться тестами, что единственный provider работает как раньше.
6. Убрать `page` из бизнес-модели статьи.
7. Добавить `RemoteKeyEntity` по ключу feed/provider.
8. При необходимости добавить `FeedArticleCrossRef`.
9. Создать отдельные `NewsDataApi`, DTO, mapper и `NewsDataProvider`.
10. Добавить `FeedCoordinator` для нескольких активных providers.
11. Сохранить enabled providers в DataStore и подключить экран настроек.
12. Разнести provider bindings и ключи по build variants.
13. Добавить fake или третий provider и проверить расширяемость решения.

На каждом этапе приложение должно собираться и сохранять рабочее поведение.

## 17. Критерий успешной архитектуры

При добавлении нового поставщика должны появиться только:

- API-интерфейс;
- DTO;
- mapper;
- реализация `NewsProvider`;
- DI binding;
- пользовательское название и конфигурация доступности;
- тесты адаптера.

Не должны изменяться:

- HomeViewModel и SearchViewModel;
- Compose-экраны ленты и поиска;
- основной алгоритм repository;
- общий mediator/coordinator;
- DAO, если новый provider не вводит действительно новое продуктовое понятие.

Главный принцип архитектурного мышления для этой задачи:

> Определить ось будущих изменений и поставить перед ней одну устойчивую
> границу. Внешние API изменчивы; статья, лента, поиск и избранное — стабильная
> модель приложения.
