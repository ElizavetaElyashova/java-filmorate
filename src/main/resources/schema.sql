CREATE TABLE IF NOT EXISTS users (
  id long AUTO_INCREMENT PRIMARY KEY,
  email varchar,
  login varchar,
  name varchar,
  birthday date
);

CREATE TABLE IF NOT EXISTS ratings (
  id integer AUTO_INCREMENT PRIMARY KEY,
  name varchar UNIQUE
);

CREATE TABLE IF NOT EXISTS films (
  id long AUTO_INCREMENT PRIMARY KEY,
  name varchar,
  description text,
  release_date date,
  duration integer,
  likes integer,
  rating_id integer REFERENCES ratings(id)
);

CREATE TABLE IF NOT EXISTS genres (
  id integer AUTO_INCREMENT PRIMARY KEY,
  name varchar UNIQUE
);


CREATE TABLE IF NOT EXISTS film_genre (
  film_id long REFERENCES films(id),
  genre_id integer REFERENCES genres(id),
  PRIMARY KEY (film_id, genre_id)
);


CREATE TABLE IF NOT EXISTS likes (
  film_id long REFERENCES films(id),
  user_id long REFERENCES users(id),
  PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS friends (
  user_id long REFERENCES users(id),
  friend_id long REFERENCES users(id),
  PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS reviews (
  review_id long AUTO_INCREMENT PRIMARY KEY,
  user_id long REFERENCES users(id) ON DELETE CASCADE,
  film_id long REFERENCES films(id) ON DELETE CASCADE,
  content varchar,
  is_positive boolean,
  useful integer
);

CREATE TABLE IF NOT EXISTS reviews_likes (
    review_id long REFERENCES reviews(review_id),
    user_id long REFERENCES users(id),
    PRIMARY KEY(review_id, user_id)
);

CREATE TABLE IF NOT EXISTS reviews_dislikes (
    review_id long REFERENCES reviews(review_id),
    user_id long REFERENCES users(id),
    PRIMARY KEY(review_id, user_id)
);

CREATE TABLE IF NOT EXISTS directors (
  id long AUTO_INCREMENT PRIMARY KEY,
  name varchar
);

CREATE TABLE IF NOT EXISTS film_director (
  film_id long REFERENCES films(id),
  director_id long REFERENCES directors(id),
  PRIMARY KEY (film_id, director_id)
);

CREATE TABLE IF NOT EXISTS event_types (
  id integer AUTO_INCREMENT PRIMARY KEY,
  name varchar UNIQUE
);

CREATE TABLE IF NOT EXISTS operations (
  id integer AUTO_INCREMENT PRIMARY KEY,
  name varchar UNIQUE
);

CREATE TABLE IF NOT EXISTS feed (
  event_id long AUTO_INCREMENT PRIMARY KEY,
  event_type_id integer REFERENCES event_types(id),
  operation_id integer REFERENCES operations(id),
  entity_id long,
  user_id long REFERENCES users(id),
  timestamp TIMESTAMP 
);