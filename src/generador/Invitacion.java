package generador;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.cos.COSName;

public class Invitacion {

	
	public static void main(String[] args) throws IOException {
		String ruta = "prueba.pdf";
        newpdfPosicionTexto(ruta);
    }
	public static ArrayList<Imagen> pdfToImages(String ruta) throws IOException{
		PDDocument document = null;
		ArrayList<Imagen> imagenes;
		try
        {
			/**
			 * Se obtiene documento para procesar sus paginas
			 */
            document = PDDocument.load( new File(ruta) );
            PdfImagen printer = new PdfImagen();
            for( PDPage page : document.getPages() )
            {
                printer.processPage(page);
            }
            /**
             * Una vez procesada sus paginas se obtienen las imagenes del documento
             */
            imagenes = printer.imagenes;
        }
        finally
        {
            if( document != null )
            {
                document.close();
            }
        }
		return imagenes;
	}
	public static ArrayList<String> obtenerNombres() throws FileNotFoundException {
		Scanner input = new Scanner(new File("nombres.txt"));
        ArrayList<String> nombres = new ArrayList<>();
        
        while (input.hasNextLine()){
        	
            String line = input.nextLine();
            
            nombres.add(line);
        }
        
        input.close();
        
        return nombres;
	}
	public static ArrayList<Texto> pdfToTexto(String ruta)throws IOException{
		PDDocument document1 = null;
		document1 = PDDocument.load( new File(ruta) );
		PdfDataText stripper = new PdfDataText();
        stripper.setSortByPosition( true );
        stripper.setStartPage( 0 );
        stripper.setEndPage( document1.getNumberOfPages() );

        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        stripper.writeText(document1, dummy);
       
		return stripper.getCoordenadas();
	}
	/**
	 * Metodo para generar un documento con el contenido
	 * de un pdf base usando PosicionTexto
	 * @param ruta
	 * @throws IOException
	 */
	public static void newpdfPosicionTexto(String ruta)throws IOException {
		/**
		 * Se obtiene la lista de nombres del txt
		 */
		ArrayList<String> nombres = obtenerNombres();
		/**
		 * Se obtiene la informacion requerida del pdf base
		 */
		ArrayList<Imagen> imagenes = pdfToImages(ruta);
		ArrayList<Texto> texto = pdfToTexto(ruta);
		/**
		 * Se construye el documento con su contenido
		 */
	    PDDocument document = new PDDocument();
	    for(int i = 0; i < nombres.size(); i++) {
	    	textoToPage(texto, document,nombres.get(i),imagenes);
	    }
	    /**
	     * Se guarda el contenido en una ruta
	     */
	    document.save("Invitaciones.pdf");
	    System.out.println("Generacion de PDF correcta");
	}
	/**
	 * Metodo para obtener la longitud mas larga
	 * de un arreglo de textos, tomando en cuenta el tipo font y tama;o de letra
	 * @param texto
	 * @return
	 * @throws IOException
	 */
	public static float getBiggerSizeRow(ArrayList<Texto> texto) throws IOException{
		float longitud = 0;
		
		for(int i = 0; i < texto.size(); i++) {
			float fontsize = texto.get(i).size;
			float size = fontsize * texto.get(i).font.getStringWidth(texto.get(i).texto) / 1000;
			if(longitud < size) {
				longitud = size;
			}
		}
		return longitud;
	}
	/**
	 * Metodo para generar un contenido de una pagina de pdf a partir
	 * de un arreglo de textos (junto con sus coordenadas y tipo de fuente)
	 * @param texto
	 * @param document
	 * @param nombre
	 * @param imagenes
	 * @throws IOException
	 */
	public static void textoToPage(ArrayList<Texto> texto,PDDocument document,String nombre,ArrayList<Imagen> imagenes) throws IOException{
		PDPage page = new PDPage(PDRectangle.A4);
        float x_pos = 0;
        
        document.addPage(page);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        /**
         * Se recorre arreglo de imagenes:
         * se insertan en la pagina junto con sus coordenadas y escala correspondiente
         */
        for(int i = 0; i < imagenes.size(); i++) {
        	byte[] bytes = Files.readAllBytes(Paths.get(imagenes.get(i).image));
            
            PDImageXObject image = PDImageXObject.createFromByteArray(document, bytes, imagenes.get(i).image);
            contentStream.drawImage(image, imagenes.get(i).x_pos , imagenes.get(i).y_pos, imagenes.get(i).x_size, imagenes.get(i).y_size);
        }
        /**
         * Se obtiene la longitud mas larga de los renglones:
         * esto se tomara de referencia para la justificacion
         * del texto
         */
        float longitud = getBiggerSizeRow(texto);
        /**
         * Bandera para empezar a justificar
         */
        boolean antesDElNombre = true;
        for(int i = 0; i < texto.size(); i++) {
        	/**
        	 * Se calcula la longitud del renglon tomando en cuenta el tipo de fuente
        	 * tama;o de letra y cuanto le falta para igualar la longitud de referencia
        	 */
        	float charSpacing = 0;
        	float fontSize = texto.get(i).size;
        	float varsize =  fontSize * texto.get(i).font.getStringWidth(texto.get(i).texto) / 1000;
        	float free =  longitud - varsize;
        	
        	if (free > 0){
                charSpacing = free / (texto.get(i).texto.length() - 1);
                
            }
        	
        	if(charSpacing > 1 || antesDElNombre) {
        		charSpacing = 0;
        	}
        	/**
        	 * Comienza la construccion del texto a ingresar (renglon)
        	 */
        	contentStream.beginText();
        	contentStream.setCharacterSpacing(charSpacing);// espacio entre caracteres
        	contentStream.setFont(texto.get(i).font, texto.get(i).size);
        	
        	x_pos = texto.get(i).x_pos;
        	/**
        	 * Se valida si es una coordenada de un titulo, renglon de parrafo o saludo/despedida
        	 */
        	if(texto.get(i).x_pos < 100 && texto.get(i).x_pos > 80) {
        		x_pos = (float) 88.361;
        	}
        	
        	x_pos =(float) (x_pos-8.361);
        	/**
        	 * Se ingresa coordenada del renglon
        	 */
        	contentStream.newLineAtOffset( x_pos, texto.get(i).y_pos);
        	/**
        	 * Si el renglon tiene la clave, se sustituye por el nombre en turno
        	 * si no se ingresa el renglon original
        	 */
        	if(texto.get(i).texto.contains("[NOMBRE]")) {
        		contentStream.setFont(PDType1Font.HELVETICA, texto.get(i).size);
        		contentStream.showText(nombre);
        		antesDElNombre = false;
        	}else {
        		contentStream.showText(texto.get(i).texto);
        	}
        	/**
        	 * Se termina el texto a ingresa en la pagina (renglon)
        	 */
        	contentStream.endText();
        }
        /**
         * Se cierra el stream de la pagina
         */
        contentStream.close();
	}
	/**
	 * Metodo para obtener la longitud mas larga
	 * de un arreglo de renglones
	 * @param arr
	 * @return
	 */
	public static int obtenerLongitudLarga(String[] arr){
		int longitud = 0;
		for(int i = 0; i < arr.length; i++) {
			if(longitud < arr[i].length()) {
				longitud = arr[i].length();
			}
		}
		return longitud;
	}public static String pdfToString(String ruta) throws IOException {
		PDDocument pdDocument = null;
		pdDocument = PDDocument.load(new File(ruta));
		
		PDFTextStripper pdfStripper = new PDFTextStripper();
	    pdfStripper.setStartPage(1);
	    pdfStripper.setEndPage(5);
	    
	    String parsedText = pdfStripper.getText(pdDocument);
	    
	    pdDocument.close();
	    
	    return parsedText;
	}
	public static void StringToPage(String parsedText,PDDocument document,String nombre,ArrayList<Imagen> imagenes) throws IOException{
		PDPage page = new PDPage(PDRectangle.A4);
        float x_pos = page.getCropBox().getWidth();
        float y_pos = page.getCropBox().getHeight();
        
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
       
        for(int i = 0; i < imagenes.size(); i++) {
        	byte[] bytes = Files.readAllBytes(Paths.get(imagenes.get(i).image));
            
            PDImageXObject image = PDImageXObject.createFromByteArray(document, bytes, imagenes.get(i).image);
            contentStream.drawImage(image, imagenes.get(i).x_pos , imagenes.get(i).y_pos, imagenes.get(i).x_size, imagenes.get(i).y_size);
        }
        
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        
        String[] arr = parsedText.replace("\r", "\n").split("\n");
        
        y_pos = (y_pos/20)*17;
        x_pos = 80;
        
        float xUltimaPos = x_pos;
        boolean atentamente = false;
        int longitud = obtenerLongitudLarga(arr)-2;
        
        for(int i = 0; i < arr.length; i++) {
        	
        	if(atentamente) {
        		x_pos = x_pos - arr[i].length()*2;
        	}
        	
        	if(arr[i].contains("Atentamente") ) {
        		x_pos = page.getCropBox().getWidth()/2;
        		x_pos = x_pos - xUltimaPos;
        		x_pos = x_pos - arr[i].length()*3;
        		xUltimaPos = x_pos;
        		atentamente = true;
        		y_pos = y_pos -20;
        	}
        	if(arr[i].contains("[NOMBRE]")) {
        		y_pos = y_pos+12;
        		contentStream.newLineAtOffset(x_pos , y_pos);
            	contentStream.showText(nombre);
            	y_pos = -15;
            	
        	}else {
        		contentStream.newLineAtOffset(x_pos , y_pos);
            	contentStream.showText(justificar(arr[i],longitud));
            	y_pos = -8;
        	}
        	
        	
        	if(arr[i].contains("  ")) {
        		
        		y_pos = -20;
        	}
        	
        	if(atentamente) {
        		
        		y_pos = -15;
        	}
        	
        	x_pos = 0;
        }
        
        contentStream.endText();
        contentStream.close();
	}
	public static void newpdfStripper(String ruta)throws IOException {
		ArrayList<String> nombres = obtenerNombres();
		ArrayList<Imagen> imagenes = pdfToImages(ruta);
		String parsedText = pdfToString(ruta);
	    PDDocument document = new PDDocument();
	    
	    for(int i = 0; i < nombres.size(); i++) {
	    	StringToPage(parsedText,document,nombres.get(i),imagenes);
	    }
	    
	    document.save("Invitaciones.pdf");
	    System.out.println("Generacion de PDF correcta");
	}
	/**
	 * Metodo para justificar texto a partir de la longitud de caracteres
	 * @param line
	 * @param longitud
	 * @return
	 */
	public static String justificar(String line, int longitud) {
		
		String newline = "";
		if(line.length()>longitud-5&& line.length()<longitud ) {
			int faltante = longitud - line.length()+3;
			for(int i = 0; i < line.length(); i++) {
				newline = newline + line.charAt(i);
				if(line.charAt(i) == ' '&& faltante > 0) {
					
					newline = newline + " ";
					faltante = faltante -1;
				}
			}
		}else {
			newline = line;
		}
		return newline;
	}
	/**
	 * Metodo para sacar las imagenes de un PDF e imprimirla
	 * @throws IOException
	 */
	public static void getImagen() throws IOException {
		PDDocument document = PDDocument.load(new File("prueba2.pdf"));
	    PDPageTree list = document.getPages();
	    for (PDPage page : list) {
	        PDResources pdResources = page.getResources();
	        for (COSName c : pdResources.getXObjectNames()) {
	        	
	            PDXObject o = pdResources.getXObject(c);
	            if (o instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
	            	PDImageXObject img = (PDImageXObject) o;
	            	System.out.println("imagen "+img.getHeight());
	                File file = new File(+ System.nanoTime() + ".jpg");
	                ImageIO.write(((org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject)o).getImage(), "jpg", file);
	            }
	        }
	    }
	}
}
