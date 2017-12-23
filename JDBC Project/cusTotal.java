import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/*
 * In This Implementation The custTotal creates a connection to the Database that with the given URL. The User can enter The customer
 * ID as a command line argument if the user forgets that then the program will ask for the ID.    
 * After Entering a correct Id number The program asks the customer to update the Name and city and if customer says yes then the
 * program asks for new name and new city. 
 * Then The program prints out every book category in the database and asks the customer to input category
 * then the program outputs every title of a book with the given category
 * IF the book exists the program finds the cheapest price of that book that is offered by one of the clubs that the customer is 
 * a member of. Then it asks to finish the purchase.
 * 
 * 
 * This program saves the values of the database in an arraylist of hashmaps (hasahmap for each tuple in the output)
 * This program auto commits, meaning any change done to the database is commited  and it cannot be rolled back.
 * This program assumes that the user can read English and follow instructions
 * The user can exit at any time by entring -1 as input however the program will not roll back 
 * meaning the changes that were done before exiting will stay
 * 
 * @author KOKO NANAH JI 
*/
public class cusTotal {
	private Connection conDB;  	// Connection to the database system.
	private String url; 	   	// URL: Which database?
	private Integer custID; 	// Who are we tallying?
	private boolean inDB; 		// exists a customer with the given ID Number
	private String custClub;	// club of the customer that offers the cheapest price for the desired book

	
	/**
	* returns integer value of the given string or 0 if string is not a number
	* @param s string
	*/
	private int inputIsInteger(String s){
		int answer=0;
		try{
			answer = Integer.parseInt(s);
		}catch(NumberFormatException e){
		}
		
		return answer;
	}
	
	
	
	/**
	* constructor for class cusTotal that should be initialized for the program to work
	* @param args which is the command line argument entered
	*/
	public cusTotal(String[] args) {
	
		inDB = false;				// Default false
		boolean keepGoing = true;  	// keep going until the customer enters -1
	
		// Set up the DB connection.	
		try {
			// Register the driver with DriverManager.
			Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(0);
		}
		// URL: Which database?
		url = "jdbc:db2:c3421a";
		// Initialize the connection.
		try {
			// Connect with a fall-thru id & password
			conDB = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.print("\nSQL: database connection error.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		// set autocommit to true 
		try {
			 conDB.setAutoCommit(true);
	    } catch(SQLException e) {
			 System.out.print("\nFailed trying to turn autocommit off.\n");
			 e.printStackTrace();
			 System.exit(0);
		}    

		Scanner sc = new Scanner(System.in);			 
		// Who are we tallying?
		custID = 0;	
		if (args.length == 1) {
			try {
				custID = inputIsInteger(args[0]); // get in value
			} catch (NumberFormatException e) {
				System.out.println("\nUsage: java CustTotal cust#");
				System.out.println("Provide an INT for the cust#.");
				System.exit(0);
			}
		} 

			
		
		String temp = null;		
		// forgot Id on command line ask for Id again
		while(custID<=0 && keepGoing){
				 temp = System.console().readLine("Enter Id Number of customer (should be greater than zero) or -1 to exit\n");
				 if(temp.equals("-1")){
					 keepGoing=false;
				 }
				custID = inputIsInteger(temp);// get in value
				
		}

		
		


		HashMap<String, String> answers = find_customer(custID);
		// after getting the Id either from command line or by promting 
		// we check is this custID in Database
		while (!inDB && keepGoing) {
			System.out.println(" customer does not exist");
			String temp1 = System.console().readLine("Enter Id Number of customer (should be greater than zero)\n");
			int t = inputIsInteger(temp1);				// get in value
			if (t > 0) {
				custID = t;
				answers = find_customer(custID);
			}
				// customer wants to Quit 
			else if (t == -1) { 
				keepGoing = false;
			}
		}
		
		
		

			// UPDATE TIME //
			
		// customer does not want to quit
		if (keepGoing) {
			System.out.println(custID + " " + answers.get("custName") + "  " + answers.get("custCity"));
			System.out.println("");
			
			boolean loop=true;
			while(loop && keepGoing){
			String temp1 = System.console().readLine("Would You Like to Update Information (1 for yes 0 for no -1 to exit)\n");
			
			int upd = inputIsInteger(temp1);	// get in value
			// customer said yes to update
			if (upd == 1) {
				keepGoing = update_customer(custID);
				loop=false;
			} 
			else if (upd == -1) {
				keepGoing = false;
				loop=false;
			}
			else if(temp1.equals("0")){
				loop=false;				
			}
			}
		}
		
		

			// Fetching Categories
		if (keepGoing) {
			ArrayList<String> rs = fetch_catagories();
			// Walk through the results and present them.
			for (int i = 0; i < rs.size(); i++) {
				System.out.println(rs.get(i));
			}
			System.out.println("");
		}
		
		
		
		
		
		// Get the desired book name and category from customer
		
		ArrayList<HashMap<String, String>> books = null;
		ArrayList<HashMap<String, String>> booksByCat = null;
		
	
		String category = null;		
		String title=null;
		while ((booksByCat == null || booksByCat.size() == 0) && keepGoing) {
			category = System.console().readLine("Enter the Category of the book or -1 to exit\n");		
			title = "";
			
			if (!category.equals("-1")) { // customer wants to contunue
				
				booksByCat = find_book_by_cat(category);
				System.out.println("");
				for (int i = 0; i < booksByCat.size(); i++) {
					System.out.println("INDEX:"+(i+1) + "  TITLE:" + booksByCat.get(i).get("title"));

				}
			}else if (category.equals("-1")) {// customer wants to quit
					keepGoing = false;
				}
		}
		
		
		// keepgoing asserts that customer did not enter -1 yet that means 
		// customer wants to keep going		
		if(keepGoing){
				
				String temp1= System.console().readLine("Enter the Index of the book Title or -1 to exit\n");
				int indexOfTemp = inputIsInteger(temp1);// get in value
				while(keepGoing &&(indexOfTemp<=0  || indexOfTemp>booksByCat.size())){
					temp1= System.console().readLine("Enter the Index of the book Title or -1 to exit\n");
					indexOfTemp = inputIsInteger(temp1);
					if (indexOfTemp==-1) { // customer wants to quit
					keepGoing = false;
				}
				}
				if(keepGoing){
					title=booksByCat.get(indexOfTemp-1).get("title");
				}
			
			
			}
			
		
			if (keepGoing) {// customer wants to contunue
				// find the books with given  category
				books = find_book(title, category);
				
				// print out the books with given category
				// print index number right before every book 
				// to make the choosing easy
				if (books != null && books.size() != 0) {
					System.out.println();
					System.out.println(books.size()+ " Books found:");
					for (int i = 0; i < books.size(); i++) {
						System.out.println("INDEX:"+(i+1) + "  TITLE:" + title + "  YEAR:" + books.get(i).get("year") + "   LANGUAGE:"
								+ books.get(i).get("language") + "   WEIGHT:" + books.get(i).get("weight"));

					}
				} else if (keepGoing) {
					System.out.println("Can't find any books try again ");
				}
			}
		

		
		
		// find the book with minimum price
		double minPrice = 0;
		int index = 0;
		if (keepGoing) { // Customer enters the index (the index number of the books that 
						 //	were printed out right before this) of the desired book
			String temp1 = System.console().readLine("Enter the INDEX of the desired book or -1 to exit\n");
			
			index = inputIsInteger(temp1);// get in value
			
			// Wrong Index 
			while ((index <= 0 || index>books.size()) && keepGoing) {
				if (index != -1) {
					temp1 = System.console().readLine("Wrong INDEX\nEnter the Number of the desired book or -1 to exit\n");
					index = inputIsInteger(temp1);
				} else {
					keepGoing = false;
				}
			}

			if (keepGoing) { // get the minimum price for the desired book
				index=index-1;
				minPrice = min_price(books.get(index).get("category"), books.get(index).get("title"),
						Integer.parseInt(books.get(index).get("year")), custID);

				System.out.println("The Minumum price for the book is " + minPrice);
			}

		}

		
		
		// Asking for quantity should be greater than 0
		int quantity = 0;
		if (keepGoing) {
			String temp1 = System.console().readLine("Enter the quantity to buy or -1 to exit\n");
			quantity = inputIsInteger(temp1);// get in value
			while (quantity <= 0 && keepGoing) {
				if (quantity != -1) {
					temp1 = System.console().readLine("Should be higher Than Zero\nEnter the quantity to buy  or -1 to exit\n");
					quantity = inputIsInteger(temp1);
				} else {
					keepGoing = false;
				}

			}
		}

		
		// Output the total price
		if (keepGoing) {
			System.out.println("The Total Price is " + (quantity * minPrice));

		}

		
		
		// Finish the purchase 
		if (keepGoing) {
			String temp1 = System.console().readLine("Do you want to Finish the purchase ? (1 for yes anything else for no)\n");

			int toPurchase = inputIsInteger(temp1); // get in value
			if (toPurchase == 1) {
				insert_purchase(custID, custClub, books.get(index).get("title"),
						Integer.parseInt(books.get(index).get("year")), quantity);
				System.out.println("Congratulations");
			}

		}

		sc.close();

		// Close the connection.
		try {
			conDB.close();
		} catch (SQLException e) {
			System.out.print("\nFailed trying to close the connection.\n");
			e.printStackTrace();
			System.exit(0);
		}

	}



	/**returns a Hashmap that has the Name and city of the customer with the given id
     * It gets the information by executing sql query ( from database)
     * if a customer does not exists with the given id it sets the global variable
	 * inDB false indicating that the customer is not in Database
     * @param idnumber of customer
     * @return Hashmap Name and city`
     */	
	public HashMap<String, String> find_customer(int idNumber) {
		String queryText = "";				 						// The SQL text.
		PreparedStatement querySt = null; 	 						// The query handle.
		ResultSet answers = null; 			 						// A cursor.
		HashMap<String, String> hm = new HashMap<String, String>(); // The answers to return

		queryText = "SELECT name,city        " + "FROM yrb_customer " + "WHERE cid = ?     ";
		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL# find_customer failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.setInt(1, idNumber);
			answers = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL# find_customer failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any answer?
		try {
			if (answers.next()) {
				inDB = true;
				hm.put("custName", answers.getString("name"));
				hm.put("custCity", answers.getString("city"));
			} else {
				inDB = false;
			}
		} catch (SQLException e) {
			System.out.println("SQL# find_customer failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL# find_customer failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL# find_customer failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return hm;

	}

	
	
	/**Ask the customer with the id to updated the name or city by entering the new values
     * it returns a boolean keepGoing that is false if the customer wants to quit and true otherwise
	 * It updates the information by executing sql query ( from database)
     * @param id number
     * @return boolean keepGoing
     */	
	public boolean update_customer(int idNumber) {
		Scanner in = new Scanner(System.in);  // Scanner for the inputs
		String queryText = ""; 			  	  // The SQL text.
		PreparedStatement querySt = null;	  // The query handle.
		boolean keepGoing = true; 		  	  // Check if customer enters -1
		int answer1;						  // temp variable
		int answer2;						  // temp variable

		String temp1 = System.console().readLine("Would You Like to Update The Name (1 for yes -1 to exit Anything else will be accepted as No)\n ");


		int custAnswer1 =  inputIsInteger(temp1); // get int value 
		if (custAnswer1 == 1) { // customer wants to update the name
			String newName = System.console().readLine("Enter new Name or -1 to exit\n");
			
			// Name can't be longer than 20 chars according to Database
			while(newName.length()>20 && keepGoing){
				newName = System.console().readLine("Name can't be Longer than 20 Characters\nEnter new Name or -1 to exit\n");
				if (newName.equals("-1")) {
					keepGoing = false;
				}
				
			}
			
			// Update 
			if (!newName.equals("-1")) {
				queryText = "UPDATE yrb_customer  " + " Set name = '" + newName + "' WHERE cid = " + idNumber;
				
				// Prepare the query.
				try {
					querySt = conDB.prepareStatement(queryText);
				} catch (SQLException e) {
					System.out.println("SQL# update1 failed in prepare");
					System.out.println(e.toString());
					System.exit(0);
				}

				// Execute the query.
				try {
					answer1 = querySt.executeUpdate();
				} catch (SQLException e) {
					System.out.println("SQL# update1 failed in execute");
					System.out.println(e.toString());
					System.exit(0);
				}

				// We're done with the handle.
				try {
					querySt.close();
				} catch (SQLException e) {
					System.out.print("SQL# update1 failed closing the handle.\n");
					System.out.println(e.toString());
					System.exit(0);
				}

			} else if (newName.equals("-1")) {
				keepGoing = false;
			}
		} else if (custAnswer1 == -1) {
			keepGoing = false;
		}

		
		if (keepGoing) { // customer wants to continue
			String temp2 = System.console().readLine("Would You Like to Update The City (1 for yes or -1 to exit Anything else will be accepted as No)\n");
			int custAnswer2 =  inputIsInteger(temp2); // get in value
			
			// customer wants to update the city
			if (custAnswer2 == 1) { 
				String newCity = System.console().readLine("Enter new City or -1 to exit\n");
				
				// Cite Name can't be longer that 15 chars according to check constraint in the Database
				while(newCity.length()>15 && keepGoing){
					newCity = System.console().readLine("City Name can't be Longer than 15 Characters\nEnter new City or -1 to exit\n");
					if (newCity.equals("-1")) {
						keepGoing = false;
					}				
				}
			
				//Update City 
				if (!newCity.equals("-1")) {				
					queryText = "UPDATE yrb_customer  " + " Set City = '" + newCity + "'  WHERE cid = " + idNumber;
					// Prepare the query.
					try {
						querySt = conDB.prepareStatement(queryText);
					} catch (SQLException e) {
						System.out.println("SQL# update2 failed in prepare");
						System.out.println(e.toString());
						System.exit(0);
					}

					// Execute the query.
					try {
						answer1 = querySt.executeUpdate();
					} catch (SQLException e) {
						System.out.println("SQL# update2 failed in execute");
						System.out.println(e.toString());
						System.exit(0);
					}

					// We're done with the handle.
					try {
						querySt.close();
					} catch (SQLException e) {
						System.out.print("SQL# update2 failed closing the handle.\n");
						System.out.println(e.toString());
						System.exit(0);
					}
				} else if (newCity.equals("-1")) {
					keepGoing = false;
				}

			}

			else if (custAnswer2 == -1) {
				keepGoing = false;
			}

		}
		
		
		return keepGoing;
	}

	
	
	/**
     * Returns an arraylist of book categories 
     * 
     * 
     * @return arraylist of book categories 
     */	
	public ArrayList<String> fetch_catagories() {
		String queryText = ""; 					  // The SQL text.
		PreparedStatement querySt = null; 		  // The query handle.
		ResultSet answers = null; 				  // A cursor.
		ArrayList<String> al = new ArrayList<>(); // answer
		
		
		queryText = "SELECT cat  " + "FROM yrb_category ";
		
		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL# fetch failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			answers = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL# fetch failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		// Any answer?
		try {
			while (answers.next()) {
				al.add(answers.getString("cat"));
			}
		} catch (SQLException e) {
			System.out.println("SQL# fetch failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL# fetch failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL# fetch failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		return al;

	}

	
	
	
	/**
     * Returns an arraylist of hashmaps that have the information about the books that 
     * have the title and belong to the category passed in the parameter
     * @param title and the category of the book
     * @return books that have the given category and title as an arraylist
     */		
	public ArrayList<HashMap<String, String>> find_book(String title, String category) {
		String queryText = ""; 										// The SQL text.
		PreparedStatement querySt = null; 							// The query handle.
		ResultSet answers = null; 									// A cursor.
		ArrayList<HashMap<String, String>> al = new ArrayList<>();	// answers
		
		queryText = "SELECT  title, year, language, weight  " + " FROM yrb_book b " + " where b.cat = '" + category
				+ "' and  b.title = '" + title + "'";
		
		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL# find_book failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			answers = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL# find_book failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		// Any answer?
		try {
			while (answers.next()) {
				HashMap<String, String> hm = new HashMap<>();
				hm.put("title", answers.getString("title"));
				hm.put("year", answers.getString("year"));
				hm.put("language", answers.getString("language"));
				hm.put("weight", answers.getString("weight"));
				al.add(hm);
			}
		} catch (SQLException e) {
			System.out.println("SQL# find_book failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL# find_book failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL# find_book failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return al;
	}

	/**
     * Returns an arraylist of hashmaps that have the information about the books that 
     * belong to the category passed in the parameter
     * @param the category of the book
     * @return books that have the given category as an arraylist
     */	
	public ArrayList<HashMap<String, String>> find_book_by_cat(String category) {
		String queryText = ""; 										// The SQL text.
		PreparedStatement querySt = null; 							// The query handle.
		ResultSet answers = null; 									// A cursor.
		ArrayList<HashMap<String, String>> al = new ArrayList<>();	// answers
		
		queryText = "SELECT  title" + " FROM yrb_book b " + " where b.cat = '" + category
				+ " '" ;
		
		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL# find_book failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			answers = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL# find_book failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		// Any answer?
		try {
			while (answers.next()) {
				HashMap<String, String> hm = new HashMap<>();
				hm.put("title", answers.getString("title"));
				al.add(hm);
			}
		} catch (SQLException e) {
			System.out.println("SQL# find_book failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL# find_book failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL# find_book failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return al;
	}
	
	
	
	/**
     * Returns min price of the books that have the given title category and year
     * gets the prices from the clubs that customer with the given id belongs to 
	 * and clubs do offer the desired book
	 *	 
	 *		 
     * @param title , category  and year of the book and the id of customer
     * @return min price of the books that have the given requirments 
     */	
	public double min_price(String category, String title, int year, int id) {
		String queryText = ""; 								// The SQL text.
		PreparedStatement querySt = null; 					// The query handle.
		ResultSet answers = null; 						    // A cursor.
		String minPrice = null;								// min price 
		
		
		queryText = "SELECT  min(price) as price, m.club  " + " FROM yrb_member m, yrb_offer o " + " where o.title = '"
				+ title + "' and o.year = " + year + " and m.cid = " + id + " and m.club = o.club"
				+ " group by m.club order by price";
		
		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL# min_price failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			answers = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL# min_price failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		// Any answer?
		try {
			if (answers.next()) {
				minPrice = answers.getString("price");
				custClub = answers.getString("club");
			}
		} catch (SQLException e) {
			System.out.println("SQL# min_price failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL# min_price failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL# min_price failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// the minimum price
		return Double.parseDouble(minPrice);
	}

	
	
	
	
	
	
	/**
     * Inserts purchase information into the database
     * 
     * @param cid of customer, club  that offers the book, title year and  quantity of the book
     */	
	public void insert_purchase(int cid, String club, String title, int year, int quantity) {
		String queryText = ""; 												  // The SQL text.
		PreparedStatement querySt = null; 									  // The query handle.
		SimpleDateFormat cDate = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss"); // date format like DB2
		Date now = new Date();												  // current date and time
		String when = cDate.format(now);									  // format the date
		
		
		queryText = "insert into yrb_purchase " + " ( cid , club, title, year, when, qnty )" + " Values ( " + cid
				+ " ,  '" + custClub + "' , '" + title + "' , " + year + " , '" + when + "' , " + quantity + " )";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL# insert_purchase failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQL# insert_purchase failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		//

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL# insert_purchase failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

	}

	
	
	
	// Main method that creates and runs the program
	public static void main(String args[]) {
	// Create object for the JDBC class called cusTotal  and pass the arguments
	cusTotal ct = new cusTotal(args);
	}

}

