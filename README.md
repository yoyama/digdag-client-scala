[![CI][CI-badge]][CI-link]

[CI-badge]:https://github.com/yoyama/digdag-client-scala/workflows/Merge%20master/badge.svg
[CI-link]:https://github.com/yoyama/digdag-client-scala/actions?query=workflow%3A%22Merge+master%22

# digdag-client-scala
digdag-client-scala is a library to access Digdag from scala.
It also contains digdag-shell which is an interactive shell based on Scala REPL.

## digdag-shell
An interractive shell to access Digdag server.
### How to install

```
> wget https://github.com/yoyama/digdag-client-scala/releases/download/v0.1.0-b1/digdag-shell.jar
> chmod +x ./digdag-shell.jar
> ./digdag-shell.jar

### Digdag Shell ###


scala> 
```

### How to use
```
scala> dcx.version
Success(0.9.42)
val res2: scala.util.Try[String] = Success(0.9.42)

scala> dcx.projects
| id | name       | revision                            | createdAt           | updatedAt           | deletedAt | archiveType | archiveMd5              |
|----|------------|-------------------------------------|---------------------|---------------------|-----------|-------------|-------------------------|
| 1  | yy_schedule| e794527d-3aae-438b-b854-b30f2a08c1a4| 2020-09-27T10:16:28Z| 2020-09-27T10:16:28Z|           | db          | kwcQWzYr1k22o6E7YsZ4Uw==|
| 2  | yy_for_each| af6f041b-b0b0-4704-ba5d-e55f76df8b48| 2020-10-09T10:18:31Z| 2020-10-09T10:18:31Z|           | db          | GrPylhMKMeoLhNHxmtu4bg==|
val res4: scala.util.Try[Seq[io.github.yoyama.digdag.client.model.ProjectRest]] = Success(List(ProjectRest(1,yy_schedule,e794527d-3aae-438b-b854-b30f2a08c1a4,2020-09-27T10:16:28Z,Some(2020-09-27T10:16:28Z),None,Some(db),Some(kwcQWzYr1k22o6E7YsZ4Uw==)), ProjectRest(2,yy_for_each,af6f041b-b0b0-4704-ba5d-e55f76df8b48,2020-10-09T10:18:31Z,Some(2020-10-09T10:18:31Z),None,Some(db),Some(GrPylhMKMeoLhNHxmtu4bg==))))

scala> dcx.vertical = true

scala> dcx.projects
          id | 1
        name | yy_schedule
    revision | e794527d-3aae-438b-b854-b30f2a08c1a4
   createdAt | 2020-09-27T10:16:28Z
   updatedAt | 2020-09-27T10:16:28Z
   deletedAt |
 archiveType | db
  archiveMd5 | kwcQWzYr1k22o6E7YsZ4Uw==
---------------------------------------------------
          id | 2
        name | yy_for_each
    revision | af6f041b-b0b0-4704-ba5d-e55f76df8b48
   createdAt | 2020-10-09T10:18:31Z
   updatedAt | 2020-10-09T10:18:31Z
   deletedAt |
 archiveType | db
  archiveMd5 | GrPylhMKMeoLhNHxmtu4bg==
---------------------------------------------------
val res7: scala.util.Try[Seq[io.github.yoyama.digdag.client.model.ProjectRest]] = Success(List(ProjectRest(1,yy_schedule,e794527d-3aae-438b-b854-b30f2a08c1a4,2020-09-27T10:16:28Z,Some(2020-09-27T10:16:28Z),None,Some(db),Some(kwcQWzYr1k22o6E7YsZ4Uw==)), ProjectRest(2,yy_for_each,af6f041b-b0b0-4704-ba5d-e55f76df8b48,2020-10-09T10:18:31Z,Some(2020-10-09T10:18:31Z),None,Some(db),Some(GrPylhMKMeoLhNHxmtu4bg==))))

```
