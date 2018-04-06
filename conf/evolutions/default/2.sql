# --- Sample dataset

# --- !Ups

insert into friend (id, fio, phone_number, social_number, email, comment) values (
  1, 'Pakhomov Alexander Sergeevich', '8-800-555-35-35', 'AlexanderPakhomov', 'Sasha@icloud.com', 'Best friend');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  2, 'Brusencev Ivan Ivanivich', '8-890-111-22-22', null, 'Brus@icloud.com', 'Good person');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  3, 'Shabanov Artemii Alexandrovich', '8-900-323-11-11', 'artemii36', 'Shopen@icloud.com', 'Blck and wt');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
   4, 'Test Testovich Testov', '8-800-555-33-33', 'Testik', 'Test@gmail.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
   5, 'Testa Testovna Testova', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  6, '66666666666666a', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  7, '77777777777777', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  8, '888888888888888', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  9, '12122112 1 112 12', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  10, 'T124124124124', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  11, 'Te1 23123124 estova', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  12, 'Terthgrghtr rrtyj stova', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  13, 'T ete ryey eyva', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  14, 'Test ert rete rer tstova', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');
insert into friend (id, fio, phone_number, social_number, email, comment) values (
  15, 'Tes er ter terstova', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');


insert into genre (id, name, id_parent_genre) values (1, "Classic", null);
insert into genre (id, name, id_parent_genre) values (2, "Classic1", null);
insert into genre (id, name, id_parent_genre) values (3, "Classic2", null);
insert into genre (id, name, id_parent_genre) values (4, "Classic3", null);
insert into genre (id, name, id_parent_genre) values (5, "Fastastic", null);
insert into genre (id, name, id_parent_genre) values (6, "Fastastic1", null);
insert into genre (id, name, id_parent_genre) values (7, "Fastastic2", null);

insert into publishing_house(id, name) values (1, "Piter house");
insert into publishing_house(id, name) values (2, "Moscow house");
insert into publishing_house(id, name) values (3, "Lipect house");
insert into publishing_house(id, name) values (4, "Voronezh house");
insert into publishing_house(id, name) values (5, "London house");
insert into publishing_house(id, name) values (6, "Warsaw house");


insert into book(id, name, pub_year, pic_autor, translator, author, comment, id_pub_house) values (
    1, "Effective java", "01-01-1997", null, null, null, null, null);
insert into book(id, name, pub_year, pic_autor, translator, author, comment, id_pub_house) values (
  2, "Effective java1", "01-01-1998", null, null, null, null, null);
insert into book(id, name, pub_year, pic_autor, translator, author, comment, id_pub_house) values (
  3, "Effective java2", "01-01-1999", null, null, null, null, null);
insert into book(id, name, pub_year, pic_autor, translator, author, comment, id_pub_house) values (
  4, "Effective java3", "01-01-1991", null, null, null, null, null);
insert into book(id, name, pub_year, pic_autor, translator, author, comment, id_pub_house) values (
  5, "Effective java4", "01-01-1992", null, null, null, null, null);


insert into borrowing(id_book, id_friend, borrow_date, is_lost, is_damaged, return_date, comment) values (
    1, 1, "01-01-2017",null, null, null, null);
insert into borrowing(id_book, id_friend, borrow_date, is_lost, is_damaged, return_date, comment) values (
  3, 2, "01-01-2018",null, null, null, null);
insert into borrowing(id_book, id_friend, borrow_date, is_lost, is_damaged, return_date, comment) values (
  2, 4, "01-01-2016",null, null, null, null);


insert into book_genre (id_book, id_genre) values (1, 1);
insert into book_genre (id_book, id_genre) values (1, 5);
insert into book_genre (id_book, id_genre) values (2, 5);



# --- !Downs

delete from friend;