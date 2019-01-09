create DATABASE testdatabase;
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

create type calProvider as enum ('google', 'office');

create table calendar
(
  id         serial primary key,
  calendarID varchar(1000)   not null,
  activated  boolean         not null,
  provider    calProvider   not null,
  user_id    int             not null references appUser (id)
);

create table room (
  room_id  serial primary key,
  room_name varchar(255) not null,
  room_size int not null
);

create table room_Equipment (
  equip_id serial primary key,
  equip_name varchar(255) not null
);

create table room_room_Equipment (
  room_id int references room (room_id) on update cascade on delete cascade,
  room_Equipment_id int references room_Equipment (equip_id) on update cascade on delete cascade,

  constraint room_room_Equipment_pkey primary key (room_id, room_Equipment_id)
);
