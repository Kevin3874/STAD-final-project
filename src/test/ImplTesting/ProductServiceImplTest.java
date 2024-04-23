package test;

import com.shashi.beans.ProductBean;
import com.shashi.beans.UserBean;
import com.shashi.constants.IUserConstants;
import com.shashi.service.impl.ProductServiceImpl;
import com.shashi.service.impl.UserServiceImpl;
import com.shashi.utility.DBUtil;
import com.shashi.utility.IDUtil;
import com.shashi.utility.MailMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.shashi.constants.IUserConstants;

import java.io.InputStream;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

  @AfterEach
  public void clearProducts() {
    try {
      Connection con = DBUtil.provideConnection();

      PreparedStatement ps1 = con.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
      ps1.execute();

      PreparedStatement ps2 = con.prepareStatement("delete from product");
      ps2.execute();

      PreparedStatement ps3 = con.prepareStatement("delete from usercart");
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
    clearProducts();
  }

//  // test addProduct with a new product
//  @Test
//  public void testAddProductWithNewProduct() {
//    String prodId = "12345";
//    String prodName = "Blue Jeans";
//    String prodType = "Pants";
//    String prodInfo = "Dark wash jeans";
//    double prodPrice = 9.99;
//    int prodQuantity = 5;
//    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");
//
//    try {
//      assertFalse(getProductExists(prodId));
//    } catch (Exception ex) {
//      fail("failed setup");
//    }
//
//    ProductServiceImpl productService = new ProductServiceImpl();
//    String status = productService.addProduct(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
//    assertEquals("Product Added Successfully with Product Id: " + prodId, status);
//
//    // cleanup and verification
//    try {
//      assertTrue(getProductExists(prodId));
//    } catch (Exception ex) {
//      fail("failed to verify");
//    }
//  }

//
//  // test addProduct with a new product
//  @Test
//  public void testAddProductWithNewProduct() {
//    String prodId = "12345";
//    String prodName = "Blue Jeans";
//    String prodType = "Pants";
//    String prodInfo = "Dark wash jeans";
//    double prodPrice = 9.99;
//    int prodQuantity = 5;
//    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");
//
//    try {
//      assertFalse(getProductExists(prodId));
//    } catch (Exception ex) {
//      fail("failed setup");
//    }
//
//    ProductServiceImpl productService = new ProductServiceImpl();
//    String status = productService.addProduct(prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
//    assertEquals("Product Added Successfully with Product Id: " + prodId, status);
//
//    // cleanup and verification
//    try {
//      assertTrue(getProductExists(prodId));
//    } catch (Exception ex) {
//      fail("failed to verify");
//    }
//  }
//
//
//  // test addProduct with a new product
//  @Test
//  public void testAddProductWithExistingProduct() {
//    String prodId = "12345";
//    String prodName = "Blue Jeans";
//    String prodType = "Pants";
//    String prodInfo = "Dark wash jeans";
//    double prodPrice = 9.99;
//    int prodQuantity = 5;
//    InputStream prodImage = ProductServiceImplTest.class.getResourceAsStream("/test.png");
//
//    try {
//      assertFalse(getProductExists(prodId));
//
//      assertTrue(getProductExists(prodId));
//
//    } catch (Exception ex) {
//      fail("failed setup");
//    }
//
//    ProductServiceImpl productService = new ProductServiceImpl();
//    String status = productService.addProduct(prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
//    assertEquals("Product Added Successfully with Product Id: " + prodId, status);
//
//    // cleanup and verification
//    try {
//      assertTrue(getProductExists(prodId));
//    } catch (Exception ex) {
//      fail("failed to verify");
//    }
//  }

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

    // cleanup and verification
    try {
      assertFalse(getProductExists(prodId));
    } catch (Exception ex) {
      fail("failed to verify");
    }
  }


}
