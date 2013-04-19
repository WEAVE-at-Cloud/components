CREATE TABLE sales.guest.customer (
    id int,
    fname varchar(25),
    lname varchar(25),
    addr1 varchar(25),
    addr2 varchar(25),
    addr3 varchar(25),
    cntry varchar(25)
);

CREATE TABLE sales.guest.product(
	id int,
	name varchar(25),
	price money
);

CREATE TABLE sales.guest.sale(
	id int,
	customerid int,
	productid int,
	quantity int,
	date datetime
);

INSERT INTO sales.guest.customer (id, fname, lname, addr1, addr2, addr3, cntry) VALUES (0, 'Paul', 'McGrath', 'Bray Wanders', 'Bray', '', 'Ireland');
INSERT INTO sales.guest.customer (id, fname, lname, addr1, addr2, addr3, cntry) VALUES (1, 'Liam', 'Brady', 'Juventus', 'Turin', '', 'Italy');
INSERT INTO sales.guest.customer (id, fname, lname, addr1, addr2, addr3, cntry) VALUES (2, 'Ian', 'Rush', 'Anfield', 'Liverpool', '', 'England');
INSERT INTO sales.guest.customer (id, fname, lname, addr1, addr2, addr3, cntry) VALUES (3, 'John', 'Barnes', 'Anfield', 'Liverpool', '', 'England');


INSERT INTO sales.guest.product (id, name, price) VALUES (0, 'Football Shorts', $12.99);
INSERT INTO sales.guest.product (id, name, price) VALUES (1, 'Football Socks', $8.49);
INSERT INTO sales.guest.product (id, name, price) VALUES (2, 'Football Boots', $99.99);
INSERT INTO sales.guest.product (id, name, price) VALUES (3, 'Football Jersey', $46.50);

-- INSERT INTO sales.guest.sale (id, customerid, productid, quantity, date) VALUES  (, , , , );