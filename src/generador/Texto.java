package generador;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class Texto {

	public float x_pos;
	public float y_pos;
	public String texto;
	public PDFont font;
	public float size;
	public float espacio;
	public Texto(float x_pos,float y_pos,String texto,PDFont font,float size,float espacio) {
		this.x_pos = x_pos;
		this.y_pos = y_pos;
		this.texto = texto;
		this.font = font;
		this.size = size;
		this.espacio = espacio;
	}
}
