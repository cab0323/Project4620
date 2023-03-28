-- Christian Cabrera


USE Pizzeria;

CREATE TABLE savedcustomer( 
SavedCustomerID				int			NOT NULL UNIQUE AUTO_INCREMENT, 
SavedCustomerName			char(50) 	NOT NULL,
SavedCustomerPhoneNumber 	char(15)	DEFAULT "NONE",
SavedCustomerAddress		char(50)	DEFAULT "NONE", 
PRIMARY KEY(SavedCustomerID),
CHECK((SavedCustomerPhoneNumber IS NOT NULL) OR (SavedCustomerAddress IS NOT NULL)) 
);


CREATE TABLE basepizza(
BasePizzaID		int		NOT NULL UNIQUE AUTO_INCREMENT, 
BasePizzaSize		char(8)		NOT NULL,
BasePizzaCrustType		char(12) 	NOT NULL,
BasePizzaPrice		numeric(4, 2)			NOT NULL,
BasePizzaCost		numeric(4,2)		NOT	NULL,
CHECK(BasePizzaPrice >= 0), 
PRIMARY KEY(BasePizzaID)
);


CREATE TABLE discount(
DiscountCode	int		NOT NULL UNIQUE AUTO_INCREMENT,
DiscountName		char(20)	NOT NULL,
PercentageOff	numeric(4,2),
AmountOff	numeric(4, 2),
CHECK((AmountOff IS NOT NULL AND PercentageOff IS NULL) OR (AmountOff IS NULL AND PercentageOff IS NOT NULL)), 
PRIMARY KEY(DiscountCode)
);


CREATE TABLE topping(
ToppingName			char(20)			NOT NULL UNIQUE,
ToppingPrice		numeric(4,2)		NOT NULL, 
ToppingCost			numeric(4,2)		NOT NULL,
ToppingInventoryLevel	int				NOT NULL,
ToppingSmallUsed	numeric(3,2)		NOT NULL,
ToppingMediumUsed	numeric(3,2)		NOT NULL,
ToppingLargeUsed	numeric(3,2)		NOT NULL,
ToppingXlargeUsed	numeric(3,2)		NOT NULL,
check(ToppingPrice >= 0),
PRIMARY KEY(ToppingName)
);


CREATE TABLE foodrequest (
FoodRequestNumber			int					NOT NULL UNIQUE AUTO_INCREMENT,
-- FoodRequestPizzaNumber	int 	NOT NULL, 
FoodRequestPrice 			numeric(4,2)		NOT NULL DEFAULT 0.0,
FoodRequestCost				numeric(4,2)		NOT NULL DEFAULT 0.0,
FoodRequestStatus			char(20)			NOT NULL DEFAULT "making", 
FoodRequestTimestamp		TIMESTAMP			NOT NULL DEFAULT CURRENT_TIMESTAMP,
FoodRequestDiscountCode		int, 
PRIMARY KEY(FoodRequestNumber),
-- FOREIGN KEY(FoodRequestPizzaNumber) REFERENCES createdpizza(CreatedPizzaID),
FOREIGN KEY(FoodRequestDiscountCode) REFERENCES discount(DiscountCode)
);

CREATE TABLE createdpizza(
CreatedPizzaID					int 			NOT NULL UNIQUE AUTO_INCREMENT,
CreatedPizzaBaseID				int				NOT NULL,
CreatedPizzaFoodRequestID		int				NOT NULL, 
CreatedPizzaStatus				char(20)		NOT NULL DEFAULT "making",
CreatedPizzaPrice				numeric(4,2)	NOT NULL DEFAULT 0.0,
CreatedPizzaCost				numeric(4,2)	NOT NULL DEFAULT 0.0,
PRIMARY KEY(CreatedPizzaID),
FOREIGN KEY(CreatedPizzaFoodRequestID) REFERENCES foodrequest(FoodRequestNumber),
FOREIGN KEY(CreatedPizzaBaseID) REFERENCES basepizza(BasePizzaID)
);

-- linking table for the toppings to go on the pizza 
CREATE TABLE toppingused(
ToppingUsedID			int		NOT NULL UNIQUE AUTO_INCREMENT,
ToppingUsedPizzaID		int 	NOT NULL,
ToppingUsedName			char(20)		NOT NULL, 
ToppingUsedPrice		numeric(4,2)	NOT NULL,
ToppingUsedCost			numeric(4,2)	NOT NULL,
ToppingUsedExtra		char(1)			NOT NULL,
PRIMARY KEY(ToppingUsedID), 
FOREIGN KEY(ToppingUsedName) REFERENCES topping(ToppingName), 
FOREIGN KEY(ToppingUsedPizzaID) REFERENCES createdpizza(CreatedPizzaID),
CHECK(ToppingUsedPrice >= 0)
);

CREATE TABLE dineinorder(
DineInOrderID				int		NOT NULL UNIQUE AUTO_INCREMENT,
DineInOrderFoodRequestID	int	 NOT NULL,
PRIMARY KEY(DineInOrderID),
FOREIGN KEY(DineInOrderFoodRequestID) REFERENCES foodrequest(FoodRequestNumber)
-- FOREIGN KEY(DineInOrderTable) REFERENCES seat(SeatTableNumber)
);


CREATE TABLE dineoutorder(
DineOutOrderID					int		NOT NULL UNIQUE AUTO_INCREMENT, 
DineOutOrderFoodRequestID		int		NOT NULL,
DineOutOrderReturnCustomerID		int		NOT NULL,
PRIMARY KEY(DineOutOrderID),
FOREIGN KEY(DineOutOrderFoodRequestID) REFERENCES foodrequest(FoodRequestNumber),
FOREIGN KEY(DineOutOrderReturnCustomerID) REFERENCES savedcustomer(SavedCustomerID)
);

CREATE TABLE seating(
SeatingID				int		NOT NULL AUTO_INCREMENT,
SeatingTable			int 	NOT NULL,
SeatingSeatNumber		int		NOT NULL,
SeatingDineInOrder		int		NOT NULL,
PRIMARY KEY(SeatingID, SeatingTable),
FOREIGN KEY(SeatingDineInOrder) REFERENCES dineinorder(DineInOrderID),
CHECK((SeatingTable > 0 ) AND (SeatingSeatNumber > 0))
);

/*
CREATE TABLE createdpizza(
CreatedPizzaID					int			NOT NULL UNIQUE AUTO_INCREMENT,
CreatedPizzaBasePizzaID			int			NOT NULL,
CreatePizzaStatus				char(10)	NOT NULL,
CreatedPizzaDiscountCode		int,
CreatedPizzaPrice				numeric(4,2)	NOT NULL,
CreatedPizzaCost				numeric(4,2)	NOT NULL,
PRIMARY KEY(CreatedPizzaID),
FOREIGN KEY(CreatedPizzaBasePizzaID) REFERENCES basepizza(BasePizzaID),
FOREIGN KEY(CreatedPizzaDiscountCode) REFERENCES discount(DiscountCode)
);
*/

















