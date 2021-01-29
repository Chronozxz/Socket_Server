//Servidor
import java.io.Serializable;

@SuppressWarnings("serial")
public class Mensaje implements Serializable, Cloneable {
    private static final long serialVersionUID = 715535542791688867L;//numero de serializacion
    
    short DATA_LEN;
    byte [] data;
    byte tipo;
    long CRC32;
    short length;
    byte numSeq;

    public Mensaje() 
    {//inicializando en nulo
        super();
        DATA_LEN = 1024;
        data = new byte[DATA_LEN];
        tipo = 0;
        CRC32 = 0;
        length = 0;
        numSeq = 0;
    }
    
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
