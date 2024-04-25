package test.ImplTesting;

import com.shashi.beans.UserBean;
import com.shashi.constants.IUserConstants;
import com.shashi.service.impl.UserServiceImpl;
import com.shashi.utility.DBUtil;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceImplTest {
  public void registerUser(String userName, Long mobileNo, String emailId, String address, int pinCode,
                           String password) throws Exception {

    Connection conn = DBUtil.provideConnection();
    PreparedStatement ps = conn.prepareStatement("insert into " + IUserConstants.TABLE_USER + " values(?,?,?,?,?,?)");

    ps.setString(1, emailId);
    ps.setString(2, userName);
    ps.setLong(3, mobileNo);
    ps.setString(4, address);
    ps.setInt(5, pinCode);
    ps.setString(6, password);

    int k = ps.executeUpdate();

    if (k == 0) {
      throw new Exception("did not insert successfully");
    }

    DBUtil.closeConnection(conn);
    DBUtil.closeConnection(ps);
  }

  public boolean getUserExists(String emailId) throws Exception {
    boolean user = false;

    Connection con = DBUtil.provideConnection();

    PreparedStatement ps = null;
    ResultSet rs = null;

    ps = con.prepareStatement("select * from user where email=?");

    ps.setString(1, emailId);
    rs = ps.executeQuery();

    if (rs.next())
      user = true;

    DBUtil.closeConnection(con);
    DBUtil.closeConnection(ps);
    DBUtil.closeConnection(rs);

    return user;
  }

  @AfterEach
  public void clearUser() {
    try {
      Connection con = DBUtil.provideConnection();

      PreparedStatement ps1 = con.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
      ps1.execute();

      PreparedStatement ps = con.prepareStatement("DELETE FROM user");

      ps.execute();

      PreparedStatement ps2 = con.prepareStatement("SET FOREIGN_KEY_CHECKS=1");
      ps2.execute();

      DBUtil.closeConnection(con);
      DBUtil.closeConnection(ps);
      DBUtil.closeConnection(ps1);
      DBUtil.closeConnection(ps2);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      System.out.println("FAILED TO CLEAN UP. CHECK SQL SERVER, DISABLE SAFE UPDATE MODE");
    }
  }


  static ResourceBundle rb = ResourceBundle.getBundle("application");
  static String DATABASE_URL = rb.getString("db.connectionString");
  static String USER = rb.getString("db.username");
  static String PASSWORD = rb.getString("db.password");
  @AfterAll
  public static void resetDatabase() {
    try {
      String sql = new String(Files.readAllBytes(Paths.get("databases/mysql_query.sql")));
      try (Connection conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD)) {

        // Create a statement to execute SQL
        Statement stmt = conn.createStatement();

        // Splitting the SQL statements by semicolon
        String[] sqlStatements = sql.split(";");

        // Execute each SQL statement
        for (String statement : sqlStatements) {
          // Trim whitespace and execute non-empty statements
          if (!statement.trim().isEmpty()) {
            stmt.execute(statement.trim());
          }
        }

        stmt.close();
        conn.close();
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      System.out.println("FAILED TO CLEAN UP. CHECK SQL SERVER, DISABLE SAFE UPDATE MODE");
    }
  }


  @BeforeEach
  public void clearBefore() {
    clearUser();
  }

  // register a new user
  @Test
  public void testRegisterUserWithNewUser() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    String status = userService.registerUser(userName, mobileNo, emailId, address, pinCode, password);
    assertEquals("User Registered Successfully!", status);

    // cleanup and verification
    try {
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed to verify");
    }
  }

  // register an existing user
  @Test
  public void testRegisterUserWithExistingUser() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    String status = userService.registerUser(userName, mobileNo, emailId, address, pinCode, password);
    assertEquals("Email Id Already Registered!", status);
  }

  // test register a new user with null inputs (null not accepted for pinCode)
  @Test
  public void testRegisterUserWithNullInputs() {
    String userName = null;
    Long mobileNo = null;
    String emailId = null;
    String address = null;
    int pinCode = 0;
    String password = null;

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = userService.registerUser(userName, mobileNo, emailId, address, pinCode, password);
    });
  }


  // test register a new user with empty email address
  @Test
  public void testRegisterUserWithEmptyEmailAddress() {
    String userName = "";
    Long mobileNo = 0L;
    String emailId = "";
    String address = "";
    int pinCode = 0;
    String password = "";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = userService.registerUser(userName, mobileNo, emailId, address, pinCode, password);
    });
  }

  // test register a new user with empty inputs other than email (empty inputs not accepted for mobileNo and pinCode)
  @Test
  public void testRegisterUserWithEmptyInputs() {
    String userName = "";
    Long mobileNo = 0L;
    String emailId = "test@gmail.com";
    String address = "";
    int pinCode = 0;
    String password = "";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = userService.registerUser(userName, mobileNo, emailId, address, pinCode, password);
    });
  }

  // test register a new user with negative values for long and int inputs
  @Test
  public void testRegisterUserWithNegativeInputs() {
    String userName = "test";
    Long mobileNo = -9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = -123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = userService.registerUser(userName, mobileNo, emailId, address, pinCode, password);
    });
  }

  // test register a new user with zero values for long and int inputs
  @Test
  public void testRegisterUserWithZeroAsInputs() {
    String userName = "test";
    Long mobileNo = 0L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 0;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = userService.registerUser(userName, mobileNo, emailId, address, pinCode, password);
    });
  }

  // test register a new user with an invalid phone number format
  @Test
  public void testRegisterUserWithInvalidPhoneNumber() {
    String userName = "test";
    Long mobileNo = 99999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = userService.registerUser(userName, mobileNo, emailId, address, pinCode, password);
    });
  }

  // test isRegistered with an existing user
  @Test
  public void testIsRegisteredWithExistingUser() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    boolean res = userService.isRegistered(emailId);
    assertTrue(res);
  }

  // test isRegistered with a nonexistent user
  @Test
  public void testIsRegisteredWithNonexistentUser() {
    String emailId = "test@gmail.com";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    boolean res = userService.isRegistered(emailId);
    assertFalse(res);
  }


  // test isRegistered with an empty emailId
  @Test
  public void testIsRegisteredWithEmptyEmailId() {
    String emailId = "";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      boolean res = userService.isRegistered(emailId);
    });
  }

  // test isRegistered with a null emailId
  @Test
  public void testIsRegisteredWithNullEmailId() {
    String emailId = null;

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      boolean res = userService.isRegistered(emailId);
    });
  }

  // test isRegistered with an invalid emailId
  @Test
  public void testIsRegisteredWithInvalidEmailId() {
    String emailId = "test";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      boolean res = userService.isRegistered(emailId);
    });
  }

  // test isValidCredential with an existing user
  @Test
  public void testIsValidCredentialWithExistingUser() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    String status = userService.isValidCredential(emailId, password);
    assertEquals("valid", status);
  }

  // test isValidCredential with a user that does not exist
  @Test
  public void testIsValidCredentialWithNonexistentUser() {
    String emailId = "test@gmail.com";
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    String status = userService.isValidCredential(emailId, password);
    assertEquals("Login Denied! Incorrect Username or Password", status);
  }

  // test isValidCredential with an existing user with incorrect password
  @Test
  public void testIsValidCredentialWithIncorrectPassword() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    String passwordIncorrect = "test1";

    UserServiceImpl userService = new UserServiceImpl();
    String status = userService.isValidCredential(emailId, passwordIncorrect);
    assertEquals("Login Denied! Incorrect Username or Password", status);
  }

  // test isValidCredential with an existing user with incorrect email
  @Test
  public void testIsValidCredentialWithIncorrectEmail() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    String emailIncorrect = "test1";

    UserServiceImpl userService = new UserServiceImpl();
    String status = userService.isValidCredential(emailIncorrect, password);
    assertEquals("Login Denied! Incorrect Username or Password", status);
  }

  // test isValidCredential with an existing user with invalid email format
  @Test
  public void testIsValidCredentialWithInvalidEmail() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    String emailIncorrect = "test";

    UserServiceImpl userService = new UserServiceImpl();
    String status = userService.isValidCredential(emailIncorrect, password);
    assertEquals("Login Denied! Incorrect Username or Password", status);
  }

  // test isValidCredential with empty inputs
  @Test
  public void testIsValidCredentialWithEmptyInputs() {
    String emailId = "";
    String password = "";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = userService.isValidCredential(emailId, password);
    });
  }

  // test isValidCredential with null inputs
  @Test
  public void testIsValidCredentialWithNullInputs() {
    String emailId = null;
    String password = null;

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = userService.isValidCredential(emailId, password);
    });
  }










  // test getUserDetails with an existing user
  @Test
  public void testGetUserDetailsWithExistingUser() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserBean expectedUser = new UserBean();
    expectedUser.setName(userName);
    expectedUser.setMobile(mobileNo);
    expectedUser.setEmail(emailId);
    expectedUser.setAddress(address);
    expectedUser.setPinCode(pinCode);
    expectedUser.setPassword(password);

    UserServiceImpl userService = new UserServiceImpl();
    UserBean user = userService.getUserDetails(emailId, password);
    assertEquals(expectedUser.getName(), user.getName());
    assertEquals(expectedUser.getMobile(), user.getMobile());
    assertEquals(expectedUser.getEmail(), user.getEmail());
    assertEquals(expectedUser.getAddress(), user.getAddress());
    assertEquals(expectedUser.getPinCode(), user.getPinCode());
    assertEquals(expectedUser.getPassword(), user.getPassword());
  }


  // test getUserDetails with a user that does not exist
  @Test
  public void testGetUserDetailsWithNonexistentUser() {
    String emailId = "test@gmail.com";
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      UserBean user = userService.getUserDetails(emailId, password);
    });
  }

  // test getUserDetails with an existing user with incorrect password
  @Test
  public void testGetUserDetailsWithIncorrectPassword() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    String passwordIncorrect = "test1";

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      UserBean user = userService.getUserDetails(emailId, passwordIncorrect);
    });
  }

  // test getUserDetails with an existing user with incorrect email
  @Test
  public void testGetUserDetailsWithIncorrectEmail() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    String emailIncorrect = "test1";

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      UserBean user = userService.getUserDetails(emailIncorrect, password);
    });
  }

  // test getUserDetails with an existing user with invalid email format
  @Test
  public void testGetUserDetailsWithInvalidEmail() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    String emailIncorrect = "test";

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      UserBean user = userService.getUserDetails(emailIncorrect, password);
    });
  }

  // test getUserDetails with empty inputs
  @Test
  public void testGetUserDetailsWithEmptyInputs() {
    String emailId = "";
    String password = "";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      UserBean user = userService.getUserDetails(emailId, password);
    });
  }

  // test getUserDetails with null inputs
  @Test
  public void testGetUserDetailsWithNullInputs() {
    String emailId = null;
    String password = null;

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      UserBean user = userService.getUserDetails(emailId, password);
    });
  }
//
//
//
//
//
//
//
//
//
//
//
//
//
















  // test getFName with existing user
  @Test
  public void testGetFNameWithExistingUser() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    String res = userService.getFName(emailId);
    assertEquals(userName, res);
  }

  // test getFName with existing user with first name and last name
  @Test
  public void testGetFNameWithExistingUserMultipleNames() {
    String userName = "test example";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    String fname = "test";

    UserServiceImpl userService = new UserServiceImpl();
    String res = userService.getFName(emailId);
    assertEquals(fname, res);
  }

  // test getFName with nonexistent user
  @Test
  public void testGetFNameWithNonexistentUser() {
    String emailId = "test@gmail.com";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = userService.getFName(emailId);
    });
  }

  // test getFName with null input
  @Test
  public void testGetFNameWithNullInput() {
    String emailId = null;

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = userService.getFName(emailId);
    });
  }

  // test getFName with empty input
  @Test
  public void testGetFNameWithEmptyInput() {
    String emailId = "";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = userService.getFName(emailId);
    });
  }

  // test getFName with invalid email input
  @Test
  public void testGetFNameWithInvalidInputEmail() {
    String emailId = "test";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = userService.getFName(emailId);
    });
  }

  // test getUserAddr with existing user
  @Test
  public void testGetUserAddrWithExistingUser() {
    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    String res = userService.getUserAddr(emailId);
    assertEquals(address, res);
  }

  // test getUserAddr with nonexistent user
  @Test
  public void testGetUserAddrWithNonexistentUser() {
    String emailId = "test@gmail.com";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = userService.getUserAddr(emailId);
    });
  }

  // test getUserAddr with null input
  @Test
  public void testGetUserAddrWithNullInput() {
    String emailId = null;

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = userService.getUserAddr(emailId);
    });
  }

  // test getUserAddr with empty input
  @Test
  public void testGetUserAddrWithEmptyInput() {
    String emailId = "";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = userService.getUserAddr(emailId);
    });
  }

  // test getUserAddr with invalid email input
  @Test
  public void testGetUserAddrWithInvalidInputEmail() {
    String emailId = "test";

    try {
      assertFalse(getUserExists(emailId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    UserServiceImpl userService = new UserServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = userService.getUserAddr(emailId);
    });
  }
}

