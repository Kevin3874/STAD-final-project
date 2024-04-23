package test;

import com.shashi.beans.TransactionBean;
import com.shashi.beans.UserBean;
import com.shashi.constants.IUserConstants;
import com.shashi.service.impl.OrderServiceImpl;
import com.shashi.service.impl.TransServiceImpl;
import com.shashi.service.impl.UserServiceImpl;
import com.shashi.utility.DBUtil;
import com.shashi.utility.MailMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.shashi.constants.IUserConstants;
import java.sql.Timestamp;
import java.time.Instant;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransServiceImplTest {
  public void addTransaction(String userName, String transId, Timestamp transDateTime, double transAmount) throws Exception {
    Connection conn = DBUtil.provideConnection();

    PreparedStatement ps = conn.prepareStatement("insert into transactions values(?,?,?,?)");

    ps.setString(1, transId);
    ps.setString(2, userName);
    ps.setTimestamp(3, transDateTime);
    ps.setDouble(4, transAmount);

    int k = ps.executeUpdate();

    if (k < 0) {
      throw new Exception("did not insert successfully");
    }

    DBUtil.closeConnection(conn);
    DBUtil.closeConnection(ps);
  }


  public boolean getTransactionExists(String transId) throws Exception {
    boolean transaction = false;

    Connection con = DBUtil.provideConnection();

    PreparedStatement ps = null;
    ResultSet rs = null;

    ps = con.prepareStatement("select * from transactions where transid=?");

    ps.setString(1, transId);
    rs = ps.executeQuery();

    if (rs.next())
      transaction = true;

    DBUtil.closeConnection(con);
    DBUtil.closeConnection(ps);
    DBUtil.closeConnection(rs);

    return transaction;
  }

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
  public void clearTransactionsAndUsers() {
    try {
      Connection con = DBUtil.provideConnection();

      PreparedStatement ps1 = con.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
      ps1.execute();

      PreparedStatement ps2 = con.prepareStatement("DELETE FROM transactions");
      ps2.execute();

      PreparedStatement ps3 = con.prepareStatement("DELETE FROM user");
      ps3.execute();

      PreparedStatement ps4 = con.prepareStatement("SET FOREIGN_KEY_CHECKS=1");
      ps4.execute();

      DBUtil.closeConnection(con);
      DBUtil.closeConnection(ps1);
      DBUtil.closeConnection(ps2);
      DBUtil.closeConnection(ps3);
      DBUtil.closeConnection(ps4);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      System.out.println("FAILED TO CLEAN UP. CHECK SQL SERVER, DISABLE SAFE UPDATE MODE");
    }
  }

  @BeforeEach
  public void clearBefore() {
    clearTransactionsAndUsers();
  }


  // test getUserId with existing user
  @Test
  public void testGetUserIdWithExistingUser() {
    String transId = "12345";
    Timestamp transDateTime= Timestamp.from(Instant.now());

//    Timestamp transDateTime = Timestamp.valueOf("2018-09-01 09:01:15");
    double transAmount = 99.99;

    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

//    TransactionBean transaction = new TransactionBean();
//    transaction.setTransactionId(transId);
//    transaction.setTransAmount(transAmount);
//    transaction.setTransDateTime(transDateTime);
//    transaction.setUserName(userName);
//    OrderServiceImpl.addTransaction(transaction);

    try {
      assertFalse(getUserExists(emailId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      assertTrue(getUserExists(emailId));
      assertFalse(getTransactionExists(transId));
      addTransaction(userName, transId, transDateTime, transAmount);
      assertTrue(getTransactionExists(transId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    TransServiceImpl transService = new TransServiceImpl();
    String res = transService.getUserId(transId);
    assertEquals(userName, res);

    // cleanup and verification
    try {
      assertTrue(getTransactionExists(transId));
    } catch (Exception ex) {
      fail("failed to verify");
    }
  }

  // test getUserId with nonexistent user
  @Test
  public void testGetUserIdWithNonexistentUser() {
    String userName = "test";
    String transId = "12345";
    Timestamp transDateTime = Timestamp.valueOf("2018-09-01 09:01:15");
    double transAmount = 99.99;

    try {
      assertFalse(getTransactionExists(transId));
      addTransaction(userName, transId, transDateTime, transAmount);
      assertTrue(getTransactionExists(transId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    TransServiceImpl transService = new TransServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = transService.getUserId(transId);
    });

    // cleanup and verification
    try {
      assertTrue(getTransactionExists(transId));
    } catch (Exception ex) {
      fail("failed to verify");
    }
  }

  // test getUserId with nonexistent transaction
  @Test
  public void testGetUserIdWithNonexistentTransaction() {
    String transId = "12345";

    try {
      assertFalse(getTransactionExists(transId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    TransServiceImpl transService = new TransServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = transService.getUserId(transId);
    });
  }


  // test getUserId with null inputs
  @Test
  public void testGetUserIdWithNullInputs() {
    String transId = null;

    try {
      assertFalse(getTransactionExists(transId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    TransServiceImpl transService = new TransServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = transService.getUserId(transId);
    });
  }

  // test getUserId with empty inputs
  @Test
  public void testGetUserIdWithEmptyInputs() {
    String transId = "";

    try {
      assertFalse(getTransactionExists(transId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    TransServiceImpl transService = new TransServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String res = transService.getUserId(transId);
    });
  }

}