-- DDL
create table accounts
(
    id          varchar(100) not null,
    name        varchar(255) not null,
    inserted_at datetime     not null,
    updated_at  datetime     not null,
    primary key (id)
);


create table balances
(
    id          varchar(100)   not null,
    type        varchar(50)    not null,
    amount      decimal(15, 2) not null,
    account_id  varchar(100)   not null,
    inserted_at datetime       not null,
    updated_at  datetime       not null,
    primary key (id),
    constraint fk_balances_accounts
        foreign key (account_id) references accounts (id)
);


create table merchants
(
    id           varchar(100) not null,
    name         varchar(255) not null,
    balance_type varchar(50)  not null,
    primary key (id)
);

create table merchants_category_code
(
    id           varchar(100) not null,
    code         varchar(10)  not null,
    balance_type varchar(50)  not null,
    primary key (id)
);


create table transactions
(
    id          varchar(100)   not null,
    payload     varchar(500)   not null,
    account_id  varchar(100)   not null,
    balance_id  varchar(100)   not null,
    amount      decimal(15, 2) not null,
    inserted_at datetime       not null,
    primary key (id)
);

-- DML
insert into accounts(id, name,inserted_at,updated_at)  values ('554e590b-a4bc-4859-b245-cbb4701fdbbd', 'Edu', now(), now());
insert into accounts(id, name,inserted_at,updated_at)  values ('fcd31a0b-3bfa-4996-9f74-74d51e98ed10', 'Monica', now(), now());

insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (UUID(),'FOOD',10000,'554e590b-a4bc-4859-b245-cbb4701fdbbd', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (UUID(),'MEAL',10000,'554e590b-a4bc-4859-b245-cbb4701fdbbd', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (UUID(),'CASH',10000,'554e590b-a4bc-4859-b245-cbb4701fdbbd', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (UUID(),'FOOD',10000,'fcd31a0b-3bfa-4996-9f74-74d51e98ed10', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (UUID(),'MEAL',10000,'fcd31a0b-3bfa-4996-9f74-74d51e98ed10', now(), now());

insert into merchants(id, name, balance_type)  values (UUID(),'UBER TRIP                   SAO PAULO BR ','CASH');
insert into merchants(id, name, balance_type)  values (UUID(),'UBER EATS                   SAO PAULO BR','MEAL');
insert into merchants(id, name, balance_type)  values (UUID(),'PAG*JoseDaSilva          RIO DE JANEI BR','FOOD');
insert into merchants(id, name, balance_type)  values (UUID(),'PICPAY*BILHETEUNICO           GOIANIA BR','CASH');

insert into merchants_category_code(id, code, balance_type) values (UUID(),'5411','FOOD');
insert into merchants_category_code(id, code, balance_type) values (UUID(),'5412','FOOD');
insert into merchants_category_code(id, code, balance_type) values (UUID(),'5811','MEAL');
insert into merchants_category_code(id, code, balance_type) values (UUID(),'5812','MEAL');