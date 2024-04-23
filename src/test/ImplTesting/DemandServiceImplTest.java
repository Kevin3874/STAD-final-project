package test;

import com.shashi.beans.DemandBean;
import com.shashi.service.impl.DemandServiceImpl;
import com.shashi.utility.DBUtil;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DemandServiceImplTest {

    // This file contains blackbox, whitebox methods to verify that each method works as intended.
    // It also contains helper methods mostly specific to the demand tests

    // IMPORTANT!! must have MYSQL Server/Workbench installed, set up, and connected. Contact Jiashu
    // if you have questions.
    // IMPORTANT!! many tests assume the default setup has been completed and unaltered. Tests also try
    // to restore the table to the default state after completion.
    // On a first startup, these tests may  fail due to SQL errors: I resolved these by running the
    // creator's startup SQL query script individually, as the product commit crashed my MySQL Workbench.

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

    public boolean getDemandExistsProdIDOnly(String productId) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from user_demand where prodid=?");

        ps.setString(1, productId);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }

    public boolean getDemandExistsUsernameOnly(String userID) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from user_demand where username=?");

        ps.setString(1, userID);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }

    public boolean getDemandExists(String userId, String productId) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from user_demand where username=? and prodid=?");

        ps.setString(1, userId);
        ps.setString(2, productId);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }

    public boolean getDemandExistsWithQuantity(String userId, String productId, int quantity) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from user_demand where username=? and prodid=? and quantity=?");

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

    public void addToUserDemands(String userId, String productId, int quantity) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("insert into user_demand values(?,?,?)");

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

    @BeforeEach
    public void deleteDemands() throws SQLException {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM user_demand");

        ps.execute();

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
    }

    @AfterEach
    public void cleanup() throws SQLException {
        deleteDemands();
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

    // Adds a new demand unsuccessfully with a null username
    @Test
    public void testAddProductNullUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct(null, prodId, 1);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExistsProdIDOnly(prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new demand unsuccessfully with null product id
    @Test
    public void testAddProductNullProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct(userId, null, 1);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExistsUsernameOnly(userId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new demand unsuccessfully with an invalid username
    @Test
    public void testAddProductInvalidUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct("abcdefg", prodId, 1);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExistsProdIDOnly(prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new demand unsuccessfully with an invalid product id
    @Test
    public void testAddProductInvalidProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct(userId, "abcdefg", 1);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExistsUsernameOnly(userId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new demand
    @Test
    public void testAddProductAddNewDemand() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct(userId, prodId, 1);
        assertTrue(status);

        // cleanup and verification
        try {
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new duplicate demand unsuccessfully
    @Test
    public void testAddProductAddDuplicateDemand() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getDemandExists(userId, prodId));
            addToUserDemands(userId, prodId, 1);
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct(userId, prodId, 3);
        // Detects a fault we explain in the doc
        assertFalse(status);

        // cleanup and verification
        try {
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
            assertFalse(getDemandExistsWithQuantity(userId, prodId, 3));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Remove a demand unsuccessfully with a null username
    @Test
    public void testRemoveProductNullUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getDemandExists(userId, prodId));
            addToUserDemands(userId, prodId, 1);
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.removeProduct(null, prodId);
        assertTrue(status);

        // cleanup and verification
        try {
            assertTrue(getDemandExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Removes a demand unsuccessfully with null product id
    @Test
    public void testRemoveProductNullProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        try {
            assertFalse(getDemandExists(userId, prodId));
            addToUserDemands(userId, prodId, 1);
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.removeProduct(userId, null);
        assertTrue(status);

        // cleanup and verification
        try {
            assertTrue(getDemandExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Removes a demand unsuccessfully with an invalid username
    @Test
    public void testRemoveProductInvalidUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        try {
            assertFalse(getDemandExists(userId, prodId));
            addToUserDemands(userId, prodId, 1);
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.removeProduct("abcdefg", prodId);
        assertTrue(status);

        // cleanup and verification
        try {
            assertTrue(getDemandExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Removes a demand unsuccessfully with an invalid product id
    @Test
    public void testRemoveProductInvalidProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        try {
            assertFalse(getDemandExists(userId, prodId));
            addToUserDemands(userId, prodId, 1);
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.removeProduct(userId, "abcdefg");
        assertTrue(status);

        // cleanup and verification
        try {
            assertTrue(getDemandExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Removes a demand
    @Test
    public void testRemoveProduct() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        try {
            assertFalse(getDemandExists(userId, prodId));
            addToUserDemands(userId, prodId, 1);
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.removeProduct(userId, prodId);
        assertTrue(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Removes a demand for multiple users
    @Test
    public void testRemoveProductMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";

        try {
            assertFalse(getDemandExists(userId1, prodId));
            assertFalse(getDemandExists(userId2, prodId));
            addToUserDemands(userId1, prodId, 1);
            addToUserDemands(userId2, prodId, 1);
            assertTrue(getDemandExistsWithQuantity(userId1, prodId, 1));
            assertTrue(getDemandExistsWithQuantity(userId2, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.removeProduct(userId1, prodId);
        assertTrue(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExists(userId1, prodId));
            assertTrue(getDemandExistsWithQuantity(userId2, prodId, 1));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Removes a demand for multiple items
    @Test
    public void testRemoveProductMultipleItems() {
        String prodId1 = "P20230423082243";
        String prodId2 = "P20230423083830";
        String userId = "guest@gmail.com";

        try {
            assertFalse(getDemandExists(userId, prodId1));
            assertFalse(getDemandExists(userId, prodId2));
            addToUserDemands(userId, prodId1, 1);
            addToUserDemands(userId, prodId2, 1);
            assertTrue(getDemandExistsWithQuantity(userId, prodId1, 1));
            assertTrue(getDemandExistsWithQuantity(userId, prodId2, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.removeProduct(userId, prodId1);
        assertTrue(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExists(userId, prodId1));
            assertTrue(getDemandExistsWithQuantity(userId, prodId2, 1));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Removes a nonexistent demand
    @Test
    public void testRemoveProductNonExistent() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        try {
            assertFalse(getDemandExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.removeProduct(userId, prodId);
        assertTrue(status);
    }

    // Adds a new demand bean unsuccessfully with a null username
    @Test
    public void testAddProductBeanNullUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandBean bean = new DemandBean();
        bean.setUserName(null);
        bean.setProdId(prodId);
        bean.setDemandQty(1);
        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExistsProdIDOnly(prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new demand bean unsuccessfully with null product id
    @Test
    public void testAddProductBeanNullProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandBean bean = new DemandBean();
        bean.setUserName(userId);
        bean.setProdId(null);
        bean.setDemandQty(1);
        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExistsUsernameOnly(userId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new demand bean unsuccessfully with an invalid username
    @Test
    public void testAddProductBeanInvalidUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandBean bean = new DemandBean();
        bean.setUserName("abcdefg");
        bean.setProdId(prodId);
        bean.setDemandQty(1);
        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExistsProdIDOnly(prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new demand bean unsuccessfully with an invalid product id
    @Test
    public void testAddProductBeanInvalidProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandBean bean = new DemandBean();
        bean.setUserName(userId);
        bean.setProdId("abcdefg");
        bean.setDemandQty(1);
        DemandServiceImpl demandService = new DemandServiceImpl();
        boolean status = demandService.addProduct(bean);
        assertFalse(status);

        // cleanup and verification
        try {
            assertFalse(getDemandExistsUsernameOnly(userId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new demand
    @Test
    public void testAddProductBeanAddNewDemand() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        DemandServiceImpl demandService = new DemandServiceImpl();
        DemandBean bean = new DemandBean();
        bean.setUserName(userId);
        bean.setProdId(prodId);
        bean.setDemandQty(1);
        boolean status = demandService.addProduct(bean);
        assertTrue(status);

        // cleanup and verification
        try {
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new duplicate demand unsuccessfully
    @Test
    public void testAddProductBeanAddDuplicateDemand() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getDemandExists(userId, prodId));
            addToUserDemands(userId, prodId, 1);
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        DemandServiceImpl demandService = new DemandServiceImpl();
        DemandBean bean = new DemandBean();
        bean.setUserName(userId);
        bean.setProdId(prodId);
        bean.setDemandQty(3);
        boolean status = demandService.addProduct(bean);
        // Detects a fault we explain in the doc
        assertFalse(status);

        // cleanup and verification
        try {
            assertTrue(getDemandExistsWithQuantity(userId, prodId, 1));
            assertFalse(getDemandExistsWithQuantity(userId, prodId, 3));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Returns demands with invalid product id
    @Test
    public void testHaveDemandedInvalidProductID() {
        DemandServiceImpl demandService = new DemandServiceImpl();
        List<DemandBean> list = demandService.haveDemanded("abcdefg");
        assertTrue(list.isEmpty());
    }

    // Returns demands with null product id
    @Test
    public void testHaveDemandedNullProductID() {
        DemandServiceImpl demandService = new DemandServiceImpl();
        List<DemandBean> list = demandService.haveDemanded(null);
        assertTrue(list.isEmpty());
    }

    // Returns demands of an empty demands list
    @Test
    public void testHaveDemandedEmpty() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getDemandExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }
        DemandServiceImpl demandService = new DemandServiceImpl();
        List<DemandBean> list = demandService.haveDemanded(prodId);
        assertTrue(list.isEmpty());
    }

    // Returns demands of a non-empty demands list, where a single entry exists
    @Test
    public void testHaveDemandedSingleEntry() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getDemandExists(userId, prodId));
            addToUserDemands(userId, prodId, 2);
        } catch (Exception ex) {
            fail("failed setup");
        }
        DemandServiceImpl demandService = new DemandServiceImpl();
        List<DemandBean> list = demandService.haveDemanded(prodId);
        assertEquals(1, list.size());

        DemandBean bean = list.get(0);
        assertEquals(prodId, bean.getProdId());
        assertEquals(userId, bean.getUserName());
        assertEquals(2, bean.getDemandQty());
    }

    // Returns demands of a non-empty demands list, where multiple entries exist for multiple products
    @Test
    public void testHaveDemandedMultipleItems() {
        String prodId1 = "P20230423082243";
        String prodId2 = "P20230423083830";
        String prodId3 = "P20230423084143";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getDemandExists(userId, prodId1));
            assertFalse(getDemandExists(userId, prodId2));
            assertFalse(getDemandExists(userId, prodId3));
            addToUserDemands(userId, prodId1, 3);
            addToUserDemands(userId, prodId2, 1);
            addToUserDemands(userId, prodId3, 5);
            assertTrue(getDemandExistsWithQuantity(userId, prodId1, 3));
            assertTrue(getDemandExistsWithQuantity(userId, prodId2, 1));
            assertTrue(getDemandExistsWithQuantity(userId, prodId3, 5));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            fail("failed setup");
        }
        DemandServiceImpl demandService = new DemandServiceImpl();
        List<DemandBean> list1 = demandService.haveDemanded(prodId1);
        assertEquals(1, list1.size());
        DemandBean bean = list1.get(0);
        assertTrue(prodId1.equals(bean.getProdId()) && userId.equals(bean.getUserName()) && bean.getDemandQty() == 3);

        List<DemandBean> list2 = demandService.haveDemanded(prodId2);
        assertEquals(1, list2.size());
        bean = list2.get(0);
        assertTrue(prodId2.equals(bean.getProdId()) && userId.equals(bean.getUserName()) && bean.getDemandQty() == 1);

        List<DemandBean> list3 = demandService.haveDemanded(prodId3);
        assertEquals(1, list3.size());
        bean = list3.get(0);
        assertTrue(prodId3.equals(bean.getProdId()) && userId.equals(bean.getUserName()) && bean.getDemandQty() == 5);
    }

    // Returns demands of a non-empty demands list, where multiple entries exist for multiple users
    @Test
    public void testHaveDemandedMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        try {
            assertFalse(getDemandExists(userId1, prodId));
            assertFalse(getDemandExists(userId2, prodId));
            addToUserDemands(userId1, prodId, 1);
            addToUserDemands(userId2, prodId, 1);
            assertTrue(getDemandExistsWithQuantity(userId1, prodId, 1));
            assertTrue(getDemandExistsWithQuantity(userId2, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }
        DemandServiceImpl demandService = new DemandServiceImpl();
        List<DemandBean> list = demandService.haveDemanded(prodId);
        assertEquals(2, list.size());
        for (DemandBean bean : list) {
            if (prodId.equals(bean.getProdId()) && userId1.equals(bean.getUserName()) && bean.getDemandQty() == 1) {
                assert(true);
            } else if (prodId.equals(bean.getProdId()) && userId2.equals(bean.getUserName()) && bean.getDemandQty() == 1) {
                assert(true);
            } else {
                fail("invalid entry");
            }
        }
    }
}