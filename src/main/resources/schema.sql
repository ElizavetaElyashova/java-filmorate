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