package apiTests;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class AuthenticationTests {
    String loginToken = null;
    String bookingId = null;

    @BeforeClass
    public void loginToApp(){
        String endPoint = "https://restful-booker.herokuapp.com/auth";
        String body = """
                {
                    "username" : "admin",
                    "password" : "password123"
                }""";
        ValidatableResponse validatableResponse =
                given().body(body).header("Content-Type","application/json")
                        .when().post(endPoint).then();
        //Extract Token
        Response response = validatableResponse.extract().response();
        JsonPath jsonPath = response.jsonPath();
        loginToken = jsonPath.getString("token") ;
        System.out.println(loginToken);
    }

    @Test(priority = 0)
    public void createBookingTest(){
        String endPoint = "https://restful-booker.herokuapp.com/booking";
        String body = """
                {
                    "firstname" : "Jim",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                }""";
        ValidatableResponse validatableResponse =
                given().body(body).header("Content-Type","application/json")
                        .when().post(endPoint).then();

        //Assertions
        validatableResponse.body("booking.firstname",equalTo("Jim"),
                "booking.lastname", equalTo("Brown"),
                "booking.totalprice", equalTo(111),
                "booking.bookingdates.checkin", equalTo("2018-01-01"));
        validatableResponse.header("Content-Type",equalTo("application/json; charset=utf-8"));

        //Extract booking Id
        Response response = validatableResponse.extract().response();
        JsonPath jsonPath = response.jsonPath();
        bookingId = jsonPath.getString("bookingid") ;
        System.out.println(bookingId);
    }

    @Test(priority = 1, dependsOnMethods = "createBookingTest")
    public void updateBookingTest(){
        String endPoint = "https://restful-booker.herokuapp.com/booking/" + bookingId;
        String body = """
                {
                    "firstname" : "James",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                }""";
        ValidatableResponse validatableResponse =
                given().body(body).header("Content-Type","application/json")
                        .header("Accept","application/json")
                        .header("Cookie","token=" + loginToken)
                        .header("Authorisation","Basic")
                        .when().put(endPoint).then();

        //Assertions
        validatableResponse.body("firstname",equalTo("James"),
                "lastname", equalTo("Brown"));
        validatableResponse.header("Content-Type",equalTo("application/json; charset=utf-8"));

    }

    @Test(priority = 2, dependsOnMethods = "updateBookingTest")
    public void getBookingTest(){
        String endPoint = "https://restful-booker.herokuapp.com/booking/" + bookingId;
        ValidatableResponse validatableResponse =
                given().header("Accept","application/json")
                        .when().get(endPoint).then();

        validatableResponse.body("firstname",equalTo("James"),
                "lastname", equalTo("Brown"));
        validatableResponse.statusCode(200);
    }

    @Test(priority = 3, dependsOnMethods = "getBookingTest")
    public void deleteBookingTest(){
        String endPoint = "https://restful-booker.herokuapp.com/booking/" + bookingId;
        ValidatableResponse validatableResponse =
                given().header("Content-Type","application/json")
                        .header("Cookie","token=" + loginToken)
                        .header("Authorisation","Basic")
                        .when().delete(endPoint).then();

        validatableResponse.statusCode(201);
        Response response = validatableResponse.extract().response();
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(response.asString(),"Created");

    }


}
