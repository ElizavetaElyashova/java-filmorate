# java-filmorate
Ссылка на схему базы данных: https://github.com/ElizavetaElyashova/java-filmorate/blob/add-database/Filmorate.png

### Описание схемы:
* Основные таблицы *users* и *films* содержат записи о пользователях и фильмах, соответственно. Первичным ключом в обеих таблицах является поле *id*.
* Таблица *ratings* содержит названия рейтингов фильмов, первичный ключ - поле *id*. Поле *rating_id* таблицы *films* является внешним ключом к таблице *ratings* и полю *id*.
* Таблица *genres* содержит названия жанров фильмов, первичный ключ - поле *id*. 
* Таблица *film_genres* связывает (many-to-many) фильмы с жанрами, поле *film_id* является <u>внешним</u> ключом к таблице *films* и полю *id*, поле *genre_id* является <u>внешним</u> ключом к таблице *genres* и полю *id*.
* Таблица *likes* связывает (many-to-many) фильмы с пользователями, поставившими лайк. Поле *film_id* является <u>внешним</u> ключом к таблице *films* и полю *id*, поле *user_id* является <u>внешним</u> ключом к таблице *users* и полю *id*.
* Таблица *friends* отображает связь между пользователями. Если дружба непотверждена, то в таблице есть только одна запись, где id человека, отправившего запрос, будет *user_id*, а id того, кто получил запрос, будет *friend_id*. Если дружба потверждена, то в таблице появляется вторая запись, где id принявшего дружбу будет *user_id*, а id того, кто изначально отправил запрос, будет *friend_id*. Оба поля являются <u>внешними</u> ключами к таблице *users* и полю *id*.

### Запросы:
* #### Найти пользователя по id:</br>
  ```sql
  SELECT *
  FROM users
  WHERE id = *target_id*;
* #### Найти фильм по id:</br>
  ```sql
  SELECT f.id, f.name, f.description, f.release_date. f.duration, f.likes, r.name
  FROM films AS f
  JOIN ratings AS r ON f.rating_id = r.id
  WHERE f.id = *target_id*;
  ```
* #### Вывести пользователей, поставивших лайк фильму:</br>
  ```sql
  SELECT *
  FROM users
  WHERE id IN
    (SELECT user_id
    FROM likes
    WHERE film_id = *target_id*);
  ```
* #### Вывести жанры фильма:</br>
  ```sql
  SELECT name
  FROM genres
  WHERE id IN
    (SELECT genre_id
    FROM film_genre
    WHERE film_id = *target_id*);
* #### Вывести всех друзей пользователя:</br>
  ```sql
  SELECT *
  FROM users
  WHERE user_id IN
    (SELECT friend_id
    FROM friends
    WHERE user_id = *target_id*);
