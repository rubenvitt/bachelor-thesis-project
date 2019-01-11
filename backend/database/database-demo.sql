insert into appUser (password, name, mail)
values ('123', 'Peter', 'peter@peter.de'),
       ('222', 'Hans', 'hans@hans.de');

insert into credential (credential, users_id)
values ('google-0001', 1),
       ('outlook-0001', 1),
       ('google-0002', 2);

insert into room (room_name, room_size)
values ('Room 1', 5),
       ('Room 2', 6),
       ('Small room', 3),
       ('Large room', 15);
select *
from room;

select * from calendar;

insert into room_Equipment (equip_name)
values ('Windows'),
       ('Beamer'),
       ('TV'),
       ('Whiteboard'),
       ('Height-adjustable desk');

insert into room_room_Equipment (room_id, room_Equipment_id)
values (1, 1),
       (1, 2),
       (1, 3),
       (2, 1),
       (2, 3),
       (2, 5),
       (3, 3),
       (4, 1),
       (4, 2),
       (4, 3),
       (4, 4),
       (4, 5);

insert into workingHours (user_fk, startTime, endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday)
VALUES (1, '08:00:00', '12:00:00', true, true, true, false, false, false, false);
