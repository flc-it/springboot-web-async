# springboot-web-async

## Présentation
Le projet *springboot-web-async* permet d'exécuter des traitements web (=request) dans des threads pool dédiés.


## Exemples d'implémentation

Avec l'utilisation des Beans :
```java
@Autowired
private WebAsyncTaskService webAsyncTaskService;

@Autowired
private PublicThreadPoolTaskExecutor executor;
```

```java
@GetMapping("/{nom}/configuration")
public WebAsyncTask<KeyStore> configuration(@PathVariable String nom) {
    return webAsyncTaskService.send(() -> service.getToOpen(nom), executor);
}
```

```java
@GetMapping("/{nom}/download")
public WebAsyncTask<Void> export2(HttpServletResponse response, @PathVariable String nom) {
    return webAsyncTaskService.sendThrows(() -> service.downloadWriteTo(nom, response), executor);
}
```

Avec l'utilisation de l'annotation @WebAsync :

```java
@GetMapping("/{nom}/configuration")
@WebAsync(PublicThreadPoolTaskExecutor.BEAN_NAME)
public WebAsyncTask<KeyStore> configuration(@PathVariable String nom) throws IOException {
    return WebAsyncTaskUtils.send(service.getToOpen(nom));
}
```

```java
@GetMapping("/{nom}/download")
@WebAsync(PublicThreadPoolTaskExecutor.BEAN_NAME)
public WebAsyncTask<Void> export2(HttpServletResponse response, @PathVariable String nom) throws IOException {
    service.downloadWriteTo(nom, response);
    return WebAsyncTaskUtils.send();
}
```

## Configuration
L'usage de l'annotation @WebAsync est désactivé par défaut, son utilisation requiert la property suivante :
```properties
async.web.annotation=true
```

## Projets dépendants
- [secure-store-manager-back](https://github.com/flc-it/secure-store-manager-back)