create table accounts(
                         id  uuid  not null,
                         name varchar(255) not null,
                         inserted_at timestamp(6) not null,
                         updated_at timestamp(6) not null,
                         primary key (id)
);


create table balances(
                         id  uuid not null,
                         type varchar(50) not null,
                         amount NUMERIC(15,2) not null,
                         account_id uuid not null,
                         inserted_at timestamp(6) not null,
                         updated_at timestamp(6) not null,
                         primary key (id),
                         constraint fk_balances_accounts
                             foreign key (account_id) references accounts (id)
);


create table merchants(
                          id  uuid not null,
                          name varchar(255) not null,
                          balance_type varchar(50) not null,
                          primary key (id)
);

create table merchants_category_code(
                                        id  uuid not null,
                                        code varchar(10) not null,
                                        balance_type varchar(50) not null,
                                        primary key (id)
);


create table transactions(
                             id  uuid not null,
                             payload varchar(500) not null,
                             account_id uuid not null,
                             balance_id uuid not null,
                             amount NUMERIC(15,2) not null,
                             inserted_at timestamp(6) not null,
                             primary key (id)
);