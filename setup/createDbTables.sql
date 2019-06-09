create table currency (
  currency_name CHAR(3) NOT NULL PRIMARY KEY,
  currency_value DOUBLE PRECISION NOT NULL
);

create table marketplace (
	id INT NOT NULL PRIMARY KEY,
	marketplace_name VARCHAR(100) NOT NULL
);

create table listingStatus (
	id INT NOT NULL PRIMARY KEY,
	status_name VARCHAR(100) NOT NULL
);

create table location (
	id UUID NOT NULL PRIMARY KEY,
	manager_name VARCHAR(100) NOT NULL,
	phone VARCHAR(100) NOT NULL,
	address_primary VARCHAR(100) NOT NULL,
	address_secondary VARCHAR(100),
	country VARCHAR(100) NOT NULL,
	town VARCHAR(100) NOT NULL,
	postal_code VARCHAR(100)
);

create table listing (
  id UUID NOT NULL PRIMARY KEY,
	title VARCHAR(100) NOT NULL,
	description VARCHAR(200) NOT NULL,
	location_id UUID NOT NULL REFERENCES location(id),
	listing_price NUMERIC(19, 2) NOT NULL,
	currency CHAR(3) NOT NULL REFERENCES currency(currency_name),
	quantity INT NOT NULL,
	listing_status INT NOT NULL REFERENCES listingStatus(id),
	marketplace INT NOT NULL REFERENCES marketplace(id),
	upload_time DATE NOT NULL,
	owner_email_address VARCHAR(150) NOT NULL
);