package generador;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

public class PdfImagen extends PDFStreamEngine{

	ArrayList<Imagen> imagenes;
	public PdfImagen() throws IOException
    {
        // preparing PDFStreamEngine
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
        
        this.imagenes = new ArrayList<Imagen>();
    }
	/**
	 * Metodo para obtener las imagenes de una pagina procesada de pdf
	 * en este se obtiene la escala y ubicacion del pdf (metodo de clase padre)
	 */
	@Override
    protected void processOperator( Operator operator, List<COSBase> operands) throws IOException
    {
        String operation = operator.getName();
        if( "Do".equals(operation) )
        {
            COSName objectName = (COSName) operands.get( 0 );
            // get the PDF object
            PDXObject xobject = getResources().getXObject( objectName );
            // check if the object is an image object
            if( xobject instanceof PDImageXObject)
            {
                PDImageXObject image = (PDImageXObject)xobject;
  
                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                float imageXScale = ctmNew.getScaleX();
                float imageYScale = ctmNew.getScaleY();
                
                String imagen = "image"+(this.imagenes.size()+1)+ ".png";
                File file = new File(imagen);
                /**
                 * Se construye la imagen en fisico
                 */
                ImageIO.write(image.getImage(),"png",file);
                /**
                 * Se guardan datos de la imagen generada
                 */
                this.imagenes.add(new Imagen(imagen,imageXScale,imageYScale,ctmNew.getTranslateX(),ctmNew.getTranslateY()));
            }
        }
        else
        {
            super.processOperator( operator, operands);
        }
    }
}
