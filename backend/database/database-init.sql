create user testuser with encrypted password 'testpass';
grant all privileges on database testdatabase to testuser;

create table appUser
(
  id       serial primary key ,
  password varchar(255)    not null,
  name     varchar(255)    not null,
  mail     varchar(255)    not null
);

create table credential
(
  id         serial primary key ,
  credential varchar(1000)   not null,
  users_id   int             not null references appUser (id)
);

create table calendar
(
  id         serial primary key,
  calendarID varchar(1000)   not null,
  activated  boolean         not null,
  user_id    int             not null references appUser (id)
);
