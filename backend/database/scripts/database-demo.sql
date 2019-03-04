insert into appUser (password, name, mail, position, avatar)
values ('123', 'Marcel Kastner', 'marcel@rubeen.me', 'Consultant', 'https://randomuser.me/api/portraits/men/78.jpg'),
       ('222', 'Alisa Amendt', 'alisa@rubeen.me', 'Senior Consultant', 'https://randomuser.me/api/portraits/women/91.jpg'),
       ('222', 'Laurenz Bergmann', 'laurenz@rubeen.me', 'Consultant', 'https://randomuser.me/api/portraits/men/33.jpg'),
       ('222', 'David Faber', 'david@rubeen.me', 'Management Consultant', 'https://randomuser.me/api/portraits/men/37.jpg'),
       ('222', 'Annett Schneider', 'annett@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/women/27.jpg'),
       ('222', 'Angelika Hirsch', 'angelika@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/women/53.jpg'),
       ('222', 'Johanna Pfeiffer', 'johanna@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/women/31.jpg'),
       ('222', 'Steffen Gersten', 'steffen@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/men/11.jpg'),
       ('222', 'Manuela Moeller', 'manuela@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/women/11.jpg'),
       ('222', 'Ines Zimmermann', 'ines@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/women/62.jpg'),
       ('222', 'Katharina Reiniger', 'katharina@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/women/61.jpg'),
       ('222', 'Kathrin Trommler', 'kathrin@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/women/60.jpg'),
       ('222', 'Michelle Loewe', 'michelle@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/women/59.jpg'),
       ('222', 'Daniel Jager', 'daniel@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/men/58.jpg'),
       ('222', 'Phillipp Rothschild', 'phillip@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/men/57.jpg'),
       ('222', 'René Peters', 'rene@rubeen.me', 'External, Hubertus AG', 'https://randomuser.me/api/portraits/men/56.jpg'),
       ('333', 'Ruben Vitt', 'r.vitt@fme.de', 'Student', 'https://www.xing.com/image/3_d_b_8160fa37a_24954449_5/ruben-vitt-foto.256x256.jpg');

insert into room (room_name, room_size, provider, calendarid)
values ('Room 1', 5, 'room-service', 'room-1-calendar-id'),
       ('Room 2', 6, 'room-service', 'room-2-calendar-id'),
       ('Small room', 3, 'room-service', 'small-room-calendar-id'),
       ('Large room', 15, 'room-service', 'large-room-calendar-id');

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
