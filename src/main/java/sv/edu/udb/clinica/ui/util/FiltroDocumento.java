package sv.edu.udb.clinica.ui.util;

import java.awt.Toolkit;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

// Filtro que impide escribir datos inválidos en un campo de texto para evitar errores manuales
// Limita la longitud, acepta solo letras o solo dígitos, o aplica una máscara como 00000000-0

public final class FiltroDocumento extends DocumentFilter {

    private enum Tipo { TEXTO, LETRAS, DIGITOS, MASCARA }

    private final JTextComponent campo;
    private final Tipo tipo;
    private final int maximo;
    private final String mascara;

    private FiltroDocumento(JTextComponent campo, Tipo tipo, int maximo, String mascara) {
        this.campo = campo;
        this.tipo = tipo;
        this.maximo = maximo;
        this.mascara = mascara;
    }

    // Cualquier texto, con una longitud máxima (la misma del VARCHAR de la base de datos)
    public static void texto(JTextComponent campo, int maximo) {
        instalar(campo, new FiltroDocumento(campo, Tipo.TEXTO, maximo, null));
    }

    // Solo letras (con tildes), espacios, punto, apóstrofe y guion, para nombres y apellidos
    public static void letras(JTextComponent campo, int maximo) {
        instalar(campo, new FiltroDocumento(campo, Tipo.LETRAS, maximo, null));
    }

    public static void digitos(JTextComponent campo, int maximo) {
        instalar(campo, new FiltroDocumento(campo, Tipo.DIGITOS, maximo, null));
    }

    // Máscara donde '#' es un dígito y cualquier otro carácter es fijo
    // Ejemplos "########-#" (DUI), "####-####" (teléfono) y "##/##/####" (fecha)
    public static void mascara(JTextComponent campo, String mascara) {
        instalar(campo, new FiltroDocumento(campo, Tipo.MASCARA, mascara.length(), mascara));
    }

    private static void instalar(JTextComponent campo, FiltroDocumento filtro) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(filtro);
    }

    @Override
    public void insertString(FilterBypass fb, int posicion, String texto, AttributeSet atributos)
            throws BadLocationException {
        replace(fb, posicion, 0, texto, atributos);
    }

    @Override
    public void remove(FilterBypass fb, int posicion, int largo) throws BadLocationException {
        replace(fb, posicion, largo, "", null);
    }

    @Override
    public void replace(FilterBypass fb, int posicion, int largo, String texto, AttributeSet atributos)
            throws BadLocationException {
        String nuevo = texto == null ? "" : texto;
        if (tipo == Tipo.MASCARA) {
            reemplazarConMascara(fb, posicion, largo, nuevo);
            return;
        }

        String limpio = depurar(nuevo);
        int libre = maximo - (fb.getDocument().getLength() - largo);
        if (limpio.length() > libre) {
            limpio = limpio.substring(0, Math.max(0, libre));
        }
        if (limpio.length() < nuevo.length()) {
            Toolkit.getDefaultToolkit().beep();
        }
        if (limpio.isEmpty() && largo == 0) {
            return;
        }
        fb.replace(posicion, largo, limpio, atributos);
    }

    // Quita del texto los caracteres que este campo no acepta
    private String depurar(String texto) {
        if (tipo == Tipo.LETRAS) {
            return texto.replaceAll("[^\\p{L} .'-]", "");
        }
        if (tipo == Tipo.DIGITOS) {
            return soloDigitos(texto);
        }
        return texto.replaceAll("[\\n\\r\\t]", " ");
    }

    // Con máscara se vuelve a armar todo el texto a partir de sus dígitos
    private void reemplazarConMascara(FilterBypass fb, int posicion, int largo, String texto)
            throws BadLocationException {
        Document doc = fb.getDocument();
        String actual = doc.getText(0, doc.getLength());
        int inicio = posicion;
        int fin = posicion + largo;

        // Si se borra un carácter fijo (como el guion) se borra el dígito anterior
        // de lo contrario el usuario quedaría atascado
        if (texto.isEmpty() && largo > 0 && soloDigitos(actual.substring(inicio, fin)).isEmpty()) {
            inicio--;
            while (inicio >= 0 && !Character.isDigit(actual.charAt(inicio))) {
                inicio--;
            }
            if (inicio < 0) {
                return;
            }
        }

        String antesDelCursor = actual.substring(0, inicio) + texto;
        String digitos = soloDigitos(antesDelCursor + actual.substring(fin));
        int capacidad = mascara.replaceAll("[^#]", "").length();
        if (digitos.length() > capacidad) {
            digitos = digitos.substring(0, capacidad);
        }
        int digitosAntes = Math.min(soloDigitos(antesDelCursor).length(), digitos.length());

        String formateado = formatear(digitos);
        if (formateado.equals(actual)) {
            if (!texto.isEmpty()) {
                Toolkit.getDefaultToolkit().beep();
            }
            return;
        }
        fb.replace(0, doc.getLength(), formateado, null);

        // El cursor se coloca después del último dígito escrito
        int cursor = posicionTrasDigito(formateado, digitosAntes);
        SwingUtilities.invokeLater(() -> {
            if (cursor <= campo.getDocument().getLength()) {
                campo.setCaretPosition(cursor);
            }
        });
    }

    // Coloca los dígitos en la máscara, un carácter fijo solo aparece si todavía sigue otro dígito
    private String formatear(String digitos) {
        StringBuilder sb = new StringBuilder();
        int siguiente = 0;
        for (char m : mascara.toCharArray()) {
            if (siguiente >= digitos.length()) {
                break;
            }
            sb.append(m == '#' ? digitos.charAt(siguiente++) : m);
        }
        return sb.toString();
    }

    private static int posicionTrasDigito(String texto, int cantidad) {
        int vistos = 0;
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isDigit(texto.charAt(i))) {
                vistos++;
                if (vistos == cantidad) {
                    return i + 1;
                }
            }
        }
        return cantidad == 0 ? 0 : texto.length();
    }

    private static String soloDigitos(String texto) {
        return texto.replaceAll("\\D", "");
    }
}
