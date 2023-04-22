package generador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class PdfDataText extends PDFTextStripper {
	public ArrayList<Texto> texto;
	public PdfDataText() throws IOException{
		this.texto = new ArrayList<Texto>();
	}
	public ArrayList<Texto> getCoordenadas(){
		return this.texto;
	}
	@Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        for (TextPosition text : textPositions) {
            if(!text.getUnicode().equals(" ")) {
            	this.texto.add(new Texto(text.getEndX(), text.getEndY(), string,text.getFont(),text.getFontSize(),text.getWidthOfSpace()));
            	break;
            }
        }
    }
}
