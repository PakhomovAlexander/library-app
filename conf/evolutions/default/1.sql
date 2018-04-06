# --- First database schema

# --- !Ups

set ignorecase true;

create table friend (
  id                 bigint not null,
  fio                varchar(255) not null,
  phone_number       varchar(255),
  social_number      varchar(255),
  email              varchar(255),
  comment            varchar(255),

  constraint pk_friend primary key (id)
);


create table genre (
  id                bigint not null,
  name              varchar(255) not null,
  id_parent_genre   bigint,

  constraint pk_genre primary key (id),
  constraint fk_genre foreign key (id_parent_genre) references genre(id)
);


create table publishing_house (
  id                bigint not null,
  name              varchar(255) not null,

  constraint  pk_publishing_house primary key (id)
);


create table book (
  id                bigint not null,
  name              varchar(255) not null,
  pub_year          varchar(255),
  pic_autor         varchar(255),
  translator        varchar(255),
  author            varchar(255),
  comment           varchar(255),
  id_pub_house      bigint,

  constraint pk_book primary key (id),
  constraint fk_pub_house foreign key (id_pub_house) references publishing_house(id)
);


create table book_genre (
  id_book           bigint not null,
  id_genre          bigint not null,

  constraint pk_book_genre primary key (id_book, id_genre)
);


create table borrowing (
  id_book           bigint not null,
  id_friend         bigint not null,
  borrow_date       date not null,
  is_lost           bool,
  is_damaged        bool,
  return_date       date,
  comment           varchar(255),

  constraint fk_book foreign key (id_book) references book(id),
  constraint fk_friend foreign key (id_friend) references friend(id)
);



create sequence friend_seq start with 1000;
create sequence genre_seq start with 1000;
create sequence publishing_house_seq start with 1000;
create sequence book_seq start with 1000;
create sequence borrowing_seq start with 1000;


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists friend;
drop table if exists genre;
drop table if exists friend_seq;
drop table if exists book_seq;
drop table if exists borrowing_seq;
drop table if exists book_genre;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists friend_seq;
drop sequence if exists genre_seq;
drop sequence if exists publishing_house_seq;
drop sequence if exists book;
drop sequence if exists borrowing;

