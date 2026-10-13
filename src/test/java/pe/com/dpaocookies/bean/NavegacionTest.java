package pe.com.dpaocookies.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Las reglas de navegación de faces-config.xml corresponden a los resultados que devuelve AccesoBean. */
class NavegacionTest {

    private static Document config;

    @BeforeAll
    static void leer() throws Exception {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        config = fabrica.newDocumentBuilder().parse(new File("src/main/webapp/WEB-INF/faces-config.xml"));
    }

    /** Caso de navegación para un resultado, o {@code null}. */
    private static Element caso(String resultado) {
        NodeList casos = config.getElementsByTagName("navigation-case");
        for (int i = 0; i < casos.getLength(); i++) {
            Element caso = (Element) casos.item(i);
            if (resultado.equals(texto(caso, "from-outcome"))) {
                return caso;
            }
        }
        return null;
    }

    private static String texto(Element padre, String etiqueta) {
        NodeList nodos = padre.getElementsByTagName(etiqueta);
        return nodos.getLength() == 0 ? null : nodos.item(0).getTextContent().trim();
    }

    private static String origen(Element caso) {
        return texto((Element) caso.getParentNode(), "from-view-id");
    }

    @Test
    void ingresoCorrectoLlevaDelAccesoAlTableroConRedireccion() {
        Element caso = caso(AccesoBean.INGRESO);
        assertNotNull(caso, "falta la regla del ingreso");
        assertEquals("/app/acceso.xhtml", origen(caso));
        assertEquals("#{acceso.ingresar}", texto(caso, "from-action"));
        assertEquals("/app/tablero.xhtml", texto(caso, "to-view-id"));
        assertTrue(caso.getElementsByTagName("redirect").getLength() > 0, "Post/Redirect/Get");
    }

    @Test
    void salirDesdeCualquierPantallaVuelveAlAcceso() {
        Element caso = caso(AccesoBean.SALIDA);
        assertNotNull(caso, "falta la regla de salida");
        assertEquals("/app/*", origen(caso));
        assertEquals("#{acceso.salir}", texto(caso, "from-action"));
        assertEquals("/app/acceso.xhtml", texto(caso, "to-view-id"));
        assertTrue(caso.getElementsByTagName("redirect").getLength() > 0);
    }

    @Test
    void lasVistasDestinoExisten() {
        assertTrue(new File("src/main/webapp/app/tablero.xhtml").isFile());
        assertTrue(new File("src/main/webapp/app/acceso.xhtml").isFile());
    }
}
