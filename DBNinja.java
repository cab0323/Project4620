//package Project4620;

import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * This file is where most of your code changes will occur You will write the code to retrieve
 * information from the database, or save information to the database
 * 
 * The class has several hard coded static variables used for the connection, you will need to
 * change those to your connection information
 * 
 * This class also has static string variables for pickup, delivery and dine-in. If your database
 * stores the strings differently (i.e "pick-up" vs "pickup") changing these static variables will
 * ensure that the comparison is checking for the right string in other places in the program. You
 * will also need to use these strings if you store this as boolean fields or an integer.
 * 
 * 
 */

/**
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {
  // enter your user name here
  private static String user = "admin";
  // enter your password here
  private static String password = "torres01";
  // enter your database name here
  private static String database_name = "Pizzeria";
  // Do not change the port. 3306 is the default MySQL port
  private static String url = "jdbc:mysql://cpsc4620.czgul3f8uqif.us-east-1.rds.amazonaws.com:3306";
  private static Connection conn;

  // Change these variables to however you record dine-in, pick-up and delivery, and sizes and
  // crusts
  public final static String pickup = "pickup";
  public final static String delivery = "delivery";
  public final static String dine_in = "dine-in";

  public final static String size_s = "Small";
  public final static String size_m = "Medium";
  public final static String size_l = "Large";
  public final static String size_xl = "X-Large";

  public final static String crust_thin = "Thin";
  public final static String crust_orig = "Original";
  public final static String crust_pan = "Pan";
  public final static String crust_gf = "Gluten-Free";



  /**
   * This function will handle the connection to the database
   * 
   * @return true if the connection was successfully made
   * @throws SQLException
   * @throws IOException
   */
  private static boolean connect_to_db() throws SQLException, IOException {

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");


    } catch (ClassNotFoundException e) {
      System.out.println("Could not load the driver");

      System.out.println("Message     : " + e.getMessage());


      return false;
    }

/*
    try {
      String dbURL = "jdbc:mysql:cpsc4620.czgul3f8uqif.us-east-1.rds.amazonaws.com:3306/Pizzeria";
      String username = "admin";
      String password = "torres01";

      Connection connection = DriverManager.getConnection(dbURL, username, password);
    } catch (SQLException e) {
      for(Throwable t : e){
        System.out.println(t);
      }
    }
*/

    conn = DriverManager.getConnection(url + "/" + database_name, user, password);
    return true;
  }


  /**
   *
   * @param o order that needs to be saved to the database
   * @throws SQLException
   * @throws IOException
   * @requires o is not NULL. o's ID is -1, as it has not been assigned yet. The pizzas do not exist
   *           in the database yet, and the topping inventory will allow for these pizzas to be made
   * @ensures o will be assigned an id and added to the database, along with all of it's pizzas.
   *          Inventory levels will be updated appropriately
   */
  public static void addOrder(Order o) throws SQLException, IOException {
    connect_to_db();
    /*
     * add code to add the order to the DB. Remember to add the pizzas and discounts as well, which
     * will involve multiple tables. Customer should already exist. Toppings will need to be added
     * to the pizzas.
     *
     * It may be beneficial to define more functions to add an individual pizza to a database, add a
     * topping to a pizza, etc.
     *
     * Note: the order ID will be -1 and will need to be replaced to be a fitting primary key.
     *
     * You will also need to add timestamps to your pizzas/orders in your database. Those timestamps
     * are not stored in this program, but you can get the current time before inserting into the
     * database
     *
     * Remember, when a new order comes in the ingredient levels for the topping need to be adjusted
     * accordingly. Remember to check for "extra" of a topping here as well.
     *
     * You do not need to check to see if you have the topping in stock before adding to a pizza.
     * You can just let it go negative.
     */

    //ADD THE FOODORDER FIRST THEN ASSIGN THE FOOD ORDER ID TO EITHER A DINE IN/OUT ORDER
    String addFoodRequest = "INSERT INTO foodrequest(FoodRequestPrice, FoodRequestCost, FoodRequestTimestamp)" +
            "VALUES(DEFAULT, DEFAULT , DEFAULT)";
    PreparedStatement addingFoodRequest = conn.prepareStatement(addFoodRequest);
    addingFoodRequest.executeUpdate();

    //GET THE FoodRequestID TO PASS TO THE PIZZA TABLE
    int foodRequestID = o.getID();
    String getFoodRequestID = "SELECT LAST_INSERT_ID()";
    PreparedStatement tf = conn.prepareStatement(getFoodRequestID);
    ResultSet t = tf.executeQuery();

    if(t.next()){
      foodRequestID = t.getInt(1);
    }

    //test fodrequest id
    System.out.println("The FoodRequestId is: "  + foodRequestID);

    //MAKE THE TOPPING ARRAYLIST
    ArrayList<Pizza> pizzasOrdered;
    pizzasOrdered = o.getPizzas();

    //CALL THE MAKE PIZZAS METHOD, WHICH WILL MAKE EACH INDIVIDUAL PIZZA
    for(int i = 0; i < pizzasOrdered.size(); i++){
      makePizza(pizzasOrdered.get(i), foodRequestID);
    }


/*    if(orderCust instanceof DeliveryCustomer) {
        DeliveryCustomer cust = (DeliveryCustomer) orderCust;

        String addFoodRequest = "INSERT INTO foodrequest(FoodRequestPrice, FoodRequestCost, FoodRequestTimestamp)" +
                "VALUES(DEFAULT, DEFAULT , DEFAULT)";
        PreparedStatement addCPrepare = conn.prepareStatement(addFoodRequest);
        ResultSet testset = addCPrepare.executeQuery();

    }
    else if(c instanceof DineOutCustomer) {
        DineOutCustomer pickupCast = (DineOutCustomer) c;
        String addC = "INSERT INTO savedCustomer(SavedCustomerName, SavedCustomerPhoneNumber, SavedCustomerAddress)" +
                "VALUES (?, ?, NULL)";
        PreparedStatement addCPrepare = conn.prepareStatement(addC);
        addCPrepare.setString(1, pickupCast.getName());
        addCPrepare.setString(2,pickupCast.getPhone());

        ResultSet testset = addCPrepare.executeQuery();
    }*/

    ICustomer customer = o.getCustomer();
    insertDineOutIn(foodRequestID, customer);


    conn.close();

  }

  public static void makePizza(Pizza p, int orderNumber) throws SQLException, IOException {
      connect_to_db();
        String addPizza = "INSERT INTO createdpizza(CreatedPizzaBaseID, CreatedPizzaFoodRequestID)" +
                "VALUES((SELECT BasePizzaID FROM basepizza WHERE BasePizzaSize = ? AND BasePizzaCrustType = ?), " + orderNumber + " );";
        PreparedStatement aPizza = conn.prepareStatement(addPizza);
        aPizza.setString(1, p.getSize());
        aPizza.setString(2,p.getCrust());
        aPizza.executeUpdate();

        int pizzaID = p.getID();
        String getPizzaID = "SELECT LAST_INSERT_ID()";
        PreparedStatement getPizza = conn.prepareStatement(getPizzaID);
        ResultSet getP = getPizza.executeQuery();
        if(getP.next()){
          pizzaID = getP.getInt(1);
          System.out.println("Last inserted PizzaID is : " + pizzaID);
        }

        //LOOP THROUGH TOPPING TO INSERT EACH TOPPING
        ArrayList<Topping> toppingsOnPizza = p.getToppings();
        for(int i = 0; i < toppingsOnPizza.size(); i++){
          //CALL FUNCTION TO ADD TOPPING USED ON SPECIFIC PIZZA
          pizzaToppingCombo(toppingsOnPizza.get(i), pizzaID);
        }


        updatePizzaPriceCost(pizzaID);

      conn.close();
  }

  //FUNCTION TO ADD THE PIZZA TOPPING TO THE TABLE THAT HOLDS THE COMBO OF WHICH PIZZA HAD WHICH TOPPING
  public static void pizzaToppingCombo(Topping t, int pizzaID) throws SQLException, IOException{
      connect_to_db();
      String insertTopping = "INSERT INTO toppingused(ToppingUsedPizzaID, ToppingUsedName, ToppingUsedPrice, ToppingUsedCost,ToppingUsedExtra)" +
              "VALUES(?, ?, (SELECT ToppingPrice FROM topping WHERE ToppingName = ?), (SELECT ToppingCost FROM topping WHERE ToppingName = ?), ?);";
      PreparedStatement insertTop = conn.prepareStatement(insertTopping);
      insertTop.setInt(1, pizzaID);
      insertTop.setString(2, t.getName());
      insertTop.setString(3, t.getName());
      insertTop.setString(4, t.getName());
      if(t.getExtra()){
        insertTop.setString(5, "Y");
      }
      else {
        insertTop.setString(5, "N");
      }

      //UPDATE TO INSERT THE TOPPING
      insertTop.executeUpdate();

      //UPDATE THE PRICE OF THE TOPPINGS THAT ARE EXTRA
      String updatePrice = "UPDATE toppingused SET ToppingUsedPrice = ToppingUsedPrice * 2 WHERE ToppingUsedExtra = " + "\"Y\"" + "AND ToppingUsedPizzaID = " + pizzaID + ";";
      PreparedStatement updatePriceP = conn.prepareStatement(updatePrice);
      updatePriceP.executeUpdate();

      //UPDATE COST
      String updateCost = "UPDATE toppingused SET ToppingUsedCost = ToppingUsedCost * 2 WHERE ToppingUsedExtra = " + "\"Y\"" + "AND ToppingUsedPizzaID = " + pizzaID + ";";
      PreparedStatement updateCostP = conn.prepareStatement(updateCost);
      updateCostP.executeUpdate();

      conn.close();

  }


  //UPDATE THE PRICE OF THE CREATED PIZZA
  public static void updatePizzaPriceCost(int pizzaID) throws SQLException, IOException{
    connect_to_db();

        //UPDATE PRICE AND COST OF PIZZA
        String updatePrice = "UPDATE createdpizza SET CreatedPizzaPrice = (SELECT SUM(ToppingUsedPrice) FROM toppingused WHERE ToppingUsedPizzaID = ?) WHERE CreatedPizzaID = ?;";
        PreparedStatement updatePriceP = conn.prepareStatement(updatePrice);
        updatePriceP.setInt(1, pizzaID);
        updatePriceP.setInt(2, pizzaID);
        updatePriceP.executeUpdate();

        String updateCost = "UPDATE createdpizza SET CreatedPizzaCost = (SELECT SUM(ToppingUsedCost) FROM toppingused WHERE ToppingUsedPizzaID = ?) WHERE CreatedPizzaID = ?;";
        PreparedStatement updateCostP = conn.prepareStatement(updateCost);
        updateCostP.setInt(1, pizzaID);
        updateCostP.setInt(2, pizzaID);
        updateCostP.executeUpdate();

    conn.close();
  }

  //ADD THE ORDERS TO THE RIGHT TABLE DEPENDING ON DINE OUT OR DINE IN
  public static void insertDineOutIn(int foodRequestID, ICustomer customer) throws SQLException, IOException{
    connect_to_db();
    if(customer instanceof DeliveryCustomer){
      String name = ((DeliveryCustomer) customer).getName();
      String addCustomer = "INSERT INTO dineoutorder(DineOutOrderFoodRequestID, DineoutOrderReturnCustomerID)" +
              "VALUES( " + foodRequestID + ", (SELECT SavedCustomerID FROM savedcustomer WHERE SavedCustomerName = ?));";
      PreparedStatement addCustomerP = conn.prepareStatement(addCustomer);
      addCustomerP.setString(1, name);
      addCustomerP.executeUpdate();
    }
    else if(customer instanceof DineInCustomer){
      String addDineIn = "INSERT INTO dineinorder(DineInOrderFoodRequestID)" +
              "VALUES(" + foodRequestID + ");";
      PreparedStatement addDineInP = conn.prepareStatement(addDineIn);
      addDineInP.executeUpdate();

      //GET THE ID OF THE DINEIN TABLE TO PASS TO SEATING TABLE
      String getDineInID = "SELECT LAST_INSERT_ID()";
      PreparedStatement getDineInP = conn.prepareStatement(getDineInID);
      ResultSet getDineInR = getDineInP.executeQuery();

      int dineInID = -1;
      if(getDineInR.next()){
        dineInID = getDineInR.getInt(1);
      }

      //ADD THE SEATS
      int table = ((DineInCustomer) customer).getTableNum();
      List<Integer> seats = ((DineInCustomer) customer).getSeats();

      String addSeat = "INSERT INTO seating(SeatingTable, SeatingSeatNumber, SeatingDineInOrder)" +
              "VALUES(?, ?, ?);";
      PreparedStatement addSeatP = conn.prepareStatement(addSeat);
      for(int i = 0; i < seats.size(); i++){
        addSeatP.setInt(1, table);
        addSeatP.setInt(2, seats.get(i));
        addSeatP.setInt(3, dineInID);
        addSeatP.executeUpdate();
      }
    }
    else {
      //PICKUP
      String name = ((DineOutCustomer) customer).getName();
      String addCustomer = "INSERT INTO dineoutorder(DineOutOrderFoodRequestID, DineoutOrderReturnCustomerID)" +
              "VALUES( " + foodRequestID + ", (SELECT SavedCustomerID FROM savedcustomer WHERE SavedCustomerName = ?));";
      PreparedStatement addCustomerP = conn.prepareStatement(addCustomer);
      addCustomerP.setString(1, name);
      addCustomerP.executeUpdate();
    }

    conn.close();
  }

  /**
   *
   * @param c the new customer to add to the database
   * @throws SQLException
   * @throws IOException
   * @requires c is not null. C's ID is -1 and will need to be assigned
   * @ensures c is given an ID and added to the database
   */
  public static void addCustomer(ICustomer c) throws SQLException, IOException {
    connect_to_db();
    /*
     * add code to add the customer to the DB. Note: the ID will be -1 and will need to be replaced
     * to be a fitting primary key Note that the customer is an ICustomer data type, which means c
     * could be a dine in, carryout or delivery customer
     */

    //CHECK WHAT TYPE OF CUSTOMER. DINE IN CUSTOMERS DO NOT GET "ACCOUNT" IN MY DATABASE

    try {
      if(c instanceof DeliveryCustomer) {
        DeliveryCustomer deliveryCast = (DeliveryCustomer) c;

        String addC = "INSERT INTO savedcustomer(SavedCustomerName, SavedCustomerPhoneNumber, SavedCustomerAddress)" +
                "VALUES(?, ? , ?)";
        PreparedStatement addCPrepare = conn.prepareStatement(addC);
        addCPrepare.setString(1, deliveryCast.getName());
        addCPrepare.setString(2, deliveryCast.getPhone());
        addCPrepare.setString(3, deliveryCast.getAddress());

        //ResultSet addDineOut = addCPrepare.executeQuery();
        addCPrepare.executeUpdate();
        System.out.println("DELIVERY CUSTOMER ADDED");
      }
      else if(c instanceof DineOutCustomer) {

        DineOutCustomer pickupCast = (DineOutCustomer) c;

        String addC = "INSERT INTO savedcustomer(SavedCustomerName, SavedCustomerPhoneNumber, SavedCustomerAddress)" +
                "VALUES (?, ?, NULL)";
        PreparedStatement addCPrepare = conn.prepareStatement(addC);
        addCPrepare.setString(1, pickupCast.getName());
        addCPrepare.setString(2,pickupCast.getPhone());
        addCPrepare.executeUpdate();

        System.out.println("PICK UP CUSTOMER ADDED");
      }

    } catch (SQLException e){
      System.out.println("CUSTOMER WAS NOT ADDED!!");
      System.out.println(e.getMessage());
      System.out.println(e.getErrorCode());
    }

    conn.close();
  }



  /**
   *
   * @param o the order to mark as complete in the database
   * @throws SQLException
   * @throws IOException
   * @requires the order exists in the database
   * @ensures the order will be marked as complete
   */
  public static void CompleteOrder(Order o) throws SQLException, IOException {
    connect_to_db();
    /*
     * add code to mark an order as complete in the DB. You may have a boolean field for this, or
     * maybe a completed time timestamp. However you have it,
     */


    conn.close();
  }

  /**
   *
   * @param t the topping whose inventory is being replenished
   * @param toAdd the amount of inventory of t to add
   * @throws SQLException
   * @throws IOException
   * @requires t exists in the database and toAdd > 0
   * @ensures t's inventory level is increased by toAdd
   */
  public static void AddToInventory(Topping t, double toAdd) throws SQLException, IOException {
    connect_to_db();
    /*
     * add code to add toAdd to the inventory level of T. This is not adding a new topping, it is
     * adding a certain amount of stock for a topping. This would be used to show that an order was
     * made to replenish the restaurants supply of pepperoni, etc
     */

    String updateInventory = "UPDATE topping SET ToppingInventoryLevel = ToppingInventoryLevel + ? WHERE ToppingName = ?";
    PreparedStatement updateInv = conn.prepareStatement(updateInventory);
    updateInv.setDouble(1, toAdd);
    updateInv.setString(2, t.getName());
    updateInv.executeUpdate();
    conn.close();
  }


  /*
   * A function to get the list of toppings and their inventory levels. I have left this code
   * "complete" as an example of how to use JDBC to get data from the database. This query will not
   * work on your database if you have different field or table names, so it will need to be changed
   *
   * Also note, this is just getting the topping ids and then calling getTopping() to get the actual
   * topping. You will need to complete this on your own
   *
   * You don't actually have to use and write the getTopping() function, but it can save some
   * repeated code if the program were to expand, and it keeps the functions simpler, more elegant
   * and easy to read. Breaking up the queries this way also keeps them simpler. I think it's a
   * better way to do it, and many people in the industry would agree, but its a suggestion, not a
   * requirement.
   */

  /**
   *
   * @return the List of all toppings in the database
   * @throws SQLException
   * @throws IOException
   * @ensures the returned list will include all toppings and accurate inventory levels
   */
  public static ArrayList<Topping> getInventory() throws SQLException, IOException {
    // start by connecting
    connect_to_db();
    ArrayList<Topping> ts = new ArrayList<Topping>();
    // create a string with out query, this one is an easy one
    String query = "Select ToppingName From topping;";

    //get topping name
    String toppingName;
    double toppingPrice;
    double toppingCost;
    double toppingInv;


    Statement stmt = conn.createStatement();
    try {
      ResultSet rset = stmt.executeQuery(query);
      // even if you only have one result, you still need to call ResultSet.next() to load the first
      // tuple
      while (rset.next()) {
        /*
         * Use getInt, getDouble, getString to get the actual value. You can use the column number
         * starting with 1, or use the column name as a string
         *
         * NOTE: You want to use rset.getInt() instead of Integer.parseInt(rset.getString()), not
         * just because it's shorter, but because of the possible NULL values. A NUll would cause
         * parseInt to fail
         *
         * If there is a possibility that it could return a NULL value you need to check to see if
         * it was NULL. In this query we won't get nulls, so I didn't. If I was going to I would do:
         *
         * int ID = rset.getInt(1); if(rset.wasNull()) { //set ID to what it should be for NULL, and
         * whatever you need to do. }
         *
         * NOTE: you can't check for NULL until after you have read the value using one of the
         * getters.
         *
         */

        //TOPPING_ID IN MY DATABASE IS TOPPINGNAME
        String toppingID = rset.getString(1);
        //toppingName = rset.getString("ToppingName");
        //toppingPrice = rset.getDouble("ToppingPrice");
        //toppingCost = rset.getDouble("ToppingCost");

        // Now I'm just passing my primary key to this function to get the topping itself
        // individually
        ts.add(getTopping(toppingID));
      }
    } catch (SQLException e) {
      System.out.println("Error loading inventory");
      while (e != null) {
        System.out.println("Message     : " + e.getMessage());
        e = e.getNextException();
      }

      // don't leave your connection open!
      conn.close();
      return ts;
    }


    // end by closing the connection
    conn.close();
    return ts;
  }

  /**
   *
   * @return a list of all orders that are currently open in the kitchen
   * @throws SQLException
   * @throws IOException
   * @ensures all currently open orders will be included in the returned list.
   */
  public static ArrayList<Order> getCurrentOrders() throws SQLException, IOException {
    connect_to_db();

    ArrayList<Order> os = new ArrayList<Order>();
    /*
     * add code to get a list of all open orders. Only return Orders that have not been completed.
     * If any pizzas are not completed, then the order is open.
     */

    String getOpenOrders = "SELECT * FROM foodrequest WHERE FoodRequestStatus = " + "making;";
    PreparedStatement getOpenOrdersP = conn.prepareStatement(getOpenOrders);
    ResultSet getOpenOrdersR = getOpenOrdersP.executeQuery();

    int orderID = -1;
    String type;
    ArrayList<Pizza> pizzas;
    ArrayList<Discount> discounts;
    Pizza p;
    Discount d;
    ICustomer customer;

    String getCustomer = "SELECT ";
    while(getOpenOrdersR.next()){
      orderID = getOpenOrdersR.getInt("FoodRequestNumber");


    }



    conn.close();
    return os;
  }

  /**
   *
   * @param size the pizza size
   * @param crust the type of crust
   * @return the base price for a pizza with that size and crust
   * @throws SQLException
   * @throws IOException
   * @requires size = size_s || size_m || size_l || size_xl AND crust = crust_thin || crust_orig ||
   *           crust_pan || crust_gf
   * @ensures the base price for a pizza with that size and crust is returned
   */
  public static double getBasePrice(String size, String crust) throws SQLException, IOException {
    connect_to_db();
    double bp = 0.0;
    // add code to get the base price for that size and crust pizza Depending on how you store size
    // and crust in your database, you may have to do a conversion

    String getBase = "SELECT BasePizzaPrice FROM basepizza WHERE BasePizzaSize = ? AND BasePizzaCrustType = ?";
    PreparedStatement getBaseP = conn.prepareStatement(getBase);
    getBaseP.setString(1, size);
    getBaseP.setString(2, crust);

    ResultSet getBaseR = getBaseP.executeQuery();
    double price = -1;

    if(getBaseR.next()){
      price = getBaseR.getDouble(1);
    }

    conn.close();
    return bp;
  }

  /**
   *
   * @return the list of all discounts in the database
   * @throws SQLException
   * @throws IOException
   * @ensures all discounts are included in the returned list
   */
  public static ArrayList<Discount> getDiscountList() throws SQLException, IOException {
    ArrayList<Discount> discs = new ArrayList<Discount>();
    connect_to_db();
    // add code to get a list of all discounts
    String getDiscounts = "SELECT * FROM discount";
    PreparedStatement getDiscountsP = conn.prepareStatement(getDiscounts);
    ResultSet getDiscountsR = getDiscountsP.executeQuery();

    Discount d;
    int dID;
    double percentageOff;
    double cashoff;
    String name;

    while(getDiscountsR.next()){
      dID = getDiscountsR.getInt("DiscountCode");
      percentageOff = getDiscountsR.getDouble("PercentageOff");
      cashoff = getDiscountsR.getDouble("AmountOff");
      name = getDiscountsR.getString("DiscountName");

      d = new Discount(name, percentageOff, cashoff, dID);
      discs.add(d);
    }


    conn.close();
    return discs;
  }

  /**
   *
   * @return the list of all delivery and carry out customers
   * @throws SQLException
   * @throws IOException
   * @ensures the list contains all carryout and delivery customers in the database
   */
  public static ArrayList<ICustomer> getCustomerList() throws SQLException, IOException {
    ArrayList<ICustomer> custs = new ArrayList<ICustomer>();
    connect_to_db();
    // add code to get a list of all customers
    String getCustomers = "SELECT * FROM savedcustomer";
    PreparedStatement getCustomersP = conn.prepareStatement(getCustomers);
    ResultSet getCustomersR = getCustomersP.executeQuery();

    //SET THE DATA RETURNED FROM DATABASE
    ICustomer temp;
    int customerID;
    String customerName;
    String customerPhone;
    String customerAddress;

    while (getCustomersR.next()){
      customerID = getCustomersR.getInt("SavedCustomerID");
      customerName = getCustomersR.getString("SavedCustomerName");
      customerPhone = getCustomersR.getString("SavedCustomerPhoneNumber");
      customerAddress = getCustomersR.getString("SavedCustomerAddress");

      //CHECK THAT IF BOTH PHONE AND ADDRESS ARE NOT EMPTY, THEN IT'S DELIVERY CUSTOMER
      if((customerAddress != null) && (customerPhone != null)){
        temp = new DeliveryCustomer(customerID, customerName, customerPhone, customerAddress);
      }
      else {
        //ITS A PICKUP CUSTOMER
        temp = new DineOutCustomer(customerID, customerName, customerPhone);
      }
      custs.add(temp);

    }



    conn.close();
    return custs;
  }



  /*
   * Note: The following incomplete functions are not strictly required, but could make your DBNinja
   * class much simpler. For instance, instead of writing one query to get all of the information
   * about an order, you can find the primary key of the order, and use that to find the primary
   * keys of the pizzas on that order, then use the pizza primary keys individually to build your
   * pizzas. We are no longer trying to get everything in one query, so feel free to break them up
   * as much as possible
   *
   * You could also add functions that take in a Pizza object and add that to the database, or take
   * in a pizza id and a topping id and add that topping to the pizza in the database, etc. I would
   * recommend this to keep your addOrder function much simpler
   *
   * These simpler functions should still not be called from our menu class. That is why they are
   * private
   *
   * We don't need to open and close the connection in these, since they are only called by a
   * function that has opened the connection and will close it after
   */


  private static Topping getTopping(String toppingName) throws SQLException, IOException {

    // add code to get a topping
    // the java compiler on unix does not like that t could be null, so I created a fake topping
    // that will be replaced
    Topping t = new Topping("fake", 0.25, 100.0);
    String query =
        "SELECT ToppingName, ToppingPrice, ToppingInventoryLevel FROM topping WHERE ToppingName = " + "\"" + toppingName + "\""
            + ";";

    Statement stmt = conn.createStatement();
    try {
      ResultSet rset = stmt.executeQuery(query);
      // even if you only have one result, you still need to call ResultSet.next() to load the first
      // tuple
      while (rset.next()) {
        String tname = rset.getString(1);
        double price = rset.getDouble(2);
        double inv = rset.getDouble(3);

        t = new Topping(tname, price, inv);
      }

    } catch (SQLException e) {
      System.out.println("Error loading Topping");
      while (e != null) {
        System.out.println("Message     : " + e.getMessage());
        e = e.getNextException();
      }

      // don't leave your connection open!
      conn.close();
      return t;
    }

    return t;

  }
  /*
   * private static Discount getDiscount() throws SQLException, IOException {
   * 
   * //add code to get a discount
   * 
   * Discount D;
   * 
   * return D;
   * 
   * }
   * 
   * private static Pizza getPizza() throws SQLException, IOException {
   * 
   * //add code to get Pizza Remember, a Pizza has toppings and discounts on it Pizza P;
   * 
   * return P;
   * 
   * }
   * 
   * private static ICustomer getCustomer() throws SQLException, IOException {
   * 
   * //add code to get customer
   * 
   * 
   * ICustomer C;
   * 
   * return C;
   * 
   * 
   * }
   * 
   * private static Order getOrder() throws SQLException, IOException {
   * 
   * //add code to get an order. Remember, an order has pizzas, a customer, and discounts on it
   * 
   * 
   * Order O;
   * 
   * return O;
   * 
   * }
   */

}
