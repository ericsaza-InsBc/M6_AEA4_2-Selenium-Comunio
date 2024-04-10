import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Driver;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EricSaladoZafra_comunio {

    // Instanciar acciones/condiciones
    public static String testId;
    WebDriver driver;

    private static EricSaladoZafra_comunio comunio_instance = null;

    // Instanciar clases de test con patrón Singleton
    public static EricSaladoZafra_comunio setInstance() {
        if (comunio_instance == null) {
            comunio_instance = new EricSaladoZafra_comunio();
        }
        return comunio_instance;
    }

    @BeforeMethod
    public void setup_test() throws Exception {

        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("start-maximized");
        chromeOptions.addArguments("--log-level=1");
        driver = new ChromeDriver(chromeOptions);
    }

    @AfterMethod
    public void breakup_test() throws Exception {

        // Cerrar el navegador
        driver.quit();
    }

    /**************************************************
     * Pruebas
     **************************************************/

    /**
     * Comprobar que carga la web correctamente
     */
    @Test(description = "Comunio - Accesso Comunio", enabled = true)
    public void Comunio_TC01() throws InterruptedException {

        // PASO 1
        // Navegar a la página con la url
        driver.get("https://www.comunio.es/");

        // PASO 2
        // Aceptar los terminos y condiciones
        driver.findElement(By.xpath("//span[text()='ACEPTO']")).click();
        Thread.sleep(500);
    }

    /**
     * Iniciará sesión página de comunio
     */
    @Test(description = "Comunio - Iniciar sesión", enabled = true)
    public void Login_TC01() throws InterruptedException {

        // Precondiciones
        Comunio_TC01();

        // PASO 1
        // Click al botón "Entrar" para or al formulario de inicio de sesión
        driver.findElement(
                By.xpath("//lore[@id='above-the-fold-container']//a[@class='login-btn registration-btn-fill']"))
                .click();
        Thread.sleep(500);

        // PASO 2
        // Escribir el usuario y contraseña en el formulario y luego darle al boton de
        // "Entrar"
        driver.findElement(By.xpath("//input[@id='input-login']")).sendKeys(User.getUser());
        Thread.sleep(500);
        driver.findElement(By.xpath("//input[@id='input-pass']")).sendKeys(User.getPsw());
        Thread.sleep(500);
        driver.findElement(By.xpath("//a[@id='login-btn-modal']")).click();
        Thread.sleep(2000);
    }

    /**
     * Entrar a la sección mercado
     */
    @Test(description = "Mercado - Entrar a la sección", enabled = true)
    public void Mercado_TCO1() throws InterruptedException {

        // Precondiciones
        Login_TC01();

        // PASO 1
        // Entramos a la sección de la "Mercado"
        Thread.sleep(10000);
        driver.findElement(By.xpath("//a[@title='Mercado']")).click();

        Thread.sleep(3000);
    }

    /**
     * Comprar jugadores
     * 
     * @throws IOException
     */
    @Test(description = "Mercado - Compra de jugadores", enabled = true)
    public void Mercado_TCO2() throws InterruptedException, IOException {

        // Precondiciones
        Mercado_TCO1();
        int saldo = Integer
                .parseInt(driver.findElement(By.xpath("//h2[@class='text_oswald']")).getText().replace(".", ""));

        // PASO 1
        // Obtenemos los datos de todos los jugadores
        List<WebElement> jugadores = driver.findElements(By.xpath("//div[@class='csspt-row']"));

        // PASO 2
        // Selección de jugadores
        List<JugadorInfo> jugadoresInfo = new ArrayList<>();

        // Crearemos un archivo
        String nombreArchivo = "./src/test/java/links.txt";
        File archivo = new File(nombreArchivo);
        PrintWriter archivoSalida = new PrintWriter(archivo);

        for (WebElement jugador : jugadores) {

            // Variables
            String puntos = jugador.findElement(By.cssSelector(".csspt-totalpoints")).getText();
            String valorMercado = jugador.findElement(By.cssSelector(".csspt-marketvalue>span")).getText();
            String ofertaMinima = jugador.findElement(By.cssSelector(".csspt-price")).getText();
            String linkJugador = jugador.findElement(By.cssSelector(".csspt-name")).getAttribute("href");
            String idJugador = linkJugador.substring(linkJugador.length() - 4);
            String nombreEnlace = quitarAcentos(
                    jugador.findElement(By.cssSelector(".text-to-slide")).getText().toLowerCase())
                    .replace(" ", "-");
            String equipo = jugador.findElement(By.xpath(".//inline-block")).getAttribute("title");

            // oferta minima = +%15 del valor mercado
            if ((!puntos.equalsIgnoreCase("-") && Integer.parseInt(puntos) >= 5 && Integer
                    .parseInt(ofertaMinima.replace(".", "")) <= (int) (Integer
                            .parseInt(valorMercado.replace(".", "")) * 1.08))
                    && !equipo.equalsIgnoreCase("ha dejado la liga")) {
                archivoSalida.println(nombreEnlace + " - " + idJugador);
                // System.out.println(nombreEnlace + " - " + idJugador);

            }
        }

        // Cerramos el teclado
        archivoSalida.close();
        Thread.sleep(5000);

        // Ejecutamos el archivo python
        try {
            Process process = Runtime.getRuntime().exec("cmd /c python .\\src\\test\\java\\main.py");
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Data created");
            } else {
                System.out.println("ERROR");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // // Formateamos la fecha
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaFormateada = fechaActual.format(formatter);
        String nombreArchivoParaLeer = "./src/test/java/players_data/" + fechaFormateada + ".txt";
        // System.out.println(fechaFormateada);
        File archivoParaLeer = new File(nombreArchivoParaLeer);

        try (// Scanner
                Scanner lectorArchivo = new Scanner(archivoParaLeer)) {
            for (WebElement jugador : jugadores) {

                // Variables
                String puntos = jugador.findElement(By.cssSelector(".csspt-totalpoints")).getText();
                String valorMercado = jugador.findElement(By.cssSelector(".csspt-marketvalue>span")).getText();
                String ofertaMinima = jugador.findElement(By.cssSelector(".csspt-price")).getText();
                String nombreJugador = jugador.findElement(By.cssSelector(".text-to-slide")).getText();
                String propietario = jugador.findElement(By.cssSelector(".csspt-owner__text")).getText();
                String equipo = jugador.findElement(By.xpath(".//inline-block")).getAttribute("title");

                // oferta minima = +%15 del valor mercado
                if ((!puntos.equalsIgnoreCase("-") && Integer.parseInt(puntos) >= 5 && Integer
                        .parseInt(ofertaMinima.replace(".", "")) <= (int) (Integer
                                .parseInt(valorMercado.replace(".", "")) * 1.08))
                        && !equipo.equalsIgnoreCase("ha dejado la liga")) {

                    // Leemos el archivo de los datos
                    String[] linea = lectorArchivo.nextLine().split(" / ");
                    // System.out.println(linea);
                    String titular = linea[2];
                    String lesionado = linea[3];
                    System.out.println(linea[0]);

                    // Precios
                    String[] arrayPrecios = linea[4].split(" > ");

                    if (lesionado.equalsIgnoreCase("NO LESIONADO") && titular.equalsIgnoreCase("SI")
                            && (arrayPrecios[0].charAt(0) == '+' || (arrayPrecios[0].charAt(0) == '-'
                                    && Integer.parseInt(arrayPrecios[0].substring(0)) < 20000))) {
                        jugadoresInfo.add(new JugadorInfo(nombreJugador, propietario, Integer.parseInt(puntos),
                                Integer.parseInt(valorMercado.replace(".", "")),
                                Integer.parseInt(ofertaMinima.replace(".", ""))));
                        // System.out.println(new JugadorInfo(nombreJugador, propietario,
                        // Integer.parseInt(puntos),
                        // Integer.parseInt(valorMercado.replace(".", "")),
                        // Integer.parseInt(ofertaMinima.replace(".", ""))).toString());
                        // Thread.sleep(1000);
                    }

                }
            }
            lectorArchivo.close();
        } catch (NumberFormatException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Ordenar la lista de jugadores por puntos en orden descendente
        Collections.sort(jugadoresInfo, (j1, j2) -> Integer.compare(j2.getPuntos(), j1.getPuntos()));

        // Hacer clic en los tres jugadores con más puntos
        int contador = 0;
        boolean maxJugadores = false;
        for (JugadorInfo jugadorInfo : jugadoresInfo) {
            if (!maxJugadores) {
                if (contador < 3) {

                    // Aquí puedes hacer clic en el jugador
                    try {
                        WebElement jugadorElement = driver
                                .findElement(By.xpath("//div[@class='text-to-slide' and text()='"
                                        + jugadorInfo.getNombre() + " ']/../../../../.."));
                        seleccionarJugador(jugadorElement, driver);

                        // Incrementar el contador
                        contador++;
                    } catch (Exception e) {
                        try {
                            WebElement jugadorElement = driver
                                    .findElement(By.xpath("//div[@class='text-to-slide' and text()=' "
                                            + jugadorInfo.getNombre() + "']/../../../../.."));
                            seleccionarJugador(jugadorElement, driver);

                            // Incrementar el contador
                            contador++;
                        } catch (Exception e2) {
                            WebElement jugadorElement = driver
                                    .findElement(By.xpath("//div[@class='text-to-slide' and text()='"
                                            + jugadorInfo.getNombre() + "']/../../../../.."));
                            seleccionarJugador(jugadorElement, driver);

                            // Incrementar el contador
                            contador++;
                        }
                    }
                } else {
                    maxJugadores = true; // Salir del bucle si ya hiciste clic en los tres jugadores con más puntos
                }
            }
        }

        int sumaValores = 0;
        // Aceptar jugadores
        List<WebElement> jugadoresAComprar = driver.findElements(By.xpath("//div[@class='row']"));
        for (WebElement jugadorAComprar : jugadoresAComprar) {
            WebElement input = jugadorAComprar.findElement(By.xpath(".//input"));
            int valor = Integer.parseInt(input.getAttribute("value").replace(".", ""));
            int nuevoValor = (int) (valor * 1.07);

            // Lo vaciamos y enviamos el nuevo precio
            input.clear();
            input.sendKeys(String.valueOf(nuevoValor));
            sumaValores += nuevoValor;
        }
        System.out.println(sumaValores + " - " + saldo);

        // Clickamos el botón de comprar
        WebElement botonComprar = driver
                .findElement(By.xpath("//*[@id='calculator']//div[@class='overflow_hidden']//span[1]"));
        if (botonComprar.getAttribute("class").equalsIgnoreCase("icons-button-check-green submit_margin")) {
            botonComprar.click();
        } else {
            // System.out.print("No puedes comprar");
            for (int i = 0; i < jugadoresAComprar.size(); i++) {
                // System.out.println(valor);
                if (i > 1) {
                    jugadoresAComprar.get(i).findElement(By.xpath("./span[@class='icons-button-decline-sm']")).click();
                    // System.out.println("borrar" + valor);
                }
            }
            botonComprar.click();
        }
        Thread.sleep(3000);
    }

    @Test(description = "Mercado - Aceptar jugadores", enabled = true)
    public void Mercado_TCO3() throws InterruptedException {

        // Precondiciones
        Mercado_TCO1();

        // PASO 1
        // Recorrer todos mis jugadores y aceptar la oferta mas cara
        List<WebElement> misJugadores = driver.findElements(By.xpath("//div[@class='csspt-row csspt-row--own']"));
        for (WebElement miJugador : misJugadores) {
            WebElement ofertaParaAceptar = null;

            String valorJugador = miJugador.findElement(By.cssSelector(".csspt-marketvalue"))
                    .getText().replace(".", "");
            String ofertaJugador = miJugador.findElement(By.xpath("//span[@class='csspt-price csspt-price--green']"))
                    .getText().replace("+ ", "").replace(".", "");
            List<WebElement> ofertas = miJugador.findElements(
                    By.xpath("./*[local-name()='market-item-offers']/*[local-name()='market-item-offer']"));
            if (ofertas.size() > 0 && Integer.parseInt(ofertaJugador) >= Integer.parseInt(valorJugador)) {

                int maxOferta = 0;

                for (WebElement oferta : ofertas) {

                    // Scroll
                    scrollAElemento(oferta, driver, 250);
                    String dineroOferta = oferta
                            .findElement(By.xpath(".//span[@class='csspt-price csspt-price--green']"))
                            .getText().replace(".", "").replace("+ ", "");
                    System.out.println(valorJugador + " - " + maxOferta);
                    if (maxOferta < Integer.parseInt(dineroOferta)) {
                        maxOferta = Integer.parseInt(dineroOferta);
                        ofertaParaAceptar = oferta;
                    }
                }
                // Click a la oferta más cara
                ofertaParaAceptar.findElement(By.xpath("./div/div[@class='csspt-info']/span[@class='csspt-accept']"))
                        .click();
            }
        }

        try {
            // Clickamos el botón de comprar
            WebElement botonComprar = driver
                    .findElement(By.xpath("//div[@id='calculator']//div[@class='overflow_hidden']//span[1]"));
            botonComprar.click();
        } catch (Exception e) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    "alert('No hay jugadores para comprar');");
            Thread.sleep(4000);
        }
        Thread.sleep(4000);
    }

    /**
     * Venta de jugadores.
     */
    @Test(description = "Mercado - Ventas", enabled = true)
    public void Mercado_TC04() throws InterruptedException {

        // Precondiciones
        Mercado_TCO1();

        // PASO 1
        // Control de posiciones

        // Controlamos los delanteros
        List<WebElement> delanteros = driver
                .findElements(By.xpath("//div[@class='sqd_table']/div")).get(0)
                .findElements(By.xpath(".//div[@class='tradable']"));
        venderJugadores(delanteros, driver, 2);

        // Controlamos los mediocampo
        List<WebElement> mediocampos = driver
                .findElements(By.xpath("//div[@class='sqd_table']/div")).get(1)
                .findElements(By.xpath(".//div[@class='tradable']"));
        venderJugadores(mediocampos, driver, 4);

        // Controlamos los defensas
        List<WebElement> defensas = driver
                .findElements(By.xpath("//div[@class='sqd_table']/div")).get(2)
                .findElements(By.xpath(".//div[@class='tradable']"));
        venderJugadores(defensas, driver, 4);

        // Controlamos los porteros
        List<WebElement> porteros = driver
                .findElements(By.xpath("//div[@class='sqd_table']/div")).get(3)
                .findElements(By.xpath(".//div[@class='tradable']"));
        venderJugadores(porteros, driver, 1);

    }

    /**
     * Función para buscar cualquier jugador
     * 
     * @param jugador elemento del jugador
     * @param driver
     * @throws InterruptedException
     */
    public static void seleccionarJugador(WebElement jugador, WebDriver driver) throws InterruptedException {

        // Scroll
        scrollAElemento(jugador, driver, 150);

        // Click
        jugador.findElement(By.xpath("./*[local-name()='market-item']//span[@id='ic-05']/span"))
                .click();
        Thread.sleep(2000);
    }

    public static void scrollAElemento(WebElement elemento, WebDriver driver, int space) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String[] coords = elemento.getLocation().toString().replace("(", "").replace(")", "")
                .split(", ");
        js.executeScript(
                "window.scrollTo(" + coords[0] + ", " + (Integer.parseInt(coords[1]) - 150) + ")");
        Thread.sleep(1000);

    }

    public static String quitarAcentos(String input) {
        // Utilizamos Normalizer para descomponer los caracteres a su forma normal
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Utilizamos una expresión regular para eliminar los caracteres diacríticos
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    public static void venderJugadores(List<WebElement> jugadores, WebDriver driver, int maxJugadores)
            throws InterruptedException {
        for (int i = 0; i < jugadores.size(); i++) {

            scrollAElemento(jugadores.get(i), driver, 50);

            String nombreJugador = jugadores.get(i).findElement(By.xpath(".//div[@class='name']")).getText();
            WebElement botonVenta = jugadores.get(i).findElement(By.xpath(".//div[@class='button']/span"));
            System.out.println(nombreJugador);
            WebElement imagenEquipo = jugadores.get(i).findElement(By.xpath(".//img[@class='pp-club']"));
            // Eliminamos los jugadores que de más
            if ((i > (maxJugadores - 1)) && !imagenEquipo.getAttribute("title").equalsIgnoreCase("ha dejado la liga")) {
                botonVenta.click();
            }
        }
    }
}
