import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.io.IOException;


public class Frame{
        public byte source; //source address
        public byte destination; //destination address
        private byte size; //size of data
        private byte crc; // cyclic reduncancy check - contains the sum of the byte values of the frame
        private byte[] data; //data

        // constructor for frame 
        public Frame(byte source, byte destination, byte size, byte[] data){
                this.source = source;
                this.destination = destination;
                this.size = size;
                this.data = data;
                this.crc = computerCRC();
        }

        //contructor for ACK
        public Frame(byte source, byte destination){
                this(source, destination, (byte)0, null);
        } 

        //convert frame to bytes
        public byte[] toBytes() throws IOException{
                ByteArrayOutputStream bs = new ByteArrayOutputStream();//output Bs for byte stream
                bs.write(source);//writes the source address as a byte
                bs.write(destination);//writes the destination address as a byte
                bs.write(size);//writes the size

                if(size >0 && data != null){//checks if size if greater than 0 and data is null
                        bs.write(data);//writes data of the array
                }
                bs.write(crc);
                return bs.toByteArray();

        }

        // method to create/make frame from bytes received 
        public static Frame mkFrame(byte[] bytes) throws IOException {
                ByteArrayInputStream bs = new ByteArrayInputStream(bytes);//input for byte stream
                byte source = (byte) bs.read();//reads source bytes
                byte destination = (byte) bs.read();//reads destination bytes
                byte size = (byte) bs.read();//reads size

                byte[] data = null;
                if (size >0){//check is size is bigger than 0 for ACK
                        data = new byte[size];//puts it in a new byte array
                        bs.read(data);//reads array
                }

                byte receivedCRD = (byte) bs.read();
                Frame frame = new Frame(source, destination, size, data);

                if(frame.crc != receivedCRD){
                        System.err.println("CRC validation failed");
                        return null;
                }

                return frame;
        }

        //computer CRCf
        private byte computerCRC(){
                int checksum = source + destination + size;
                if (data != null){
                        for(byte b : data){
                                checksum += b;
                        }
                }
                return (byte)(checksum & 0xFF); //fits in one byte
        }

        public boolean isAck(){
                return size == 0;
        }

        //unique identifier for frame
        public String getFrameID(){
                return source + "-" + destination + "-" + size + "-" + Arrays.hashCode(data);
        }

        // getters
        public byte getSource(){
                return source;
        }

        public byte getDest(){
                return destination;
        }

        public byte getSize(){
                return size;
        }

        public byte[] getData(){
                return data;
        }


}

