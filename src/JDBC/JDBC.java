package JDBC;

import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 *
 * @author Mimi Opkins with some tweaking from Dave Brown
 */
public class JDBC {
    //  Database credentials
    static String USER;
    static String PASS;
    static String DBNAME;
    //This is the specification for the printout that I'm doing:
    //each % denotes the start of a new field.
    //The - denotes left justification.
    //The number indicates how wide to make the field.
    //The "s" denotes that it's a string.  All of our output in this test are 
    //strings, but that won't always be the case.
    //static final String displayFormat="%-5s%-15s%-15s%-15s\n";
    static final String wgFormat="%-21s%-21s%-12s%-21s\n";
    static final String pubFormat="%-21s%-41s%-13s%-21s\n";
    static final String bookFormat="%-21s%-21s%-21s%-21s%-4s\n";
// JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    static String DB_URL = "jdbc:derby://localhost:1527/";
//            + "testdb;user=";
/**
 * Takes the input string and outputs "N/A" if the string is empty or null.
 * @param input The string to be mapped.
 * @return  Either the input string or "N/A" as appropriate.
 */
    public static String dispNull (String input) {
        //because of short circuiting, if it's null, it never checks the length.
        if (input == null || input.length() == 0)
            return "N/A";
        else
            return input;
    }
    
    public static void main(String[] args) {
        //Prompt the user for the database name, and the credentials.
        //If your database has no credentials, you can update this code to 
        //remove that from the connection string.
        Scanner in = new Scanner(System.in);
        System.out.print("Name of the database (not the user account): ");
        DBNAME = in.nextLine();
//        System.out.print("Database user name: ");
//        USER = in.nextLine();
//        System.out.print("Database password: ");
//        PASS = in.nextLine();
        //Constructing the database URL connection string
        DB_URL = DB_URL + DBNAME;// + ";user="+ USER + ";password=" + PASS;
        Connection conn = null; //initialize the connection
        Statement stmt = null;  //initialize the statement that we're using
        PreparedStatement pstmt = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName("org.apache.derby.jdbc.ClientDriver");

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL);

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql;
            
            menu(conn, stmt, pstmt);

            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
    }//end main
    
    public static void menu(Connection conn, Statement stmt, PreparedStatement pstmt){
        int user_input = -1;
        while(user_input != 0){
            System.out.println("############################## Menu ##############################");
            System.out.println("# 1. List all writing groups                                     #");
            System.out.println("# 2. List all the data for a group specified by the user         #");
            System.out.println("# 3. List all publishers                                         #");
            System.out.println("# 4. List all the data for a publisher specified by the user     #");
            System.out.println("# 5. List all book titles                                        #");
            System.out.println("# 6. List all the data for a book specified by the user          #");
            System.out.println("# 7. Insert a new book                                           #");
            System.out.println("# 8. Insert a new publisher and update all book published by     #"
                           + "\n#      one publisher to be published by the new publisher        #");
            System.out.println("# 9. Remove a book specified by the user                         #");
            System.out.println("# 0. Exit                                                        #");
            System.out.println("##################################################################");
            System.out.print("Select the number from the menu: ");
            Scanner in = new Scanner(System.in);
            while(!in.hasNextInt()){
                System.out.println("Wrong input. Try again (0-9):");
                in.next();
            }
            user_input = in.nextInt();
            switch(user_input){
                case 1:
                    allWritingGroups(stmt);
                    break;
                case 2:
                    specifiedWritingGroups(conn, pstmt);
                    break;
                case 3:
                    allPublishers(stmt);
                    break;
                case 4:
                    specifiedPublisher(conn, pstmt);
                    break;
                case 5:
                    allBookTitles(stmt);
                    break;
                case 6:
                    specifiedBookTitle(conn, pstmt);
                    break;
                case 7:
                    insertBook(conn, pstmt);
                    break;
                case 8:
                    insertPublisher(conn, pstmt);
                    break;
                case 9:
                    removeBook(conn, pstmt);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Out of Range. Try again");
            }
        }
    }
    
    public static void allWritingGroups(Statement stmt){
        String sql = "SELECT GroupName, HeadWriter, YearFormed, Subject FROM WritingGroup ORDER BY GroupName";
        ResultSet rs;
        try {
            rs = stmt.executeQuery(sql);
        //STEP 5: Extract data from result set
            System.out.printf(wgFormat, "Name", "Head Writer", "Year Formed", "Subject");
            while (rs.next()) {
                //Retrieve by column name
                String groupName = rs.getString("GroupName");
                String headWriter = rs.getString("HeadWriter");
                String yearFormed = rs.getString("YearFormed");
                String subject = rs.getString("Subject");
                
                //Display values
                System.out.printf(wgFormat,
                        dispNull(groupName), dispNull(headWriter), dispNull(yearFormed), dispNull(subject));
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.print("Press Enter key to continue...");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        //STEP 6: Clean-up environment
    }
    
    public static void specifiedWritingGroups(Connection conn, PreparedStatement pstmt){
        String sql = "", attribute, value;
        ResultSet rs;
        boolean validity = false;
        Scanner scan = new Scanner(System.in);
//        System.out.print("Enter full clause to constraint data (ex: GroupName = Comedy Central, Subject = Science Fiction): ");
//        value = scan.nextLine();
        while(!validity){
            System.out.print("Enter the name of attribute (GroupName, HeadWriter, YearFormed, Subject): ");
            attribute = scan.nextLine();
            switch (attribute){
                case "GroupName":
                    sql = "SELECT GroupName, HeadWriter, YearFormed, Subject FROM WritingGroup WHERE GroupName = ?";
                    validity = true;
                    break;
                case "HeadWriter":
                    sql = "SELECT GroupName, HeadWriter, YearFormed, Subject FROM WritingGroup WHERE HeadWriter = ?";
                    validity = true;
                    break;
                case "YearFormed":
                    sql = "SELECT GroupName, HeadWriter, YearFormed, Subject FROM WritingGroup WHERE YearFormed = ?";                    
                    validity = true;
                    break;
                case "Subject":
                    sql = "SELECT GroupName, HeadWriter, YearFormed, Subject FROM WritingGroup WHERE Subject = ?";
                    validity = true;
                    break;
                default:
                    System.out.println("Invalid Input. Options are GroupName, HeadWriter, YearFormed, or Subject");
            }
        }
        System.out.print("Enter the value of attribute: ");
        value = scan.nextLine();
//        sql = "SELECT GroupName, HeadWriter, YearFormed, Subject FROM WritingGroup WHERE ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            rs = pstmt.executeQuery();
        //STEP 5: Extract data from result set
            System.out.printf(wgFormat, "Name", "Head Writer", "Year Formed", "Subject");
            while (rs.next()) {
                //Retrieve by column name
                String groupName = rs.getString("GroupName");
                String headWriter = rs.getString("HeadWriter");
                String yearFormed = rs.getString("YearFormed");
                String subject = rs.getString("Subject");
                
                //Display values
                System.out.printf(wgFormat,
                        dispNull(groupName), dispNull(headWriter), dispNull(yearFormed), dispNull(subject));
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.out.print("Press Enter key to continue...");
        scan.nextLine();
        //STEP 6: Clean-up environment
    }
    
    public static void allPublishers(Statement stmt){
        String sql = "SELECT PublisherName, PublisherAddress, PublisherPhone, PublisherEmail FROM Publisher ORDER BY PublisherName";
        ResultSet rs;
        try {
            rs = stmt.executeQuery(sql);
        //STEP 5: Extract data from result set
            System.out.printf(pubFormat, "Name", "Address", "Phone #", "E-mail");
            while (rs.next()) {
                //Retrieve by column name
                String name = rs.getString("PublisherName");
                String address = rs.getString("PublisherAddress");
                String phone = rs.getString("PublisherPhone");
                String email = rs.getString("PublisherEmail");
                
                //Display values
                System.out.printf(pubFormat,
                        dispNull(name), dispNull(address), dispNull(phone), dispNull(email));
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.print("Press Enter key to continue...");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        //STEP 6: Clean-up environment
    }
    
    public static void specifiedPublisher(Connection conn, PreparedStatement pstmt){
        String sql = "", attribute, value;
        ResultSet rs;
        boolean validity = false;
        Scanner scan = new Scanner(System.in);
        while(!validity){
            System.out.print("Enter the name of attribute (PublisherName, PublisherAddress, PublisherPhone, PublisherEmail): ");
            attribute = scan.nextLine();
            switch (attribute){
                case "PublisherName":
                    sql = "SELECT PublisherName, PublisherAddress, PublisherPhone, PublisherEmail FROM Publisher WHERE PublisherName = ?";
                    validity = true;
                    break;
                case "PublisherAddress":
                    sql = "SELECT PublisherName, PublisherAddress, PublisherPhone, PublisherEmail FROM Publisher WHERE PublisherAddress = ?";
                    validity = true;
                    break;
                case "PublisherPhone":
                    sql = "SELECT PublisherName, PublisherAddress, PublisherPhone, PublisherEmail FROM Publisher WHERE PublisherPhone = ?";
                    validity = true;
                    break;
                case "PublisherEmail":
                    sql = "SELECT PublisherName, PublisherAddress, PublisherPhone, PublisherEmail FROM Publisher WHERE PublisherEmail = ?";
                    validity = true;
                    break;
                default:
                    System.out.println("Invalid Input. Options are PublisherName, PublisherAddress, PublisherPhone, PublisherEmail");
            }
        }
        System.out.print("Enter the value of attribute: ");
        value = scan.nextLine();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            rs = pstmt.executeQuery();
        //STEP 5: Extract data from result set
            System.out.printf(pubFormat, "Name", "Address", "Phone #", "E-mail");
            while (rs.next()) {
                //Retrieve by column name
                String name = rs.getString("PublisherName");
                String address = rs.getString("PublisherAddress");
                String phone = rs.getString("PublisherPhone");
                String email = rs.getString("PublisherEmail");
                
                //Display values
                System.out.printf(pubFormat,
                        dispNull(name), dispNull(address), dispNull(phone), dispNull(email));
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.print("Press Enter key to continue...");
        scan.nextLine();
        //STEP 6: Clean-up environment
    }
    
    public static void allBookTitles(Statement stmt){
        String sql = "SELECT GroupName, BookTitle, PublisherName, YearPublished, NumberPages FROM Book NATURAL JOIN WritingGroup NATURAL JOIN Publisher ORDER BY BookTitle";
        ResultSet rs;
        try {
            rs = stmt.executeQuery(sql);
        //STEP 5: Extract data from result set
            System.out.printf(bookFormat, "Group Name", "Title", "Publisher Name", "Year Published", "Pages");
            while (rs.next()) {
                //Retrieve by column name
                String group = rs.getString("GroupName");
                String title = rs.getString("BookTitle");
                String publisher = rs.getString("PublisherName");
                String year = rs.getString("YearPublished");
                String pages = rs.getString("NumberPages");
                
                //Display values
                System.out.printf(bookFormat,
                        dispNull(group), dispNull(title), dispNull(publisher), dispNull(year), dispNull(pages));
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.print("Press Enter key to continue...");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        //STEP 6: Clean-up environment
    }
    
    public static void specifiedBookTitle(Connection conn, PreparedStatement pstmt){
        String sql = "", attribute, value;
        ResultSet rs;
        boolean validity = false;
        Scanner scan = new Scanner(System.in);
        while(!validity){
            System.out.print("Enter the name of attribute (GroupName, BookTitle, PublisherName, YearPublished, NumberPages): ");
            attribute = scan.nextLine();
            switch (attribute){
                case "GroupName":
                    sql = "SELECT GroupName, BookTitle, PublisherName, YearPublished, NumberPages FROM Book NATURAL JOIN WritingGroup NATURAL JOIN Publisher WHERE GroupName = ?";
                    validity = true;
                    break;
                case "BookTitle":
                    sql = "SELECT GroupName, BookTitle, PublisherName, YearPublished, NumberPages FROM Book NATURAL JOIN WritingGroup NATURAL JOIN Publisher WHERE BookTitle = ?";
                    validity = true;
                    break;
                case "PublisherName":
                    sql = "SELECT GroupName, BookTitle, PublisherName, YearPublished, NumberPages FROM Book NATURAL JOIN WritingGroup NATURAL JOIN Publisher WHERE PublisherName = ?";
                    validity = true;
                    break;
                case "YearPublished":
                    sql = "SELECT GroupName, BookTitle, PublisherName, YearPublished, NumberPages FROM Book NATURAL JOIN WritingGroup NATURAL JOIN Publisher WHERE YearPublished = ?";
                    validity = true;
                    break;
                case "NumberPages":
                    sql = "SELECT GroupName, BookTitle, PublisherName, YearPublished, NumberPages FROM Book NATURAL JOIN WritingGroup NATURAL JOIN Publisher WHERE NumberPages = ?";
                    validity = true;
                    break;
                default:
                    System.out.println("Invalid Input. Options are GroupName, BookTitle, PublisherName, YearPublished, NumberPages");
            }
        }
        System.out.print("Enter the value of attribute: ");
        value = scan.nextLine();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            rs = pstmt.executeQuery();
        //STEP 5: Extract data from result set
            System.out.printf(bookFormat, "Group Name", "Title", "Publisher Name", "Year Published", "Pages");
            while (rs.next()) {
                //Retrieve by column name
                String group = rs.getString("GroupName");
                String title = rs.getString("BookTitle");
                String publisher = rs.getString("PublisherName");
                String year = rs.getString("YearPublished");
                String pages = rs.getString("NumberPages");
                
                //Display values
                System.out.printf(bookFormat,
                        dispNull(group), dispNull(title), dispNull(publisher), dispNull(year), dispNull(pages));
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.print("Press Enter key to continue...");
        scan.nextLine();
    }
    
    public static void insertBook(Connection conn, PreparedStatement pstmt){
        Scanner scan = new Scanner(System.in);
        boolean valid = false;
        String sqlCheck = "", gName = "", newTitle = "", pubName = "", newYear = "";
        int newPages = 0;
        
        System.out.print("Enter the group name contributed Book: ");
        while(!valid)
        {
            gName = scan.nextLine();
            sqlCheck = "SELECT * FROM WritingGroup where GroupName = ?";
            ResultSet rs;
            try{
                pstmt = conn.prepareStatement(sqlCheck);
                pstmt.setString(1,gName);
                rs = pstmt.executeQuery();
                
                int i = 0;
                while (rs.next()) {
                    i++;
                }
                if( i == 0 )
                {
                    System.out.print(gName + " is not in the database. Try again: ");
                }else{
                    valid = true;
                }
                rs.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        }
        
        valid = false;
        System.out.print("Enter the title of the Book: ");
        while(!valid)
        {
            do{
                newTitle = scan.nextLine();
                if( newTitle.length() == 0 || newTitle.length() > 20 )
                {
                    System.out.print( "Invalid title. Try again: " );
                }
            }while( newTitle.length() == 0 || newTitle.length() > 20 );
            sqlCheck = "SELECT GroupName, BookTitle, PublisherName, YearPublished, NumberPages FROM Book"
                    + " where GroupName = ? AND BookTitle = ?";
            ResultSet rs;
            try{
                pstmt = conn.prepareStatement(sqlCheck);
                pstmt.setString(1,gName);
                pstmt.setString(2,newTitle);
                rs = pstmt.executeQuery();
                
                int i = 0;
                while (rs.next()) {
                    i++;
                }
                if( i != 0 )
                {
                    System.out.print(newTitle + " by " + gName + " is already in database. Try again: ");
                }else{
                    valid = true;
                }
                rs.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        }
        
        valid = false;
        System.out.print("Enter the publisher of the Book: ");
        while(!valid)
        {
            pubName = scan.nextLine();
            sqlCheck = "SELECT * FROM Publisher where PublisherName = ?";
            ResultSet rs;
            try{
                pstmt = conn.prepareStatement(sqlCheck);
                pstmt.setString(1,pubName);
                rs = pstmt.executeQuery();
                
                int i = 0;
                while (rs.next()) {
                    i++;
                }
                if( i == 0 )
                {
                    System.out.print(pubName + " is not in the database. Try again: ");
                }else{
                    valid = true;
                }
                rs.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        }
        
        System.out.print("Enter the year the book has published: ");
        do{
            newYear = scan.nextLine();
            if( newYear.length() > 4 )
            {
                System.out.print( "Invalid title. Try again: " );
            }            
        }while( newYear.length() > 4 );
        
        valid = false;
        System.out.print("Enter the number of pages the book has: ");
        
        while(!valid)
        {
            try{
                newPages = scan.nextInt();
                if(newPages < 0)
                {
                    System.out.print( "Invalid input. Enter again: " );
                }
                else
                {
                    valid = true;
                }
            }catch(InputMismatchException im){
                scan.next();
                System.out.print( "Invalid input. Enter again: " );
            }
        }
        
        String sql = "INSERT INTO Book (GroupName, BookTitle, PublisherName, YearPublished, NumberPages) "
                + "VALUES(?,?,?,?,?)";
        
        try {
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, gName);
            pstmt.setString(2, newTitle);
            pstmt.setString(3, pubName);
            pstmt.setString(4, newYear);
            pstmt.setInt(5, newPages);
            
            pstmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        
        System.out.println("The book " + newTitle + " by " + gName + " has been added");
        
        scan.nextLine();
        System.out.print("Press Enter key to continue...");
        scan.nextLine();
        //STEP 6: Clean-up environment
    }
    
    public static void insertPublisher(Connection conn, PreparedStatement pstmt) {
        Scanner scan = new Scanner(System.in);
        String sqlCheck, choice, pubName = "", pubAddr, pubPhone, pubEmail, pubChoice;
        boolean valid = false;
        System.out.print("Enter the publisher's name: ");
        while(!valid)
        {
            do{
                pubName = scan.nextLine();
                if(pubName.length() == 0 || pubName.length() > 20)
                {
                    System.out.print("Invalid publisher name. Try again: ");
                }
            }while(pubName.length() == 0 || pubName.length() > 20);
        
            sqlCheck = "SELECT * FROM Publisher where PublisherName = ?";
            ResultSet rs;
            try{
                pstmt = conn.prepareStatement(sqlCheck);
                pstmt.setString(1,pubName);
                rs = pstmt.executeQuery();
                
                int i = 0;
                while (rs.next()) {
                    i++;
                }
                if( i != 0 )
                {
                    System.out.print(pubName + " is already in database. Try again: ");
                }else{
                    valid = true;
                }
                rs.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        }
        
        System.out.print("Enter the publisher's address: ");
        do{
            pubAddr = scan.nextLine();
            if(pubAddr.length() == 0 || pubAddr.length() > 40)
            {
                System.out.print("Invalid publisher address. Try again: ");
            }
        }while(pubAddr.length() == 0 || pubAddr.length() > 40);
        
        System.out.print("Enter the publisher's phone number: ");
        do{
            pubPhone = scan.nextLine();
            if(pubPhone.length() == 0 || pubPhone.length() > 12)
            {
                System.out.print("Invalid publisher phone number. Try again: ");
            }
        }while(pubPhone.length() == 0 || pubPhone.length() > 12);
        
        System.out.print("Enter the publisher's email: ");
        do{
            pubEmail = scan.nextLine();
            if(pubEmail.length() == 0 || pubEmail.length() > 20)
            {
                System.out.print("Invalid publisher email. Try again: ");
            }
        }while(pubEmail.length() == 0 || pubEmail.length() > 20);
        
        String sql = "INSERT INTO publisher(PublisherName, PublisherAddress, PublisherPhone, PublisherEmail) "
                + "VALUES(?,?,?,?)";
        
        try{
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, pubName);
            pstmt.setString(2, pubAddr);
            pstmt.setString(3, pubPhone);
            pstmt.setString(4, pubEmail);
            
            pstmt.execute();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        System.out.println("The row " + pubName + ", " + pubAddr + ", " + pubPhone + ", " + pubEmail + " has been added");
        
        
        System.out.print("Do you want to update all books published by one publisher to be\n" +
                         "published by the new publisher(Y/N): ");
        choice = scan.nextLine();
        
        if(choice.equalsIgnoreCase("Y"))
        {
            System.out.print("Enter the publisher you wish to update: ");
            pubChoice = scan.nextLine();
            while(!valid)
            {
                do{
                    pubChoice = scan.nextLine();
                    if(pubChoice.length() == 0 || pubChoice.length() > 20)
                    {
                        System.out.print("Invalid publisher name. Try again: ");
                    }
                }while(pubChoice.length() == 0 || pubChoice.length() > 20);
        
                sqlCheck = "SELECT * FROM Publisher where PublisherName = ?";
                ResultSet rs;
                try{
                    pstmt = conn.prepareStatement(sqlCheck);
                    pstmt.setString(1,pubChoice);
                    rs = pstmt.executeQuery();
                
                    int i = 0;
                    while (rs.next()) {
                        i++;
                    }
                    if( i == 0 )
                    {
                        System.out.print(pubChoice + " is not in the database. Try again: ");
                    }else{
                        valid = true;
                    }
                    rs.close();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            String sqlUp = "UPDATE Book SET PublisherName = ? WHERE PublisherName = ?";
            
            try{
                pstmt = conn.prepareStatement(sqlUp);
                
                pstmt.setString(1,pubName);
                pstmt.setString(2,pubChoice);
                
                pstmt.execute();
            }catch(SQLException ex){
                ex.printStackTrace();
            }
            
            System.out.println("All books by " + pubChoice + " has been updated to be published by " + pubName);
        }
        
        System.out.print("Press enter to continue...");
        scan.nextLine();
        //STEP 6: Clean-up environment
    }
    
    public static void removeBook(Connection conn, PreparedStatement pstmt)
    {
        Scanner scan = new Scanner(System.in);
        String groupName = "", bookTitle = "", sqlCheck;
        boolean valid = false;
        
        System.out.print("Enter the writing group: ");
        while(!valid)
        {
            do{
                groupName = scan.nextLine();
                if(groupName.length() == 0 || groupName.length() > 20)
                {
                    System.out.print("Invalid group name. Try again: ");
                }
            }while(groupName.length() == 0 || groupName.length() > 20);
        
            sqlCheck = "SELECT * FROM Book where GroupName = ?";
            ResultSet rs;
            try{
                pstmt = conn.prepareStatement(sqlCheck);
                pstmt.setString(1,groupName);
                rs = pstmt.executeQuery();
                
                int i = 0;
                while (rs.next()) {
                    i++;
                }
                if( i == 0 )
                {
                    System.out.print(groupName + " is not in the database. Try again: ");
                }else{
                    valid = true;
                }
                rs.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        }
        
        valid = false;
        System.out.print("Enter the title: ");
        while(!valid)
        {
            do{
                bookTitle = scan.nextLine();
                if(bookTitle.length() == 0 || bookTitle.length() > 20)
                {
                    System.out.print("Invalid title. Try again: ");
                }
            }while(bookTitle.length() == 0 || bookTitle.length() > 20);
        
            sqlCheck = "SELECT * FROM Book where GroupName = ? AND BookTitle = ?";
            ResultSet rs;
            try{
                pstmt = conn.prepareStatement(sqlCheck);
                pstmt.setString(1,groupName);
                pstmt.setString(2,bookTitle);
                rs = pstmt.executeQuery();
                
                int i = 0;
                while (rs.next()) {
                    i++;
                }
                if( i == 0 )
                {
                    System.out.print(bookTitle + " by " + groupName + " is not in the database. Try again: ");
                }else{
                    valid = true;
                }
                rs.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        }
        
        String sql = "delete from Book where GroupName = ? AND BookTitle = ?";
        try{
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, groupName);
            pstmt.setString(2, bookTitle);
            
            pstmt.execute();
        }catch(SQLException ex){
            System.out.println(ex.getMessage());
        }
        System.out.println("The book " + bookTitle + " by " + groupName + " has been removed");
        System.out.print("Press enter to continue...");
        
        scan.nextLine();
        //STEP 6: Clean-up environment
    }
}//end FirstExample}
