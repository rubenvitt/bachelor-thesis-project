create type calProvider as enum ('google', 'office', 'room-service');

create table appUser
(
  id       serial primary key,
  password varchar(255) not null,
  name     varchar(255) not null,
  mail     varchar(255) not null unique,
  position varchar(255) not null,
  avatar   varchar(500) not null default 'https://randomuser.me/api/portraits/lego/1.jpg'
);

create table workingHours
(
  id        serial primary key,
  user_fk   int references appUser (id) on delete cascade,
  startTime time    not null,
  endTime   time    not null,
  monday    boolean not null default false,
  tuesday   boolean not null default false,
  wednesday boolean not null default false,
  thursday  boolean not null default false,
  friday    boolean not null default false,
  saturday  boolean not null default false,
  sunday    boolean not null default false
);

create table credential
(
  user_id   int            not null references appUser (id) on delete cascade,
  credential text          not null,
  provider   calprovider   not null,

  PRIMARY KEY (user_id)
);

create table calendar
(
  id         serial primary key,
  calendarID varchar(1000)   not null,
  activated  boolean         not null,
  provider    calProvider   not null,
  user_id    int             not null references appUser (id),
  isDefault   boolean         not null default false
);

create table room (
  room_id  serial primary key,
  room_name varchar(255) not null,
  room_size int not null,
  provider calProvider not null,
  calendarID varchar(1000) not null
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
