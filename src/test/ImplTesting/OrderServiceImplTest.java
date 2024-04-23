package test.ImplTesting;

import com.shashi.beans.OrderBean;
import com.shashi.beans.OrderDetails;
import com.shashi.beans.TransactionBean;
import com.shashi.service.impl.DemandServiceImpl;
import com.shashi.service.impl.OrderServiceImpl;
import com.shashi.utility.DBUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceImplTest {

    // This file contains blackbox, whitebox methods to verify that each method works as intended.
    // It also contains helper methods mostly specific to the cart tests

    // IMPORTANT!! This test requires the Gmail app password setup in the README that the author created.
    // IMPORTANT!! many tests assume the default setup has been completed and unaltered. Tests also try
    // to restore the table to the default state after completion.
    // IMPORTANT!! must have MYSQL Server/Workbench installed, set up, and connected. Contact Jiashu
    // if you have questions.
    // On a first startup, these tests may  fail due to SQL errors: I resolved these by running the
    // creator's startup SQL query script individually, as the product commit crashed my MySQL Workbench.

    public boolean getProductExistsUsernameOnly(String userID) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from usercart where username=?");

        ps.setString(1, userID);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }


    public boolean getProductExistsWithQuantity(String userId, String productId, int quantity) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from usercart where username=? and prodid=? and quantity=?");

        ps.setString(1, userId);
        ps.setString(2, productId);
        ps.setInt(3, quantity);

        ResultSet rs = ps.executeQuery();

        int result = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return result > 0;
    }

    public void addToCart(String userId, String productId, int quantity) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("insert into usercart values(?,?,?)");

        ps.setString(1, userId);

        ps.setString(2, productId);

        ps.setInt(3, quantity);

        int k = ps.executeUpdate();

        if (k == 0) {
            throw new Exception("did not insert successfully");
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
    }

    public void resetProductCountTo1000(String prodId) throws SQLException {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("update product set pquantity=1000 where pid=?");

        ps.setString(1, prodId);

        ps.execute();

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
    }

    public int getProductCount(String prodId) throws SQLException {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * from product where pid=?");

        ps.setString(1, prodId);

        int num = -1;

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            num = rs.getInt("pquantity");
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num;
    }

    public int getResultSetSize(ResultSet rs) {
        try {
            int size = 0;
            while (rs.next()) {
                size++;
            }
            return size;
        } catch (Exception ex) {
            return -1;
        }
    }

    public boolean getOrderExistsProdIDOnly(String productId) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from orders where prodid=?");

        ps.setString(1, productId);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }

    public boolean getOrderExistsOrderIdOnly(String orderID) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from orders where orderid=?");

        ps.setString(1, orderID);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }

    public boolean getOrderExists(String orderId, String productId) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from orders where orderid=? and prodid=?");

        ps.setString(1, orderId);
        ps.setString(2, productId);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }

    public boolean getTransactionExists(String transId, String username) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from transactions where transid=? and username=?");

        ps.setString(1, transId);
        ps.setString(2, username);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }

    // the amount is hardcoded here, as using the double value from Java may incorporate doubles precision, causing an error
    public boolean getTransactionExistsWithTimeAmount125999(String transId, String username, Timestamp time) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from transactions where transid=? and username=? and time=? and amount=125999.00");

        ps.setString(1, transId);
        ps.setString(2, username);
        ps.setTimestamp(3, time);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }

    // the amount is hardcoded here, as using the double value from Java may incorporate doubles precision, causing an error
    public boolean getOrderExistsWithQuantity125999AmountShipped(String orderId, String productId, int quantity, int shipped) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from orders where orderid=? and prodid=? and quantity=? and amount=125999.00 and shipped=?");

        ps.setString(1, orderId);
        ps.setString(2, productId);
        ps.setInt(3, quantity);
        ps.setInt(4, shipped);

        ResultSet rs = ps.executeQuery();

        int result = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return result > 0;
    }

    public void addTransactionWithAmount125999(String transId, String username, Timestamp time) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("insert into transactions values(?,?,?,125999.00)");

        ps.setString(1, transId);
        ps.setString(2, username);
        ps.setTimestamp(3, time);

        int k = ps.executeUpdate();

        if (k == 0) {
            throw new Exception("did not insert successfully");
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
    }

    // the amount is hardcoded here, as using the double value from Java may incorporate doubles precision, causing an error
    public void addToOrdersWithQuantity125999(String orderId, String productId, int quantity, int shipped) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("insert into orders values(?,?,?,125999.00,?)");

        ps.setString(1, orderId);
        ps.setString(2, productId);
        ps.setInt(3, quantity);
        ps.setDouble(4, shipped);

        int k = ps.executeUpdate();

        if (k == 0) {
            throw new Exception("did not insert successfully");
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
    }

    public static void disableForeignKeyCheck() throws SQLException {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("SET foreign_key_checks = 0;");
        ps.execute();

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
    }

    public static void enableForeignKeyCheck() throws SQLException {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("SET foreign_key_checks = 1;");
        ps.execute();

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
    }

    public static void deleteTransactions() throws SQLException {
        disableForeignKeyCheck();
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM transactions");
        ps.execute();

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        enableForeignKeyCheck();
    }

    public void deleteDemands() throws SQLException {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM user_demand");

        ps.execute();

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
    }

    @AfterAll
    public static void restoreDemand() {
        try {
            Connection con = DBUtil.provideConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO `shopping-cart`.`user_demand` (`username`, `prodid`, `quantity`) VALUES ('guest@gmail.com', 'P20230423084144', 3)");

            ps.execute();

            DBUtil.closeConnection(con);
            DBUtil.closeConnection(ps);

        } catch (Exception ex) {
            System.out.println("FAILED TO SET/CLEAN UP. CHECK SQL SERVER, DISABLE SAFE UPDATE MODE");
            fail();
        }
    }

    @AfterEach
    public void clearCartResetOrder() {
        try {
            Connection con = DBUtil.provideConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM usercart");

            ps.execute();

            DBUtil.closeConnection(con);
            DBUtil.closeConnection(ps);

            deleteDemands();

        } catch (Exception ex) {
            System.out.println("FAILED TO SET/CLEAN UP. CHECK SQL SERVER, DISABLE SAFE UPDATE MODE");
            fail();
        }
    }

    @BeforeEach
    public void deleteOrders() throws SQLException {
        deleteTransactions();
        restoreTransaction();
        disableForeignKeyCheck();
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM orders");
        ps.execute();

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        enableForeignKeyCheck();
    }

    @AfterEach
    public void cleanup() throws SQLException {
        deleteOrders();
        enableForeignKeyCheck();
        resetProductCountTo1000("P20230423084145");
        resetProductCountTo1000("P20230423083830");
    }

    public static void restoreTransaction() throws SQLException {
        disableForeignKeyCheck();
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO `shopping-cart`.`transactions` (`transid`, `username`, `time`, `amount`) VALUES ('TR10001', 'guest@gmail.com', '2023-04-23 09:26:56', 125999.00)");

        ps.execute();

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        enableForeignKeyCheck();
    }

    @AfterAll
    public static void restoreOrder() {
        try {
            deleteTransactions();
            restoreTransaction();
            Connection con = DBUtil.provideConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO `shopping-cart`.`orders` (`orderid`, `prodid`, `quantity`, `amount`, `shipped`) VALUES ('TR10001', 'P20230423082243', 1, 125999.00, 0)");

            ps.execute();

            DBUtil.closeConnection(con);
            DBUtil.closeConnection(ps);
        } catch (Exception ex) {
            System.out.println("FAILED TO SET/CLEAN UP. CHECK SQL SERVER, DISABLE SAFE UPDATE MODE");
            fail();
        }
    }

    // adds an order with a null transaction ID
    @Test
    public void testAddOrderNullTransactionID() {
        String prodId = "P20230423082243";
        String orderId = "TR10001";

        OrderServiceImpl orderService = new OrderServiceImpl();
        OrderBean bean = new OrderBean();
        bean.setTransactionId(null);
        bean.setProductId(prodId);
        bean.setAmount(125999.0);
        bean.setShipped(0);
        bean.setQuantity(1);
        boolean status = orderService.addOrder(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getOrderExistsOrderIdOnly(orderId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds an order with a null product ID
    @Test
    public void testAddOrderNullProductID() {
        String prodId = "P20230423082243";
        String orderId = "TR10001";

        OrderServiceImpl orderService = new OrderServiceImpl();
        OrderBean bean = new OrderBean();
        bean.setTransactionId(orderId);
        bean.setProductId(null);
        bean.setAmount(125999.0);
        bean.setShipped(0);
        bean.setQuantity(1);
        boolean status = orderService.addOrder(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getOrderExistsProdIDOnly(prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds an order with an invalid product ID
    @Test
    public void testAddOrderInvalidProductID() {
        String prodId = "P20230423082243";
        String orderId = "TR10001";

        OrderServiceImpl orderService = new OrderServiceImpl();
        OrderBean bean = new OrderBean();
        bean.setTransactionId(orderId);
        bean.setProductId("abcdefg");
        bean.setAmount(125999.0);
        bean.setShipped(0);
        bean.setQuantity(1);
        boolean status = orderService.addOrder(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getOrderExists(orderId, "abcdefg"));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds an order
    @Test
    public void testAddOrder() {
        String prodId = "P20230423082243";
        String orderId = "TR10001";

        OrderServiceImpl orderService = new OrderServiceImpl();
        OrderBean bean = new OrderBean();
        bean.setTransactionId(orderId);
        bean.setProductId(prodId);
        bean.setAmount(125999.0);
        bean.setShipped(0);
        bean.setQuantity(1);
        boolean status = orderService.addOrder(bean);
        assertTrue(status);

        // cleanup and verification
        try {
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(orderId, prodId, 1, 0));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds an order that already exists
    @Test
    public void testAddOrderDuplicate() {
        String prodId = "P20230423082243";
        String orderId = "TR10001";

        try {
            assertFalse(getOrderExists(orderId, prodId));
            addToOrdersWithQuantity125999(orderId, prodId, 1, 0);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(orderId, prodId, 1, 0));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        OrderBean bean = new OrderBean();
        bean.setTransactionId(orderId);
        bean.setProductId(prodId);
        bean.setAmount(125999.0);
        bean.setShipped(0);
        bean.setQuantity(1);
        boolean status = orderService.addOrder(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(orderId, prodId, 1, 0));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds a transaction with a null transaction ID
    @Test
    public void testAddTransactionNullTransactionID() {
        String transId = "TR10002";
        String username = "admin@gmail.com";
        try {
            assertFalse(getTransactionExists(null, username));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        TransactionBean bean = new TransactionBean();
        bean.setTransactionId(null);
        bean.setUserName(username);
        bean.setTransAmount(125999.0);
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        bean.setTransDateTime(time);
        boolean status = orderService.addTransaction(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getTransactionExists(null, username));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds a transaction with an invalid transaction ID
    @Test
    public void testAddTransactionInvalidTransactionID() {
        String transId = "TR10001";
        String username = "admin@gmail.com";
        try {
            assertFalse(getTransactionExists("abcdefg", username));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        TransactionBean bean = new TransactionBean();
        bean.setTransactionId("abcdefg");
        bean.setUserName(username);
        bean.setTransAmount(125999.0);
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        bean.setTransDateTime(time);
        boolean status = orderService.addTransaction(bean);
        assertFalse(status);

        // cleanup and verification
            try {
            assertFalse(getTransactionExists("abcdefg", username));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds a transaction with a null username
    @Test
    public void testAddTransactionNullUsername() {
        String transId = "TR10001";
        String username = "admin@gmail.com";
        try {
            assertFalse(getTransactionExists(transId, null));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        TransactionBean bean = new TransactionBean();
        bean.setTransactionId(transId);
        bean.setUserName(null);
        bean.setTransAmount(125999.0);
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        bean.setTransDateTime(time);
        boolean status = orderService.addTransaction(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getTransactionExists(transId, null));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds a transaction with an invalid username
    @Test
    public void testAddTransactionInvalidUsername() {
        String transId = "TR10001";
        String username = "admin@gmail.com";
        try {
            assertFalse(getTransactionExists(transId, "abcdefg"));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        TransactionBean bean = new TransactionBean();
        bean.setTransactionId(transId);
        bean.setUserName("abcdefg");
        bean.setTransAmount(125999.0);
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        bean.setTransDateTime(time);
        boolean status = orderService.addTransaction(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getTransactionExists(transId, "abcdefg"));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds a transaction with a null timestamp
    @Test
    public void testAddTransactionNullTimestamp() {
        String transId = "TR10001";
        String username = "admin@gmail.com";
        try {
            assertFalse(getTransactionExists(transId, username));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        TransactionBean bean = new TransactionBean();
        bean.setTransactionId(transId);
        bean.setUserName(username);
        bean.setTransAmount(125999.0);
        bean.setTransDateTime(null);
        boolean status = orderService.addTransaction(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getTransactionExists(transId, username));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds a transaction
    @Test
    public void testAddTransaction() {
        String transId = "TR10002";
        String username = "admin@gmail.com";
        String prodId = "P20230423084144";
        try {
            assertFalse(getTransactionExists(transId, username));
            addToOrdersWithQuantity125999(transId, prodId, 0, 1);
            assertTrue(getOrderExists(transId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        TransactionBean bean = new TransactionBean();
        bean.setTransactionId(transId);
        bean.setUserName(username);
        bean.setTransAmount(125999.0);
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        bean.setTransDateTime(time);
        boolean status = orderService.addTransaction(bean);
        assertTrue(status);

        // cleanup and verification
        try {
            assertTrue(getTransactionExistsWithTimeAmount125999(transId, username, time));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // adds a transaction when a duplicate exists
    @Test
    public void testAddTransactionDuplicate() {
        String transId = "TR10002";
        String username = "admin@gmail.com";
        String prodId = "P20230423084144";
        try {
            assertFalse(getTransactionExists(transId, username));
            addToOrdersWithQuantity125999(transId, prodId, 0, 1);
            assertTrue(getOrderExists(transId, prodId));
            Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
            addTransactionWithAmount125999(transId, username, time);
            assertTrue(getTransactionExistsWithTimeAmount125999(transId, username, time));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        TransactionBean bean = new TransactionBean();
        bean.setTransactionId(transId);
        bean.setUserName(username);
        bean.setTransAmount(125999.0);
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        bean.setTransDateTime(time);
        boolean status = orderService.addTransaction(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertTrue(getTransactionExistsWithTimeAmount125999(transId, username, time));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // counts sum of quantity in orders with null product id
    @Test
    public void testCountSoldItemNullProductId() {
        String transId = "TR10001";
        String prodId = "P20230423082243";
        try {
            assertFalse(getOrderExists(transId, prodId));
            assertFalse(getOrderExists(transId, null));
            addToOrdersWithQuantity125999(transId, prodId, 1, 0);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId, prodId, 1, 0));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        int count = orderService.countSoldItem(null);
        assertEquals(0, count);
    }

    // counts sum of quantity in orders with an invalid product id
    @Test
    public void testCountSoldItemInvalidProductId() {
        String transId = "TR10001";
        String prodId = "P20230423084144";
        try {
            assertFalse(getOrderExists(transId, prodId));
            assertFalse(getOrderExists(transId, "abcdefg"));
            addToOrdersWithQuantity125999(transId, prodId, 1, 0);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId, prodId, 1, 0));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        int count = orderService.countSoldItem("abcdefg");
        assertEquals(0, count);
    }

    // counts sum of quantity in orders
    @Test
    public void testCountSoldItem() {
        String transId = "TR10001";
        String prodId = "P20230423084144";
        try {
            assertFalse(getOrderExists(transId, prodId));
            addToOrdersWithQuantity125999(transId, prodId, 1, 0);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId, prodId, 1, 0));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        int count = orderService.countSoldItem(prodId);
        assertEquals(1, count);
    }

    // counts sum of quantity in orders for multiple transactions
    @Test
    public void testCountSoldItemMultipleOrders() {
        String transId1 = "TR10001";
        String transId2 = "TR10002";
        String transId3 = "TR10003";
        String prodId1 = "P20230423084144";
        String prodId2 = "P20230423083830";
        try {
            assertFalse(getOrderExists(transId1, prodId1));
            assertFalse(getOrderExists(transId2, prodId2));
            assertFalse(getOrderExists(transId3, prodId2));
            addToOrdersWithQuantity125999(transId1, prodId1, 2, 0);
            addToOrdersWithQuantity125999(transId2, prodId2, 5, 0);
            addToOrdersWithQuantity125999(transId3, prodId2, 1, 0);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId1, prodId1, 2, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId2, prodId2, 5, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId3, prodId2, 1, 0));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        int count1 = orderService.countSoldItem(prodId1);
        assertEquals(2, count1);
        int count2 = orderService.countSoldItem(prodId2);
        assertEquals(6, count2);
    }

    // gets all orders in an empty table
    @Test
    public void testGetAllOrdersEmpty() {
        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderBean> list = orderService.getAllOrders();
        assertTrue(list.isEmpty());
    }

    // gets all orders in a non-empty table
    @Test
    public void testGetAllOrders() {
        String transId1 = "TR10001";
        String transId2 = "TR10002";
        String transId3 = "TR10003";
        String prodId1 = "P20230423084144";
        String prodId2 = "P20230423083830";
        try {
            assertFalse(getOrderExists(transId1, prodId1));
            assertFalse(getOrderExists(transId2, prodId2));
            assertFalse(getOrderExists(transId3, prodId2));
            addToOrdersWithQuantity125999(transId1, prodId1, 2, 0);
            addToOrdersWithQuantity125999(transId2, prodId2, 5, 0);
            addToOrdersWithQuantity125999(transId3, prodId2, 1, 0);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId1, prodId1, 2, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId2, prodId2, 5, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId3, prodId2, 1, 0));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderBean> list = orderService.getAllOrders();
        assertEquals(3, list.size());

        for (OrderBean bean : list) {
            if (bean.getAmount() != 125999.0) {
                fail("incorrect amount returned");
            } else if (bean.getShipped() != 0) {
                fail("incorrect shipped returned");
            } else if (bean.getProductId().equals(prodId1) && bean.getQuantity() == 2 && bean.getTransactionId().equals(transId1)) {
                continue;
            } else if (bean.getProductId().equals(prodId2) && bean.getQuantity() == 5 && bean.getTransactionId().equals(transId2)) {
                continue;
            } else if (bean.getProductId().equals(prodId2) && bean.getQuantity() == 1 && bean.getTransactionId().equals(transId3)) {
                continue;
            } else {
                fail("invalid entry");
            }
        }
    }

    // gets all orders by a null user id
    @Test
    public void testGetOrdersByUserIdNullUserId() {
        String userId = "admin@gmail.com";
        String transId = "TR10001";
        try {
            assertFalse(getTransactionExists(transId, null));
        } catch (Exception ex) {
            fail("failed setup");
        }
        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderBean> list = orderService.getOrdersByUserId(null);
        assertTrue(list.isEmpty());
    }

    // gets all orders by an invalid user id
    @Test
    public void testGetOrdersByUserIdInvalidUserId() {
        String userId = "admin@gmail.com";
        String transId = "TR10001";
        try {
            assertFalse(getTransactionExists(transId, "abcdefg"));
        } catch (Exception ex) {
            fail("failed setup");
        }
        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderBean> list = orderService.getOrdersByUserId("abcdefg");
        assertTrue(list.isEmpty());
    }

    // gets all orders by user id in an empty table
    @Test
    public void testGetOrdersByUserIdEmpty() {
        String userId = "admin@gmail.com";
        String transId = "TR10001";
        try {
            assertFalse(getTransactionExists(transId, userId));
        } catch (Exception ex) {
            fail("failed setup");
        }
        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderBean> list = orderService.getOrdersByUserId(userId);
        assertTrue(list.isEmpty());
    }

    // gets all orders by user id in a non-empty table
    @Test
    public void testGetAllOrdersByUserIdMultipleEntries() {
        String userId = "guest@gmail.com";
        String transId1 = "TR10001";
        String transId2 = "TR10002";
        String transId3 = "TR10003";
        String prodId1 = "P20230423084144";
        String prodId2 = "P20230423083830";
        try {
            assertFalse(getOrderExists(transId1, prodId1));
            assertFalse(getOrderExists(transId2, prodId2));
            assertFalse(getOrderExists(transId3, prodId2));
            addToOrdersWithQuantity125999(transId1, prodId1, 2, 0);
            addToOrdersWithQuantity125999(transId2, prodId2, 5, 0);
            addToOrdersWithQuantity125999(transId3, prodId2, 1, 0);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId1, prodId1, 2, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId2, prodId2, 5, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId3, prodId2, 1, 0));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        // This is a fault where the query to select orders tries to access the transid field on an order, but it should be orderid
        List<OrderBean> list = orderService.getOrdersByUserId(userId);
        assertEquals(3, list.size());

        for (OrderBean bean : list) {
            if (bean.getAmount() != 125999.0) {
                fail("incorrect amount returned");
            } else if (bean.getShipped() != 0) {
                fail("incorrect shipped returned");
            } else if (bean.getProductId().equals(prodId1) && bean.getQuantity() == 2 && bean.getTransactionId().equals(transId1)) {
                continue;
            } else if (bean.getProductId().equals(prodId2) && bean.getQuantity() == 5 && bean.getTransactionId().equals(transId2)) {
                continue;
            } else if (bean.getProductId().equals(prodId2) && bean.getQuantity() == 1 && bean.getTransactionId().equals(transId3)) {
                continue;
            } else {
                fail("invalid entry");
            }
        }
    }

    // gets all orders by user id in a non-empty table
    @Test
    public void testGetAllOrdersByUserIdMultipleUsers() {
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        String transId1 = "TR10001";
        String transId2 = "TR10002";
        String transId3 = "TR10003";
        String prodId1 = "P20230423084144";
        String prodId2 = "P20230423083830";
        try {
            assertFalse(getOrderExists(transId1, prodId1));
            assertFalse(getOrderExists(transId2, prodId2));
            assertFalse(getOrderExists(transId3, prodId2));
            addToOrdersWithQuantity125999(transId1, prodId1, 2, 0);
            addToOrdersWithQuantity125999(transId2, prodId2, 5, 0);
            addToOrdersWithQuantity125999(transId3, prodId2, 1, 0);
            Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
            addTransactionWithAmount125999(transId3, userId2, time);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId1, prodId1, 2, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId2, prodId2, 5, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId3, prodId2, 1, 0));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId3, userId2, time));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        // This is a fault where the query to select orders tries to access the transid field on an order, but it should be orderid
        List<OrderBean> list1 = orderService.getOrdersByUserId(userId1);
        assertEquals(3, list1.size());

        for (OrderBean bean : list1) {
            if (bean.getAmount() != 125999.0) {
                fail("incorrect amount returned");
            } else if (bean.getShipped() != 0) {
                fail("incorrect shipped returned");
            } else if (bean.getProductId().equals(prodId1) && bean.getQuantity() == 2 && bean.getTransactionId().equals(transId1)) {
                continue;
            } else if (bean.getProductId().equals(prodId2) && bean.getQuantity() == 5 && bean.getTransactionId().equals(transId2)) {
                continue;
            } else if (bean.getProductId().equals(prodId2) && bean.getQuantity() == 1 && bean.getTransactionId().equals(transId3)) {
                continue;
            } else {
                fail("invalid entry");
            }
        }

        List<OrderBean> list2 = orderService.getOrdersByUserId(userId2);
        assertEquals(1, list2.size());
        OrderBean bean = list2.get(0);
        assertTrue(bean.getProductId().equals(prodId2) && bean.getQuantity() == 10 && bean.getTransactionId().equals(transId3));
    }

    // gets all order details by a null user id
    @Test
    public void testGetAllOrderDetailsNullUserId() {
        String userId = "admin@gmail.com";
        String transId = "TR10001";
        try {
            assertFalse(getTransactionExists(transId, null));
        } catch (Exception ex) {
            fail("failed setup");
        }
        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderDetails> list = orderService.getAllOrderDetails(null);
        assertTrue(list.isEmpty());
    }

    // gets all order details by an invalid user id
    @Test
    public void testGetAllOrderDetailsInvalidUserId() {
        String userId = "admin@gmail.com";
        String transId = "TR10001";
        try {
            assertFalse(getTransactionExists(transId, "abcdefg"));
        } catch (Exception ex) {
            fail("failed setup");
        }
        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderDetails> list = orderService.getAllOrderDetails("abcdefg");
        assertTrue(list.isEmpty());
    }

    // gets all order details by user id in an empty table
    @Test
    public void testGetAllOrderDetailsEmpty() {
        String userId = "admin@gmail.com";
        String transId = "TR10001";
        try {
            assertFalse(getTransactionExists(transId, userId));
        } catch (Exception ex) {
            fail("failed setup");
        }
        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderDetails> list = orderService.getAllOrderDetails(userId);
        assertTrue(list.isEmpty());
    }

    // gets all order details by user id in a table with multiple entries
    @Test
    public void testGetAllOrderDetailsMultipleEntries() {
        String userId = "guest@gmail.com";
        String transId1 = "TR10004";
        String transId2 = "TR10002";
        String transId3 = "TR10003";
        String prodId1 = "P20230423084144";
        String prodId2 = "P20230423083830";
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        try {
            assertFalse(getOrderExists(transId1, prodId1));
            assertFalse(getOrderExists(transId2, prodId2));
            assertFalse(getOrderExists(transId3, prodId2));
            addToOrdersWithQuantity125999(transId1, prodId1, 2, 0);
            addToOrdersWithQuantity125999(transId2, prodId2, 5, 0);
            addToOrdersWithQuantity125999(transId3, prodId2, 1, 0);
            addTransactionWithAmount125999(transId1, userId, time);
            addTransactionWithAmount125999(transId2, userId, time);
            addTransactionWithAmount125999(transId3, userId, time);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId1, prodId1, 2, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId2, prodId2, 5, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId3, prodId2, 1, 0));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId1, userId, time));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId2, userId, time));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId3, userId, time));
        } catch (Exception ex) {
            fail("failed setup");
        }
        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderDetails> list = orderService.getAllOrderDetails(userId);
        assertEquals(3, list.size());

        for (OrderDetails detail : list) {
            if (!detail.getAmount().contains("125999.0")) {
                fail("incorrect amount returned");
            } else if (detail.getShipped() != 0) {
                fail("incorrect shipped returned");
            } else if (detail.getProductId().equals(prodId1) && detail.getOrderId().equals(transId1) && detail.getQty().equals("2")
                        && detail.getProdName().equals("MOTOROLA G32 Mobile") && detail.getShipped() == 0 && detail.getTime().equals(time)) {
                continue;
            } else if (detail.getProductId().equals(prodId2) && detail.getOrderId().equals(transId2) && detail.getQty().equals("5")
                        && detail.getProdName().equals("HP Intel Core i5 11th Gen") && detail.getShipped() == 0 && detail.getTime().equals(time)) {
                continue;
            } else if (detail.getProductId().equals(prodId2) && detail.getOrderId().equals(transId3) && detail.getQty().equals("1")
                        && detail.getProdName().equals("HP Intel Core i5 11th Gen") && detail.getShipped() == 0 && detail.getTime().equals(time)) {
                continue;
            } else {
                fail("invalid entry");
            }
        }
    }

    // gets all order details in a non-empty table
    @Test
    public void testGetAllOrderDetailsMultipleUsers() {
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        String transId1 = "TR10004";
        String transId2 = "TR10002";
        String transId3 = "TR10003";
        String prodId1 = "P20230423084144";
        String prodId2 = "P20230423083830";
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        try {
            assertFalse(getOrderExists(transId1, prodId1));
            assertFalse(getOrderExists(transId2, prodId2));
            assertFalse(getOrderExists(transId3, prodId2));
            addToOrdersWithQuantity125999(transId1, prodId1, 2, 0);
            addToOrdersWithQuantity125999(transId2, prodId2, 5, 0);
            addToOrdersWithQuantity125999(transId3, prodId2, 1, 0);
            addTransactionWithAmount125999(transId1, userId1, time);
            addTransactionWithAmount125999(transId2, userId1, time);
            addTransactionWithAmount125999(transId3, userId2, time);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId1, prodId1, 2, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId2, prodId2, 5, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId3, prodId2, 1, 0));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId1, userId1, time));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId2, userId1, time));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId3, userId2, time));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        List<OrderDetails> list1 = orderService.getAllOrderDetails(userId1);
        assertEquals(2, list1.size());

        for (OrderDetails detail : list1) {
            if (!detail.getAmount().contains("125999.0")) {
                fail("incorrect amount returned");
            } else if (detail.getShipped() != 0) {
                fail("incorrect shipped returned");
            } else if (detail.getProductId().equals(prodId1) && detail.getOrderId().equals(transId1) && detail.getQty().equals("2")
                    && detail.getProdName().equals("MOTOROLA G32 Mobile") && detail.getShipped() == 0 && detail.getTime().equals(time)) {
                continue;
            } else if (detail.getProductId().equals(prodId2) && detail.getOrderId().equals(transId2) && detail.getQty().equals("5")
                    && detail.getProdName().equals("HP Intel Core i5 11th Gen") && detail.getShipped() == 0 && detail.getTime().equals(time)) {
                continue;
            } else {
                fail("invalid entry");
            }
        }
        List<OrderDetails> list2 = orderService.getAllOrderDetails(userId2);
        assertEquals(1, list2.size());
        OrderDetails detail = list2.get(0);
        assertTrue(detail.getProductId().equals(prodId2) && detail.getOrderId().equals(transId3) && detail.getQty().equals("1")
                    && detail.getProdName().equals("HP Intel Core i5 11th Gen") && detail.getShipped() == 0 && detail.getTime().equals(time));
    }

    // ships all orders with null order id
    @Test
    public void testShipNowNullOrderId() {
        String transId = "TR10001";
        String productId = "P20230423082243";
        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.shipNow(null, productId);
        assertEquals("FAILURE", status);
    }

    // ships all orders with invalid order id
    @Test
    public void testShipNowInvalidOrderId() {
        String transId = "TR10001";
        String productId = "P20230423082243";
        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.shipNow("abcdefg", productId);
        assertEquals("FAILURE", status);
    }

    // ships all orders with null product id
    @Test
    public void testShipNowNullProductId() {
        String transId = "TR10001";
        String productId = "P20230423082243";
        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.shipNow(transId, null);
        assertEquals("FAILURE", status);
    }

    // ships all orders with invalid product id
    @Test
    public void testShipNowInvalidProductId() {
        String transId = "TR10001";
        String productId = "P20230423082243";
        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.shipNow(transId, "abcdefg");
        assertEquals("FAILURE", status);
    }

    // ships all orders in a table with multiple transaction and product ids
    @Test
    public void testGetShipNowMultipleTransactions() {
        String userId = "guest@gmail.com";
        String transId1 = "TR10004";
        String transId2 = "TR10002";
        String transId3 = "TR10003";
        String prodId1 = "P20230423084144";
        String prodId2 = "P20230423083830";
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        try {
            assertFalse(getOrderExists(transId1, prodId1));
            assertFalse(getOrderExists(transId2, prodId2));
            assertFalse(getOrderExists(transId3, prodId2));
            addToOrdersWithQuantity125999(transId1, prodId1, 2, 0);
            addToOrdersWithQuantity125999(transId2, prodId2, 5, 0);
            addToOrdersWithQuantity125999(transId3, prodId2, 1, 0);
            addTransactionWithAmount125999(transId1, userId, time);
            addTransactionWithAmount125999(transId2, userId, time);
            addTransactionWithAmount125999(transId3, userId, time);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId1, prodId1, 2, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId2, prodId2, 5, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId3, prodId2, 1, 0));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId1, userId, time));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId2, userId, time));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId3, userId, time));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.shipNow(transId1, prodId1);
        assertEquals("Order Has been shipped successfully!!", status);

        try {
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId1, prodId1, 2, 1));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId2, prodId2, 5, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId3, prodId2, 1, 0));
        } catch (Exception ex) {
            fail("failed verification 1");
        }

        status = orderService.shipNow(transId3, prodId2);
        assertEquals("Order Has been shipped successfully!!", status);

        try {
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId1, prodId1, 2, 1));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId2, prodId2, 5, 0));
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId3, prodId2, 1, 1));
        } catch (Exception ex) {
            fail("failed verification 2");
        }
    }

    // ships all orders in a table where an order is already shipped
    @Test
    public void testGetShipNowShippedOrder() {
        String userId = "guest@gmail.com";
        String transId = "TR10004";
        String prodId = "P20230423084144";
        Timestamp time = Timestamp.valueOf("2023-04-23 09:26:56");
        try {
            assertFalse(getOrderExists(transId, prodId));
            addToOrdersWithQuantity125999(transId, prodId, 2, 1);
            addTransactionWithAmount125999(transId, userId, time);
            assertTrue(getOrderExistsWithQuantity125999AmountShipped(transId, prodId, 2, 1));
            assertTrue(getTransactionExistsWithTimeAmount125999(transId, userId, time));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.shipNow(transId, prodId);
        assertEquals("FAILURE", status);
    }

    // proceeds with payment with null username
    @Test
    public void testPaymentSuccessNullUsername() {
        String transId = "TR10001";
        String productId = "P20230423082243";
        String userName = "guest@gmail.com";
        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.paymentSuccess(null, 100);
        assertEquals("Order Placement Failed!", status);
    }

    // proceeds with payment with invalid username
    @Test
    public void testPaymentSuccessInvalidUsername() {
        String transId = "TR10001";
        String productId = "P20230423082243";
        String userName = "guest@gmail.com";
        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.paymentSuccess("abcdefg", 100);
        assertEquals("Order Placement Failed!", status);
    }

    // proceeds with payment for empty cart
    @Test
    public void testPaymentSuccessEmptyCart() {
        String transId = "TR10001";
        String productId = "P20230423082243";
        String userName = "admin@gmail.com";
        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.paymentSuccess(userName, 100);
        assertEquals("Order Placement Failed!", status);
    }

    // proceeds with payment for multiple entries
    @Test
    public void testPaymentSuccess() {
        String productId1 = "P20230423084145";
        String productId2 = "P20230423083830";
        String username = "guest@gmail.com";

        try {
            addToCart(username, productId1, 2);
            addToCart(username, productId2, 1);
            assertTrue(getProductExistsWithQuantity(username, productId1, 2));
            assertTrue(getProductExistsWithQuantity(username, productId2, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.paymentSuccess(username, 100);
        assertEquals("Order Placed Successfully!", status);

        try {
            assertEquals(998, getProductCount(productId1));
            assertEquals(999, getProductCount(productId2));
        } catch (Exception ex) {
            fail("failed verification");
        }
    }

    // proceeds with payment for multiple users
    @Test
    public void testPaymentSuccessMultipleUsers() {
        String productId = "P20230423084145";
        String username1 = "guest@gmail.com";
        String username2 = "admin@gmail.com";

        try {
            addToCart(username1, productId, 2);
            addToCart(username2, productId, 1);
            assertTrue(getProductExistsWithQuantity(username1, productId, 2));
            assertTrue(getProductExistsWithQuantity(username2, productId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.paymentSuccess(username1, 100);
        assertEquals("Order Placed Successfully!", status);

        try {
            assertEquals(998, getProductCount(productId));
            assertTrue(getProductExistsWithQuantity(username2, productId, 1));
        } catch (Exception ex) {
            fail("failed verification");
        }
    }

    // todo remove?
    // proceeds with payment for a quantity above what is available
    /*@Test
    public void testPaymentSuccessMoreThanAvailable() {
        String productId = "P20230423084145";
        String username = "guest@gmail.com";

        try {
            addToCart(username, productId, 1001);
            assertTrue(getProductExistsWithQuantity(username, productId, 1001));
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.paymentSuccess(username, 100);
        assertEquals("Order Placed Successfully!", status);

        try {
           // assertEquals(1000, getProductCount(productId));
           // assertTrue(getProductExistsWithQuantity(username, productId, 1001));
        } catch (Exception ex) {
            fail("failed verification");
        }
    }*/

    // proceeds with payment when addOrder fails
    // technically tests a mostly-infeasible case, but helps to make sure the logic is still working
    @Test
    public void testPaymentSuccessAddOrderFails() {
        String productId = "P20230423084145";
        String username = "guest@gmail.com";

        try {
            disableForeignKeyCheck();
            addToCart(username, "abcdefg", 2);
            assertTrue(getProductExistsWithQuantity(username, "abcdefg", 2));
            enableForeignKeyCheck();
        } catch (Exception ex) {
            fail("failed setup");
        }

        OrderServiceImpl orderService = new OrderServiceImpl();
        String status = orderService.paymentSuccess(username, 100);
        assertEquals("Order Placement Failed!", status);
    }

    // proceeds with payment when sellNProduct fails
    // technically tests a mostly-infeasible case, but helps to make sure the logic is still working
    @Test
    public void testPaymentSuccessSellNProductFails() {
        String productId = "P20230423084145";
        String username = "guest@gmail.com";

        try {
            disableForeignKeyCheck();
            addToCart(username, "abcdefg", 2);
            assertTrue(getProductExistsUsernameOnly(username));
            enableForeignKeyCheck();
        } catch (Exception ex) {
            fail("failed setup");
        }

        try {
            disableForeignKeyCheck();
            OrderServiceImpl orderService = new OrderServiceImpl();
            String status = orderService.paymentSuccess(username, 100);
            enableForeignKeyCheck();
            assertEquals("Order Placement Failed!", status);
        } catch (Exception ex) {
            fail("failed verification");
        }
    }
}