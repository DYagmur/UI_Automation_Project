package com.beymen.page;

import com.thoughtworks.gauge.Step;
import beymen.base.BaseTest;
import beymen.model.ElementInfo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.util.Scanner;
import org.apache.poi.ss.usermodel.*;
import org.openqa.selenium.JavascriptExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseSteps extends BaseTest {

    List<String> tempDataList = new ArrayList<>();
    List<Integer> tempDataListInt = new ArrayList<>();

    String tempData;

    public static int DEFAULT_MAX_ITERATION_COUNT = 100;  // bir eylemin kaç kez tekrarlanacağını ifade eder
    public static int DEFAULT_MILLISECOND_WAIT_AMOUNT = 300; // her bir kontrol arasında bekleyeceğimiz süre

    /*
    static olarak tanımlamamızın sebebi sınıfın tüm örneklerin arasında paylaşılmasını ve değiştirilmesi gerektiği zaman tek bir yerden değiştirilmesini sağlamaktır.
     */

    public BaseSteps() throws IOException {
        String workingDir = System.getProperty("user.dir");
        initMap(getFileList(workingDir + "/src"));

    }

    public By getElementInfoBy(ElementInfo elementInfo) {
        By by = null;
        if (elementInfo.getType().equals("css")) {
            by = By.cssSelector(elementInfo.getValue());
        } else if (elementInfo.getType().equals("xpath")) {
            by = By.xpath(elementInfo.getValue());
        } else if (elementInfo.getType().equals("id")) {
            by = By.id(elementInfo.getValue());
        }
        return by;
    }

    WebElement findElement(String key) {

        By by = getElementInfoBy(findElementInfoByKey(key));
        WebDriverWait wait = new WebDriverWait(driver, 20);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'})", element);
        return element;
    }

    List<WebElement> findElements(String key) {
        return driver.findElements(getElementInfoBy(findElementInfoByKey(key)));
    }

    private void clickTo(WebElement element) {
        element.click();
    }

    private void sendKeysTo(WebElement element, String text) {
        element.sendKeys(text);
    }

    public void javaScriptClickTo(WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }


    @Step("<key> li elementi bul temizle ve <text> değerini yaz")
    public void sendKeys(String key, String text) {
        WebElement element = findElement(key);
        element.clear();
        sendKeysTo(element, text);
        logger.info("Element bulundu ve yazıldı: Key : " + key + " text : " + text);
    }

    @Step("<key> elementini temizle")
    public void clearelement(String key) {
        WebElement element = findElement(key);
        element.clear();
        // JavaScriptExecutor nesnesini oluşturun.
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // JavaScript kodunu çalıştırın.
        js.executeScript("clickElement(arguments[0])", element);
        js.executeScript("clearTextElement(arguments[0])", element);

        logger.info("Element temizlendi");
    }

    @Step("Elementine tıkla <key>")
    public void clickElement(String key) {
        clickTo(findElement(key));
        logger.info(key + " elementine tıklandı.");
    }

    @Step("<int> saniye bekle")
    public void waitSecond(int seconds) throws InterruptedException {
        try {
            logger.info(seconds + " saniye bekleniyor");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step("<key> elementinin disabled olduğunu kotrol et")
    public void checkDisabled(String key) {
        WebElement element = findElement(key);
        Assertions.assertTrue(element.isDisplayed(), " Element disabled değil");
        logger.info(key + " elementi disabled");
    }

    @Step("<key> elementi <expected> değerini içerdiği <unexpected> değerini içermediği kontrol edilir")
    public void checkExpectedEqualsUnexpected(String key, String expected, String unexpected) {
        String elementText = findElement(key).getText();

        if (!elementText.equals(expected)) {
            Assertions.fail(key + " elementi " + expected + " değerini içeriyor.");
        }
        logger.info(key + " elementi beklenen '" + expected + "' değerini içeriyor.");

        if (elementText.equals(unexpected)) {
            Assertions.fail(key + " elementi beklenmeyen '" + unexpected + "' metnini içeriyor. Alınan metin: " + elementText);
        }
        logger.info(key + " elementi beklenmeyen '" + unexpected + "' değerini içermiyor.");
    }

    @Step("<key> elementinin <attribute> niteliği <value> değerine sahip mi")
    public void elementAttributeValueCheck(String key, String attribute, String value) throws InterruptedException {
        WebElement element = findElement(key);
        String actualValue;
        int count = 0;
        while (count < DEFAULT_MAX_ITERATION_COUNT) {
            actualValue = element.getAttribute(attribute).trim();
            if (actualValue.equals(value)) {
                logger.info(key + " elementinin " + attribute + " niteliği " + value + " değerine sahip.");
                return;
            }
            waitSecond(DEFAULT_MILLISECOND_WAIT_AMOUNT);
        }
        Assertions.fail(key + " elementinin " + attribute + " niteliği " + value + " değeri ile eşleşmiyor.");
    }

    @Step("<key> elementi <expectedText> değerini içeriyor mu kontrol et")
    public void checkElementEqualsText(String key, String expectedText) {

        String actualText = findElement(key).getText();
        logger.info("Element str:" + actualText);
        logger.info("Expected str:" + expectedText);
        Assertions.assertEquals(actualText, expectedText, "Beklenen metni içermiyor " + key);
        logger.info(key + " elementi " + expectedText + " degerine eşittir.");
    }

    @Step("ENTER tuşuna bas")
    public void pressEnter() {
        action.sendKeys(Keys.ENTER).build().perform();
    }

    @Step("<key> menusünden random seçim yap")
    public void clickOnRandomItemInList(String key) {
        List<WebElement> elements = findElements(key);
        Random random = new Random();
        int index = random.nextInt(elements.size());
        elements.get(index).click();
        //logger.info(key + " elementine random tiklandi " + elements.get(index).getText());
    }

    @Step("İkinci sekmeye geçilir")
    public void switchToPage2() {
        String parentWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();
        for (String curWindow : allWindows) {
            driver.switchTo().window(curWindow);
        }
        logger.info("Ikinci sekmeye gecildi");
    }

    @Step("<key> elementinin text değerinin boşluk atılır ve txt dosyasına kaydedilir")
    public void dropDownRandomddSaveAfter(String key) {

        String element = findElement(key).getText();
        logger.info(element);
        if (element.contains(" ")) {
            String[] parts = element.split(" ");

            element = parts[0].trim();
            element = element.replace("TL", "").replace("TL", "").trim();

        }

        tempData = element;
        logger.info("Urunun fiyati: " + tempData);

        try {
            FileWriter writer = new FileWriter("a.txt");
            writer.write(element);
            writer.close();
            logger.info("Değişken değeri '" + "a.txt" + "' dosyasına yazıldı.");
        } catch (IOException e) {
            logger.info("Dosyaya yazma işlemi sırasında hata oluştu: " + e.getMessage());
        }
    }

    @Step("<key> elementinin text içeriği belleğe kaydedilen text ile eşit olduğu kontrol edilir <count>")
    public void priceAssertionsSplit(String key, int count) throws FileNotFoundException {
        String expectedText = findElement(key).getText();
        logger.info("Orijinal Metin: " + expectedText);


        String[] parts = expectedText.split(" ");

        expectedText = parts[0].trim();


        logger.info("Seçilen Parça: " + expectedText);

        // Noktaları ve TL metnini çıkar
        expectedText = expectedText.replace(",", "").replace("TL", "").replace("00", "").trim();
        logger.info("Sayısal Değer: " + expectedText);

        // Dosya yolu belirleme
        String dosyaYolu = "a.txt";

        // Scanner nesnesi oluşturma
        Scanner scanner = new Scanner(new File(dosyaYolu));

        // Tek satırı okuma
        String satir = scanner.nextLine();


        String errorMessage = String.format("Fiyatlar eşit değil. Beklenen: %s, Gerçek: %s", satir, expectedText);
        Assertions.assertTrue(Objects.equals(expectedText, satir), errorMessage);
        logger.info("Belleğe kaydedilen değer : " + tempData + " ile " + "ExpectedText değeri : " + expectedText + " eşittir");
    }


    @Step("<key> elementinin text değeri tempData'da saklanan değere eşittir")
    public void checkElementValueEqualsTempData(String key) {
        String element = findElement(key).getText();
        Assertions.assertEquals(tempData, element, "Elementin text değeri '" + key + "' tempData'da saklanan değerle eşleşmiyor");
        logger.info("Elementin değeri '" + key + "' tempData'da saklanan değerle eşleşir");
    }

    @Step({"<key> element size değeri <expectedCount> değerine eşit mi kontrol et"})
    public void checkElementCountEquals(String key, int expectedCount) {
        int actualCount = findElements(key).size();
        assertEquals(expectedCount, actualCount, "Expected count does not match for " + key);
        logger.info(key + " elementi sayısı " + expectedCount + " değerine eşittir.");
    }

    @Step("<key> elementi varsa <key1> listesindeki <value> elementini seç")
    public void selectItem(String key, String key1, String value) {
        WebElement element = findElement(key);
        WebElement element1 = findElement(key1);
        if (element.isDisplayed()) {
            // Element görünürse işlem yap

            Select select = new Select(element1);

            List<WebElement> options = select.getOptions();

            for (WebElement option : options) {
                if (option.getAttribute("value").equals("secilecek_deger")) {
                    option.click();
                    logger.info("ürün 2 adet seçildi");
                    break;

                }


            }
        }else {
            // Element görünür değilse alternatif işlem yapma
            logger.info("Element görünür değil!");
        }
    }

    @Step("<element> elementine Exceldeki <index> .index satır <index2> .index sütundaki veriyi  yaz")
    public void excelVeriYaz(String element,Integer index,Integer index2) throws IOException {

        // Excel dosyasının yolu
        String filePath = "Kitap.xlsx";

        // Excel dosyasını okuma
        FileInputStream fis = new FileInputStream(new File(filePath));
        Workbook workbook = new XSSFWorkbook(fis);

        // Sayfayı seçme
        Sheet sheet = workbook.getSheet("sayfa1");

        // 1. satır 1. sütunu okuma
        String veri = sheet.getRow(index).getCell(index2).getStringCellValue();

        // Veriyi web formundaki elemente yazma
        WebElement elementWeb = findElement(element);
        elementWeb.sendKeys(veri);

        fis.close();
    }

    @Step("<key> elementinin görünür olduğu kontrol edilir")
    public void selectItem(String key) {
        WebElement element = findElement(key);

        if (element.isDisplayed()) {
            logger.info("element bulundu");
        }
    }

}

                                     /*
                                     @Step anatosyonu Gauge kütüphanesine ait bir anatosyondur. bunun ile testlerimizde sürekli olarak çağırabileceğimiz
                                     cümlecikler halinde metodlar oluşturuyoruz.
                                     Bu sınıfı BaseTest sınıfı ile extends ediyoruz çünkü BaseTest sınıfında driver nesnesini oluşturuyoruz.
                                     BaseTest de olması gerekenler burada da olmalıdır.
                                     Extend ederek bir başka sınıfın özelliklerini miras alıp kullanabiliriz.

                                     bir sınıf başka bir sınıfı "extend" ettiğinde, temel alınan sınıfın ("superclass" veya "parent class" olarak adlandırılır) tüm halka açık metotları ve özellikleri, türetilen sınıfa ("subclass" veya "child class") aktarılır.
                                     Bu işlem sayesinde, kod tekrarını önlemek ve kodun yeniden kullanılabilirliğini artırmak mümkün olur.

                                     Polimorfizm: Alt sınıflar, üst sınıfın metodlarını kendi ihtiyaçlarına göre "override" edebilir (üzerine yazabilir),
                                     böylece aynı metot adı farklı sınıflarda farklı davranışlar sergileyebilir.
                                      */

