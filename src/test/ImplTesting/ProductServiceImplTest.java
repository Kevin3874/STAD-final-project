package test;

import com.shashi.beans.ProductBean;
import com.shashi.constants.IUserConstants;
import com.shashi.service.impl.ProductServiceImpl;
import com.shashi.utility.DBUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

public class ProductServiceImplTest {

  public boolean addProduct(String prodId, String prodName, String prodType, String prodInfo, double prodPrice, int prodQuantity, InputStream prodImage) throws Exception {
    boolean prod = false;

    Connection con = DBUtil.provideConnection();

    PreparedStatement ps = con.prepareStatement("insert into product values(?,?,?,?,?,?,?)");
    ps.setString(1, prodId);
    ps.setString(2, prodName);
    ps.setString(3, prodType);
    ps.setString(4, prodInfo);
    ps.setDouble(5, prodPrice);
    ps.setInt(6, prodQuantity);
    ps.setBlob(7, prodImage);

    int k = ps.executeUpdate();

    if (k <= 0) {
      throw new Exception("did not insert successfully");
    }

    DBUtil.closeConnection(con);
    DBUtil.closeConnection(ps);

    return prod;
  }

  public boolean getProductExists(String prodId) throws Exception {
    boolean prod = false;

    Connection con = DBUtil.provideConnection();

    PreparedStatement ps = null;
    ResultSet rs = null;

    ps = con.prepareStatement("select * from product where pid=?");

    ps.setString(1, prodId);
    rs = ps.executeQuery();

    if (rs.next())
      prod = true;

    DBUtil.closeConnection(con);
    DBUtil.closeConnection(ps);
    DBUtil.closeConnection(rs);

    return prod;
  }

  public boolean updateProduct(String prodId, String prodName, String prodType, String prodInfo, double prodPrice, int prodQuantity, InputStream prodImage) throws Exception {
    boolean prod = false;

    Connection con = DBUtil.provideConnection();

    PreparedStatement ps = null;
    ResultSet rs = null;

    ps = con.prepareStatement(
        "update product set pname=?,ptype=?,pinfo=?,pprice=?,pquantity=?,image=? where pid=?");

    ps.setString(1, prodName);
    ps.setString(2, prodType);
    ps.setString(3, prodInfo);
    ps.setDouble(4, prodPrice);
    ps.setInt(5, prodQuantity);
    ps.setBlob(6, prodImage);
    ps.setString(7,prodId);

    rs = ps.executeQuery();

    if (rs.next())
      prod = true;

    DBUtil.closeConnection(con);
    DBUtil.closeConnection(ps);
    DBUtil.closeConnection(rs);

    return prod;
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

  public boolean addDemandForProduct(String userId, String prodId, int demandQty) throws Exception {
    boolean flag = false;

    Connection con = DBUtil.provideConnection();

    PreparedStatement ps = null;
    PreparedStatement ps2 = null;
    ResultSet rs = null;

    PreparedStatement ps1 = con.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
    ps1.execute();

    ps = con.prepareStatement("select * from user_demand where username=? and prodid=?");

    ps.setString(1, userId);
    ps.setString(2, prodId);

    rs = ps.executeQuery();

    if (rs.next()) {
      flag = true;
    } else {
      ps2 = con.prepareStatement("insert into  user_demand values(?,?,?)");
      ps2.setString(1, userId);
      ps2.setString(2, prodId);
      ps2.setInt(3, demandQty);
      int k = ps2.executeUpdate();
      if (k > 0)
        flag = true;
    }

    PreparedStatement ps3 = con.prepareStatement("SET FOREIGN_KEY_CHECKS=1");
    ps3.execute();

    DBUtil.closeConnection(con);
    DBUtil.closeConnection(ps);
    DBUtil.closeConnection(ps1);
    DBUtil.closeConnection(ps2);
    DBUtil.closeConnection(ps3);
    return flag;
  }

  @AfterEach
  public void clearProducts() {
    try {
      Connection con = DBUtil.provideConnection();

      PreparedStatement ps1 = con.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
      ps1.execute();

      PreparedStatement ps2 = con.prepareStatement("delete from product");
      ps2.execute();

      PreparedStatement ps3 = con.prepareStatement("delete from user");
      ps3.execute();

      PreparedStatement ps4 = con.prepareStatement("delete from usercart");
      ps4.execute();

      PreparedStatement ps5 = con.prepareStatement("delete from user_demand");
      ps5.execute();

      PreparedStatement ps6 = con.prepareStatement("SET FOREIGN_KEY_CHECKS=1");
      ps6.execute();

      DBUtil.closeConnection(con);
      DBUtil.closeConnection(ps1);
      DBUtil.closeConnection(ps2);
      DBUtil.closeConnection(ps3);
      DBUtil.closeConnection(ps4);
      DBUtil.closeConnection(ps5);
      DBUtil.closeConnection(ps6);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      System.out.println("FAILED TO CLEAN UP. CHECK SQL SERVER, DISABLE SAFE UPDATE MODE");
    }
  }

  @BeforeEach
  public void clearBefore() {
    clearProducts();
  }

  // test addProduct with a new product
  @Test
  public void testAddProductWithNewProduct() {
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.addProduct(prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
    assertNotNull(status);
    assertNotEquals("Product Updation Failed!", status);
  }

  // test addProduct with an existing product with same details
  @Test
  public void testAddProductWithExistingProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.addProduct(prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
    assertNotNull(status);
    assertNotEquals("Product Updation Failed!", status);
  }

  // test addProduct with null inputs
  @Test
  public void testAddProductWithNullInputs() {
    String prodName = null;
    String prodType = null;
    String prodInfo = null;
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = null;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.addProduct(prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
    });
  }

  // test addProduct with empty and zeroes as inputs
  @Test
  public void testAddProductWithEmptyAndZeroInputs() {
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 0;
    int prodQuantity = 0;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("");

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.addProduct(prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
    });
  }

  // test addProduct with negative inputs
  @Test
  public void testAddProductWithNegativeInputs() {
    String prodName = "";
    String prodType = "";
    String prodInfo = "";
    double prodPrice = -5;
    int prodQuantity = -5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.addProduct(prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
    });
  }

  // test removeProduct with an existing product
  @Test
  public void testRemoveProductWithExistingProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));

    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.removeProduct(prodId);
    assertEquals("Product Removed Successfully!", status);
  }

  // test removeProduct with an existing product
  @Test
  public void testRemoveProductWithNonexistentProduct() {
    String prodId = "12345";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.removeProduct(prodId);
    });
  }

  // test removeProduct with null input
  @Test
  public void testRemoveProductWithNullInput() {
    String prodId = null;

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.removeProduct(prodId);
    });
  }

  // test removeProduct with empty input
  @Test
  public void testRemoveProductWithEmptyInput() {
    String prodId = "";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.removeProduct(prodId);
    });
  }

  // test updateProduct with an existing product
  @Test
  public void testUpdateProductWithExistingProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductBean prodOriginal = new ProductBean();
    prodOriginal.setProdId(prodId);
    prodOriginal.setProdName(prodName);
    prodOriginal.setProdType(prodType);
    prodOriginal.setProdInfo(prodInfo);
    prodOriginal.setProdPrice(prodPrice);
    prodOriginal.setProdQuantity(prodQuantity);
    prodOriginal.setProdImage(prodImage);

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId(prodId);
    prodUpdated.setProdName("New name");
    prodUpdated.setProdType(prodType);
    prodUpdated.setProdInfo(prodInfo);
    prodUpdated.setProdPrice(prodPrice);
    prodUpdated.setProdQuantity(prodQuantity);
    prodUpdated.setProdImage(prodImage);

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProduct(prodOriginal, prodUpdated);
    assertEquals("Product Updated Successfully!", status);
  }


  // test updateProduct with different IDs
  @Test
  public void testUpdateProductWithMismatchedIDs() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductBean prodOriginal = new ProductBean();
    prodOriginal.setProdId(prodId);
    prodOriginal.setProdName(prodName);
    prodOriginal.setProdType(prodType);
    prodOriginal.setProdInfo(prodInfo);
    prodOriginal.setProdPrice(prodPrice);
    prodOriginal.setProdQuantity(prodQuantity);
    prodOriginal.setProdImage(prodImage);

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId("newID");
    prodUpdated.setProdName("New name");
    prodUpdated.setProdType(prodType);
    prodUpdated.setProdInfo(prodInfo);
    prodUpdated.setProdPrice(prodPrice);
    prodUpdated.setProdQuantity(prodQuantity);
    prodUpdated.setProdImage(prodImage);

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProduct(prodOriginal, prodUpdated);
    assertEquals("Both Products are Different, Updation Failed!", status);
  }

  // test updateProduct with no changes
  @Test
  public void testUpdateProductWithNoChanges() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductBean prodOriginal = new ProductBean();
    prodOriginal.setProdId(prodId);
    prodOriginal.setProdName(prodName);
    prodOriginal.setProdType(prodType);
    prodOriginal.setProdInfo(prodInfo);
    prodOriginal.setProdPrice(prodPrice);
    prodOriginal.setProdQuantity(prodQuantity);
    prodOriginal.setProdImage(prodImage);

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId(prodId);
    prodUpdated.setProdName(prodName);
    prodUpdated.setProdType(prodType);
    prodUpdated.setProdInfo(prodInfo);
    prodUpdated.setProdPrice(prodPrice);
    prodUpdated.setProdQuantity(prodQuantity);
    prodUpdated.setProdImage(prodImage);

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProduct(prodOriginal, prodUpdated);
    assertEquals("Product Updated Successfully!", status);
  }

  // test updateProduct with null inputs
  @Test
  public void testUpdateProductWithNullInputs() {
    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProduct(null, null);
    });
  }

  // test updateProduct with null changes
  @Test
  public void testUpdateProductWithNullUpdates() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductBean prodOriginal = new ProductBean();
    prodOriginal.setProdId(prodId);
    prodOriginal.setProdName(prodName);
    prodOriginal.setProdType(prodType);
    prodOriginal.setProdInfo(prodInfo);
    prodOriginal.setProdPrice(prodPrice);
    prodOriginal.setProdQuantity(prodQuantity);
    prodOriginal.setProdImage(prodImage);

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId(null);
    prodUpdated.setProdName(null);
    prodUpdated.setProdType(null);
    prodUpdated.setProdInfo(null);
    prodUpdated.setProdPrice(0);
    prodUpdated.setProdQuantity(0);
    prodUpdated.setProdImage(null);

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProduct(prodOriginal, prodUpdated);
    });
  }

  // test updateProduct with empty changes
  @Test
  public void testUpdateProductWithEmptyUpdates() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductBean prodOriginal = new ProductBean();
    prodOriginal.setProdId(prodId);
    prodOriginal.setProdName(prodName);
    prodOriginal.setProdType(prodType);
    prodOriginal.setProdInfo(prodInfo);
    prodOriginal.setProdPrice(prodPrice);
    prodOriginal.setProdQuantity(prodQuantity);
    prodOriginal.setProdImage(prodImage);

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId("");
    prodUpdated.setProdName("");
    prodUpdated.setProdType("");
    prodUpdated.setProdInfo("");
    prodUpdated.setProdPrice(0);
    prodUpdated.setProdQuantity(0);
    prodUpdated.setProdImage(ProductServiceImplTest.class.getResourceAsStream(""));

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProduct(prodOriginal, prodUpdated);
    });
  }

  // test updateProductPrice with an existing product
  @Test
  public void testUpdateProductPriceWithExistingProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    double updatedPrice = 5.75;

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProductPrice(prodId, updatedPrice);
    assertEquals("Price Updated Successfully!", status);
  }

  // test updateProductPrice with nonexistent product
  @Test
  public void testUpdateProductPriceWithNonexistentProduct() {
    String prodId = "12345";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    double updatedPrice = 9.99;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProductPrice(prodId, updatedPrice);
    });
  }

  // test updateProductPrice with null prodId
  @Test
  public void testUpdateProductPriceWithNullProdID() {
    String prodId = null;

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    double updatedPrice = 10.99;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProductPrice(prodId, updatedPrice);
    });
  }

  // test updateProductPrice with null prodId
  @Test
  public void testUpdateProductPriceWithEmptyProdId() {
    String prodId = "";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    double updatedPrice = 0;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProductPrice(prodId, updatedPrice);
    });
  }

  // test updateProductPrice with negative price
  @Test
  public void testUpdateProductPriceWithNegativePrice() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    double updatedPrice = -10;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProductPrice(prodId, updatedPrice);
    });
  }

  // test getProductDetails with an existing product
  @Test
  public void testGetProductDetailsWithExistingProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    ProductBean res = productService.getProductDetails(prodId);
    assertEquals(prodId, res.getProdId());
    assertEquals(prodName, res.getProdName());
    assertEquals(prodType, res.getProdType());
    assertEquals(prodInfo, res.getProdInfo());
    assertEquals(prodPrice, res.getProdPrice());
    assertEquals(prodQuantity, res.getProdQuantity());
    assertEquals(prodImage, res.getProdImage());
  }

  // test getProductDetails with nonexistent product
  @Test
  public void testGetProductDetailsWithNonExistentProduct() {
    String prodId = "12345";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ProductBean res = productService.getProductDetails(prodId);
    });
  }

  // test getProductDetails with null input
  @Test
  public void testGetProductDetailsWithNullInput() {
    String prodId = null;

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ProductBean res = productService.getProductDetails(prodId);
    });
  }

  // test getProductDetails with empty input
  @Test
  public void testGetProductDetailsWithEmptyInput() {
    String prodId = "";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ProductBean res = productService.getProductDetails(prodId);
    });
  }

  // test getProductDetails after updating price
  @Test
  public void testGetProductDetailsAfterUpdating() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    String newProdName = "Light Blue Jeans";
    String newProdType = "Jeans";
    String newProdInfo = "Light wash jeans";
    double newProdPrice = 7.99;
    int newProdQuantity = 4;

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      updateProduct(prodId, newProdName, newProdType, newProdInfo, newProdPrice, newProdQuantity, prodImage);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    ProductBean res = productService.getProductDetails(prodId);
    assertEquals(prodId, res.getProdId());
    assertEquals(newProdName, res.getProdName());
    assertEquals(newProdType, res.getProdType());
    assertEquals(newProdInfo, res.getProdInfo());
    assertEquals(newProdPrice, res.getProdPrice());
    assertEquals(newProdQuantity, res.getProdQuantity());
    assertEquals(prodImage, res.getProdImage());
  }

  // test getAllProducts with one product in database
  @Test
  public void testGetAllProductsWithSingleProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    List<ProductBean> res = productService.getAllProducts();
    assertEquals(1, res.size());
    assertEquals(prodId, res.get(0).getProdId());
    assertEquals(prodName, res.get(0).getProdName());
    assertEquals(prodType, res.get(0).getProdType());
    assertEquals(prodInfo, res.get(0).getProdInfo());
    assertEquals(prodPrice, res.get(0).getProdPrice());
    assertEquals(prodQuantity, res.get(0).getProdQuantity());
    assertEquals(prodImage, res.get(0).getProdImage());
  }

  // test getAllProducts with no products in database
  @Test
  public void testGetAllProductsWithNoProducts() {
    ProductServiceImpl productService = new ProductServiceImpl();
    List<ProductBean> res = productService.getAllProducts();
    assertEquals(0, res.size());
  }

  // test getAllProducts with multiple products in database
  @Test
  public void testGetAllProductsWithMultipleProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    String prodId2 = "123456";
    String prodName2 = "Blue Jeans";
    String prodType2 = "Pants";
    String prodInfo2 = "Dark wash jeans";
    double prodPrice2 = 9.99;
    int prodQuantity2 = 5;
    InputStream prodImage2 = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      assertFalse(getProductExists(prodId2));
      addProduct(prodId2, prodName2, prodType2, prodInfo2, prodPrice2, prodQuantity2, prodImage2);
      assertTrue(getProductExists(prodId2));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    List<ProductBean> res = productService.getAllProducts();
    assertEquals(2, res.size());
    assertEquals(prodId, res.get(0).getProdId());
    assertEquals(prodName, res.get(0).getProdName());
    assertEquals(prodType, res.get(0).getProdType());
    assertEquals(prodInfo, res.get(0).getProdInfo());
    assertEquals(prodPrice, res.get(0).getProdPrice());
    assertEquals(prodQuantity, res.get(0).getProdQuantity());
    assertEquals(prodImage, res.get(0).getProdImage());
    assertEquals(prodId2, res.get(1).getProdId());
    assertEquals(prodName2, res.get(1).getProdName());
    assertEquals(prodType2, res.get(1).getProdType());
    assertEquals(prodInfo2, res.get(1).getProdInfo());
    assertEquals(prodPrice2, res.get(1).getProdPrice());
    assertEquals(prodQuantity2, res.get(1).getProdQuantity());
    assertEquals(prodImage2, res.get(1).getProdImage());
  }

  // test getAllProducts with one product of that type in database
  @Test
  public void testGetAllProductsByTypeWithSingleProductOfType() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    String prodId2 = "123456";
    String prodName2 = "Blue Jeans";
    String prodType2 = "Jeans";
    String prodInfo2 = "Dark wash jeans";
    double prodPrice2 = 9.99;
    int prodQuantity2 = 5;
    InputStream prodImage2 = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      assertFalse(getProductExists(prodId2));
      addProduct(prodId2, prodName2, prodType2, prodInfo2, prodPrice2, prodQuantity2, prodImage2);
      assertTrue(getProductExists(prodId2));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    List<ProductBean> res = productService.getAllProductsByType(prodType);
    assertEquals(1, res.size());
  }

  // test getAllProducts with no products of specified type in database
  @Test
  public void testGetAllProductsByTypeWithNoProducts() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    List<ProductBean> res = productService.getAllProductsByType("shirts");
    assertEquals(0, res.size());
  }

  // test getAllProducts with multiple products of specified type in database
  @Test
  public void testGetAllProductsByTypeWithMultipleProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    String prodId2 = "123456";
    String prodName2 = "Blue Pants";
    String prodType2 = "Pants";
    String prodInfo2 = "Dark wash pants";
    double prodPrice2 = 9.99;
    int prodQuantity2 = 5;
    InputStream prodImage2 = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      assertFalse(getProductExists(prodId2));
      addProduct(prodId2, prodName2, prodType2, prodInfo2, prodPrice2, prodQuantity2, prodImage2);
      assertTrue(getProductExists(prodId2));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    List<ProductBean> res = productService.getAllProductsByType(prodType);
    assertEquals(2, res.size());
  }

  // test getAllProducts with empty input
  @Test
  public void testGetAllProductsByTypeWithEmptyInput() {
    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      List<ProductBean> res = productService.getAllProductsByType("");
    });
  }

  // test getAllProducts with null input
  @Test
  public void testGetAllProductsByTypeWithNullInput() {
    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      List<ProductBean> res = productService.getAllProductsByType(null);
    });
  }

  // test searchAllProducts with multiple matching products
  @Test
  public void testSearchAllProductsWithMultipleProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    String prodId2 = "123456";
    String prodName2 = "Blue Pants";
    String prodType2 = "Pants";
    String prodInfo2 = "Dark wash pants";
    double prodPrice2 = 9.99;
    int prodQuantity2 = 5;
    InputStream prodImage2 = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      assertFalse(getProductExists(prodId2));
      addProduct(prodId2, prodName2, prodType2, prodInfo2, prodPrice2, prodQuantity2, prodImage2);
      assertTrue(getProductExists(prodId2));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    List<ProductBean> res = productService.searchAllProducts("dark");
    assertEquals(2, res.size());
  }


  // test searchAllProducts with one matching product
  @Test
  public void testSearchAllProductsWithSingleProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Jeans";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    String prodId2 = "123456";
    String prodName2 = "Blue Pants";
    String prodType2 = "Pants";
    String prodInfo2 = "Dark wash pants";
    double prodPrice2 = 9.99;
    int prodQuantity2 = 5;
    InputStream prodImage2 = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      assertFalse(getProductExists(prodId2));
      addProduct(prodId2, prodName2, prodType2, prodInfo2, prodPrice2, prodQuantity2, prodImage2);
      assertTrue(getProductExists(prodId2));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    List<ProductBean> res = productService.searchAllProducts("jeans");
    assertEquals(1, res.size());
  }

  // test searchAllProducts with no matching products
  @Test
  public void testSearchAllProductsWithNoMatchingProducts() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    String prodId2 = "123456";
    String prodName2 = "Blue Pants";
    String prodType2 = "Pants";
    String prodInfo2 = "Dark wash pants";
    double prodPrice2 = 9.99;
    int prodQuantity2 = 5;
    InputStream prodImage2 = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      assertFalse(getProductExists(prodId2));
      addProduct(prodId2, prodName2, prodType2, prodInfo2, prodPrice2, prodQuantity2, prodImage2);
      assertTrue(getProductExists(prodId2));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    List<ProductBean> res = productService.searchAllProducts("shirt");
    assertEquals(0, res.size());
  }

  // test searchAllProducts with empty input
  @Test
  public void testSearchAllProductsWithEmptyInput() {
    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      List<ProductBean> res = productService.searchAllProducts("");
    });
  }

  // test searchAllProducts with null input
  @Test
  public void testSearchAllProductsWithNullInput() {
    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      List<ProductBean> res = productService.searchAllProducts(null);
    });
  }

  // test getImage with an existing product
//  @Test
//  public void testGetImageWithExistingProduct() {
//    String prodId = "12345";
//    String prodName = "Blue Jeans";
//    String prodType = "Pants";
//    String prodInfo = "Dark wash jeans";
//    double prodPrice = 9.99;
//    int prodQuantity = 5;
//    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");
//    byte[] data = null;
//
//    try {
//      assertFalse(getProductExists(prodId));
//      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
//      assertTrue(getProductExists(prodId));
//
//      BufferedImage bImage = ImageIO.read(new File("sample.jpg"));
//      ByteArrayOutputStream bos = new ByteArrayOutputStream();
//      ImageIO.write(bImage, "jpg", bos );
//      data = bos.toByteArray();
//    } catch (Exception ex) {
//      fail("failed setup");
//    }
//
//    ProductServiceImpl productService = new ProductServiceImpl();
//    byte[] res = productService.getImage(prodId);
//    assertEquals(data, res);
//  }

  // test getImage with nonexistent product
  @Test
  public void testGetImageWithNonExistentProduct() {
    String prodId = "12345";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      byte[] res = productService.getImage(prodId);
    });
  }

  // test getImage with null input
  @Test
  public void testGetImageWithNullInput() {
    String prodId = null;

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      byte[] res = productService.getImage(prodId);
    });
  }

  // test getImage with empty input
  @Test
  public void testGetImageWithEmptyInput() {
    String prodId = "";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      byte[] res = productService.getImage(prodId);
    });
  }









  // test getProductPrice with an existing product
  @Test
  public void testGetProductPriceWithExistingProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    double res = productService.getProductPrice(prodId);
    assertEquals(prodPrice, res);
  }

  // test getProductPrice after updating price
  @Test
  public void testGetProductPriceAfterUpdating() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");
    double newProdPrice = 7.99;

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      updateProduct(prodId, prodName, prodType, prodInfo, newProdPrice, prodQuantity, prodImage);
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    double res = productService.getProductPrice(prodId);
    assertEquals(newProdPrice, res);
  }

  // test getProductPrice with nonexistent product
  @Test
  public void testGetProductPriceWithNonExistentProduct() {
    String prodId = "12345";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      double res = productService.getProductPrice(prodId);
    });
  }

  // test getProductPrice with null input
  @Test
  public void testGetProductPriceWithNullInput() {
    String prodId = null;

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      double res = productService.getProductPrice(prodId);
    });
  }

  // test getProductPrice with empty input
  @Test
  public void testGetProductPriceWithEmptyInput() {
    String prodId = "";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      double res = productService.getProductPrice(prodId);
    });
  }

  // test sellNProduct with an existing product
  @Test
  public void testSellNProductWithExistingProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    int sellQuantity = 1;

    ProductServiceImpl productService = new ProductServiceImpl();
    boolean res = productService.sellNProduct(prodId, sellQuantity);
    assertTrue(res);
  }

  // test sellNProduct with nonexistent product
  @Test
  public void testSellNProductWithNonexistent() {
    String prodId = "12345";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    int sellQuantity = 1;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      boolean res = productService.sellNProduct(prodId, sellQuantity);
    });
  }

  // test sellNProduct with null input
  @Test
  public void testSellNProductWithNullInput() {
    String prodId = null;

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    int sellQuantity = 1;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      boolean res = productService.sellNProduct(prodId, sellQuantity);
    });
  }

  // test sellNProduct with empty input
  @Test
  public void testSellNProductWithEmptyInput() {
    String prodId = "";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    int sellQuantity = 0;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      boolean res = productService.sellNProduct(prodId, sellQuantity);
    });
  }

  // test sellNProduct with selling 0 products
  @Test
  public void testSellNProductZero() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    int sellQuantity = 0;

    ProductServiceImpl productService = new ProductServiceImpl();
    boolean res = productService.sellNProduct(prodId, sellQuantity);
    assertTrue(res);
  }

  // test sellNProduct with selling more products than exists
  @Test
  public void testSellNProductSellTooMany() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    int sellQuantity = 7;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      boolean res = productService.sellNProduct(prodId, sellQuantity);
    });
  }

  // test sellNProduct with selling negative products
  @Test
  public void testSellNProductSellNegativeQuantity() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    int sellQuantity = -7;

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      boolean res = productService.sellNProduct(prodId, sellQuantity);
    });
  }

  // test sellNProduct with selling the remaining quantity
  @Test
  public void testSellNProductSellEntireStock() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    int sellQuantity = 5;

    ProductServiceImpl productService = new ProductServiceImpl();
    boolean res = productService.sellNProduct(prodId, sellQuantity);
    assertTrue(res);
  }

  // test getProductQuantity with an existing product
  @Test
  public void testGetProductQuantityWithExistingProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    int res = productService.getProductQuantity(prodId);
    assertEquals(prodQuantity, res);
  }

  // test getProductQuantity after updating quantity
  @Test
  public void testGetProductQuantityAfterUpdating() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");
    int newProdQuantity = 7;

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      updateProduct(prodId, prodName, prodType, prodInfo, prodPrice, newProdQuantity, prodImage);
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    int res = productService.getProductQuantity(prodId);
    assertEquals(newProdQuantity, res);
  }

  // test getProductQuantity with nonexistent product
  @Test
  public void testGetProductQuantityWithNonExistentProduct() {
    String prodId = "12345";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      int res = productService.getProductQuantity(prodId);
    });
  }

  // test getProductQuantity with null input
  @Test
  public void testGetProductQuantityWithNullInput() {
    String prodId = null;

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      int res = productService.getProductQuantity(prodId);
    });
  }

  // test getProductQuantity with empty input
  @Test
  public void testGetProductQuantityWithEmptyInput() {
    String prodId = "";

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      int res = productService.getProductQuantity(prodId);
    });
  }


  // test updateProductWithoutImage with an existing product
  @Test
  public void testUpdateProductWithoutImageWithExistingProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId(prodId);
    prodUpdated.setProdName("New name");
    prodUpdated.setProdType("type");
    prodUpdated.setProdInfo("info");
    prodUpdated.setProdPrice(10);
    prodUpdated.setProdQuantity(7);
    prodUpdated.setProdImage(prodImage);

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProductWithoutImage(prodId, prodUpdated);
    assertEquals("Product Updated Successfully!", status);
  }

  // test updateProductWithoutImage with nonexistent product
  @Test
  public void testUpdateProductWithoutImageWithNonexistentProduct() {
    String prodId = "12345";
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId(prodId);
    prodUpdated.setProdName("New name");
    prodUpdated.setProdType("type");
    prodUpdated.setProdInfo("info");
    prodUpdated.setProdPrice(10);
    prodUpdated.setProdQuantity(7);
    prodUpdated.setProdImage(prodImage);

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProductWithoutImage(prodId, prodUpdated);
    assertEquals("Product Not available in the store!", status);
  }

  // test updateProductWithoutImage with less quantity
  @Test
  public void testUpdateProductWithoutImageWithLessQuantity() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId(prodId);
    prodUpdated.setProdName("New name");
    prodUpdated.setProdType("type");
    prodUpdated.setProdInfo("info");
    prodUpdated.setProdPrice(0);
    prodUpdated.setProdQuantity(7);
    prodUpdated.setProdImage(prodImage);

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProductWithoutImage(prodId, prodUpdated);
    assertEquals("Product Updated Successfully!", status);
  }


  // test updateProductWithoutImage with different IDs
  @Test
  public void testUpdateProductWithoutImageWithMismatchedIDs() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      fail("failed setup");
    }

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId("newID");
    prodUpdated.setProdName("New name");
    prodUpdated.setProdType(prodType);
    prodUpdated.setProdInfo(prodInfo);
    prodUpdated.setProdPrice(prodPrice);
    prodUpdated.setProdQuantity(prodQuantity);
    prodUpdated.setProdImage(prodImage);

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProductWithoutImage(prodId, prodUpdated);
    assertEquals("Both Products are Different, Updation Failed!", status);
  }

  // test updateProductWithoutImage with no changes
  @Test
  public void testUpdateProductWithoutImageWithNoChanges() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId(prodId);
    prodUpdated.setProdName(prodName);
    prodUpdated.setProdType(prodType);
    prodUpdated.setProdInfo(prodInfo);
    prodUpdated.setProdPrice(prodPrice);
    prodUpdated.setProdQuantity(prodQuantity);
    prodUpdated.setProdImage(prodImage);

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProductWithoutImage(prodId, prodUpdated);
    assertEquals("Product Updated Successfully!", status);
  }

  // test updateProductWithoutImage with null inputs
  @Test
  public void testUpdateProductWithoutImageWithNullInputs() {
    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProductWithoutImage(null, null);
    });
  }

  // test updateProductWithoutImage with null changes
  @Test
  public void testUpdateProductWithoutImageWithNullUpdates() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId(null);
    prodUpdated.setProdName(null);
    prodUpdated.setProdType(null);
    prodUpdated.setProdInfo(null);
    prodUpdated.setProdPrice(0);
    prodUpdated.setProdQuantity(0);
    prodUpdated.setProdImage(null);

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProductWithoutImage(prodId, prodUpdated);
    });
  }

  // test updateProductWithoutImage with empty changes
  @Test
  public void testUpdateProductWithoutImageWithEmptyUpdates() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId("");
    prodUpdated.setProdName("");
    prodUpdated.setProdType("");
    prodUpdated.setProdInfo("");
    prodUpdated.setProdPrice(0);
    prodUpdated.setProdQuantity(0);
    prodUpdated.setProdImage(ProductServiceImplTest.class.getResourceAsStream(""));

    ProductServiceImpl productService = new ProductServiceImpl();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      String status = productService.updateProductWithoutImage(prodId, prodUpdated);
    });
  }

  // test updateProductWithoutImage with demand for product
  @Test
  public void testUpdateProductWithoutImageWithDemandForProduct() {
    String prodId = "12345";
    String prodName = "Blue Jeans";
    String prodType = "Pants";
    String prodInfo = "Dark wash jeans";
    double prodPrice = 9.99;
    int prodQuantity = 5;
    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");

    String userName = "test";
    Long mobileNo = 9999999999L;
    String emailId = "test@gmail.com";
    String address = "test street";
    int pinCode = 123456;
    String password = "test";

    int demandQty = 1;

    try {
      assertFalse(getProductExists(prodId));
      addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
      assertTrue(getProductExists(prodId));
      registerUser(userName, mobileNo, emailId, address, pinCode, password);
      addDemandForProduct(userName, prodId, demandQty);
    } catch (Exception ex) {
      fail("failed setup");
    }

    ProductBean prodUpdated = new ProductBean();
    prodUpdated.setProdId(prodId);
    prodUpdated.setProdName("New name");
    prodUpdated.setProdType("type");
    prodUpdated.setProdInfo(prodInfo);
    prodUpdated.setProdPrice(prodPrice);
    prodUpdated.setProdQuantity(12);
    prodUpdated.setProdImage(prodImage);

    ProductServiceImpl productService = new ProductServiceImpl();
    String status = productService.updateProductWithoutImage(prodId, prodUpdated);
    assertEquals("Product Updated Successfully! And Mail Send to the customers who were waiting for this product!", status);
  }


}
