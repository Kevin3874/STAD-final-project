package test;

import com.shashi.beans.CartBean;
import com.shashi.utility.DBUtil;
import org.junit.jupiter.api.*;
import com.shashi.service.impl.CartServiceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CartServiceImplTest {
    // This file contains blackbox, whitebox methods to verify that each method works as intended.
    // It also contains helper methods mostly specific to the cart tests

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

    public boolean getProductExistsProdIDOnly(String productId) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from usercart where prodid=?");

        ps.setString(1, productId);

        ResultSet rs = ps.executeQuery();

        int num = getResultSetSize(rs);

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return num > 0;
    }

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

    public boolean getProductExists(String userId, String productId) throws Exception {
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = con.prepareStatement("select * from usercart where username=? and prodid=?");

        ps.setString(1, userId);
        ps.setString(2, productId);

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
    public void clearBefore() {
        clearCartResetOrder();
    }

    // Adds a new item to a cart using update with null username
    @Test
    public void testUpdateProductToCartNullUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(null, prodId, 1);
        assertEquals("Product Successfully Updated to Cart!", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsProdIDOnly(prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new item to a cart using update with null product id
    @Test
    public void testUpdateProductToCartNullProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(userId, null, 1);
        assertEquals("Product Successfully Updated to Cart!", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsUsernameOnly(userId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new item to a cart using update unsuccessfully with invalid username
    @Test
    public void testUpdateProductToCartInvalidUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart("abcdefg", prodId, 1);

        // cleanup and verification
        try {
            assertTrue(status.contains("Error: Cannot add or update a child row"));
            assertFalse(getProductExistsUsernameOnly("abcdefg"));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new item to a cart using update unsuccessfully with invalid product id
    @Test
    public void testUpdateProductToCartInvalidProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        String status = "";

        CartServiceImpl cartService = new CartServiceImpl();
        status = cartService.updateProductToCart(userId, "abcdefg", 1);

        try {
            assertTrue(status.contains("Error: Cannot add or update a child row: a foreign key constraint fails"));
            assertFalse(getProductExistsWithQuantity(userId, "abcdefg", 1));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new item to a cart using update
    @Test
    public void testUpdateProductToCartAddNewItem() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(userId, prodId, 1);
        assertEquals("Product Successfully Updated to Cart!", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Updates an old item to a cart using update
    @Test
    public void testUpdateProductToCartUpdateExistingItem() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 3);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(userId, prodId, 1);
        assertEquals("Product Successfully Updated to Cart!", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Updates an old item to a cart using update, with multiple users
    @Test
    public void testUpdateProductToCartUpdateExistingItemMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        try {
            assertFalse(getProductExists(userId1, prodId));
            addToCart(userId1, prodId, 3);
            addToCart(userId2, prodId, 3);
            assertTrue(getProductExistsWithQuantity(userId1, prodId, 3));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(userId1, prodId, 1);
        assertEquals("Product Successfully Updated to Cart!", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsWithQuantity(userId1, prodId, 1));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 3));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Updates an old item to a cart using update, with multiple items
    @Test
    public void testUpdateProductToCartUpdateExistingItemMultipleItems() {
        String prodId1 = "P20230423082243";
        String prodId2 = "P20230423083830";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId1));
            assertFalse(getProductExists(userId, prodId2));
            addToCart(userId, prodId1, 3);
            addToCart(userId, prodId2, 3);
            assertTrue(getProductExistsWithQuantity(userId, prodId1, 3));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(userId, prodId1, 1);
        assertEquals("Product Successfully Updated to Cart!", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsWithQuantity(userId, prodId1, 1));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 3));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Updates an old item to a cart using update, but using a negative quantity
    @Test
    public void testUpdateProductToCartUpdateExistingItemNegativeQuantity() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 3);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(userId, prodId, -1);
        assertEquals("Failed to Add into Cart", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsWithQuantity(userId, prodId, 3));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Removes an old item to a cart using update
    @Test
    public void testUpdateProductToCartRemoveExistingItem() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 3);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(userId, prodId, 0);
        assertEquals("Product Successfully Updated in Cart!", status);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Removes an old item to a cart using update with multiple users
    @Test
    public void testUpdateProductToCartRemoveExistingItemMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        try {
            assertFalse(getProductExists(userId1, prodId));
            addToCart(userId1, prodId, 3);
            addToCart(userId2, prodId, 3);
            assertTrue(getProductExistsWithQuantity(userId1, prodId, 3));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(userId1, prodId, 0);
        assertEquals("Product Successfully Updated in Cart!", status);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId1, prodId));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 3));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Removes an old item to a cart using update with multiple items
    @Test
    public void testUpdateProductToCartRemoveExistingItemMultipleItems() {
        String prodId1 = "P20230423082243";
        String prodId2 = "P20230423083830";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId1));
            assertFalse(getProductExists(userId, prodId2));
            addToCart(userId, prodId1, 3);
            addToCart(userId, prodId2, 3);
            assertTrue(getProductExistsWithQuantity(userId, prodId1, 3));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.updateProductToCart(userId, prodId1, 0);
        assertEquals("Product Successfully Updated in Cart!", status);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId, prodId1));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 3));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Adds a new item to a cart with null username
    @Test
    public void testAddProductToCartNullUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.addProductToCart(null, prodId, 1);
        assertEquals("Failed to Add into Cart", status);

        // cleanup and verification
        try {
            assertFalse(getProductExistsProdIDOnly(prodId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds a new item to a cart with null product id
    @Test
    public void testAddProductToCartNullProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.addProductToCart(userId, null, 1);
        assertEquals("Failed to Add into Cart", status);

        // cleanup and verification
        try {
            assertFalse(getProductExistsUsernameOnly(userId));
        } catch (Exception ex) {
            fail("failed to verify");
        }
    }

    // Adds an item to cart unsuccessfully (addProductToCart operates on existing cart items)
    @Test
    public void testAddProductToCartNoItemFound() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.addProductToCart(userId, prodId, 1);
        assertEquals("Failed to Add into Cart", status);
        // cleanup and verification
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Adds an item to cart successfully (addProductToCart operates on existing items)
    @Test
    public void testAddProductToCartItemFoundSufficientQuantity() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 3);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.addProductToCart(userId, prodId, 1);
        assertEquals("Product Successfully Updated to Cart!", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsWithQuantity(userId, prodId, 4));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Adds an item to cart unsuccessfully due to low stock (addProductToCart operates on existing items)
    @Test
    public void testAddProductToCartItemFoundInsufficientQuantity() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 1001);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 1001));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.addProductToCart(userId, prodId, 1001);
        assertEquals("Only 1000 no of APPLE iPhone 13 Pro (Graphite, 512 GB) are available in the shop! So we are adding only 1000 no of that item into Your Cart<br/>Later, We Will Mail You when APPLE iPhone 13 Pro (Graphite, 512 GB) will be available into the Store!", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsWithQuantity(userId, prodId, 1000));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Adds a new item to a cart unsuccessfully with invalid username
    @Test
    public void testAddProductToCartInvalidUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        String status = "";

        CartServiceImpl cartService = new CartServiceImpl();
        status = cartService.addProductToCart("abcdefg", prodId, 1);

        try {
            assertEquals("Failed to Add into Cart", status);
            assertFalse(getProductExists("abcdefg", prodId));
        } catch (Exception e) {
            fail("failed to clean up");
        }
    }

    // Adds a new item to a cart unsuccessfully with invalid product id
    @Test
    public void testAddProductToCartInvalidProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        String status = "";

        try {
            CartServiceImpl cartService = new CartServiceImpl();
            status = cartService.addProductToCart(userId, "abcdefg", 1);
        } catch (Exception ex) {
            assert (true);
        }
        assertEquals("Failed to Add into Cart", status);
    }

    // Retrieves all cart items when username is null
    @Test
    public void testGetAllCartItemsNullUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        List<CartBean> list = cartService.getAllCartItems(null);
        assertTrue(list.isEmpty());
    }

    // Retrieves all cart items when username is invalid
    @Test
    public void testGetAllCartItemsInvalidUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        List<CartBean> list = cartService.getAllCartItems("abcdefg");
        assertTrue(list.isEmpty());
    }

    // Retrieves all cart items when none exist
    @Test
    public void testGetAllCartItemsEmptyCart() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        List<CartBean> list = cartService.getAllCartItems(userId);
        assertTrue(list.isEmpty());
    }

    // Retrieves all cart items when some exist
    @Test
    public void testGetAllCartItemsNonEmptyCart() {
        String prodId1 = "P20230423082243";
        String prodId2 = "P20230423083830";
        String prodId3 = "P20230423084143";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId1));
            assertFalse(getProductExists(userId, prodId2));
            assertFalse(getProductExists(userId, prodId3));
            addToCart(userId, prodId1, 3);
            addToCart(userId, prodId2, 2);
            addToCart(userId, prodId3, 1);
            assertTrue(getProductExistsWithQuantity(userId, prodId1, 3));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 2));
            assertTrue(getProductExistsWithQuantity(userId, prodId3, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        List<CartBean> list = cartService.getAllCartItems(userId);
        assertTrue(list.size() == 3);
        boolean has1 = false;
        boolean has2 = false;
        boolean has3 = false;
        for (CartBean cartBean : list) {
            if (cartBean.getProdId().equals(prodId1) && cartBean.getUserId().equals(userId) && cartBean.getQuantity() == 3) {
                has1 = true;
            } else if (cartBean.getProdId().equals(prodId2) && cartBean.getUserId().equals(userId) && cartBean.getQuantity() == 2) {
                has2 = true;
            } else if (cartBean.getProdId().equals(prodId3) && cartBean.getUserId().equals(userId) && cartBean.getQuantity() == 1) {
                has3 = true;
            }
        }
        assertTrue(has1 && has2 && has3);
    }

    // Retrieves all cart items when some exist for multiple users
    @Test
    public void testGetAllCartItemsNonEmptyCartMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        try {
            assertFalse(getProductExists(userId1, prodId));
            assertFalse(getProductExists(userId2, prodId));
            addToCart(userId1, prodId, 3);
            addToCart(userId2, prodId, 2);
            assertTrue(getProductExistsWithQuantity(userId1, prodId, 3));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 2));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        List<CartBean> list = cartService.getAllCartItems(userId1);
        assertEquals(1, list.size());
        for (CartBean cartBean : list) {
            if (cartBean.getProdId().equals(prodId) && cartBean.getUserId().equals(userId1) && cartBean.getQuantity() == 3) {
                continue;
            } else {
                fail("Invalid entry");
            }
        }
    }

    // Retrieves sum of all cart item quantities when none exist
    @Test
    public void testGetCartCountEmptyCart() {
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartCount(userId);
        assertEquals(0, sum);
    }

    // Retrieves sum of all cart item quantities when some exist
    @Test
    public void testGetCartCountNonEmptyCart() {
        String prodId1 = "P20230423082243";
        String prodId2 = "P20230423083830";
        String prodId3 = "P20230423084143";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId1));
            assertFalse(getProductExists(userId, prodId2));
            assertFalse(getProductExists(userId, prodId3));
            addToCart(userId, prodId1, 3);
            addToCart(userId, prodId2, 2);
            addToCart(userId, prodId3, 1);
            assertTrue(getProductExistsWithQuantity(userId, prodId1, 3));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 2));
            assertTrue(getProductExistsWithQuantity(userId, prodId3, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartCount(userId);
        assertEquals(sum, 6);
    }

    // Retrieves sum of all cart item quantities when some exist for multiple users
    @Test
    public void testGetCartCountNonEmptyCartMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        try {
            assertFalse(getProductExists(userId1, prodId));
            assertFalse(getProductExists(userId2, prodId));
            addToCart(userId1, prodId, 3);
            addToCart(userId2, prodId, 2);
            assertTrue(getProductExistsWithQuantity(userId1, prodId, 3));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 2));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartCount(userId1);
        assertEquals(sum, 3);
    }

    // Retrieves sum of all cart item quantities when a null value exists in a column
    @Test
    public void testGetCartCountNullColumn() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            Connection con = DBUtil.provideConnection();
            PreparedStatement ps = con.prepareStatement("insert into usercart values(\"guest@gmail.com\",\"P20230423082243\",NULL)");
            int k = ps.executeUpdate();
            if (k == 0) {
                throw new Exception("did not insert successfully");
            }
            assertTrue(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartCount(userId);
        assertEquals(sum, 0);
    }

    // Retrieves sum of all cart item quantities when username is invalid
    @Test
    public void testGetCartCountInvalid() {
        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartCount("abcdefg");
        assertEquals(sum, 0);
    }

    // Retrieves sum of all cart item quantities when username is null
    @Test
    public void testGetCartCountNull() {
        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartCount(null);
        assertEquals(sum, 0);
    }

    // Retrieves sum of all cart item quantities for a certain product when none exist
    @Test
    public void testGetProductCountEmptyCart() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getProductCount(userId, prodId);
        assertEquals(0, sum);
    }

    // Retrieves sum of all cart item quantities for a certain product when some exist
    @Test
    public void testGetProductCountNonEmptyCart() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 3);
            addToCart(userId, prodId, 2);
            addToCart(userId, prodId, 1);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 3));
            assertTrue(getProductExistsWithQuantity(userId, prodId, 2));
            assertTrue(getProductExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getProductCount(userId, prodId);
        assertEquals(6, sum);
    }

    // Retrieves sum of all cart item quantities for a certain product when some exist for multiple users
    @Test
    public void testGetProductCountNonEmptyCartMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        try {
            assertFalse(getProductExists(userId1, prodId));
            assertFalse(getProductExists(userId2, prodId));
            addToCart(userId1, prodId, 3);
            addToCart(userId2, prodId, 2);
            assertTrue(getProductExistsWithQuantity(userId1, prodId, 3));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 2));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getProductCount(userId1, prodId);
        assertEquals(3, sum);
    }

    // Retrieves sum of all cart item quantities for a certain product when a null value exists in a column
    @Test
    public void testGetProductCountNullColumn() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            Connection con = DBUtil.provideConnection();
            PreparedStatement ps = con.prepareStatement("insert into usercart values(\"guest@gmail.com\",\"P20230423082243\",NULL)");
            int k = ps.executeUpdate();
            if (k == 0) {
                throw new Exception("did not insert successfully");
            }
            assertTrue(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getProductCount(userId, prodId);
        assertEquals(sum, 0);
    }

    // Retrieves sum of all cart item quantities for a certain product when username is invalid
    @Test
    public void testGetProductCountUsernameInvalid() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getProductCount("abcdefg", prodId);
        assertEquals(sum, 0);
    }

    // Retrieves sum of all cart item quantities for a certain product when username is null
    @Test
    public void testGetProductCountUsernameNull() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getProductCount("abcdefg", prodId);
        assertEquals(sum, 0);
    }

    // Retrieves sum of all cart item quantities for a certain product when prodid is invalid
    @Test
    public void testGetProductCountProductIDInvalid() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getProductCount(userId, "abcdefg");
        assertEquals(sum, 0);
    }

    // Retrieves cart item quantity of an item when it exists
    @Test
    public void testGetCartItemCountNonEmptyCart() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 3);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int count = cartService.getCartItemCount(userId, prodId);
        assertEquals(3, count);
    }

    // Retrieves cart item quantity of an item when it exists for multiple users
    @Test
    public void testGetCartItemCountNonEmptyCartMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        try {
            assertFalse(getProductExists(userId1, prodId));
            assertFalse(getProductExists(userId2, prodId));
            addToCart(userId1, prodId, 3);
            addToCart(userId2, prodId, 2);
            assertTrue(getProductExistsWithQuantity(userId1, prodId, 3));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 2));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int count = cartService.getCartItemCount(userId1, prodId);
        assertEquals(3, count);
    }

    // Retrieves cart item quantity of an item when cart is empty
    @Test
    public void testGetCartItemCountEmptyCart() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int count = cartService.getCartItemCount(userId, prodId);
        assertEquals(0, count);
    }

    // Retrieves cart item quantity of an item when a null value exists in a column
    @Test
    public void testGetCartItemCountNullColumn() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            Connection con = DBUtil.provideConnection();
            PreparedStatement ps = con.prepareStatement("insert into usercart values(\"guest@gmail.com\",\"P20230423082243\",NULL)");
            int k = ps.executeUpdate();
            if (k == 0) {
                throw new Exception("did not insert successfully");
            }
            assertTrue(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartItemCount(userId, prodId);
        assertEquals(sum, 0);
    }

    // Retrieves cart item quantity of an item when username is invalid
    @Test
    public void testGetCartItemCountUsernameInvalid() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartItemCount("abcdefg", prodId);
        assertEquals(sum, 0);
    }

    // Retrieves cart item quantity of an item when username is null
    @Test
    public void testGetCartItemCountUsernameNull() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartItemCount(null, prodId);
        assertEquals(sum, 0);
    }

    // Retrieves cart item quantity of an item when prodid is invalid
    @Test
    public void testGetCartItemCountProductIDInvalid() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartItemCount(userId, "abcdefg");
        assertEquals(sum, 0);
    }

    // Retrieves cart item quantity of an item when prodid is null
    @Test
    public void testGetCartItemCountProductIDNull() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        CartServiceImpl cartService = new CartServiceImpl();
        int sum = cartService.getCartItemCount(userId, null);
        assertEquals(sum, 0);
    }

    // Deletes a cart item of an id when it exists and there are more than one
    @Test
    public void testRemoveProductFromCartGreaterThanOne() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 3);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 3));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.removeProductFromCart(userId, prodId);
        assertEquals("Product Successfully removed from the Cart!", status);

        // cleanup and verification
        try {
            assertTrue(getProductExistsWithQuantity(userId, prodId, 2));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Deletes a cart item of an id when it exists and there are more than one for multiple users
    @Test
    public void testRemoveProductFromCartGreaterThanOneMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        try {
            assertFalse(getProductExists(userId1, prodId));
            assertFalse(getProductExists(userId2, prodId));
            addToCart(userId1, prodId, 1);
            addToCart(userId2, prodId, 1);
            assertTrue(getProductExistsWithQuantity(userId1, prodId, 1));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.removeProductFromCart(userId1, prodId);
        assertEquals("Product Successfully removed from the Cart!", status);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId1, prodId));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 1));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Deletes a cart item of an id when it exists and there are more than one for multiple items
    @Test
    public void testRemoveProductFromCartGreaterThanOneMultipleItems() {
        String prodId1 = "P20230423082243";
        String prodId2 = "P20230423083830";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId1));
            assertFalse(getProductExists(userId, prodId2));
            addToCart(userId, prodId1, 1);
            addToCart(userId, prodId2, 1);
            assertTrue(getProductExistsWithQuantity(userId, prodId1, 1));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.removeProductFromCart(userId, prodId1);
        assertEquals("Product Successfully removed from the Cart!", status);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId, prodId1));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 1));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Deletes a cart item of an id when it exists and there is one
    @Test
    public void testRemoveProductFromCartEqualToOne() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 1);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.removeProductFromCart(userId, prodId);
        assertEquals("Product Successfully removed from the Cart!", status);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Deletes a cart item of an id when cart is empty
    @Test
    public void testRemoveProductFromCartEmpty() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.removeProductFromCart(userId, prodId);
        assertEquals("Product Not Available in the cart!", status);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Deletes a cart item of an id unsuccessfully when username is invalid
    @Test
    public void testRemoveProductFromCartInvalidUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.removeProductFromCart("abcdefg", prodId);
        assertEquals("Product Not Available in the cart!", status);
    }

    // Deletes a cart item of an id unsuccessfully when product id is invalid
    @Test
    public void testRemoveProductFromCartInvalidProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.removeProductFromCart(userId, "abcdefg");
        assertEquals("Product Not Available in the cart!", status);
    }

    // Deletes a cart item of an id when username is null
    @Test
    public void testRemoveProductFromCartNullUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.removeProductFromCart(null, prodId);
        assertEquals("Product Not Available in the cart!", status);
    }

    // Deletes a cart item of an id when product id is null
    @Test
    public void testRemoveProductFromCartNullProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        String status = cartService.removeProductFromCart(userId, null);
        assertEquals("Product Not Available in the cart!", status);
    }

    // Deletes all cart item of an id when it exists
    @Test
    public void testRemoveAProductExists() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
            addToCart(userId, prodId, 1);
            assertTrue(getProductExistsWithQuantity(userId, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        boolean flag = cartService.removeAProduct(userId, prodId);
        assertTrue(flag);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Deletes all cart item of an id when it exists for multiple users
    @Test
    public void testRemoveAProductExistsMultipleUsers() {
        String prodId = "P20230423082243";
        String userId1 = "guest@gmail.com";
        String userId2 = "admin@gmail.com";
        try {
            assertFalse(getProductExists(userId1, prodId));
            assertFalse(getProductExists(userId2, prodId));
            addToCart(userId1, prodId, 1);
            addToCart(userId2, prodId, 1);
            assertTrue(getProductExistsWithQuantity(userId1, prodId, 1));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        boolean flag = cartService.removeAProduct(userId1, prodId);
        assertTrue(flag);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId1, prodId));
            assertTrue(getProductExistsWithQuantity(userId2, prodId, 1));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Deletes all cart item of an id when it exists for multiple products
    @Test
    public void testRemoveAProductExistsMultipleItems() {
        String prodId1 = "P20230423082243";
        String prodId2 = "P20230423083830";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId1));
            assertFalse(getProductExists(userId, prodId2));
            addToCart(userId, prodId1, 1);
            addToCart(userId, prodId2, 1);
            assertTrue(getProductExistsWithQuantity(userId, prodId1, 1));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 1));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        boolean flag = cartService.removeAProduct(userId, prodId1);
        assertTrue(flag);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId, prodId1));
            assertTrue(getProductExistsWithQuantity(userId, prodId2, 1));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Deletes all cart item of an id when it does not exist
    @Test
    public void testRemoveAProductNonExistent() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed setup");
        }

        CartServiceImpl cartService = new CartServiceImpl();
        boolean flag = cartService.removeAProduct(userId, prodId);
        assertFalse(flag);

        // cleanup and verification
        try {
            assertFalse(getProductExists(userId, prodId));
        } catch (Exception ex) {
            fail("failed to clean up");
        }
    }

    // Deletes all cart item of an id when username is null
    @Test
    public void testRemoveAProductNullUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        boolean status = cartService.removeAProduct(null, prodId);
        assertFalse(status);
    }

    // Deletes all cart item of an id when product id is null
    @Test
    public void testRemoveAProductNullProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        boolean status = cartService.removeAProduct(userId, null);
        assertFalse(status);
    }

    // Deletes all cart item of an id unsuccessfully when username is invalid
    @Test
    public void testRemoveAProductInvalidUsername() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        boolean status = cartService.removeAProduct("abcdefg", prodId);
        assertFalse(status);
    }

    // Deletes all cart item of an id unsuccessfully when product id is invalid
    @Test
    public void testRemoveAProductInvalidProductID() {
        String prodId = "P20230423082243";
        String userId = "guest@gmail.com";

        CartServiceImpl cartService = new CartServiceImpl();
        boolean status = cartService.removeAProduct(userId, "abcdefg");
        assertFalse(status);
    }
}